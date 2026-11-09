package com.mealplanner.ui.screens.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mealplanner.ui.components.ErrorRetryPanel
import com.mealplanner.ui.components.MealSlotCard
import com.mealplanner.ui.components.NutritionBalanceCueCard
import com.mealplanner.ui.components.R05FallbackPanel
import com.mealplanner.ui.components.SeasonContextIndicator
import com.mealplanner.ui.components.StatusBanner
import com.mealplanner.ui.navigation.SlotContextNav
import com.mealplanner.ui.state.PlanScreenState
import com.mealplanner.ui.state.SlotActionContext

/** RM-03 Plan week view (Step 7). R-06 shared slot state; lock links to Grocery without auto-generation (R-03). */
@Composable
fun PlanScreen(
    state: PlanScreenState,
    onViewRecipe: (SlotActionContext) -> Unit,
    onReplace: (SlotActionContext) -> Unit,
    onChooseRecipe: (SlotActionContext) -> Unit,
    onLockRecipes: () -> Unit,
    onNavigateGrocery: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp).testTag("plan-screen")) {
        Text(
            text = "Plan",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        SeasonContextIndicator(state.seasonContextLabel, modifier = Modifier.padding(vertical = 8.dp))
        if (state.loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).testTag("plan-loading"))
            return@Column
        }
        state.r05State?.let { r05 ->
            R05FallbackPanel(state = r05, onRetry = onRetry, onBundledFallback = onRetry, onManualPlanning = onRetry)
            return@Column
        }
        state.errorMessage?.let { ErrorRetryPanel(message = it, onRetry = onRetry) }
        state.fallbackMessage?.let { StatusBanner(it) }
        state.statusMessage?.let { StatusBanner(it) }
        NutritionBalanceCueCard(
            label = state.nutritionCueLabel.orEmpty(),
            unavailable = state.nutritionCueLabel.isNullOrBlank(),
            modifier = Modifier.padding(vertical = 8.dp),
        )
        Card(modifier = Modifier.fillMaxWidth().testTag("lock-control-card")) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Lock status: ${state.lockStateLabel}", style = MaterialTheme.typography.titleSmall)
                state.lockSummary?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                Text(
                    "Locking recipes prepares the shopping snapshot. It does not generate the shopping list automatically.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onLockRecipes,
                        modifier = Modifier.testTag("lock-recipes-action").semantics { contentDescription = "Lock recipes for shopping list" },
                    ) { Text(if (state.isLocked) "Update lock" else "Lock recipes for shopping list") }
                    OutlinedButton(
                        onClick = onNavigateGrocery,
                        modifier = Modifier.testTag("go-to-grocery-action").semantics { contentDescription = "Go to Grocery" },
                    ) { Text("Grocery") }
                }
            }
        }
        if (state.weekSlots.isEmpty()) {
            StatusBanner("No plan slots available. Continue with manual planning or retry loading data.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                items(state.weekSlots, key = { SlotContextNav.stableSlotKey(it.context) }) { slot ->
                    MealSlotCard(
                        model = slot,
                        onViewRecipe = { onViewRecipe(slot.context) },
                        onReplace = { onReplace(slot.context) },
                        onChooseRecipe = { onChooseRecipe(slot.context) },
                    )
                }
            }
        }
    }
}
