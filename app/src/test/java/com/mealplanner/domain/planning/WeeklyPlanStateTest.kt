package com.mealplanner.domain.planning

import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.ExclusionReasonCode
import com.mealplanner.domain.model.ExclusionSourceMarker
import com.mealplanner.domain.model.Freshness
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.RecipeSourceStatus
import com.mealplanner.domain.model.RecipeSourceType
import com.mealplanner.domain.model.RecipeSummary
import com.mealplanner.domain.model.SlotStatus
import com.mealplanner.domain.model.SydanmerkkiStatus
import com.mealplanner.domain.model.TimeConfidence
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WeeklyPlanStateTest {
    private fun recipe(
        id: String,
        total: Int?,
        confidence: TimeConfidence,
    ) = RecipeSummary(
        recipeId = id,
        title = id,
        totalTimeMinutes = total,
        timeConfidence = confidence,
        sydanmerkkiStatus = SydanmerkkiStatus.CURATED_FALLBACK,
        sourceStatus = RecipeSourceStatus(RecipeSourceType.CURATED_SYDANMERKKI_FALLBACK, Freshness.CACHED, true),
    )

    @Test
    fun `required 7-day slot structure includes weekend lunch and excludes weekday lunch`() {
        val plan = WeekdayEligibilityEvaluator.createEmptyPlan("plan-1")
        assertEquals(16, plan.slots.size, "Sat/Sun 3 meals + Mon-Fri 2 meals = 16")
        val sat = plan.slots.filter { it.slotRef.dayOfWeek == DayOfWeek.SATURDAY }.map { it.slotRef.mealType }.toSet()
        val mon = plan.slots.filter { it.slotRef.dayOfWeek == DayOfWeek.MONDAY }.map { it.slotRef.mealType }.toSet()
        assertEquals(setOf(MealSlotType.BREAKFAST, MealSlotType.LUNCH, MealSlotType.DINNER), sat)
        assertEquals(setOf(MealSlotType.BREAKFAST, MealSlotType.DINNER), mon)
        assertTrue(plan.slots.none { it.slotRef.dayOfWeek == DayOfWeek.WEDNESDAY && it.slotRef.mealType == MealSlotType.LUNCH })
    }

    @Test
    fun `empty slots remain visible and addressable`() {
        val plan = WeekdayEligibilityEvaluator.createEmptyPlan("plan-2")
        assertTrue(plan.slots.all { it.slotStatus == SlotStatus.EMPTY })
        assertTrue(plan.slots.all { it.assignedRecipeId == null })
        assertTrue(plan.slots.map { it.slotRef }.distinct().size == plan.slots.size)
    }

    @Test
    fun `weekday recipe over 30 minutes is ineligible with WEEKDAY_TIME_OVER_30`() {
        val reason = WeekdayEligibilityEvaluator.weekdayTimeExclusion(
            DayOfWeek.TUESDAY,
            recipe("r1", 31, TimeConfidence.KNOWN),
        )
        assertNotNull(reason)
        assertEquals(ExclusionReasonCode.WEEKDAY_TIME_OVER_30, reason.reasonCode)
        assertEquals("WEEKDAY_TIME_OVER_30", reason.messageKeyOrTextSource)
        assertEquals(ExclusionSourceMarker.RULE, reason.sourceMarker)
        assertEquals("r1", reason.recipeOrCandidateRef)
    }

    @Test
    fun `weekday recipe at 30 minutes is eligible`() {
        val reason = WeekdayEligibilityEvaluator.weekdayTimeExclusion(
            DayOfWeek.FRIDAY,
            recipe("r2", 30, TimeConfidence.KNOWN),
        )
        assertNull(reason)
    }

    @Test
    fun `weekday recipe with missing time is ineligible with WEEKDAY_TIME_AMBIGUOUS`() {
        val reason = WeekdayEligibilityEvaluator.weekdayTimeExclusion(
            DayOfWeek.MONDAY,
            recipe("r3", null, TimeConfidence.MISSING),
        )
        assertNotNull(reason)
        assertEquals(ExclusionReasonCode.WEEKDAY_TIME_AMBIGUOUS, reason.reasonCode)
        assertEquals(ExclusionSourceMarker.DATA, reason.sourceMarker)
    }

    @Test
    fun `weekday recipe with ambiguous time is ineligible with WEEKDAY_TIME_AMBIGUOUS and explanation fields`() {
        val reason = WeekdayEligibilityEvaluator.weekdayTimeExclusion(
            DayOfWeek.THURSDAY,
            recipe("r4", 20, TimeConfidence.AMBIGUOUS),
        )
        assertNotNull(reason)
        assertEquals(ExclusionReasonCode.WEEKDAY_TIME_AMBIGUOUS, reason.reasonCode)
        assertTrue(reason.messageKeyOrTextSource.isNotBlank())
        assertNotNull(reason.recipeOrCandidateRef)
    }

    @Test
    fun `weekend slots are not excluded by weekday time rule`() {
        val reason = WeekdayEligibilityEvaluator.weekdayTimeExclusion(
            DayOfWeek.SATURDAY,
            recipe("r5", 90, TimeConfidence.KNOWN),
        )
        assertNull(reason)
    }
}
