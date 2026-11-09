package com.mealplanner.domain.recipe

import com.mealplanner.domain.model.AlternativeRecipesRequest
import com.mealplanner.domain.model.AlternativeRecipesResult
import com.mealplanner.domain.model.AlternativeRecipesStatus
import com.mealplanner.domain.model.DayKind
import com.mealplanner.domain.model.ExcludedRecipeCandidate
import com.mealplanner.domain.model.ExclusionReason
import com.mealplanner.domain.model.ExclusionReasonCode
import com.mealplanner.domain.model.ExclusionSourceMarker
import com.mealplanner.domain.model.RecipeCandidateResult
import com.mealplanner.domain.model.RecipeDetail
import com.mealplanner.domain.model.RecipeQueryStatus
import com.mealplanner.domain.model.RecipeSummary
import com.mealplanner.domain.model.SeasonFitLabel
import com.mealplanner.domain.model.dayKind

/** Narrow port so alternatives can be tested without full catalog service. */
interface RecipeCatalogPort {
    suspend fun catalogCandidates(): RecipeCandidateResult
    suspend fun detailsByIds(ids: List<String>): List<RecipeDetail> = emptyList()
}

/** BR2.6: alternatives respect eligibility, season fit context, source/fallback status; no fabrication. */
class AlternativeRecipesSelector(private val catalog: RecipeCatalogPort) {
    suspend fun getAlternatives(request: AlternativeRecipesRequest): AlternativeRecipesResult {
        val candidates = catalog.catalogCandidates()
        if (!request.includeFallbackSources && candidates.sourceStatus.fallbackUsed) {
            return AlternativeRecipesResult(
                status = AlternativeRecipesStatus.UNAVAILABLE,
                alternatives = emptyList(),
                sourceStatus = candidates.sourceStatus,
                emptyReason = "FALLBACK_SOURCES_EXCLUDED",
            )
        }
        val details = catalog.detailsByIds(candidates.candidates.map { it.recipeId }).associateBy { it.recipeId }
        val (eligible, excluded) = partitionEligible(request, candidates.candidates, details)
        val filtered = eligible
            .filterNot { it.recipeId == request.currentRecipeId }
            .let { list -> request.limit?.let { list.take(it.coerceAtLeast(0)) } ?: list }
        val status = when {
            candidates.status == RecipeQueryStatus.UNAVAILABLE && filtered.isEmpty() -> AlternativeRecipesStatus.UNAVAILABLE
            filtered.isEmpty() -> AlternativeRecipesStatus.EMPTY
            candidates.sourceStatus.fallbackUsed || candidates.status == RecipeQueryStatus.FALLBACK -> AlternativeRecipesStatus.FALLBACK
            else -> AlternativeRecipesStatus.OK
        }
        return AlternativeRecipesResult(
            status = status,
            alternatives = filtered,
            excludedCandidates = excluded.takeIf { it.isNotEmpty() },
            sourceStatus = candidates.sourceStatus,
            emptyReason = if (filtered.isEmpty()) candidates.fallbackReason?.message ?: "NO_ELIGIBLE_ALTERNATIVES" else null,
        )
    }

    private fun partitionEligible(
        request: AlternativeRecipesRequest,
        candidates: List<RecipeSummary>,
        details: Map<String, RecipeDetail>,
    ): Pair<List<RecipeSummary>, List<ExcludedRecipeCandidate>> {
        val eligible = mutableListOf<RecipeSummary>()
        val excluded = mutableListOf<ExcludedRecipeCandidate>()
        for (c in candidates) {
            val norm = RecipeTimeNormalizationEvaluator.normalize(request.slotRef.dayOfWeek, request.slotRef.mealType, c)
            if (request.slotRef.dayOfWeek.dayKind() == DayKind.WEEKDAY && norm.weekdayEligible != true) {
                val reason = norm.exclusionReason ?: ExclusionReason(
                    reasonCode = ExclusionReasonCode.WEEKDAY_TIME_AMBIGUOUS,
                    messageKeyOrTextSource = "WEEKDAY_TIME_AMBIGUOUS",
                    recipeOrCandidateRef = c.recipeId,
                    sourceMarker = ExclusionSourceMarker.RULE,
                )
                excluded += ExcludedRecipeCandidate(recipeSummary = c, recipeOrCandidateRef = c.recipeId, exclusionReason = reason)
                continue
            }
            val detail = details[c.recipeId]
            if (detail != null && request.effectiveSeasonResolution != null) {
                val fit = SeasonFitEvaluator.evaluate(detail, request.effectiveSeasonResolution)
                if (fit.seasonFitLabel == SeasonFitLabel.OUT_OF_SEASON) {
                    excluded += ExcludedRecipeCandidate(
                        recipeSummary = c,
                        recipeOrCandidateRef = c.recipeId,
                        exclusionReason = ExclusionReason(
                            reasonCode = ExclusionReasonCode.OTHER,
                            messageKeyOrTextSource = "SEASON_FIT_OUT_OF_SEASON",
                            recipeOrCandidateRef = c.recipeId,
                            sourceMarker = ExclusionSourceMarker.RULE,
                        ),
                    )
                    continue
                }
            }
            eligible += c
        }
        return eligible to excluded
    }
}
