package com.mealplanner.domain.shopping

import com.mealplanner.domain.model.ApplyBatchPresenceToListRequest
import com.mealplanner.domain.model.ApplyBatchPresenceToListResult
import com.mealplanner.domain.model.ApplyPresenceCheckBatchRequest
import com.mealplanner.domain.model.ApplyPresenceCheckBatchResult
import com.mealplanner.domain.model.ApplyPresenceStatus
import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.GenerationStatus
import com.mealplanner.domain.model.IngredientAvailabilityStatus
import com.mealplanner.domain.model.IngredientLine
import com.mealplanner.domain.model.MergeDecision
import com.mealplanner.domain.model.MergeDecisionType
import com.mealplanner.domain.model.MergeReasonCode
import com.mealplanner.domain.model.ShoppingItemIngredientDataStatus
import com.mealplanner.domain.model.ShoppingItemPatch
import com.mealplanner.domain.model.ShoppingItemPatchOperation
import com.mealplanner.domain.model.ShoppingListAggregate
import com.mealplanner.domain.model.ShoppingListGenerationOperation
import com.mealplanner.domain.model.ShoppingListGenerationOutcome
import com.mealplanner.domain.model.ShoppingListGenerationOutcomeResult
import com.mealplanner.domain.model.ShoppingListGenerationRequest
import com.mealplanner.domain.model.ShoppingListItem
import com.mealplanner.domain.model.ShoppingListPatchRequest
import com.mealplanner.domain.model.ShoppingListStateStatus
import com.mealplanner.domain.model.ShoppingListStaleStatus
import com.mealplanner.domain.model.StorePresenceResult
import com.mealplanner.domain.model.StorePresenceStatus
import com.mealplanner.domain.model.effectiveDisplayText
import com.mealplanner.domain.model.effectiveQuantityText
import java.time.Instant
import java.util.Locale
import java.util.UUID

/**
 * C4 shopping/merge/presence application (ADR-006/R-02/R-03, BR3.1-BR3.5).
 * C4 never performs PRISMA lookup; C5 mapping is supplied as projections.
 */
object ShoppingListRules {
    /** BR1.10/BR3.2: normalize for merge comparison; keep explicit ambiguity instead of guessing. */
    fun normalizeName(name: String): String = name.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")

    fun resolveIngredientLines(lines: List<IngredientLine>): Pair<List<ShoppingListItem>, List<IngredientLine>> {
        val unresolved = lines.filter {
            it.availabilityStatus != IngredientAvailabilityStatus.KNOWN || it.name.isBlank()
        }.map { line ->
            // BR1.8/R-03: never fabricate quantities/units; preserve explicit unknown/missing state.
            if (line.availabilityStatus == IngredientAvailabilityStatus.KNOWN && line.name.isBlank()) {
                line.copy(availabilityStatus = IngredientAvailabilityStatus.MISSING)
            } else line
        }
        val resolvable = lines.filter {
            it.availabilityStatus == IngredientAvailabilityStatus.KNOWN && it.name.isNotBlank()
        }
        val merged = mergeCompatible(resolvable)
        return merged to unresolved
    }

