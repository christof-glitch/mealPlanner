package com.mealplanner.domain.recipe

import com.mealplanner.domain.model.EffectiveSeasonResolution
import com.mealplanner.domain.model.EffectiveSeasonStatus
import com.mealplanner.domain.model.RecipeDetail
import com.mealplanner.domain.model.RecipeDataCompleteness
import com.mealplanner.domain.model.RecipeSourceStatus
import com.mealplanner.domain.model.RecipeSourceType
import com.mealplanner.domain.model.RecipeSummary
import com.mealplanner.domain.model.SeasonContext
import com.mealplanner.domain.model.SeasonFitLabel
import com.mealplanner.domain.model.SeasonMappingSource
import com.mealplanner.domain.model.SeasonResolutionSource
import com.mealplanner.domain.model.SydanmerkkiStatus
import com.mealplanner.domain.model.TimeConfidence
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonFitEvaluatorTest {
    private val src = RecipeSourceStatus(RecipeSourceType.CURATED_SYDANMERKKI_FALLBACK, com.mealplanner.domain.model.Freshness.CACHED, true)
    private val resolution = EffectiveSeasonResolution(
        effectiveContext = SeasonContext(resolvedSeason = "WINTER", resolutionSource = SeasonResolutionSource.OVERRIDE),
        status = EffectiveSeasonStatus.OVERRIDDEN,
    )
    private fun detail(tags: List<String>?) = RecipeDetail(
        recipeId = "r1", title = "Soup", summary = RecipeSummary("r1", "Soup", 20, TimeConfidence.KNOWN, SydanmerkkiStatus.CURATED_FALLBACK, src),
        seasonTags = tags, dataCompleteness = RecipeDataCompleteness.PARTIAL, sourceStatus = src,
    )

    @Test
    fun `recipe tag mapping yields in season or out of season with evidence`() {
        val inSeason = SeasonFitEvaluator.evaluate(detail(listOf("winter", "soup")), resolution)
        assertEquals(SeasonFitLabel.IN_SEASON, inSeason.seasonFitLabel)
        assertEquals(SeasonMappingSource.RECIPE_TAG, inSeason.mappingSource)
        assertTrue(inSeason.matchedTags?.contains("winter") == true)

        val out = SeasonFitEvaluator.evaluate(detail(listOf("summer")), resolution)
        assertEquals(SeasonFitLabel.OUT_OF_SEASON, out.seasonFitLabel)
    }

    @Test
    fun `insufficient evidence returns SEASON_FIT_UNKNOWN and never false match`() {
        val unknown = SeasonFitEvaluator.evaluate(detail(null), resolution)
        assertEquals(SeasonFitLabel.SEASON_FIT_UNKNOWN, unknown.seasonFitLabel)
        assertEquals(SeasonMappingSource.NONE, unknown.mappingSource)
        assertTrue(unknown.explanationKey?.contains("UNKNOWN", ignoreCase = true) == true)
    }

    @Test
    fun `unknown effective season resolution yields SEASON_FIT_UNKNOWN`() {
        val unknownRes = EffectiveSeasonResolution(
            effectiveContext = SeasonContext(resolvedSeason = null, resolutionSource = SeasonResolutionSource.UNKNOWN),
            status = EffectiveSeasonStatus.UNKNOWN,
        )
        val fit = SeasonFitEvaluator.evaluate(detail(listOf("winter")), unknownRes)
        assertEquals(SeasonFitLabel.SEASON_FIT_UNKNOWN, fit.seasonFitLabel)
    }

    @Test
    fun `override precedence is reflected in effective resolution carried through`() {
        val fit = SeasonFitEvaluator.evaluate(detail(listOf("winter")), resolution)
        assertEquals(EffectiveSeasonStatus.OVERRIDDEN, fit.effectiveSeasonResolution.status)
        assertEquals(SeasonResolutionSource.OVERRIDE, fit.effectiveSeasonResolution.effectiveContext.resolutionSource)
    }
}
