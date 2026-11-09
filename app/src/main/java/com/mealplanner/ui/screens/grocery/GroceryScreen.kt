package com.mealplanner.ui.screens.grocery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mealplanner.ui.components.GroceryChecklistRow
import com.mealplanner.ui.components.RegenerateEditedListConfirmationDialog
import com.mealplanner.ui.components.StatusBanner
import com.mealplanner.ui.state.GroceryIntent
import com.mealplanner.ui.state.GroceryScreenState

/** RM-06 Grocery (Step 9). ADR-006/R-01-R-03, FR8, FR10/OQ4, BR3.5/BR3.8/BR3.9. */
@Composable
fun GroceryScreen(
    state: GroceryScreenState,
    onIntent: (GroceryIntent) -> Unit,
    onNavigatePlan: () -> Unit,
    onSelectStore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var newItemText by rememberSaveable { mutableStateOf("") }
    var newItemQty by rememberSaveable { mutableStateOf("") }
    var editingItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingText by rememberSaveable { mutableStateOf("") }
    var editingQty by rememberSaveable { mutableStateOf("") }

    if (state.showRegenerateConfirmation) {
        RegenerateEditedListConfirmationDialog(
            onConfirm = { onIntent(GroceryIntent.ConfirmRegenerateAndReplace) },
            onDismiss = { onIntent(GroceryIntent.CancelRegenerate) },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("grocery-screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Grocery", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })

        Card(modifier = Modifier.fillMaxWidth().testTag("grocery-readiness-card")) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Plan readiness", style = MaterialTheme.typography.titleMedium)
                Text(state.lockSummary)
                if (!state.recipesLocked) {
                    StatusBanner(GroceryScreenState.LOCK_REQUIRED_MESSAGE)
                    OutlinedButton(onClick = onNavigatePlan, modifier = Modifier.testTag("grocery-go-to-plan")) {
                        Text("Go to Plan to lock recipes")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onIntent(GroceryIntent.Generate) },
                        enabled = state.recipesLocked && !state.hasList,
                        modifier = Modifier.testTag("generate-shopping-list-action").semantics { contentDescription = "Generate shopping list" },
                    ) { Text("Generate shopping list") }
                    if (state.hasList) {
                        OutlinedButton(
                            onClick = { onIntent(GroceryIntent.RequestRegenerate) },
                            modifier = Modifier.testTag("regenerate-shopping-list-action").semantics { contentDescription = "Regenerate shopping list" },
                        ) { Text("Regenerate") }
                    }
                }
            }
        }

        if (state.isStale) {
            StatusBanner(GroceryScreenState.LOCKED_RECIPES_CHANGED_STATUS)
            state.staleWarningText?.let {
                StatusBanner(it) // exact ADR-006 warning when applicable; no auto-regeneration
            }
        }
        state.errorMessage?.let { StatusBanner(it) }
        state.exportMessage?.let { StatusBanner(it) }
        state.exportInstructions?.let { StatusBanner(it) }

        Card(modifier = Modifier.fillMaxWidth().testTag("store-presence-card")) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PRISMA store and presence", style = MaterialTheme.typography.titleMedium)
                Text("Selected store: ${state.selectedStore?.displayName ?: state.selectedStore?.storeId ?: "None selected"}")
                Text(state.presenceStatusLabel ?: "Name/presence only: PRESENT, NOT_FOUND, or UNKNOWN. Never stock or guaranteed availability.")
                OutlinedButton(onClick = onSelectStore, modifier = Modifier.testTag("select-prisma-store-action")) {
                    Text("Choose or change PRISMA store")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().testTag("shopping-list-card")) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Shopping list", style = MaterialTheme.typography.titleMedium)
                if (!state.hasList) {
                    Text("No shopping list yet. Lock recipes in Plan and generate explicitly.")
                } else {
                    Text("Current edited list is used for export. Edits/check-offs are preserved until you explicitly regenerate.")
                    if (state.items.isEmpty()) {
                        Text("The list is empty. Add items manually if needed.")
                    }
                    state.items.forEach { item ->
                        if (editingItemId == item.itemId) {
                            OutlinedTextField(
                                value = editingText,
                                onValueChange = { editingText = it },
                                label = { Text("Item") },
                                modifier = Modifier.fillMaxWidth().testTag("edit-item-text-${item.itemId}"),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = editingQty,
                                onValueChange = { editingQty = it },
                                label = { Text("Quantity (optional)") },
                                modifier = Modifier.fillMaxWidth().testTag("edit-item-qty-${item.itemId}"),
                                singleLine = true,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    onIntent(GroceryIntent.EditItem(item.itemId, editingText, editingQty.ifBlank { null }))
                                    editingItemId = null
                                }, modifier = Modifier.testTag("save-edit-${item.itemId}")) { Text("Save") }
                                OutlinedButton(onClick = { editingItemId = null }) { Text("Cancel") }
                            }
                        } else {
                            GroceryChecklistRow(
                                item = item,
                                onToggle = { onIntent(GroceryIntent.ToggleChecked(item.itemId)) },
                                onRemove = { onIntent(GroceryIntent.RemoveItem(item.itemId)) },
                            )
                            OutlinedButton(
                                onClick = {
                                    editingItemId = item.itemId
                                    editingText = item.displayText
                                    editingQty = item.quantityText.orEmpty()
                                },
                                modifier = Modifier.testTag("edit-item-${item.itemId}").semantics { contentDescription = "Edit ${item.displayText}" },
                            ) { Text("Edit") }
                        }
                    }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Add item", style = MaterialTheme.typography.titleSmall)
                            OutlinedTextField(
                                value = newItemText,
                                onValueChange = { newItemText = it },
                                label = { Text("Item name") },
                                modifier = Modifier.fillMaxWidth().testTag("add-item-text"),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = newItemQty,
                                onValueChange = { newItemQty = it },
                                label = { Text("Quantity (optional)") },
                                modifier = Modifier.fillMaxWidth().testTag("add-item-qty"),
                                singleLine = true,
                            )
                            Button(
                                onClick = {
                                    if (newItemText.isNotBlank()) {
                                        onIntent(GroceryIntent.AddItem(newItemText, newItemQty.ifBlank { null }))
                                        newItemText = ""
                                        newItemQty = ""
                                    }
                                },
                                modifier = Modifier.testTag("add-item-action").semantics { contentDescription = "Add item to shopping list" },
                            ) { Text("Add item") }
                        }
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().testTag("export-card")) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Export / share (RM-08)", style = MaterialTheme.typography.titleMedium)
                Text("Exports use the current edited shopping list. Official S-ostoslista integration is investigational; share/copy fallback is always available.")
                OutlinedButton(
                    onClick = { onIntent(GroceryIntent.ExportOfficialSostoslista) },
                    modifier = Modifier.fillMaxWidth().testTag("export-official-sostoslista-action")
                        .semantics { contentDescription = "Try official S-ostoslista export" },
                ) { Text("Try official S-ostoslista export (if available)") }
                Button(
                    onClick = { onIntent(GroceryIntent.ShareFallback) },
                    modifier = Modifier.fillMaxWidth().testTag("share-fallback-action")
                        .semantics { contentDescription = "Share shopping list" },
                ) { Text("Share shopping list") }
                OutlinedButton(
                    onClick = { onIntent(GroceryIntent.CopyTextFallback) },
                    modifier = Modifier.fillMaxWidth().testTag("copy-fallback-action")
                        .semantics { contentDescription = "Copy shopping list text" },
                ) { Text("Copy text") }
                Text("If official export is unavailable, use Share or Copy and follow the app's S-ostoslista instructions.")
            }
        }
    }
}
