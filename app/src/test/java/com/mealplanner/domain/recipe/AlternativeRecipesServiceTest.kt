package com.mealplanner.domain.recipe

import com.mealplanner.domain.model.AlternativeRecipesRequest
import com.mealplanner.domain.model.AlternativeRecipesStatus
import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.EffectiveSeasonResolution
import com.mealplanner.domain.model.EffectiveSeasonStatus
import com.mealplanner.domain.model.Freshness
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.PlanSlotRef
import com.mealplanner.domain.model.RecipeCandidateResult
import com.mealplanner.domain.model.RecipeDataCompleteness
import com.mealplanner.domain.model.RecipeDetail
import com.mealplanner.domain.model.RecipeQueryStatus
import com.mealplanner.domain.model.RecipeSourceStatus
import com.mealplanner.domain.model.RecipeSourceType
import com.mealplanner.domain.model.RecipeSummary
import com.mealplanner.domain.model.SeasonContext
import com.mealplanner.domain.model.SydanmerkkiStatus
import com.mealplanner.domain.model.TimeConfidence
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AlternativeRecipesServiceTest {
    private val fallbackSrc = RecipeSourceStatus(RecipeSourceType.CURATED_SYDANMERKKI_FALLBACK, Freshness.CACHED, true)
    private val okSrc = RecipeSourceStatus(RecipeSourceType.CURATED_SYDANMERKKI_FALLBACK, Freshness.CACHED, false)
    private val winter = EffectiveSeasonResolution(effectiveContext = SeasonContext(resolvedSeason = "WINTER"), status = EffectiveSeasonStatus.RESOLVED)
    private val slot = PlanSlotRef("p1", DayOfWeek.MONDAY, MealSlotType.DINNER)

    private fun summary(id: String, total: Int? = 20, conf: TimeConfidence = TimeConfidence.KNOWN, source: RecipeSourceStatus = fallbackSrc) = RecipeSummary(
        recipeId = id, title = id, totalTimeMinutes = total, timeConfidence = conf,
        sydanmerkkiStatus = SydanmerkkiStatus.CURATED_FALLBACK, sourceStatus = source,
    )
    private fun detail(id: String, tags: List<String>?, source: RecipeSourceStatus = fallbackSrc) = RecipeDetail(
        recipeId = id, title = id,
        summary = summary(id, source = source),
        seasonTags = tags,
        dataCompleteness = RecipeDataCompleteness.PARTIAL,
        sourceStatus = source,
    )

    @Test
    fun `alternatives filter weekday ineligible and return typed result`() = runTest {
        val catalog = catalog(
            candidates = listOf(summary("ok", 20), summary("slow", 45), summary("amb", null, TimeConfidence.AMBIGUOUS)),
            sourceStatus = fallbackSrc,
        )
        val result = AlternativeRecipesSelector(catalog).getAlternatives(
            AlternativeRecipesRequest(slotRef = slot, seasonContext = SeasonContext(), effectiveSeasonResolution = winter, includeFallbackSources = true),
        )
        assertEquals(AlternativeRecipesStatus.FALLBACK, result.status)
        assertTrue(result.alternatives.any { it.recipeId == "ok" })
        assertTrue(result.alternatives.none { it.recipeId == "slow" || it.recipeId == "amb" })
    }

    @Test
    fun `empty alternatives return explicit reason`() = runTest {
        val catalog = catalog(listOf(summary("slow", 45)), fallbackSrc)
        val result = AlternativeRecipesSelector(catalog).getAlternatives(
            AlternativeRecipesRequest(slotRef = slot, seasonContext = SeasonContext(), includeFallbackSources = true),
        )
        assertEquals(AlternativeRecipesStatus.EMPTY, result.status)
        assertTrue(result.emptyReason?.isNotBlank() == true)
    }

    @Test
    fun `season fit context excludes out of season and keeps in season and unknown evidence`() = runTest {
        val catalog = catalog(
            candidates = listOf(summary("in"), summary("out"), summary("unknown")),
            sourceStatus = okSrc,
            details = listOf(detail("in", listOf("winter"), okSrc), detail("out", listOf("summer"), okSrc), detail("unknown", null, okSrc)),
        )
        val result = AlternativeRecipesSelector(catalog).getAlternatives(
            AlternativeRecipesRequest(slotRef = slot, seasonContext = SeasonContext(resolvedSeason = "WINTER"), effectiveSeasonResolution = winter, includeFallbackSources = true),
        )
        assertEquals(AlternativeRecipesStatus.OK, result.status)
        assertTrue(result.alternatives.any { it.recipeId == "in" })
        assertTrue(result.alternatives.any { it.recipeId == "unknown" })
        assertTrue(result.alternatives.none { it.recipeId == "out" })
    }

    @Test
    fun `includeFallbackSources false returns typed unavailable when only fallback source exists`() = runTest {
        val catalog = catalog(listOf(summary("ok", 20)), fallbackSrc)
        val result = AlternativeRecipesSelector(catalog).getAlternatives(
            AlternativeRecipesRequest(slotRef = slot, seasonContext = SeasonContext(), includeFallbackSources = false),
        )
        assertEquals(AlternativeRecipesStatus.UNAVAILABLE, result.status)
        assertTrue(result.alternatives.isEmpty())
        assertTrue(result.emptyReason?.contains("FALLBACK", ignoreCase = true) == true)
    }

    private fun catalog(
        candidates: List<RecipeSummary>,
        sourceStatus: RecipeSourceStatus,
        details: List<RecipeDetail> = emptyList(),
        status: RecipeQueryStatus = if (sourceStatus.fallbackUsed) RecipeQueryStatus.FALLBACK else RecipeQueryStatus.OK,
    ) = object : RecipeCatalogPort {
        override suspend fun catalogCandidates(): RecipeCandidateResult = RecipeCandidateResult(
            candidates = candidates, status = status, sourceStatus = sourceStatus,
        )
        override suspend fun detailsByIds(ids: List<String>): List<RecipeDetail> = details.filter { it.recipeId in ids }
    }
}
