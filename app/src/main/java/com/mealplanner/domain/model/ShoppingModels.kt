package com.mealplanner.domain.model

import java.time.Instant

/** ENT-U1-005 */
data class LockedRecipeSelection(
    val slotRef: PlanSlotRef,
    val recipeId: RecipeId,
    val selectionSource: SelectionSource = SelectionSource.USER_SELECTED,
)

/** ENT-U1-006 */
data class PlanSnapshotContext(
    val selectedStoreRef: StoreRef? = null,
    val seasonContext: SeasonContext? = null,
    val seasonOverride: SeasonOverride? = null,
    val schedulePolicyVersion: String? = null,
)

/** ENT-U1-004 (ADR-006/R-01: IDs/selections + plan context only; no ingredient freeze). */
data class LockSnapshot(
    val snapshotId: LockSnapshotId,
    val planId: PlanId,
    val lockedRecipeSelections: List<LockedRecipeSelection>,
    val planContext: PlanSnapshotContext = PlanSnapshotContext(),
    val createdAt: Instant,
    val sourceCatalogVersion: String? = null,
)

enum class ShoppingItemIngredientDataStatus { COMPLETE, INCOMPLETE, MISSING_SOURCE, MANUAL }

enum class ShoppingListStateStatus { EMPTY, GENERATED, PARTIAL, STALE, ERROR_MAPPED }

enum class MergeDecisionType { MERGED, SEPARATE, MANUAL_REVIEW }

enum class MergeReasonCode {
    NAME_UNIT_COMPATIBLE,
    UNIT_INCOMPATIBLE,
    AMOUNT_INCOMPATIBLE,
    MISSING_DATA,
    UNSAFE_TO_MERGE,
}

data class MergeDecision(
    val decision: MergeDecisionType,
    val mergeKey: String? = null,
    val reasonCode: MergeReasonCode,
    val explanation: String? = null,
)

/** ENT-U1-008 / U3 C4 item. Preserves U1 fields for compatibility; adds U3 projection/edit/remap fields. */
data class ShoppingListItem(
    val itemId: ShoppingListItemId,
    val displayName: String,
    val normalizedName: String? = null,
    val amount: Double? = null,
    val unit: String? = null,
    val amountText: String? = null,
    val checked: Boolean = false,
    val sourceRecipeIds: List<RecipeId> = emptyList(),
    val presence: StorePresenceStatus? = null,
    val presenceReason: PresenceLookupReason? = null,
    val manuallyEdited: Boolean = false,
    val mergeGroupKey: String? = null,
    /** True when merge/mapping/resolution became ambiguous; C4 must keep explicit unknown state. */
    val unresolved: Boolean = false,
    val unresolvedReason: String? = null,
    // U3 additive fields (BR3.3/BR3.5/BR3.10)
    val displayText: String = displayName,
    val quantityText: String? = amountText,
    val userEdited: Boolean = manuallyEdited,
    val sourceIngredientRefs: List<String> = emptyList(),
    val ingredientDataStatus: ShoppingItemIngredientDataStatus = ShoppingItemIngredientDataStatus.COMPLETE,
    val availabilityStatus: StorePresenceStatus = presence ?: StorePresenceStatus.NOT_CHECKED,
    val availabilityReason: String? = presenceReason?.message ?: presenceReason?.code,
    val availabilityCheckedAt: Instant? = null,
    val selectedStoreId: String? = null,
    val presenceQueryKey: String? = mergeGroupKey ?: normalizedName,
    val addedManually: Boolean = false,
    val removed: Boolean = false,
)

fun ShoppingListItem.effectiveDisplayText(): String = displayText.ifBlank { displayName }
fun ShoppingListItem.effectiveQuantityText(): String? = quantityText ?: amountText

/** ADR-006/R-03 exact warning text (must not change). */
object ShoppingListStaleWarning {
    const val EXACT_TEXT: String =
        "Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."
}

data class ShoppingListStaleStatus(
    val snapshotId: LockSnapshotId? = null,
    val isStale: Boolean = false,
    val staleWarning: String? = if (isStale) ShoppingListStaleWarning.EXACT_TEXT else null,
    val staleReason: String? = null,
)

/** C4 aggregate — current edited list state (BR3.1-BR3.5). */
data class ShoppingListAggregate(
    val shoppingListId: ShoppingListId,
    val snapshotId: LockSnapshotId? = null,
    val sourceRecipeIds: List<RecipeId> = emptyList(),
    val items: List<ShoppingListItem> = emptyList(),
    val listStatus: ShoppingListStateStatus = ShoppingListStateStatus.EMPTY,
    val staleStatus: ShoppingListStaleStatus = ShoppingListStaleStatus(),
    val hasUserEdits: Boolean = false,
    val generationNotes: List<String> = emptyList(),
    val lastGeneratedAt: Instant? = null,
    val lastEditedAt: Instant? = null,
    val selectedStoreRef: StoreRef? = null,
)

