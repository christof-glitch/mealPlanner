package com.mealplanner.ui.state

data class UserSettingsState(
    val seasonContextLabel: String = "Using device date and location",
    val seasonOverrideEnabled: Boolean = false,
    val seasonSelection: String? = null,
    val areaLabel: String? = null,
    val validationMessage: String? = null,
    val locationPermission: LocationPermissionUiState = LocationPermissionUiState.NOT_ASKED,
    val privacySummary: String = "On-device MVP. Minimal data. No MealPlanner account. Location is optional and purpose-limited.",
)

data class DiagnosticsUiMessage(
    val userSafeText: String,
    val hasNextAction: Boolean = false,
)
