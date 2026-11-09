package com.mealplanner.domain.model

import java.time.Instant

/** ENT-U1-009 */
data class StoreRef(
    val storeId: String? = null,
    val displayName: String,
    val chain: StoreChain? = null,
    val selectionSource: StoreSelectionSource = StoreSelectionSource.MANUAL,
    val isConfirmed: Boolean = false,
)

/** ENT-U1-010: name/presence only, never stock quantity or guaranteed availability. */
data class StorePresenceResult(
    val ingredientRef: IngredientRefId? = null,
    val queryText: String,
    val storeRef: StoreRef,
    val status: StorePresenceStatus,
    val reason: PresenceLookupReason? = null,
    val checkedAt: Instant? = null,
    val source: PresenceSource = PresenceSource.UNAVAILABLE,
)

enum class PresenceSource { PRISMA, MANUAL_FALLBACK, CACHE, UNAVAILABLE }

/** ENT-U1-011 */
data class BatchPresenceResult(
    val storeRef: StoreRef,
    val results: List<StorePresenceResult>,
    val overallStatus: PresenceBatchOverallStatus,
    val fallbackReason: FallbackReason? = null,
)

/** ENT-U1-026 */
data class MapIngredientsToStorePresenceBatchRequest(
    val ingredientRefs: List<IngredientRefId> = emptyList(),
    val queryItems: List<PresenceQueryItem>,
    val storeRef: StoreRef,
    val lookupContext: Map<String, String> = emptyMap(),
)

data class PresenceQueryItem(
    val ingredientRef: IngredientRefId? = null,
    val queryText: String,
    val normalizedName: String? = null,
)

/** ENT-U1-027 */
data class MapIngredientsToStorePresenceBatchResult(
    val batchResult: BatchPresenceResult,
    val mappingWarnings: List<PresenceLookupReason> = emptyList(),
)

/** ENT-U1-024 */
data class ApplyBatchPresenceToListRequest(
    val shoppingListId: ShoppingListId? = null,
    val items: List<ShoppingListItem>,
    val projections: List<StorePresenceResult>,
    val storeRef: StoreRef,
    val mappingContext: Map<String, String> = emptyMap(),
)

/** ENT-U1-025 */
data class ApplyBatchPresenceToListResult(
    val items: List<ShoppingListItem>,
    val status: ApplyPresenceStatus,
    val unknownOrRemapCount: Int = 0,
    val fallbackReason: FallbackReason? = null,
)
