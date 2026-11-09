package com.mealplanner.ui.mappers

import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.PresenceBatchStatus
import com.mealplanner.domain.model.StorePresenceStatus
import com.mealplanner.domain.model.StoreSuggestionStatus
import com.mealplanner.ui.state.DiagnosticsUiMessage
import com.mealplanner.ui.state.LocationPermissionUiState
import com.mealplanner.ui.state.PresenceRowUiModel
import com.mealplanner.ui.state.R05NextAction
import com.mealplanner.ui.state.R05State

/**
 * Maps typed domain outcomes to user-safe UI labels/actions (NFR3, NFR2).
 * Never exposes raw secrets, GPS coordinates, PII, or internal endpoints.
 */
object DiagnosticsUiMapper {
    private val SENSITIVE = listOf("lat", "lon", "latitude", "longitude", "gps", "token", "secret", "api_key", "apikey", "password", "endpoint", "http://", "https://")

    fun sanitizeUserText(text: String?): String? {
        val raw = text?.trim().orEmpty()
        if (raw.isEmpty()) return null
        val lowered = raw.lowercase()
        return if (SENSITIVE.any { lowered.contains(it) }) "Details unavailable for privacy." else raw
    }

    fun r05NoLocalDataRemoteUnavailable(bundledFallbackAvailable: Boolean = true): R05State = R05State(
        visible = true,
        message = "No local data available and remote data is unavailable.",
        nextActions = buildList {
            add(R05NextAction.RETRY)
            if (bundledFallbackAvailable) add(R05NextAction.USE_BUNDLED_FALLBACK_CATALOG)
            add(R05NextAction.CONTINUE_WITH_MANUAL_PLANNING)
        },
    )

    fun fallbackMessage(reason: FallbackReason?): DiagnosticsUiMessage? = reason?.let {
        val safe = sanitizeUserText(it.userActionRequired ?: it.message ?: it.code) ?: "Fallback is active."
        DiagnosticsUiMessage(userSafeText = safe, hasNextAction = !it.userActionRequired.isNullOrBlank())
    }

    fun presenceRow(itemId: String, queryName: String, status: StorePresenceStatus, reason: String?): PresenceRowUiModel {
        val safeStatus = when (status) {
            StorePresenceStatus.PRESENT -> StorePresenceStatus.PRESENT
            StorePresenceStatus.NOT_FOUND -> StorePresenceStatus.NOT_FOUND
            StorePresenceStatus.UNKNOWN, StorePresenceStatus.NOT_CHECKED -> StorePresenceStatus.UNKNOWN
        }
        val reasonLabel = sanitizeUserText(reason)?.let { "Reason: $it" } ?: "Reason: Unknown"
        return PresenceRowUiModel(itemId = itemId, queryName = queryName, status = safeStatus, reasonLabel = reasonLabel)
    }

    fun storeSuggestionLabel(status: StoreSuggestionStatus?, permission: LocationPermissionUiState): String = when {
        permission == LocationPermissionUiState.DENIED -> "Location permission denied. Manual store selection is available."
        status == StoreSuggestionStatus.DENIED -> "Suggestion denied. Manual store selection is available."
        status == StoreSuggestionStatus.UNAVAILABLE -> "Nearest store suggestion unavailable. Manual selection is always available."
        status == StoreSuggestionStatus.NOT_FOUND -> "No nearby store found. Manual selection is available."
        status == StoreSuggestionStatus.UNKNOWN -> "Store suggestion status unknown. Manual selection is available."
        status == StoreSuggestionStatus.OK -> "Nearest store suggestion available."
        permission == LocationPermissionUiState.NOT_ASKED -> "Location not requested. Manual store selection is available."
        else -> "Manual store selection is always available."
    }

    fun presenceBatchLabel(status: PresenceBatchStatus?): String = when (status) {
        PresenceBatchStatus.OK -> "Presence check complete (name/presence only)."
        PresenceBatchStatus.PARTIAL -> "Presence check partially complete. Unknown items remain."
        PresenceBatchStatus.UNAVAILABLE -> "Presence check unavailable. Manual checklist available."
        PresenceBatchStatus.DENIED -> "Presence check denied. Manual checklist available."
        PresenceBatchStatus.FAILED_MAPPED -> "Presence check failed safely. Manual checklist available."
        null -> "Presence not checked."
    }
}
