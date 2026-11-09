package com.mealplanner.ui.facades

import com.mealplanner.domain.model.PresenceCheckBatchResult
import com.mealplanner.domain.model.PrismaStoreRef
import com.mealplanner.domain.model.StoreSuggestionResult
import com.mealplanner.ui.state.LocationPermissionUiState
import com.mealplanner.ui.state.PresenceBatchUiState
import com.mealplanner.ui.state.StoreSelectionUiState

/** Thin UI facade for store selection/presence display (FR8, BR3.6, NFR3). Manual selection always available. */
interface StoreAvailabilityFacade {
    fun currentStoreSelectionState(): StoreSelectionUiState
    fun selectStoreManually(store: PrismaStoreRef): StoreSelectionUiState
    fun onSuggestionResult(result: StoreSuggestionResult, permission: LocationPermissionUiState): StoreSelectionUiState
    fun mapPresenceBatch(result: PresenceCheckBatchResult): PresenceBatchUiState
}
