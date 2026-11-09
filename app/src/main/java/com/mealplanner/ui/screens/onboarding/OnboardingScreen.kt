package com.mealplanner.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mealplanner.ui.components.R05FallbackPanel
import com.mealplanner.ui.state.LocationPermissionUiState
import com.mealplanner.ui.state.R05State

/** RM-01 Onboarding / Permissions (Step 7). FR7/FR12/NFR3: location purpose + manual alternative always available. */
@Composable
fun OnboardingScreen(
    permission: LocationPermissionUiState,
    r05State: R05State?,
    onAllowLocation: () -> Unit,
    onChooseManuallyLater: () -> Unit,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
    onUseBundledFallback: () -> Unit,
    onContinueManualPlanning: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("onboarding-screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Weekly Meal Planner",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Plan meals and shopping", style = MaterialTheme.typography.titleMedium)
                Text("Build a weekly plan, generate a shopping list from locked recipes, and use the current edited list for export/share.")
            }
        }
        Card(modifier = Modifier.fillMaxWidth().testTag("location-purpose-card")) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Location (optional)", style = MaterialTheme.typography.titleMedium)
                Text("Location is used only to suggest a nearby PRISMA store when you choose to check store presence. It is not required.")
                Text("Permission status: ${permission.name}")
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Privacy", style = MaterialTheme.typography.titleMedium)
                Text("On-device MVP. Minimal data. No MealPlanner account. Location is optional and purpose-limited.")
            }
        }
        if (r05State != null) {
            R05FallbackPanel(
                state = r05State,
                onRetry = onRetry,
                onBundledFallback = onUseBundledFallback,
                onManualPlanning = onContinueManualPlanning,
            )
        }
        Button(
            onClick = onAllowLocation,
            modifier = Modifier.fillMaxWidth().testTag("allow-location-action"),
        ) { Text("Allow location") }
        OutlinedButton(
            onClick = onChooseManuallyLater,
            modifier = Modifier.fillMaxWidth().testTag("choose-manually-later-action"),
        ) { Text("Choose store manually later") }
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().testTag("onboarding-continue-action"),
        ) { Text("Continue") }
    }
}
