package com.mealplanner.domain.availability

import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.PrismaStoreRef
import com.mealplanner.domain.model.StoreSelectionMode
import com.mealplanner.domain.model.StoreSelectionState
import com.mealplanner.domain.model.StoreSuggestionResult
import com.mealplanner.domain.model.StoreSuggestionStatus

/** A2 nearest-suggestion port (OQ2 investigational). */
interface NearestStoreSuggestionPort {
    suspend fun suggestNearest(): StoreSuggestionResult
}

class ManualOnlyNearestStoreSuggestionPort : NearestStoreSuggestionPort {
    override suspend fun suggestNearest(): StoreSuggestionResult = StoreSuggestionResult(
        status = StoreSuggestionStatus.UNAVAILABLE,
        message = "Nearest store suggestion unavailable; manual selection is always available.",
        fallbackReason = FallbackReason("NEAREST_STORE_NOT_CONFIGURED", "Nearest PRISMA store lookup not configured.", "Select a store manually."),
    )
}

/**
 * C5/A2 store selection: manual always available; nearest suggestion best-effort (BR3.6).
 */
class StoreSelectionUseCase(
    private val nearestPort: NearestStoreSuggestionPort = ManualOnlyNearestStoreSuggestionPort(),
) {
    fun selectStoreManually(store: PrismaStoreRef): StoreSelectionState = StoreSelectionState(
        selectedStore = store,
        selectionMode = StoreSelectionMode.MANUAL,
        message = "Store selected manually.",
    )

    suspend fun suggestNearestStore(): StoreSuggestionResult = try {
        nearestPort.suggestNearest()
    } catch (e: Exception) {
        StoreSuggestionResult(
            status = StoreSuggestionStatus.UNKNOWN,
            message = "Store suggestion failed safely.",
            fallbackReason = FallbackReason("SUGGESTION_FAILED_MAPPED", "Nearest store suggestion failed.", "Select a store manually."),
        )
    }
}
