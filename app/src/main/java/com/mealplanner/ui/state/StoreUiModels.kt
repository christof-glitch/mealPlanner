package com.mealplanner.ui.state

import com.mealplanner.domain.model.PresenceBatchStatus
import com.mealplanner.domain.model.PrismaStoreRef
import com.mealplanner.domain.model.StorePresenceStatus
import com.mealplanner.domain.model.StoreSelectionMode
import com.mealplanner.domain.model.StoreSuggestionStatus

enum class LocationPermissionUiState { GRANTED, DENIED, NOT_ASKED }

data class StoreSelectionUiState(
    val currentStore: PrismaStoreRef? = null,
    val selectionMode: StoreSelectionMode = StoreSelectionMode.NONE,
    val locationPermission: LocationPermissionUiState = LocationPermissionUiState.NOT_ASKED,
    val suggestionStatus: StoreSuggestionStatus? = null,
    val suggestedStore: PrismaStoreRef? = null,
    val lookupStatusLabel: String? = null,
    val manualSelectionEnabled: Boolean = true, // FR8/NFR3: always available
    val loading: Boolean = false,
    val errorMessage: String? = null,
)

data class PresenceRowUiModel(
    val itemId: String,
    val queryName: String,
    val status: StorePresenceStatus,
    val reasonLabel: String,
) {
    fun statusLabel(): String = when (status) {
        StorePresenceStatus.PRESENT -> "PRESENT"
        StorePresenceStatus.NOT_FOUND -> "NOT_FOUND"
        StorePresenceStatus.UNKNOWN -> "UNKNOWN"
        StorePresenceStatus.NOT_CHECKED -> "UNKNOWN"
    }
}

data class PresenceBatchUiState(
    val rows: List<PresenceRowUiModel> = emptyList(),
    val batchStatus: PresenceBatchStatus? = null,
    val manualChecklistAvailable: Boolean = true,
    val message: String? = null,
)
