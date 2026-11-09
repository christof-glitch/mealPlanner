package com.mealplanner.ui.facades

import com.mealplanner.domain.model.PresenceCheckBatchResult
import com.mealplanner.domain.model.PrismaStoreRef
import com.mealplanner.domain.model.StoreSelectionMode
import com.mealplanner.domain.model.StoreSuggestionResult
import com.mealplanner.domain.model.StoreSuggestionStatus
import com.mealplanner.ui.mappers.DiagnosticsUiMapper
import com.mealplanner.ui.state.LocationPermissionUiState
import com.mealplanner.ui.state.PresenceBatchUiState
import com.mealplanner.ui.state.StoreSelectionUiState

/** Thin store/presence orchestration (FR8 name/presence only, BR3.6, NFR3). */
class DefaultStoreAvailabilityFacade : StoreAvailabilityFacade {
    private var current: StoreSelectionUiState = StoreSelectionUiState()

    override fun currentStoreSelectionState(): StoreSelectionUiState = current

    override fun selectStoreManually(store: PrismaStoreRef): StoreSelectionUiState {
        current = current.copy(
            currentStore = store,
            selectionMode = StoreSelectionMode.MANUAL,
            manualSelectionEnabled = true,
            lookupStatusLabel = "Store selected manually.",
            errorMessage = null,
        )
        return current
    }

    override fun onSuggestionResult(result: StoreSuggestionResult, permission: LocationPermissionUiState): StoreSelectionUiState {
        val manualAlways = true
        val selected = result.suggestedStore?.takeIf { result.status == StoreSuggestionStatus.OK }
        current = current.copy(
            currentStore = selected ?: current.currentStore,
            selectionMode = if (selected != null) StoreSelectionMode.NEAREST_SUGGESTION else StoreSelectionMode.MANUAL,
            locationPermission = permission,
            suggestionStatus = if (permission == LocationPermissionUiState.DENIED) StoreSuggestionStatus.DENIED else result.status,
            suggestedStore = result.suggestedStore,
            lookupStatusLabel = DiagnosticsUiMapper.storeSuggestionLabel(
                if (permission == LocationPermissionUiState.DENIED) StoreSuggestionStatus.DENIED else result.status,
                permission,
            ),
            manualSelectionEnabled = manualAlways,
            errorMessage = if (result.status == StoreSuggestionStatus.OK) null else DiagnosticsUiMapper.sanitizeUserText(result.message ?: result.fallbackReason?.message),
        )
        // Permission denied/not-asked must still map to manual flow.
        if (permission == LocationPermissionUiState.DENIED || permission == LocationPermissionUiState.NOT_ASKED) {
            current = current.copy(selectionMode = StoreSelectionMode.MANUAL, manualSelectionEnabled = true)
        }
        return current
    }

    override fun mapPresenceBatch(result: PresenceCheckBatchResult): PresenceBatchUiState {
        val rows = result.results.map { r ->
            DiagnosticsUiMapper.presenceRow(
                itemId = r.itemRef,
                queryName = r.queryName,
                status = r.resultStatus,
                reason = r.reason,
            )
        }
        return PresenceBatchUiState(
            rows = rows,
            batchStatus = result.batchStatus,
            manualChecklistAvailable = true, // FR8 fallback
            message = DiagnosticsUiMapper.presenceBatchLabel(result.batchStatus),
        )
    }
}
