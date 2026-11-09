package com.mealplanner.ui

import com.mealplanner.domain.model.WeeklyPlanState

/**
 * Minimal UI shell state for U1 foundation. Full UX flows are owned by U4.
 */
data class MealPlannerUiState(
    val plan: WeeklyPlanState? = null,
    val statusMessage: String? = null,
)
