package com.mealplanner.ui.facades

import com.mealplanner.domain.model.SlotSelectionChangeRequest
import com.mealplanner.ui.state.PlanIntent
import com.mealplanner.ui.state.PlanScreenState
import com.mealplanner.ui.state.SlotActionContext
import com.mealplanner.ui.state.TodayScreenState

/**
 * Thin UI facade for shared Today/Plan slot state (R-06, ADR-005).
 * Domain owns selection/lock/stale rules; UI orchestrates and renders typed state.
 */
interface PlanExperienceFacade {
    fun currentPlanState(): PlanScreenState
    fun currentTodayState(): TodayScreenState
    /** Applies a plan intent. Selection/replace must update shared state and carry slot context. */
    fun handle(intent: PlanIntent): PlanScreenState
    fun selectRecipe(context: SlotActionContext, recipeId: String): PlanScreenState
    fun clearSlot(context: SlotActionContext): PlanScreenState
}
