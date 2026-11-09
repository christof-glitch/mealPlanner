package com.mealplanner.domain.recipe

import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.ExclusionReasonCode
import com.mealplanner.domain.model.ExclusionSourceMarker
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.RecipeSourceStatus
import com.mealplanner.domain.model.RecipeSourceType
import com.mealplanner.domain.model.RecipeSummary
import com.mealplanner.domain.model.SydanmerkkiStatus
import com.mealplanner.domain.model.TimeConfidence
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecipeTimeNormalizationEvaluatorTest {
    private val src = RecipeSourceStatus(RecipeSourceType.CURATED_SYDANMERKKI_FALLBACK, com.mealplanner.domain.model.Freshness.CACHED, true)

    private fun recipe(total: Int?, confidence: TimeConfidence, id: String = "r1") = RecipeSummary(
        recipeId = id, title = id, totalTimeMinutes = total, timeConfidence = confidence,
        sydanmerkkiStatus = SydanmerkkiStatus.CURATED_FALLBACK, sourceStatus = src,
    )

    @Test
    fun `KNOWN total 30 or less is eligible for weekday breakfast dinner`() {
        val n = RecipeTimeNormalizationEvaluator.normalize(DayOfWeek.MONDAY, MealSlotType.DINNER, recipe(25, TimeConfidence.KNOWN))
        assertEquals(TimeConfidence.KNOWN, n.timeVerifiability)
        assertEquals(25, n.totalTimeMinutes)
        assertTrue(n.weekdayEligible == true)
        assertNull(n.exclusionReason)
    }

    @Test
    fun `KNOWN total over 30 excluded with WEEKDAY_TIME_OVER_30`() {
        val n = RecipeTimeNormalizationEvaluator.normalize(DayOfWeek.TUESDAY, MealSlotType.BREAKFAST, recipe(45, TimeConfidence.KNOWN, "slow"))
        assertEquals(TimeConfidence.KNOWN, n.timeVerifiability)
        assertFalse(n.weekdayEligible == true)
        assertEquals(ExclusionReasonCode.WEEKDAY_TIME_OVER_30, n.exclusionReason?.reasonCode)
        assertEquals(ExclusionSourceMarker.RULE, n.exclusionReason?.sourceMarker)
        assertEquals("slow", n.exclusionReason?.recipeOrCandidateRef)
    }

    @Test
    fun `MISSING or AMBIGUOUS time is never guessed and excluded with WEEKDAY_TIME_AMBIGUOUS`() {
        val missing = RecipeTimeNormalizationEvaluator.normalize(DayOfWeek.WEDNESDAY, MealSlotType.DINNER, recipe(null, TimeConfidence.MISSING))
        assertEquals(TimeConfidence.MISSING, missing.timeVerifiability)
        assertFalse(missing.weekdayEligible == true)
        assertEquals(ExclusionReasonCode.WEEKDAY_TIME_AMBIGUOUS, missing.exclusionReason?.reasonCode)

        val ambiguous = RecipeTimeNormalizationEvaluator.normalize(DayOfWeek.THURSDAY, MealSlotType.BREAKFAST, recipe(null, TimeConfidence.AMBIGUOUS), rawTimeText = "20-40 min")
        assertEquals(TimeConfidence.AMBIGUOUS, ambiguous.timeVerifiability)
        assertNull(ambiguous.totalTimeMinutes)
        assertEquals(ExclusionReasonCode.WEEKDAY_TIME_AMBIGUOUS, ambiguous.exclusionReason?.reasonCode)
        assertEquals("20-40 min", ambiguous.rawTimeText)
    }

    @Test
    fun `KNOWN confidence but missing total minutes is treated as ambiguous not guessed`() {
        val n = RecipeTimeNormalizationEvaluator.normalize(DayOfWeek.FRIDAY, MealSlotType.DINNER, recipe(null, TimeConfidence.KNOWN))
        assertFalse(n.weekdayEligible == true)
        assertEquals(ExclusionReasonCode.WEEKDAY_TIME_AMBIGUOUS, n.exclusionReason?.reasonCode)
        assertTrue(n.timeVerifiability == TimeConfidence.AMBIGUOUS || n.timeVerifiability == TimeConfidence.MISSING)
    }

    @Test
    fun `weekend slots are not subject to weekday 30 minute rule`() {
        val n = RecipeTimeNormalizationEvaluator.normalize(DayOfWeek.SATURDAY, MealSlotType.LUNCH, recipe(90, TimeConfidence.KNOWN))
        assertEquals(TimeConfidence.KNOWN, n.timeVerifiability)
        assertNull(n.exclusionReason)
        assertTrue(n.weekdayEligible != false)
    }
}
