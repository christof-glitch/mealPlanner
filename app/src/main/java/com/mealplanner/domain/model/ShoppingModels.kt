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

/** ENT-U1-008 */
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
)

/** ENT-U1-007 */
data class ShoppingListGenerationResult(
    val status: GenerationStatus,
    val snapshotId: LockSnapshotId? = null,
    val shoppingListId: ShoppingListId? = null,
    val items: List<ShoppingListItem> = emptyList(),
    val unresolvedIngredients: List<IngredientLine> = emptyList(),
    val fallbackReason: FallbackReason? = null,
    val userActionRequired: String? = null,
)
