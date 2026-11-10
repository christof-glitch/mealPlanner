package com.mealplanner.ui.facades

import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.planning.WeekdayEligibilityEvaluator
import com.mealplanner.domain.recipe.CuratedRecipeCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateWeekPlanFacadeTest {
    @Test
    fun generateWeekPlanFillsComingWeekSlotsWithoutDuplicateRecipeIds() {
        val facade = DefaultPlanExperienceFacade(
            initialPlan = WeekdayEligibilityEvaluator.createEmptyPlan("plan-ui"),
            todayDayProvider = { DayOfWeek.MONDAY },
        )
        facade.generateWeekPlan()
        val plan = facade.currentPlanState()
        val filled = plan.weekSlots.filterNot { it.isEmpty }
        assertTrue(filled.isNotEmpty())
        val recipeIds = filled.mapNotNull { it.context.currentRecipeId }
        assertEquals(recipeIds.size, recipeIds.toSet().size, "A recipe must appear only once in weekly calendar")
        val mondayBreakfast = plan.weekSlots.first { it.context.dayOfWeek == DayOfWeek.MONDAY && it.context.mealType == MealSlotType.BREAKFAST }
        val mondayBreakfastId = mondayBreakfast.context.currentRecipeId
        if (mondayBreakfastId != null) {
            val recipe = CuratedRecipeCatalog.details().firstOrNull { it.recipeId == mondayBreakfastId }
            assertTrue(recipe != null && CuratedRecipeCatalog.isBreakfastTitle(recipe.title), "Breakfast slot should get breakfast recipe when available")
        }
    }
}
