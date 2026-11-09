package com.mealplanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mealplanner.ui.state.GroceryItemUiModel
import com.mealplanner.ui.state.MealSlotUiModel
import com.mealplanner.ui.state.PresenceRowUiModel
import com.mealplanner.ui.state.R05NextAction
import com.mealplanner.ui.state.R05State

/** Shared UI primitives (Step 6). Accessibility: non-color-only labels, content descriptions, testTag. */

@Composable
fun StatusBanner(text: String, modifier: Modifier = Modifier) {
    if (text.isBlank()) return
    Card(modifier = modifier.fillMaxWidth().semantics { contentDescription = "Status: $text" }) {
        Text(text = text, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ErrorRetryPanel(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().testTag("error-retry-panel")) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onRetry, modifier = Modifier.testTag("retry-action")) { Text("Retry") }
        }
    }
}

@Composable
fun R05FallbackPanel(
    state: R05State,
    onRetry: () -> Unit,
    onBundledFallback: () -> Unit,
    onManualPlanning: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.visible) return
    Card(modifier = modifier.fillMaxWidth().testTag("r05-fallback-panel")) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = state.message, modifier = Modifier.semantics { heading() })
            state.nextActions.forEach { action ->
                when (action) {
                    R05NextAction.RETRY -> OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth().testTag("r05-retry")) { Text("Retry") }
                    R05NextAction.USE_BUNDLED_FALLBACK_CATALOG -> OutlinedButton(onClick = onBundledFallback, modifier = Modifier.fillMaxWidth().testTag("r05-bundled-fallback")) { Text("Use bundled fallback catalog") }
                    R05NextAction.CONTINUE_WITH_MANUAL_PLANNING -> Button(onClick = onManualPlanning, modifier = Modifier.fillMaxWidth().testTag("r05-manual-planning")) { Text("Continue with manual planning") }
                }
            }
        }
    }
}

@Composable
fun MealSlotCard(
    model: MealSlotUiModel,
    onViewRecipe: () -> Unit,
    onReplace: () -> Unit,
    onChooseRecipe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val slotLabel = "${model.context.dayOfWeek} ${model.context.mealType}"
    val empty = model.isEmpty
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("meal-slot-${model.context.slotId}")
            .semantics { contentDescription = if (empty) "$slotLabel: No recipe selected" else "$slotLabel: ${model.recipeTitle}" },
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = slotLabel, style = MaterialTheme.typography.titleSmall, modifier = Modifier.semantics { heading() })
            if (empty) {
                Text("No recipe selected")
                OutlinedButton(
                    onClick = onChooseRecipe,
                    modifier = Modifier.testTag("choose-recipe-${model.context.slotId}").semantics { contentDescription = "Choose recipe for $slotLabel" },
                ) { Text("Choose recipe") }
            } else {
                Text(model.recipeTitle ?: "Recipe")
                model.timeLabel?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                model.nutritionLabel?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                RecipeMetaBadges(model.badges)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onViewRecipe,
                        modifier = Modifier.testTag("view-recipe-${model.context.slotId}").semantics { contentDescription = "View recipe for $slotLabel" },
                    ) { Text("View recipe") }
                    OutlinedButton(
                        onClick = onReplace,
                        modifier = Modifier.testTag("replace-recipe-${model.context.slotId}").semantics { contentDescription = "Replace $slotLabel" },
                    ) { Text("Replace") }
                }
            }
            model.statusLabels.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun NutritionBalanceCueCard(label: String, partial: Boolean = false, unavailable: Boolean = false, modifier: Modifier = Modifier) {
    val text = when {
        unavailable -> "Nutrition information unavailable"
        partial && label.isNotBlank() -> "$label. Some nutrition details unavailable"
        label.isNotBlank() -> label
        else -> "Nutrition information unavailable"
    }
    Card(modifier = modifier.fillMaxWidth().semantics { contentDescription = "Nutrition balance cue: $text" }) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Nutrition balance cue", style = MaterialTheme.typography.titleSmall)
            Text(text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun SeasonContextIndicator(label: String, modifier: Modifier = Modifier) {
    AssistChip(onClick = {}, label = { Text(label) }, modifier = modifier.testTag("season-context-indicator").semantics { contentDescription = "Season context: $label" })
}

@Composable
fun RecipeMetaBadges(badges: List<String>, modifier: Modifier = Modifier) {
    if (badges.isEmpty()) return
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        badges.forEach { badge -> AssistChip(onClick = {}, label = { Text(badge) }, modifier = Modifier.semantics { contentDescription = badge }) }
    }
}

@Composable
fun StorePresenceStatusRow(row: PresenceRowUiModel, modifier: Modifier = Modifier) {
    // FR8: name/presence only (PRESENT/NOT_FOUND/UNKNOWN + reason), never stock/guaranteed availability.
    val label = "${row.queryName}: ${row.statusLabel()} — ${row.reasonLabel}"
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("presence-row-${row.itemId}")
            .semantics { contentDescription = label },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(row.queryName, style = MaterialTheme.typography.bodyMedium)
            Text("${row.statusLabel()} (${row.reasonLabel})", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun RegenerateEditedListConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("regenerate-confirm-dialog"),
        title = { Text("Regenerate shopping list?") },
        text = { Text("Regenerate shopping list from locked recipes? Your current list edits/check-offs may be replaced or need review.") },
        confirmButton = {
            Button(onClick = onConfirm, modifier = Modifier.testTag("regenerate-confirm-action")) { Text("Regenerate and replace") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("regenerate-cancel-action")) { Text("Cancel") }
        },
    )
}

@Composable
fun GroceryChecklistRow(item: GroceryItemUiModel, onToggle: () -> Unit, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    val presence = when (item.availabilityStatus.name) {
        "PRESENT", "NOT_FOUND", "UNKNOWN" -> item.availabilityStatus.name
        else -> "UNKNOWN"
    }
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("grocery-item-${item.itemId}")
            .semantics { contentDescription = "${item.displayText}, ${if (item.checked) "checked" else "unchecked"}, presence $presence" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = item.checked, onCheckedChange = { onToggle() }, modifier = Modifier.testTag("grocery-check-${item.itemId}"))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.displayText)
            item.quantityText?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            Text("Presence: $presence${item.availabilityReason?.let { " ($it)" } ?: ""}", style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = onRemove, modifier = Modifier.testTag("grocery-remove-${item.itemId}").semantics { contentDescription = "Remove ${item.displayText}" }) { Text("Remove") }
    }
}
