package com.mealplanner.domain.recipe

import com.mealplanner.domain.model.RecipeCandidateResult
import com.mealplanner.domain.model.RecipeDetail

/** Adapter to let RecipeCatalogService satisfy RecipeCatalogPort without changing service API. */
class RecipeCatalogPortAdapter(private val service: RecipeCatalogService) : RecipeCatalogPort {
    override suspend fun catalogCandidates(): RecipeCandidateResult = service.catalog()
    override suspend fun detailsByIds(ids: List<String>): List<RecipeDetail> =
        service.getByIds(ids).mapNotNull { it.recipe }
}
