package com.mealplanner.ui.screens.store

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.mealplanner.domain.model.PrismaStoreRef
import com.mealplanner.ui.components.StatusBanner
import com.mealplanner.ui.state.LocationPermissionUiState
import com.mealplanner.ui.state.StoreSelectionUiState

/** RM-07 Select PRISMA store (full screen, Step 9). Manual always available (FR8/NFR3). */
@Composable
fun SelectPrismaStoreScreen(
    state: StoreSelectionUiState,
    onUseSuggestion: () -> Unit,
    onSelectManual: (PrismaStoreRef) -> Unit,
    onConfirm: () -> Unit,
    onManagePermission: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var manualStoreId by rememberSaveable { mutableStateOf(state.currentStore?.storeId ?: "") }
    var manualStoreName by rememberSaveable { mutableStateOf(state.currentStore?.displayName ?: "") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("select-store-screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.testTag("store-back-action")) { Text("Back") }
            Text("Select PRISMA store", style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        }
        Text("Manual store selection is always available.")
        Text("Location permission: ${state.locationPermission.name}")
        state.lookupStatusLabel?.let { StatusBanner(it) }
        state.errorMessage?.let { StatusBanner(it) }
        state.suggestedStore?.let { suggested ->
            val suggestedName = suggested.displayName ?: suggested.storeId
            Card(modifier = Modifier.fillMaxWidth().testTag("suggested-store-card")) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Suggested store (best effort)", style = MaterialTheme.typography.titleMedium)
                    Text("$suggestedName (${suggested.storeId})")
                    Button(
                        onClick = onUseSuggestion,
                        modifier = Modifier.testTag("use-suggestion-action").semantics { contentDescription = "Use suggested store $suggestedName" },
                    ) { Text("Use suggested store") }
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth().testTag("manual-store-card")) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Manual selection", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = manualStoreName,
                    onValueChange = { manualStoreName = it },
                    label = { Text("Store name") },
                    modifier = Modifier.fillMaxWidth().testTag("manual-store-name"),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = manualStoreId,
                    onValueChange = { manualStoreId = it },
                    label = { Text("Store ID") },
                    modifier = Modifier.fillMaxWidth().testTag("manual-store-id"),
                    singleLine = true,
                )
                Button(
                    onClick = {
                        val id = manualStoreId.ifBlank { "manual-${manualStoreName.ifBlank { "store" }.hashCode()}" }
                        val name = manualStoreName.ifBlank { "PRISMA store" }
                        onSelectManual(PrismaStoreRef(storeId = id, displayName = name))
                    },
                    modifier = Modifier.testTag("select-manual-store-action").semantics { contentDescription = "Select store manually" },
                ) { Text("Select manually") }
            }
        }
        state.currentStore?.let { current ->
            StatusBanner("Current store: ${current.displayName ?: current.storeId} (${current.storeId})")
        }
        if (state.locationPermission != LocationPermissionUiState.GRANTED) {
            OutlinedButton(onClick = onManagePermission, modifier = Modifier.fillMaxWidth().testTag("manage-store-permission-action")) {
                Text("Manage location permission")
            }
        }
        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth().testTag("confirm-store-action")) {
            Text("Confirm")
        }
    }
}
