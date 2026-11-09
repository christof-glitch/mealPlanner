package com.mealplanner.ui.platform

import com.mealplanner.ui.state.LocationPermissionUiState

/** Minimal platform boundary implementation (Step 10). Runtime Android wiring can replace this. */
class DefaultPermissionStatusProvider(
    private val current: () -> LocationPermissionUiState = { LocationPermissionUiState.NOT_ASKED },
) : PermissionStatusProvider {
    override fun locationPermission(): LocationPermissionUiState = current()
}
