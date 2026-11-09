package com.mealplanner.ui.platform

import com.mealplanner.ui.state.LocationPermissionUiState

/** Platform boundary (BR3.9/NFR3): Android permission state mapped to typed UI/domain-safe outcomes. No raw GPS. */
interface PermissionStatusProvider {
    fun locationPermission(): LocationPermissionUiState
}
