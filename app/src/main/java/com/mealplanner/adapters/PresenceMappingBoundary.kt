package com.mealplanner.adapters

import com.mealplanner.domain.model.MapIngredientsToStorePresenceBatchRequest
import com.mealplanner.domain.model.MapIngredientsToStorePresenceBatchResult

/**
 * C5 owns MapIngredientsToStorePresenceBatch (R-02). U1 exposes contract/stub only.
 * No PRISMA lookup implementation in U1; no list merge/stale semantics.
 */
interface PresenceMappingBoundary {
    suspend fun mapIngredientsToStorePresenceBatch(
        request: MapIngredientsToStorePresenceBatchRequest,
    ): MapIngredientsToStorePresenceBatchResult
}
