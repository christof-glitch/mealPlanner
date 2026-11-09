package com.mealplanner.adapters

import com.mealplanner.domain.model.IngredientLine
import com.mealplanner.domain.model.RecipeCandidateResult
import com.mealplanner.domain.model.RecipeId
import com.mealplanner.domain.model.RecipeSourceAdapterRequest
import com.mealplanner.domain.model.RecipeSourceAdapterResult
import com.mealplanner.domain.model.RecipeSourceStatus
import com.mealplanner.domain.model.RecipeSummary

/** C2/A1 boundary. U1 provides typed fallback-safe contracts; U2 completes behavior. */
interface RecipeSourceBoundary {
    suspend fun findCandidates(query: Map<String, String> = emptyMap()): RecipeCandidateResult
    suspend fun getRecipeSummary(recipeId: RecipeId): RecipeSummary?
    suspend fun resolveIngredients(recipeIds: List<RecipeId>): Map<RecipeId, List<IngredientLine>>
    fun sourceStatus(): RecipeSourceStatus

    /** U2 A1 execution path. Default keeps U1-compatible boundaries typed/unavailable. */
    suspend fun execute(request: RecipeSourceAdapterRequest): RecipeSourceAdapterResult =
        RecipeSourceAdapterResult(
            status = com.mealplanner.domain.model.RecipeAdapterStatus.unavailable,
            investigationOutcome = com.mealplanner.domain.model.A1InvestigationOutcome.unknown,
            sourceStatus = sourceStatus(),
            fallbackReason = com.mealplanner.domain.model.FallbackReason("NOT_IMPLEMENTED", "U2 adapter execution not configured."),
        )
}
