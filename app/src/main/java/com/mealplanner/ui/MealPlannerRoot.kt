package com.mealplanner.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.mealplanner.ui.navigation.MealPlannerScaffold

/**
 * App UI entry (Step 10). Launches U4 navigation shell (RM-01–RM-09) with thin orchestration boundaries.
 * Brownfield signature preserved for MainActivity; real facade/DI wiring is minimal in this MVP slice.
 */
@Composable
fun MealPlannerRoot(@Suppress("UNUSED_PARAMETER") state: MealPlannerUiState = MealPlannerUiState()) {
    MaterialTheme {
        MealPlannerScaffold()
    }
}

