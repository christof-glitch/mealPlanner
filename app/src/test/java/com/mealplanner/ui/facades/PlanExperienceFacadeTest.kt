package com.mealplanner.ui.facades

import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.PlanSlotRef
import com.mealplanner.domain.planning.WeekdayEligibilityEvaluator
import com.mealplanner.ui.state.SlotActionContext
import com.mealplanner.ui.state.SlotOrigin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Step 3 (test-first, key behavior): R-06 shared Today/Plan slot state, unfilled slots remain empty,
 * slot actions carry slotId/day/mealType/origin, selection updates shared state without auto-fill/hide.
 */
class PlanExperienceFacadeTest {
    private fun facade() = DefaultPlanExperienceFacade(
        initialPlan = WeekdayEligibilityEvaluator.createEmptyPlan("plan-ui"),
        todayDayProvider = { DayOfWeek.MONDAY },
    )

    private fun mondayDinnerContext(origin: SlotOrigin = SlotOrigin.PLAN) = SlotActionContext(
        slotRef = PlanSlotRef("plan-ui", DayOfWeek.MONDAY, MealSlotType.DINNER),
        origin = origin,
    )

    @Test
    fun todayAndPlanObserveSameSharedSlotState() {
        val f = facade()
        f.selectRecipe(mondayDinnerContext(SlotOrigin.TODAY), "recipe-1")
        val planSlot = f.currentPlanState().weekSlots.first { it.context.dayOfWeek == DayOfWeek.MONDAY && it.context.mealType == MealSlotType.DINNER }
        val todaySlot = f.currentTodayState().todaySlots.firstOrNull { it.context.dayOfWeek == DayOfWeek.MONDAY && it.context.mealType == MealSlotType.DINNER }
        assertEquals("recipe-1", planSlot.context.currentRecipeId)
        assertNotNull(todaySlot, "Today should observe Monday dinner from shared state")
        assertEquals("recipe-1", todaySlot.context.currentRecipeId)
    }

    @Test
    fun unfilledSlotsRemainEmptyAndNoAutoFillOrHide() {
        val f = facade()
        f.selectRecipe(mondayDinnerContext(), "recipe-1")
        val otherFilled = f.currentPlanState().weekSlots.filter { it.context.slotId != mondayDinnerContext().slotId && !it.isEmpty }
        assertTrue(otherFilled.isEmpty(), "No other slots may be auto-filled")
        val emptyCount = f.currentPlanState().weekSlots.count { it.isEmpty }
        assertTrue(emptyCount >= 1, "Unfilled slots must remain present/empty, not hidden")
    }

    @Test
    fun slotActionsCarrySlotIdDayMealTypeAndOrigin() {
        val ctx = mondayDinnerContext(SlotOrigin.TODAY).copy(currentRecipeId = null)
        assertEquals("plan-ui:MONDAY:DINNER", ctx.slotId)
        assertEquals(DayOfWeek.MONDAY, ctx.dayOfWeek)
        assertEquals(MealSlotType.DINNER, ctx.mealType)
        assertEquals(SlotOrigin.TODAY, ctx.origin)
    }

    @Test
    fun selectionUpdatesSharedStateForTargetSlotOnly() {
        val f = facade()
        val other = SlotActionContext(PlanSlotRef("plan-ui", DayOfWeek.MONDAY, MealSlotType.BREAKFAST), origin = SlotOrigin.PLAN)
        f.selectRecipe(mondayDinnerContext(), "r-mon")
        f.selectRecipe(other, "r-other")
        val mondayDinner = f.currentPlanState().weekSlots.first { it.context.dayOfWeek == DayOfWeek.MONDAY && it.context.mealType == MealSlotType.DINNER }
        val mondayBreakfast = f.currentPlanState().weekSlots.first { it.context.dayOfWeek == DayOfWeek.MONDAY && it.context.mealType == MealSlotType.BREAKFAST }
        assertEquals("r-mon", mondayDinner.context.currentRecipeId)
        assertEquals("r-other", mondayBreakfast.context.currentRecipeId)
    }

    @Test
    fun replaceOrClearPreservesEmptySlotBehavior() {
        val f = facade()
        f.selectRecipe(mondayDinnerContext(), "r1")
        f.clearSlot(mondayDinnerContext())
        val monday = f.currentPlanState().weekSlots.first { it.context.slotId == mondayDinnerContext().slotId }
        assertTrue(monday.isEmpty)
        assertNull(monday.context.currentRecipeId)
    }
}
