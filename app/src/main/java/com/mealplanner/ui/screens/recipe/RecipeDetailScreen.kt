package com.mealplanner.ui.screens.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mealplanner.ui.components.RecipeMetaBadges
import com.mealplanner.ui.components.StatusBanner
import com.mealplanner.ui.state.SlotActionContext

/** RM-04 Recipe detail (pushed, Step 8). Domain-provided labels/eligibility explanations only; no fabrication (ADR-005). */
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    slotContext: SlotActionContext?,
    onUseForSlot: (SlotActionContext, String) -> Unit,
    onChooseDifferent: (SlotActionContext) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    sourceLabel: String? = null,
    seasonFitLabel: String = "Season fit unknown",
    timeLabel: String? = null,
    weekdayEligibilityLabel: String? = null,
    nutritionLabel: String = "Nutrition information unavailable",
    ingredients: List<String> = emptyList(),
    prepNotes: String? = null,
    missingDataLabels: List<String> = emptyList(),
    badges: List<String> = emptyList(),
) {
    val slotLabel = slotContext?.let { "${it.dayOfWeek} ${it.mealType}" }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("recipe-detail-screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(onClick = onBack, modifier = Modifier.testTag("recipe-detail-back").semantics { contentDescription = "Back" }) {
            Text("Back")
        }
        Text(
            text = "Recipe $recipeId",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        slotLabel?.let { Text("For meal slot: $it", style = MaterialTheme.typography.bodyMedium) }
        RecipeMetaBadges(badges)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Details", style = MaterialTheme.typography.titleMedium)
                Text("Source: ${sourceLabel ?: "Not specified"}")
                Text("Season fit: $seasonFitLabel")
                timeLabel?.let { Text("Total time: $it") }
                weekdayEligibilityLabel?.let { Text(it) }
                Text("Nutrition: $nutritionLabel")
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Ingredients", style = MaterialTheme.typography.titleMedium)
                if (ingredients.isEmpty()) {
                    Text("Ingredient details unavailable")
                } else {
                    ingredients.forEach { Text("• $it") }
                }
                prepNotes?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("Preparation", style = MaterialTheme.typography.titleSmall)
                    Text(it)
                }
            }
        }
        if (missingDataLabels.isNotEmpty()) {
            missingDataLabels.forEach { StatusBanner(it) }
        }
        if (slotContext != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onUseForSlot(slotContext, recipeId) },
                    modifier = Modifier.weight(1f).testTag("use-for-slot-action")
                        .semantics { contentDescription = "Use for this meal slot: $slotLabel" },
                ) { Text("Use for this meal slot") }
                OutlinedButton(
                    onClick = { onChooseDifferent(slotContext) },
                    modifier = Modifier.weight(1f).testTag("choose-different-action")
                        .semantics { contentDescription = "Choose different recipe for $slotLabel" },
                ) { Text("Choose different") }
            }
        }
    }
}
