package com.mealplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mealplanner.domain.planning.Adr006LockRules

/**
 * Minimal Compose shell for the integrated U1 skeleton (ADR-005 thin orchestration boundary).
 * U4 owns full Today/Plan/Recipe/Grocery UX.
 */
@Composable
fun MealPlannerRoot(state: MealPlannerUiState = MealPlannerUiState()) {
    MaterialTheme {
        Scaffold { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "Meal Planner — U1 foundation")
                    Text(text = "Plan slots: ${state.plan?.slots?.size ?: 0}")
                    Text(text = "Lock state: ${state.plan?.lockState ?: "not loaded"}")
                    state.plan?.staleWarning?.let { warning ->
                        Text(text = warning)
                    }
                    state.statusMessage?.let { Text(text = it) }
                    Text(text = "Stale warning contract: ${Adr006LockRules.STALE_WARNING_TEXT}")
                }
            }
        }
    }
}
