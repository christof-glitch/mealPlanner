package com.mealplanner.adapters

import com.mealplanner.domain.model.BatchPresenceResult
import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.Freshness
import com.mealplanner.domain.model.IngredientLine
import com.mealplanner.domain.model.MapIngredientsToStorePresenceBatchRequest
import com.mealplanner.domain.model.MapIngredientsToStorePresenceBatchResult
import com.mealplanner.domain.model.PresenceBatchOverallStatus
import com.mealplanner.domain.model.PresenceLookupReason
import com.mealplanner.domain.model.PresenceSource
import com.mealplanner.domain.model.RecipeCandidateResult
import com.mealplanner.domain.model.RecipeId
import com.mealplanner.domain.model.RecipeQueryStatus
import com.mealplanner.domain.model.RecipeSourceStatus
import com.mealplanner.domain.model.RecipeSourceType
import com.mealplanner.domain.model.RecipeSummary
import com.mealplanner.domain.model.StorePresenceResult
import com.mealplanner.domain.model.StorePresenceStatus

/** Fallback-safe C2 stub: no live Sydänmerkki calls in U1. */
class FallbackRecipeSourceBoundary : RecipeSourceBoundary {
    private val status = RecipeSourceStatus(
        sourceType = RecipeSourceType.NONE,
        freshness = Freshness.UNKNOWN,
        fallbackUsed = true,
        fallbackReason = FallbackReason("RECIPE_SOURCE_UNAVAILABLE", "Live recipe source not configured in U1 foundation."),
        userActionRequired = "Use curated recipes or configure recipe source in a later unit.",
    )

    override suspend fun findCandidates(query: Map<String, String>): RecipeCandidateResult =
        RecipeCandidateResult(status = RecipeQueryStatus.UNAVAILABLE, sourceStatus = status, fallbackReason = status.fallbackReason)

    override suspend fun getRecipeSummary(recipeId: RecipeId): RecipeSummary? = null

    override suspend fun resolveIngredients(recipeIds: List<RecipeId>): Map<RecipeId, List<IngredientLine>> =
        recipeIds.associateWith { emptyList() }

    override fun sourceStatus(): RecipeSourceStatus = status
}

/**
 * Fallback-safe C5 stub: name/presence mapping only, returns UNKNOWN projections.
 * Never PRISMA lookup in U1; never owns merge/stale semantics.
 */
class FallbackPresenceMappingBoundary : PresenceMappingBoundary {
    override suspend fun mapIngredientsToStorePresenceBatch(
        request: MapIngredientsToStorePresenceBatchRequest,
    ): MapIngredientsToStorePresenceBatchResult {
        val reason = PresenceLookupReason(
            code = "PRISMA_LOOKUP_NOT_CONFIGURED",
            message = "Store presence lookup is unavailable in U1 foundation.",
            userActionRequired = "Use manual checklist or configure PRISMA in a later unit.",
        )
        val results = request.queryItems.map { item ->
            StorePresenceResult(
                ingredientRef = item.ingredientRef,
                queryText = item.queryText,
                storeRef = request.storeRef,
                status = StorePresenceStatus.UNKNOWN,
                reason = reason,
                source = PresenceSource.UNAVAILABLE,
            )
        }
        return MapIngredientsToStorePresenceBatchResult(
            batchResult = BatchPresenceResult(
                storeRef = request.storeRef,
                results = results,
                overallStatus = if (results.isEmpty()) PresenceBatchOverallStatus.COMPLETE else PresenceBatchOverallStatus.PARTIAL,
                fallbackReason = FallbackReason("PRISMA_UNAVAILABLE", reason.message, reason.userActionRequired),
            ),
            mappingWarnings = listOf(reason),
        )
    }
}
