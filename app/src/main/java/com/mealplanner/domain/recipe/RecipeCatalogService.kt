package com.mealplanner.domain.recipe

import com.mealplanner.adapters.RecipeSourceBoundary
import com.mealplanner.domain.model.IngredientLine
import com.mealplanner.domain.model.RecipeAdapterStatus
import com.mealplanner.domain.model.RecipeCandidateResult
import com.mealplanner.domain.model.RecipeDataCompleteness
import com.mealplanner.domain.model.RecipeDetail
import com.mealplanner.domain.model.RecipeId
import com.mealplanner.domain.model.RecipeQueryStatus
import com.mealplanner.domain.model.RecipeSourceAdapterRequest
import com.mealplanner.domain.model.RecipeSourceQueryType
import com.mealplanner.domain.model.RecipeSummary

/**
 * C2 catalog/query facade (BR2.2, BR2.9). Uses A1 boundary; normalizes missing fields to explicit states.
 * No fabrication. U4 owns presentation.
 */
class RecipeCatalogService(private val boundary: RecipeSourceBoundary) {
    suspend fun catalog(request: RecipeSourceAdapterRequest = RecipeSourceAdapterRequest(RecipeSourceQueryType.CATALOG, allowLiveSource = false)): RecipeCandidateResult =
        normalizeCandidates(boundary.execute(request.copy(allowLiveSource = false, queryType = RecipeSourceQueryType.CATALOG)))

    suspend fun getById(recipeId: RecipeId): RecipeDetail? {
        val result = boundary.execute(RecipeSourceAdapterRequest(RecipeSourceQueryType.BY_ID, recipeIds = listOf(recipeId), allowLiveSource = false))
        return result.recipes?.firstOrNull { it.recipeId == recipeId }?.let(::normalizeDetail)
            ?: boundary.getRecipeSummary(recipeId)?.let { RecipeDetail(it.recipeId, it.title, it, dataCompleteness = RecipeDataCompleteness.MINIMAL, sourceStatus = it.sourceStatus) }
    }

    suspend fun getByIds(recipeIds: List<RecipeId>): List<RecipeDetailOrMissing> {
        val result = boundary.execute(RecipeSourceAdapterRequest(RecipeSourceQueryType.BY_IDS, recipeIds = recipeIds, allowLiveSource = false))
        val found = result.recipes.orEmpty().associateBy { it.recipeId }
        return recipeIds.map { id ->
            val detail = found[id]
            if (detail != null) RecipeDetailOrMissing(id, RecipeDetailFoundStatus.found, normalizeDetail(detail))
            else RecipeDetailOrMissing(id, RecipeDetailFoundStatus.missing, message = "Recipe unavailable from current source/fallback.")
        }
    }

    suspend fun resolveIngredients(recipeIds: List<RecipeId>): Map<RecipeId, List<IngredientLine>> =
        boundary.resolveIngredients(recipeIds).mapValues { (_, lines) -> lines.map(::normalizeIngredientLine) }

    private fun normalizeCandidates(result: com.mealplanner.domain.model.RecipeSourceAdapterResult): RecipeCandidateResult {
        val candidates = (result.summaries ?: result.recipes?.map { it.summary }.orEmpty()).map(::normalizeSummary)
        val status = when (result.status) {
            RecipeAdapterStatus.ok -> if (candidates.isEmpty()) RecipeQueryStatus.UNAVAILABLE else RecipeQueryStatus.OK
            RecipeAdapterStatus.fallback_used, RecipeAdapterStatus.parse_error_mapped -> RecipeQueryStatus.FALLBACK
            else -> if (candidates.isEmpty()) RecipeQueryStatus.UNAVAILABLE else RecipeQueryStatus.PARTIAL
        }
        return RecipeCandidateResult(candidates = candidates, status = status, sourceStatus = result.sourceStatus, fallbackReason = result.fallbackReason)
    }

    private fun normalizeSummary(summary: RecipeSummary): RecipeSummary = summary.copy(
        title = summary.title.ifBlank { "Unavailable recipe" },
    )

    private fun normalizeDetail(detail: RecipeDetail): RecipeDetail = detail.copy(
        title = detail.title.ifBlank { "Unavailable recipe" },
        ingredients = detail.ingredients?.map(::normalizeIngredientLine),
        preparationNotes = detail.preparationNotes?.filter { it.isNotBlank() }?.ifEmpty { null },
        cookingGuidance = detail.cookingGuidance?.filter { it.isNotBlank() }?.ifEmpty { null },
        seasonTags = detail.seasonTags?.filter { it.isNotBlank() }?.ifEmpty { null },
    )

    private fun normalizeIngredientLine(line: IngredientLine): IngredientLine = line.copy(
        name = line.name.ifBlank { "Unknown ingredient" },
        availabilityStatus = if (line.name.isBlank()) com.mealplanner.domain.model.IngredientAvailabilityStatus.MISSING else line.availabilityStatus,
    )
}

enum class RecipeDetailFoundStatus { found, missing, changed }
data class RecipeDetailOrMissing(val recipeId: RecipeId, val status: RecipeDetailFoundStatus, val recipe: RecipeDetail? = null, val message: String? = null)
