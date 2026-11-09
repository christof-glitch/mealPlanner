package com.mealplanner.domain.recipe

import com.mealplanner.adapters.CuratedRecipeFallbackSource
import com.mealplanner.adapters.RecipeSourceAdapterBoundary
import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.Freshness
import com.mealplanner.domain.model.IngredientLine
import com.mealplanner.domain.model.RecipeAdapterStatus
import com.mealplanner.domain.model.RecipeDataCompleteness
import com.mealplanner.domain.model.RecipeDetail
import com.mealplanner.domain.model.RecipeSourceAdapterRequest
import com.mealplanner.domain.model.RecipeSourceQueryType
import com.mealplanner.domain.model.RecipeSourceStatus
import com.mealplanner.domain.model.RecipeSourceType
import com.mealplanner.domain.model.RecipeSummary
import com.mealplanner.domain.model.SydanmerkkiStatus
import com.mealplanner.domain.model.TimeConfidence
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecipeCatalogServiceTest {
    private val sourceStatus = RecipeSourceStatus(RecipeSourceType.CURATED_SYDANMERKKI_FALLBACK, Freshness.CACHED, true, FallbackReason("CURATED", "curated"))
    private val summary = RecipeSummary("r1", "Oats", 10, TimeConfidence.KNOWN, SydanmerkkiStatus.CURATED_FALLBACK, sourceStatus)
    private val detail = RecipeDetail("r1", "Oats", summary, ingredients = listOf(IngredientLine(name = "oats", normalizedName = "oats", sourceRecipeId = "r1")), dataCompleteness = RecipeDataCompleteness.PARTIAL, sourceStatus = sourceStatus)

    @Test
    fun `catalog exposes fallback source status and never fabricates missing fields`() = runTest {
        val service = RecipeCatalogService(RecipeSourceAdapterBoundary(fallbackSource = FixedFallback(listOf(detail), sourceStatus)))
        val result = service.catalog()
        assertEquals(com.mealplanner.domain.model.RecipeQueryStatus.FALLBACK, result.status)
        assertEquals("r1", result.candidates.single().recipeId)
        assertTrue(result.sourceStatus.fallbackUsed)
    }

    @Test
    fun `getById returns explicit missing when unavailable`() = runTest {
        val service = RecipeCatalogService(RecipeSourceAdapterBoundary(fallbackSource = FixedFallback(emptyList(), sourceStatus)))
        val missing = service.getByIds(listOf("missing"))
        assertEquals(RecipeDetailFoundStatus.missing, missing.single().status)
        assertTrue(missing.single().message?.isNotBlank() == true)
    }

    @Test
    fun `resolveIngredients normalizes blank ingredient name explicitly`() = runTest {
        val bad = detail.copy(ingredients = listOf(IngredientLine(name = "  ", sourceRecipeId = "r1")))
        val service = RecipeCatalogService(RecipeSourceAdapterBoundary(fallbackSource = FixedFallback(listOf(bad), sourceStatus)))
        val resolved = service.resolveIngredients(listOf("r1"))
        assertEquals("Unknown ingredient", resolved["r1"]?.single()?.name)
    }

    private class FixedFallback(
        private val recipes: List<RecipeDetail>,
        private val status: RecipeSourceStatus,
    ) : CuratedRecipeFallbackSource {
        override suspend fun find(request: RecipeSourceAdapterRequest): List<RecipeDetail> = recipes
        override fun sourceStatus(): RecipeSourceStatus = status
    }
}
