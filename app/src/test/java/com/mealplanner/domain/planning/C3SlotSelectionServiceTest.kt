package com.mealplanner.domain.planning

import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.PlanSlotRef
import com.mealplanner.domain.model.SlotSelectionChangeRequest
import com.mealplanner.domain.model.SlotSelectionOperation
import com.mealplanner.domain.model.SlotSelectionResultStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class C3SlotSelectionServiceTest {
    private val service = C3SlotSelectionService()
    private val plan = WeekdayEligibilityEvaluator.createEmptyPlan("plan-1")
    private val slot = PlanSlotRef("plan-1", DayOfWeek.MONDAY, MealSlotType.DINNER)

    @Test
    fun `set selection affects only target slot and preserves single state`() {
        val req = SlotSelectionChangeRequest(slotRef = slot, operation = SlotSelectionOperation.SET_SELECTION, recipeId = "r1")
        val result = service.apply(plan, req, lockOrListActive = false)
        assertEquals(SlotSelectionResultStatus.APPLIED, result.status)
        assertEquals("r1", result.weeklyPlanState.slots.first { it.slotRef == slot }.assignedRecipeId)
        assertTrue(result.weeklyPlanState.slots.filterNot { it.slotRef == slot }.all { it.assignedRecipeId == null })
    }

    @Test
    fun `clear selection affects only target slot`() {
        val withRecipe = service.apply(plan, SlotSelectionChangeRequest(slot, SlotSelectionOperation.SET_SELECTION, recipeId = "r1"), false).weeklyPlanState
        val cleared = service.apply(withRecipe, SlotSelectionChangeRequest(slot, SlotSelectionOperation.CLEAR_SELECTION), false)
        assertEquals(SlotSelectionResultStatus.APPLIED, cleared.status)
        assertNull(cleared.weeklyPlanState.slots.first { it.slotRef == slot }.assignedRecipeId)
    }

    @Test
    fun `invalid slot ref rejected with typed result`() {
        val bad = SlotSelectionChangeRequest(PlanSlotRef("", DayOfWeek.MONDAY, MealSlotType.DINNER), SlotSelectionOperation.SET_SELECTION, recipeId = "r1")
        val result = service.apply(plan, bad, lockOrListActive = true)
        assertEquals(SlotSelectionResultStatus.REJECTED, result.status)
        assertTrue(result.rejectionReason?.contains("INVALID", ignoreCase = true) == true)
    }

    @Test
    fun `change marks stale with exact ADR-006 warning when lock or list context active and no auto regeneration`() {
        val locked = PlanLockService().lockPlan(plan).first
        val result = service.apply(locked, SlotSelectionChangeRequest(slot, SlotSelectionOperation.SET_SELECTION, recipeId = "r2"), lockOrListActive = true)
        assertEquals(SlotSelectionResultStatus.APPLIED, result.status)
        assertEquals(Adr006LockRules.STALE_WARNING_TEXT, result.lockStaleStatus?.staleWarning)
        assertEquals("Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.", result.lockStaleStatus?.staleWarning)
    }
}
