package com.mealplanner.ui.screens.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
import com.mealplanner.ui.state.SlotActionContext
import com.mealplanner.ui.state.TodayScreenState

/** RM-02 Today's meals (Step 7). R-06 shared slot state; unfilled slots remain empty. */
@Composable
fun TodayScreen(
    state: TodayScreenState,
    onViewRecipe: (SlotActionContext) -> Unit,
    onReplace: (SlotActionContext) -> Unit,
    onChooseRecipe: (SlotActionContext) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp).testTag("today-screen")) {
        Text(
            text = "Today${if (state.dateLabel.isNotBlank()) " — ${state.dateLabel}" else ""}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        SeasonContextIndicator(state.seasonContextLabel, modifier = Modifier.padding(vertical = 8.dp))
        if (state.loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).testTag("today-loading"))
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
        if (state.todaySlots.isEmpty()) {
            StatusBanner("No meals planned for today. Open Plan to choose recipes; unfilled slots remain empty until you select one.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                items(state.todaySlots, key = { SlotContextNav.stableSlotKey(it.context) }) { slot ->
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