enum class ShoppingItemPatchOperation { SET_CHECKED, EDIT_TEXT, EDIT_QUANTITY, ADD_ITEM, REMOVE_ITEM }

data class ShoppingItemPatch(
    val itemId: ShoppingListItemId? = null,
    val operation: ShoppingItemPatchOperation = ShoppingItemPatchOperation.EDIT_TEXT,
    val displayText: String? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val quantityText: String? = null,
    val checked: Boolean? = null,
    val removed: Boolean? = null,
    val addedItem: ShoppingListItem? = null,
)

data class ShoppingListPatchRequest(
    val shoppingListId: ShoppingListId? = null,
    val patch: ShoppingItemPatch,
    val editedAt: Instant? = null,
)

enum class ShoppingListGenerationOperation { GENERATE, REGENERATE }

data class ShoppingListGenerationRequest(
    val snapshotId: LockSnapshotId,
    val operation: ShoppingListGenerationOperation = ShoppingListGenerationOperation.GENERATE,
    val confirmRegenerateIfEdited: Boolean = false,
    val requestedAt: Instant? = null,
    val staleStatus: ShoppingListStaleStatus = ShoppingListStaleStatus(),
)

enum class ShoppingListGenerationOutcome {
    GENERATED,
    CONFIRMATION_REQUIRED,
    INVALID_SNAPSHOT,
    PARTIAL,
    FAILED_MAPPED,
}

data class ShoppingListGenerationOutcomeResult(
    val status: ShoppingListGenerationOutcome,
    val shoppingList: ShoppingListAggregate? = null,
    val missingOrChangedRecipeIds: List<RecipeId> = emptyList(),
    val message: String? = null,
    val staleStatus: ShoppingListStaleStatus = ShoppingListStaleStatus(),
    val fallbackReason: FallbackReason? = null,
)

fun ShoppingListGenerationOutcomeResult.toLegacy(): ShoppingListGenerationResult = ShoppingListGenerationResult(
    status = when (status) {
        ShoppingListGenerationOutcome.GENERATED -> GenerationStatus.GENERATED
        ShoppingListGenerationOutcome.CONFIRMATION_REQUIRED -> GenerationStatus.NEEDS_CONFIRMATION
        ShoppingListGenerationOutcome.INVALID_SNAPSHOT,
        ShoppingListGenerationOutcome.FAILED_MAPPED -> GenerationStatus.FAILED
        ShoppingListGenerationOutcome.PARTIAL -> GenerationStatus.GENERATED
    },
    snapshotId = shoppingList?.snapshotId,
    shoppingListId = shoppingList?.shoppingListId,
    items = shoppingList?.items.orEmpty(),
    fallbackReason = fallbackReason,
    userActionRequired = message,
)

/** ENT-U1-007 legacy compatibility result. */
data class ShoppingListGenerationResult(
    val status: GenerationStatus,
    val snapshotId: LockSnapshotId? = null,
    val shoppingListId: ShoppingListId? = null,
    val items: List<ShoppingListItem> = emptyList(),
    val unresolvedIngredients: List<IngredientLine> = emptyList(),
    val fallbackReason: FallbackReason? = null,
    val userActionRequired: String? = null,
)

/** R-02 / BR3.5: C4-owned explicit batch presence application with list/store scope. */
data class ApplyPresenceCheckBatchRequest(
    val shoppingListId: ShoppingListId? = null,
    val storeId: String? = null,
    val shoppingList: ShoppingListAggregate? = null,
    val batch: BatchPresenceResult? = null,
    val projections: List<StorePresenceResult> = batch?.results.orEmpty(),
    val storeRef: StoreRef? = batch?.storeRef,
    val allowIdentityRemap: Boolean = true,
)

data class ApplyPresenceCheckBatchResult(
    val shoppingList: ShoppingListAggregate? = null,
    val items: List<ShoppingListItem> = shoppingList?.items.orEmpty(),
    val status: ApplyPresenceStatus = ApplyPresenceStatus.APPLIED,
    val unknownOrRemapCount: Int = 0,
    val fallbackReason: FallbackReason? = null,
    val remapReasonsByItemId: Map<String, String> = emptyMap(),
)
