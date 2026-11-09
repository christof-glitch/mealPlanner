package com.mealplanner.ui.screens.user

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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mealplanner.ui.components.SeasonContextIndicator
import com.mealplanner.ui.components.StatusBanner
import com.mealplanner.ui.state.UserSettingsState

/** RM-09 User (Step 7). Season override/default/reset, permission/privacy status. */
@Composable
fun UserScreen(
    state: UserSettingsState,
    onToggleSeasonOverride: (Boolean) -> Unit,
    onSeasonSelected: (String) -> Unit,
    onAreaLabelChanged: (String) -> Unit,
    onResetSeason: () -> Unit,
    onManagePermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("user-screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("User", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
        SeasonContextIndicator(state.seasonContextLabel)
        Card(modifier = Modifier.fillMaxWidth().testTag("season-override-card")) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Season context", style = MaterialTheme.typography.titleMedium)
                Text("Default uses device date and location. Override is optional and affects season fit display.")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Switch(
                        checked = state.seasonOverrideEnabled,
                        onCheckedChange = onToggleSeasonOverride,
                        modifier = Modifier.testTag("season-override-switch"),
                    )
                    Text("Override season context")
                }
                if (state.seasonOverrideEnabled) {
                    OutlinedTextField(
                        value = state.seasonSelection.orEmpty(),
                        onValueChange = onSeasonSelected,
                        label = { Text("Season") },
                        modifier = Modifier.fillMaxWidth().testTag("season-selection-field"),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.areaLabel.orEmpty(),
                        onValueChange = onAreaLabelChanged,
                        label = { Text("Area/region label (optional)") },
                        modifier = Modifier.fillMaxWidth().testTag("area-label-field"),
                        singleLine = true,
                    )
                }
                state.validationMessage?.let { StatusBanner(it) }
                OutlinedButton(onClick = onResetSeason, modifier = Modifier.testTag("reset-season-action")) {
                    Text("Reset to device default")
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth().testTag("permissions-privacy-card")) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Permissions and privacy", style = MaterialTheme.typography.titleMedium)
                Text("Location permission: ${state.locationPermission.name}")
                Text(state.privacySummary)
                Button(onClick = onManagePermission, modifier = Modifier.testTag("manage-permission-action")) {
                    Text("Manage location permission")
                }
            }
        }
    }
}