    private fun mergeCompatible(lines: List<IngredientLine>): List<ShoppingListItem> {
        data class Key(val normalized: String, val unit: String?)
        val grouped = LinkedHashMap<Key, MutableList<IngredientLine>>()
        lines.forEach { line ->
            val normalized = normalizeName(line.name)
            val unit = line.unit?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotBlank() }
            val canMergeByAmount = line.amount != null && unit != null
            val key = if (canMergeByAmount) Key(normalized, unit) else Key(normalized, UUID.randomUUID().toString())
            grouped.getOrPut(key) { mutableListOf() }.add(line)
        }
        return grouped.values.map { group ->
            val first = group.first()
            val allSameUnit = group.map { it.unit?.trim()?.lowercase(Locale.ROOT) }.distinct().size == 1
            val amounts = group.mapNotNull { it.amount }
            val canAggregate = allSameUnit && amounts.size == group.size && amounts.isNotEmpty()
            val ingredientDataStatus = when {
                group.any { it.availabilityStatus != IngredientAvailabilityStatus.KNOWN } -> ShoppingItemIngredientDataStatus.INCOMPLETE
                else -> ShoppingItemIngredientDataStatus.COMPLETE
            }
            ShoppingListItem(
                itemId = UUID.randomUUID().toString(),
                displayName = first.name.trim(),
                normalizedName = normalizeName(first.name),
                amount = if (canAggregate) amounts.sum() else first.amount?.takeIf { group.size == 1 },
                unit = if (canAggregate || group.size == 1) first.unit else null,
                amountText = if (!canAggregate && group.size > 1) group.mapNotNull { it.amountText ?: it.amount?.toString()?.let { a -> "${a}${it.unit?.let { u -> " $u" } ?: ""}" } }.joinToString(" + ").ifBlank { null } else first.amountText,
                checked = false,
                sourceRecipeIds = group.map { it.sourceRecipeId }.distinct(),
                manuallyEdited = false,
                mergeGroupKey = normalizeName(first.name),
                unresolved = !canAggregate && group.size > 1 && amounts.size != group.size,
                unresolvedReason = if (!canAggregate && group.size > 1 && amounts.size != group.size) "INCOMPATIBLE_OR_AMBIGUOUS_MERGE" else null,
                displayText = first.name.trim(),
                quantityText = if (!canAggregate && group.size > 1) group.mapNotNull { it.amountText ?: it.amount?.toString()?.let { a -> "${a}${it.unit?.let { u -> " $u" } ?: ""}" } }.joinToString(" + ").ifBlank { null } else first.amountText,
                sourceIngredientRefs = group.mapNotNull { it.ingredientRef },
                ingredientDataStatus = ingredientDataStatus,
                availabilityStatus = StorePresenceStatus.NOT_CHECKED,
                presenceQueryKey = normalizeName(first.name),
            )
        }
    }

    fun mergeDecisionFor(lines: List<IngredientLine>): MergeDecision {
        if (lines.size <= 1) {
            return MergeDecision(MergeDecisionType.MERGED, normalizeName(lines.firstOrNull()?.name.orEmpty()), MergeReasonCode.NAME_UNIT_COMPATIBLE)
        }
        val units = lines.map { it.unit?.trim()?.lowercase(Locale.ROOT).orEmpty() }.distinct()
        val amounts = lines.mapNotNull { it.amount }
        return when {
            lines.any { it.availabilityStatus != IngredientAvailabilityStatus.KNOWN } ->
                MergeDecision(MergeDecisionType.SEPARATE, normalizeName(lines.first().name), MergeReasonCode.MISSING_DATA, "Source ingredient data missing/changed.")
            units.size > 1 ->
                MergeDecision(MergeDecisionType.SEPARATE, normalizeName(lines.first().name), MergeReasonCode.UNIT_INCOMPATIBLE, "Units incompatible; kept separate.")
            amounts.size != lines.size ->
                MergeDecision(MergeDecisionType.SEPARATE, normalizeName(lines.first().name), MergeReasonCode.AMOUNT_INCOMPATIBLE, "Amounts not safely addable; kept separate.")
            units.size == 1 -> MergeDecision(MergeDecisionType.MERGED, normalizeName(lines.first().name), MergeReasonCode.NAME_UNIT_COMPATIBLE)
            else -> MergeDecision(MergeDecisionType.SEPARATE, normalizeName(lines.first().name), MergeReasonCode.UNSAFE_TO_MERGE, "Unsafe merge.")
        }
    }

    /** BR3.3: regeneration confirmation required when current list has user edits. */
    fun evaluateRegenerationRequest(
        current: ShoppingListAggregate?,
        request: ShoppingListGenerationRequest,
    ): ShoppingListGenerationOutcomeResult {
        if (current != null && current.hasUserEdits &&
            request.operation == ShoppingListGenerationOperation.REGENERATE &&
            !request.confirmRegenerateIfEdited
        ) {
            return ShoppingListGenerationOutcomeResult(
                status = ShoppingListGenerationOutcome.CONFIRMATION_REQUIRED,
                shoppingList = current,
                message = "Regeneration replaces edited list items. Confirm to continue.",
                staleStatus = current.staleStatus,
                fallbackReason = FallbackReason("CONFIRM_REGENERATE_REQUIRED", "User edits exist; explicit confirmation required."),
            )
        }
        return ShoppingListGenerationOutcomeResult(
            status = ShoppingListGenerationOutcome.GENERATED,
            shoppingList = current,
            staleStatus = request.staleStatus,
        )
    }

    /** BR3.3: apply patch and mark aggregate edited/export-current-state. */
    fun applyItemPatch(aggregate: ShoppingListAggregate, request: ShoppingListPatchRequest, now: Instant = Instant.now()): ShoppingListAggregate {
        val patch = request.patch
        val items = when (patch.operation) {
            ShoppingItemPatchOperation.ADD_ITEM -> {
                val added = (patch.addedItem ?: ShoppingListItem(
                    itemId = patch.itemId ?: "manual-${UUID.randomUUID()}",
                    displayName = patch.displayText ?: "Manual item",
                    displayText = patch.displayText ?: "Manual item",
                    quantityText = patch.quantityText,
                    checked = patch.checked ?: false,
                    manuallyEdited = true,
                    userEdited = true,
                    addedManually = true,
                    ingredientDataStatus = ShoppingItemIngredientDataStatus.MANUAL,
                    availabilityStatus = StorePresenceStatus.NOT_CHECKED,
                    presenceQueryKey = normalizeName(patch.displayText ?: "manual item"),
                ))
                aggregate.items + added.copy(
                    addedManually = true,
                    manuallyEdited = true,
                    userEdited = true,
                    availabilityStatus = StorePresenceStatus.NOT_CHECKED,
                    availabilityReason = "MANUAL_ITEM_NOT_CHECKED",
                )
            }
            ShoppingItemPatchOperation.REMOVE_ITEM -> aggregate.items.filterNot { it.itemId == patch.itemId }
            else -> aggregate.items.map { item ->
                if (item.itemId != patch.itemId) return@map item
                val changedIdentity = (patch.displayText != null && normalizeName(patch.displayText) != normalizeName(item.effectiveDisplayText())) ||
                    (patch.quantityText != null && patch.quantityText != item.effectiveQuantityText())
                val nextDisplay = patch.displayText ?: item.displayText.ifBlank { item.displayName }
                item.copy(
                    checked = if (patch.operation == ShoppingItemPatchOperation.SET_CHECKED) patch.checked ?: item.checked else item.checked,
                    displayName = if (patch.displayText != null) patch.displayText else item.displayName,
                    displayText = nextDisplay,
                    amount = patch.quantity ?: item.amount,
                    unit = patch.unit ?: item.unit,
                    amountText = patch.quantityText ?: item.amountText,
                    quantityText = patch.quantityText ?: item.quantityText,
                    manuallyEdited = true,
                    userEdited = true,
                    normalizedName = if (patch.displayText != null) normalizeName(patch.displayText) else item.normalizedName,
                    presenceQueryKey = if (patch.displayText != null) normalizeName(patch.displayText) else item.presenceQueryKey,
                    availabilityStatus = if (changedIdentity) StorePresenceStatus.UNKNOWN else item.availabilityStatus,
                    availabilityReason = if (changedIdentity) "UNKNOWN_AFTER_EDIT_IDENTITY_CHANGED" else item.availabilityReason,
                    unresolved = changedIdentity || item.unresolved,
                    unresolvedReason = if (changedIdentity) "UNKNOWN_AFTER_EDIT_IDENTITY_CHANGED" else item.unresolvedReason,
                )
            }
        }
        return aggregate.copy(
            items = items,
            hasUserEdits = true,
            lastEditedAt = request.editedAt ?: now,
            staleStatus = aggregate.staleStatus,
        )
    }

    /**
     * R-02 / BR3.5: explicit C4-owned batch presence application.
     * Scoped by shoppingListId + selected storeId/scope + stable item/query key.
     * Ambiguous identity/query changes remap to UNKNOWN with reason.
     */
    fun applyPresenceCheckBatch(request: ApplyPresenceCheckBatchRequest): ApplyPresenceCheckBatchResult {
        val aggregate = request.shoppingList ?: return ApplyPresenceCheckBatchResult(
            status = ApplyPresenceStatus.FAILED,
            fallbackReason = FallbackReason("MISSING_SHOPPING_LIST", "Shopping list aggregate required for BR3.5 projection."),
        )
        val scopedList = request.shoppingListId == null || request.shoppingListId == aggregate.shoppingListId
        if (!scopedList) {
            return ApplyPresenceCheckBatchResult(
                shoppingList = aggregate,
                items = aggregate.items,
                status = ApplyPresenceStatus.FAILED,
                fallbackReason = FallbackReason("LIST_SCOPE_MISMATCH", "Batch list scope does not match shopping list."),
            )
        }
        val batchStoreId = request.storeId ?: request.storeRef?.storeId ?: request.batch?.storeRef?.storeId
        val selectedStoreId = aggregate.selectedStoreRef?.storeId
        val storeScoped = batchStoreId == null || selectedStoreId == null || batchStoreId == selectedStoreId
        if (!storeScoped) {
            return ApplyPresenceCheckBatchResult(
                shoppingList = aggregate.copy(items = aggregate.items.map { markUnknown(it, "STORE_SCOPE_MISMATCH", "Presence batch store scope differs from selected store.") }),
                status = ApplyPresenceStatus.PARTIAL,
                unknownOrRemapCount = aggregate.items.size,
                fallbackReason = FallbackReason("STORE_SCOPE_MISMATCH", "Presence batch store scope differs from selected store."),
            )
        }
        val projections = request.projections
        var unknownCount = 0
        val reasons = mutableMapOf<String, String>()
        val updated = aggregate.items.map { item ->
            val key = presenceKey(item)
            val matches = projections.filter { projectionMatches(item, it, key) }
            when {
                // Preserve legacy/U1 semantics: manually edited item with no mapping stays explicit unknown.
                item.manuallyEdited && matches.isEmpty() -> {
                    unknownCount++
                    reasons[item.itemId] = "UNKNOWN_AFTER_EDIT_NO_MAPPING"
                    item.copy(
                        presence = StorePresenceStatus.UNKNOWN,
                        presenceReason = com.mealplanner.domain.model.PresenceLookupReason(
                            code = "UNKNOWN_AFTER_EDIT_NO_MAPPING",
                            message = "No presence projection mapped for edited item.",
                            userActionRequired = "Review item and recheck presence.",
                        ),
                        availabilityStatus = StorePresenceStatus.UNKNOWN,
                        availabilityReason = "No presence projection mapped for edited item.",
                        unresolved = true,
                        unresolvedReason = "UNKNOWN_AFTER_EDIT_NO_MAPPING",
                    )
                }
                matches.isEmpty() -> {
                    unknownCount++
                    val reason = "NO_PRESENCE_PROJECTION"
                    reasons[item.itemId] = reason
                    markUnknown(item, reason, "No presence projection mapped for item key '$key'.")
                }
                matches.size > 1 -> {
                    unknownCount++
                    val reason = "AMBIGUOUS_PRESENCE_MAPPING"
                    reasons[item.itemId] = reason
                    markUnknown(item, reason, "Ambiguous presence projection after edit/merge/query mapping.")
                }
                else -> {
                    val match = matches.first()
                    // Preserve legacy invariant: unresolved merged/ambiguous item must not be upgraded to fabricated PRESENT.
                    if (item.unresolved && match.status == StorePresenceStatus.PRESENT) {
                        unknownCount++
                        reasons[item.itemId] = "AMBIGUOUS_AFTER_MERGE_OR_EDIT"
                        markUnknown(
                            item,
                            "AMBIGUOUS_AFTER_MERGE_OR_EDIT",
                            match.reason?.message ?: "Presence mapping is ambiguous after edit/merge/regenerate context.",
                        ).copy(presenceReason = match.reason ?: com.mealplanner.domain.model.PresenceLookupReason("AMBIGUOUS_AFTER_MERGE_OR_EDIT", "Presence mapping is ambiguous after edit/merge/regenerate context."))
                    } else if (!request.allowIdentityRemap && item.userEdited && normalizeName(match.queryText) != key) {
                        unknownCount++
                        reasons[item.itemId] = "UNKNOWN_AFTER_EDIT_IDENTITY_CHANGED"
                        markUnknown(item, "UNKNOWN_AFTER_EDIT_IDENTITY_CHANGED", "Identity changed after edit; remap not safe.")
                    } else projectItem(item, match, batchStoreId)
                }
            }
        }
        val status = when {
            updated.isEmpty() -> ApplyPresenceStatus.APPLIED
            unknownCount == 0 -> ApplyPresenceStatus.APPLIED
            unknownCount == updated.size -> ApplyPresenceStatus.FAILED
            else -> ApplyPresenceStatus.PARTIAL
        }
        return ApplyPresenceCheckBatchResult(
            shoppingList = aggregate.copy(items = updated),
            items = updated,
            status = status,
            unknownOrRemapCount = unknownCount,
            fallbackReason = if (unknownCount > 0) FallbackReason("PRESENCE_MAPPING_INCOMPLETE", "Some items have unknown presence after mapping/edit/merge context.") else null,
            remapReasonsByItemId = reasons,
        )
    }

    /**
     * Legacy R-02 API: C4 applies mapped presence projections to the current list.
     * Preserved for existing callers/tests; uses BR3.5-compatible projection semantics.
     */
    fun applyBatchPresenceToList(request: ApplyBatchPresenceToListRequest): ApplyBatchPresenceToListResult {
        if (request.items.isEmpty()) {
            return ApplyBatchPresenceToListResult(emptyList(), ApplyPresenceStatus.APPLIED, 0)
        }
        val aggregate = ShoppingListAggregate(
            shoppingListId = request.shoppingListId ?: "legacy-list",
            selectedStoreRef = request.storeRef,
            items = request.items,
        )
        val result = applyPresenceCheckBatch(
            ApplyPresenceCheckBatchRequest(
                shoppingListId = aggregate.shoppingListId,
                storeId = request.storeRef.storeId,
                shoppingList = aggregate,
                projections = request.projections,
                storeRef = request.storeRef,
                allowIdentityRemap = true,
            ),
        )
        val legacyItems = result.items.map { it.copy(presence = it.availabilityStatus.takeUnless { s -> s == StorePresenceStatus.NOT_CHECKED } ?: StorePresenceStatus.UNKNOWN) }
        return ApplyBatchPresenceToListResult(
            items = legacyItems,
            status = result.status,
            unknownOrRemapCount = result.unknownOrRemapCount,
            fallbackReason = result.fallbackReason,
        )
    }

    private fun presenceKey(item: ShoppingListItem): String =
        item.presenceQueryKey?.takeIf { it.isNotBlank() } ?: item.normalizedName ?: normalizeName(item.effectiveDisplayText())

    private fun projectionMatches(item: ShoppingListItem, projection: StorePresenceResult, key: String): Boolean {
        val projectionKey = normalizeName(projection.queryText)
        if (projectionKey != key) return false
        val ingredientRefMatch = item.sourceIngredientRefs.isNotEmpty() && projection.ingredientRef != null &&
            item.sourceIngredientRefs.contains(projection.ingredientRef)
        // Prefer explicit itemRef/ingredientRef when present; fall back to normalized query only when unambiguous by key.
        return projection.ingredientRef == null || item.sourceIngredientRefs.isEmpty() || ingredientRefMatch || projectionKey == key
    }

    private fun projectItem(item: ShoppingListItem, match: StorePresenceResult, storeId: String?): ShoppingListItem {
        val status = when (match.status) {
            StorePresenceStatus.PRESENT, StorePresenceStatus.NOT_FOUND, StorePresenceStatus.UNKNOWN -> match.status
            StorePresenceStatus.NOT_CHECKED -> StorePresenceStatus.UNKNOWN
        }
        return item.copy(
            presence = status,
            presenceReason = match.reason,
            availabilityStatus = status,
            availabilityReason = match.reason?.message ?: match.reason?.code,
            availabilityCheckedAt = match.checkedAt,
            selectedStoreId = storeId ?: item.selectedStoreId,
            unresolved = item.unresolved && status == StorePresenceStatus.UNKNOWN,
            unresolvedReason = if (status == StorePresenceStatus.UNKNOWN) (item.unresolvedReason ?: match.reason?.code) else item.unresolvedReason,
        )
    }

    private fun markUnknown(item: ShoppingListItem, code: String, message: String): ShoppingListItem = item.copy(
        presence = StorePresenceStatus.UNKNOWN,
        presenceReason = com.mealplanner.domain.model.PresenceLookupReason(code = code, message = message),
        availabilityStatus = StorePresenceStatus.UNKNOWN,
        availabilityReason = message,
        unresolved = true,
        unresolvedReason = code,
    )

    @Deprecated("Use applyBatchPresenceToList for legacy callers or applyPresenceCheckBatch for BR3.5 aggregate scope.")
    private fun findProjection(item: ShoppingListItem, projections: List<StorePresenceResult>): StorePresenceResult? {
        val itemNormalized = item.normalizedName ?: normalizeName(item.displayName)
        val exact = projections.filter { projection ->
            val p = normalizeName(projection.queryText)
            p == itemNormalized
        }
        return when (exact.size) {
            1 -> exact.first()
            else -> null
        }
    }
}
