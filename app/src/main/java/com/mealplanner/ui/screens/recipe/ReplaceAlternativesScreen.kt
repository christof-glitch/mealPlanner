package com.mealplanner.ui.screens.recipe

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
import com.mealplanner.ui.components.RecipeMetaBadges
import com.mealplanner.ui.components.StatusBanner
import com.mealplanner.ui.state.SlotActionContext

data class AlternativeRecipeUiModel(
    val recipeId: String,
    val title: String,
    val badges: List<String> = emptyList(),
    val explanation: String? = null,
)

/** RM-05 Replace/alternatives full screen (Step 8). Domain-provided eligibility/explanations only (ADR-005). */
@Composable
fun ReplaceAlternativesScreen(
    slotContext: SlotActionContext,
    onView: (SlotActionContext, String) -> Unit,
    onSelect: (SlotActionContext, String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    rulesSummary: List<String> = listOf("Showing alternatives for the selected slot. Eligibility is provided by domain rules."),
    alternatives: List<AlternativeRecipeUiModel> = emptyList(),
    loading: Boolean = false,
    errorMessage: String? = null,
    emptyReason: String? = null,
) {
    val slotLabel = "${slotContext.dayOfWeek} ${slotContext.mealType}"
    val title = if (slotContext.currentRecipeId == null) "Choose recipe for $slotLabel" else "Replace $slotLabel"
    Column(modifier = modifier.fillMaxSize().padding(16.dp).testTag("replace-alternatives-screen")) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.testTag("alternatives-cancel").semantics { contentDescription = "Cancel and keep current selection" },
            ) { Text("Cancel") }
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        }
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Rules summary", style = MaterialTheme.typography.titleSmall)
                rulesSummary.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
            }
        }
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).testTag("alternatives-loading"))
            return@Column
        }
        errorMessage?.let {
            StatusBanner(it)
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Back") }
            return@Column
        }
        if (alternatives.isEmpty()) {
            StatusBanner(emptyReason ?: "No eligible alternatives found. You can keep the current selection or leave the slot empty.")
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().testTag("alternatives-keep-current")) {
                Text(if (slotContext.currentRecipeId == null) "Leave empty" else "Keep current")
            }
            return@Column
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            items(alternatives, key = { it.recipeId }) { alt ->
                Card(modifier = Modifier.fillMaxWidth().testTag("alternative-${alt.recipeId}")) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(alt.title, style = MaterialTheme.typography.titleMedium)
                        RecipeMetaBadges(alt.badges)
                        alt.explanation?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onView(slotContext, alt.recipeId) },
                                modifier = Modifier.testTag("alternative-view-${alt.recipeId}")
                                    .semantics { contentDescription = "View ${alt.title}" },
                            ) { Text("View") }
                            Button(
                                onClick = { onSelect(slotContext, alt.recipeId) },
                                modifier = Modifier.testTag("alternative-select-${alt.recipeId}")
                                    .semantics { contentDescription = "Select ${alt.title} for $slotLabel" },
                            ) { Text("Select") }
                        }
                    }
                }
            }
        }
    }
}
