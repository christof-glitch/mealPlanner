package com.mealplanner.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mealplanner.ui.facades.DefaultPlanExperienceFacade
import com.mealplanner.ui.navigation.MealPlannerScaffold

/**
 * App UI entry (Step 10). Launches U4 navigation shell (RM-01–RM-09) with thin orchestration boundaries.
 * Brownfield signature preserved for MainActivity; real facade/DI wiring is minimal in this MVP slice.
 */
@Composable
fun MealPlannerRoot(@Suppress("UNUSED_PARAMETER") state: MealPlannerUiState = MealPlannerUiState()) {
    MaterialTheme {
        val planFacade = remember { DefaultPlanExperienceFacade() }
        var planState by remember { mutableStateOf(planFacade.currentPlanState()) }
        var todayState by remember { mutableStateOf(planFacade.currentTodayState()) }

        fun refresh() {
            planState = planFacade.currentPlanState()
            todayState = planFacade.currentTodayState()
        }

        MealPlannerScaffold(
            planState = planState,
            todayState = todayState,
            onPlanIntent = { intent ->
                planState = planFacade.handle(intent)
                todayState = planFacade.currentTodayState()
            },
            onPlanRetry = { refresh() },
            onTodayRetry = { refresh() },
            onSelectAlternative = { ctx, recipeId ->
                planState = planFacade.selectRecipe(ctx, recipeId)
                todayState = planFacade.currentTodayState()
            },
            onUseForSlot = { ctx, recipeId ->
                planState = planFacade.selectRecipe(ctx, recipeId)
                todayState = planFacade.currentTodayState()
            },
        )
    }
}

