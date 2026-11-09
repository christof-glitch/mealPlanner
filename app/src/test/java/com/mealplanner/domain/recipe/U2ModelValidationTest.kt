package com.mealplanner.domain.recipe

import com.mealplanner.domain.model.A1InvestigationOutcome
import com.mealplanner.domain.model.AlternativeRecipesRequest
import com.mealplanner.domain.model.AlternativeRecipesResult
import com.mealplanner.domain.model.AlternativeRecipesStatus
import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.EffectiveSeasonResolution
import com.mealplanner.domain.model.ExclusionReason
import com.mealplanner.domain.model.ExclusionReasonCode
import com.mealplanner.domain.model.ExclusionSourceMarker
import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.Freshness
import com.mealplanner.domain.model.LockStaleStatus
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.NutritionAvailability
import com.mealplanner.domain.model.NutritionCueStatus
import com.mealplanner.domain.model.NutritionLabels
import com.mealplanner.domain.model.NutritionProfile
import com.mealplanner.domain.model.NutritionSourceConfidence
import com.mealplanner.domain.model.PlanSlotRef
import com.mealplanner.domain.model.RecipeAdapterStatus
import com.mealplanner.domain.model.RecipeDataCompleteness
import com.mealplanner.domain.model.RecipeDetail
import com.mealplanner.domain.model.RecipeFilters
import com.mealplanner.domain.model.RecipeSourceAdapterRequest
import com.mealplanner.domain.model.RecipeSourceAdapterResult
import com.mealplanner.domain.model.RecipeSourceQueryType
import com.mealplanner.domain.model.RecipeSourceStatus
import com.mealplanner.domain.model.RecipeSourceType
import com.mealplanner.domain.model.RecipeSummary
import com.mealplanner.domain.model.RecipeTimeNormalization
import com.mealplanner.domain.model.SeasonContext
import com.mealplanner.domain.model.SeasonFitEvaluation
import com.mealplanner.domain.model.SeasonFitLabel
import com.mealplanner.domain.model.SeasonMappingSource
import com.mealplanner.domain.model.SeasonResolutionSource
import com.mealplanner.domain.model.SlotSelectionChangeRequest
import com.mealplanner.domain.model.SlotSelectionChangeResult
import com.mealplanner.domain.model.SlotSelectionOperation
import com.mealplanner.domain.model.SlotSelectionResultStatus
import com.mealplanner.domain.model.SydanmerkkiStatus
import com.mealplanner.domain.model.TimeConfidence
import com.mealplanner.domain.model.U2Validation
import com.mealplanner.domain.model.WeeklyNutritionBalanceCue
import com.mealplanner.domain.model.WeeklyNutritionBalanceCueRequest
import com.mealplanner.domain.planning.WeekdayEligibilityEvaluator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class U2ModelValidationTest {
    private val slot = PlanSlotRef("plan-1", DayOfWeek.MONDAY, MealSlotType.DINNER)
    private val sourceStatus = RecipeSourceStatus(
        RecipeSourceType.CURATED_SYDANMERKKI_FALLBACK, Freshness.CACHED, fallbackUsed = true,
    )

    @Test
    fun `adapter request and result carry typed A1 statuses and investigation outcome`() {
        val request = RecipeSourceAdapterRequest(
            queryType = RecipeSourceQueryType.BY_ID,
            recipeIds = listOf("r1"),
            slotContext = slot,
            allowLiveSource = false,
        )
        assertFalse(request.allowLiveSource)
        assertEquals(RecipeSourceQueryType.BY_ID, request.queryType)
        assertNull(U2Validation.validateAdapterRequest(request))

        val result = RecipeSourceAdapterResult(
            status = RecipeAdapterStatus.fallback_used,
            investigationOutcome = A1InvestigationOutcome.not_feasible_or_allowed,
            sourceStatus = sourceStatus,
            fallbackReason = FallbackReason("LIVE_DISABLED_DEFAULT", "Live source disabled by default."),
            partialDataUsable = false,
        )
        assertEquals(RecipeAdapterStatus.fallback_used, result.status)
        assertEquals(A1InvestigationOutcome.not_feasible_or_allowed, result.investigationOutcome)
        assertNotNull(result.fallbackReason)
    }

    @Test
    fun `adapter request validation rejects malformed slot and missing ids`() {
        val badSlot = RecipeSourceAdapterRequest(
            queryType = RecipeSourceQueryType.ALTERNATIVES,
            slotContext = PlanSlotRef("", DayOfWeek.MONDAY, MealSlotType.DINNER),
            allowLiveSource = false,
        )
        assertEquals("INVALID_SLOT_REF", U2Validation.validateAdapterRequest(badSlot)?.code)

        val missingIds = RecipeSourceAdapterRequest(
            queryType = RecipeSourceQueryType.BY_ID,
            recipeIds = emptyList(),
            allowLiveSource = false,
        )
        assertEquals("INVALID_REQUEST", U2Validation.validateAdapterRequest(missingIds)?.code)
    }

    @Test
    fun `recipe detail and normalization preserve explicit unavailable partial states`() {
        val summary = RecipeSummary(
            recipeId = "r1", title = "Fallback oats", totalTimeMinutes = null,
            timeConfidence = TimeConfidence.MISSING, sydanmerkkiStatus = SydanmerkkiStatus.CURATED_FALLBACK,
            sourceStatus = sourceStatus, nutritionAvailability = NutritionAvailability.UNAVAILABLE,
        )
        val detail = RecipeDetail(
            recipeId = "r1", title = summary.title, summary = summary,
            ingredients = null, preparationNotes = null, nutrition = null,
            dataCompleteness = RecipeDataCompleteness.MINIMAL, sourceStatus = sourceStatus,
        )
        assertEquals(RecipeDataCompleteness.MINIMAL, detail.dataCompleteness)
        assertNull(detail.ingredients)

        val normalization = RecipeTimeNormalization(
            timeVerifiability = TimeConfidence.AMBIGUOUS,
            weekdayEligible = false,
            exclusionReason = ExclusionReason(ExclusionReasonCode.WEEKDAY_TIME_AMBIGUOUS, "WEEKDAY_TIME_AMBIGUOUS", sourceMarker = ExclusionSourceMarker.DATA),
        )
        assertEquals(TimeConfidence.AMBIGUOUS, normalization.timeVerifiability)
        assertFalse(normalization.weekdayEligible == true)
        assertNotNull(normalization.exclusionReason)
    }

    @Test
    fun `season fit evaluation supports unknown with explanation and override precedence context`() {
        val resolution = EffectiveSeasonResolution(
            effectiveContext = SeasonContext(resolvedSeason = "WINTER", resolutionSource = SeasonResolutionSource.OVERRIDE),
            status = com.mealplanner.domain.model.EffectiveSeasonStatus.OVERRIDDEN,
        )
        val fit = SeasonFitEvaluation(
            seasonFitLabel = SeasonFitLabel.SEASON_FIT_UNKNOWN,
            effectiveSeasonResolution = resolution,
            mappingSource = SeasonMappingSource.NONE,
            explanationKey = "SEASON_FIT_UNKNOWN_NO_MAPPING",
        )
        assertEquals(SeasonFitLabel.SEASON_FIT_UNKNOWN, fit.seasonFitLabel)
        assertEquals("SEASON_FIT_UNKNOWN_NO_MAPPING", fit.explanationKey)
        assertEquals(SeasonMappingSource.NONE, fit.mappingSource)
    }

    @Test
    fun `nutrition profile and cue use explicit unavailable states and count exclusions`() {
        val missing = NutritionProfile(nutritionAvailability = NutritionAvailability.UNAVAILABLE)
        assertEquals(NutritionLabels.UNAVAILABLE, missing.unavailableLabel)

        val cue = WeeklyNutritionBalanceCue(
            status = NutritionCueStatus.PARTIAL, selectedRecipeCount = 5, recipesWithNutritionCount = 2,
            averageEnergyKcal = 420.0, excludedFromCalculationCount = 3,
            explanationKey = "NUTRITION_CUE_PARTIAL_EXCLUDES_MISSING",
        )
        assertEquals(3, cue.excludedFromCalculationCount)
        assertEquals(NutritionCueStatus.PARTIAL, cue.status)

        val req = WeeklyNutritionBalanceCueRequest(
            selectedRecipeRefs = listOf("r1", "r2"),
            nutritionProfilesByRecipeId = mapOf("r1" to missing, "r2" to NutritionProfile(NutritionAvailability.AVAILABLE, energyKcal = 500.0, sourceConfidence = NutritionSourceConfidence.HIGH)),
            includePartialProfiles = true,
        )
        assertNull(U2Validation.validateNutritionCueRequest(req))
        assertNotNull(U2Validation.validateNutritionCueRequest(WeeklyNutritionBalanceCueRequest(selectedRecipeRefs = listOf(""), includePartialProfiles = true)))
    }

    @Test
    fun `alternatives and slot selection contracts are typed and validated`() {
        val altReq = AlternativeRecipesRequest(slotRef = slot, seasonContext = SeasonContext(), includeFallbackSources = true)
        assertTrue(altReq.includeFallbackSources)
        val alt = AlternativeRecipesResult(
            status = AlternativeRecipesStatus.EMPTY, alternatives = emptyList(),
            sourceStatus = sourceStatus, emptyReason = "NO_ELIGIBLE_ALTERNATIVES",
        )
        assertEquals(AlternativeRecipesStatus.EMPTY, alt.status)

        val badSelection = SlotSelectionChangeRequest(slotRef = PlanSlotRef(" ", DayOfWeek.MONDAY, MealSlotType.DINNER), operation = SlotSelectionOperation.SET_SELECTION, recipeId = "r1")
        assertEquals("INVALID_SLOT_REF", U2Validation.validateSlotSelectionRequest(badSelection)?.code)

        val goodSelection = SlotSelectionChangeRequest(slotRef = slot, operation = SlotSelectionOperation.SET_SELECTION, recipeId = "r1")
        assertNull(U2Validation.validateSlotSelectionRequest(goodSelection))

        val plan = WeekdayEligibilityEvaluator.createEmptyPlan("plan-1")
        val result = SlotSelectionChangeResult(
            status = SlotSelectionResultStatus.APPLIED, weeklyPlanState = plan, changedSlotRef = slot,
            lockStaleStatus = LockStaleStatus(isStale = true, staleWarning = "Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."),
        )
        assertEquals("Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.", result.lockStaleStatus?.staleWarning)
    }

    @Test
    fun `recipe filters are additive and optional`() {
        val filters = RecipeFilters(mealTypes = listOf(MealSlotType.DINNER), maxTotalTimeMinutes = 30, includeUnknownSeason = true)
        assertEquals(30, filters.maxTotalTimeMinutes)
        assertNull(RecipeFilters().mealTypes)
    }
}
