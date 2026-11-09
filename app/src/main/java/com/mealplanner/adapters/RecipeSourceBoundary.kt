package com.mealplanner.adapters

import com.mealplanner.domain.model.IngredientLine
import com.mealplanner.domain.model.RecipeCandidateResult
import com.mealplanner.domain.model.RecipeId
import com.mealplanner.domain.model.RecipeSourceStatus
import com.mealplanner.domain.model.RecipeSummary

/** C2/A1 boundary baseline. U1 provides typed fallback-safe contracts; U2 completes behavior. */
interface RecipeSourceBoundary {
    suspend fun findCandidates(query: Map<String, String> = emptyMap()): RecipeCandidateResult
    suspend fun getRecipeSummary(recipeId: RecipeId): RecipeSummary?
    suspend fun resolveIngredients(recipeIds: List<RecipeId>): Map<RecipeId, List<IngredientLine>>
    fun sourceStatus(): RecipeSourceStatus
}
