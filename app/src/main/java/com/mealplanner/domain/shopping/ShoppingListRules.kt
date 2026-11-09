package com.mealplanner.domain.shopping

import com.mealplanner.domain.model.ApplyBatchPresenceToListRequest
import com.mealplanner.domain.model.ApplyBatchPresenceToListResult
import com.mealplanner.domain.model.ApplyPresenceStatus
import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.IngredientAvailabilityStatus
import com.mealplanner.domain.model.IngredientLine
import com.mealplanner.domain.model.ShoppingListItem
import com.mealplanner.domain.model.StorePresenceResult
import com.mealplanner.domain.model.StorePresenceStatus
import java.util.Locale
import java.util.UUID

/**
 * C4 shopping/merge/presence application baseline (ADR-006/R-02/R-03).
 * C4 never performs PRISMA lookup; C5 mapping is supplied as projections.
 */
object ShoppingListRules {
    /** BR1.10: normalize for merge comparison; keep explicit ambiguity instead of guessing. */
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
            )
        }
    }

    /**
     * R-02: C4 applies mapped presence projections to the current list.
     * Never performs PRISMA lookup. Unknown after ambiguous mapping/edit/merge context.
     */
    fun applyBatchPresenceToList(request: ApplyBatchPresenceToListRequest): ApplyBatchPresenceToListResult {
        if (request.items.isEmpty()) {
            return ApplyBatchPresenceToListResult(emptyList(), ApplyPresenceStatus.APPLIED, 0)
        }
        var unknownCount = 0
        val updated = request.items.map { item ->
            val match = findProjection(item, request.projections)
            when {
                item.manuallyEdited && match == null -> {
                    unknownCount++
                    item.copy(
                        presence = StorePresenceStatus.UNKNOWN,
                        unresolved = true,
                        unresolvedReason = "UNKNOWN_AFTER_EDIT_NO_MAPPING",
                    )
                }
                match == null -> {
                    unknownCount++
                    item.copy(
                        presence = StorePresenceStatus.UNKNOWN,
                        presenceReason = com.mealplanner.domain.model.PresenceLookupReason(
                            code = "NO_PRESENCE_PROJECTION",
                            message = "No presence projection mapped for item.",
                        ),
                        unresolved = true,
                        unresolvedReason = "UNMAPPED_PRESENCE_PROJECTION",
                    )
                }
                item.unresolved && match.status == StorePresenceStatus.PRESENT -> {
                    unknownCount++
                    item.copy(
                        presence = StorePresenceStatus.UNKNOWN,
                        presenceReason = match.reason ?: com.mealplanner.domain.model.PresenceLookupReason(
                            code = "AMBIGUOUS_AFTER_MERGE_OR_EDIT",
                            message = "Presence mapping is ambiguous after edit/merge/regenerate context.",
                        ),
                        unresolved = true,
                        unresolvedReason = item.unresolvedReason ?: "AMBIGUOUS_AFTER_MERGE_OR_EDIT",
                    )
                }
                else -> item.copy(
                    presence = match.status,
                    presenceReason = match.reason,
                    unresolved = item.unresolved && match.status == StorePresenceStatus.UNKNOWN,
                )
            }
        }
        val status = when {
            unknownCount == 0 -> ApplyPresenceStatus.APPLIED
            unknownCount == updated.size -> ApplyPresenceStatus.FAILED
            else -> ApplyPresenceStatus.PARTIAL
        }
        return ApplyBatchPresenceToListResult(
            items = updated,
            status = status,
            unknownOrRemapCount = unknownCount,
            fallbackReason = if (unknownCount > 0) FallbackReason("PRESENCE_MAPPING_INCOMPLETE", "Some items have unknown presence after mapping/edit/merge context.") else null,
        )
    }

    private fun findProjection(item: ShoppingListItem, projections: List<StorePresenceResult>): StorePresenceResult? {
        val itemNormalized = item.normalizedName ?: normalizeName(item.displayName)
        val exact = projections.filter { projection ->
            val p = normalizeName(projection.queryText)
            p == itemNormalized
        }
        return when (exact.size) {
            1 -> exact.first()
            else -> null // ambiguous or missing => C4 keeps unknown
        }
    }
}
