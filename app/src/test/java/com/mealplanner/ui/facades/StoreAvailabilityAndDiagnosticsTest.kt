package com.mealplanner.ui.facades

import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.PresenceBatchStatus
import com.mealplanner.domain.model.PresenceCheckBatchResult
import com.mealplanner.domain.model.PresenceCheckResult
import com.mealplanner.domain.model.PrismaStoreRef
import com.mealplanner.domain.model.StorePresenceStatus
import com.mealplanner.domain.model.StoreSuggestionResult
import com.mealplanner.domain.model.StoreSuggestionStatus
import com.mealplanner.ui.mappers.DiagnosticsUiMapper
import com.mealplanner.ui.state.LocationPermissionUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Step 5 (test-first, key behavior): FR8 presence labels PRESENT/NOT_FOUND/UNKNOWN + reason only,
 * never stock/guaranteed availability; manual store selection always enabled; permission denied/not-asked
 * maps to manual flow; diagnostics contain no raw GPS/secrets/PII.
 */
class StoreAvailabilityAndDiagnosticsTest {
    private val facade = DefaultStoreAvailabilityFacade()
    private val store = PrismaStoreRef("prisma-1", "Prisma Itäkeskus")

    @Test
    fun presenceRowsUseOnlyPresentNotFoundUnknownWithReasonNeverStock() {
        val batch = PresenceCheckBatchResult(
            storeId = store.storeId,
            results = listOf(
                PresenceCheckResult("i1", "Milk", StorePresenceStatus.PRESENT, "exact match"),
                PresenceCheckResult("i2", "Eggs", StorePresenceStatus.NOT_FOUND, "no match"),
                PresenceCheckResult("i3", "Tofu", StorePresenceStatus.UNKNOWN, "ambiguous"),
                PresenceCheckResult("i4", "Bread", StorePresenceStatus.NOT_CHECKED, null),
            ),
            batchStatus = PresenceBatchStatus.PARTIAL,
        )
        val ui = facade.mapPresenceBatch(batch)
        assertEquals(listOf("PRESENT", "NOT_FOUND", "UNKNOWN", "UNKNOWN"), ui.rows.map { it.statusLabel() })
        assertTrue(ui.rows.all { it.reasonLabel.startsWith("Reason:") })
        val joined = ui.rows.joinToString(" ") { "${it.statusLabel()} ${it.reasonLabel}" }.lowercase()
        assertFalse(joined.contains("stock"))
        assertFalse(joined.contains("guaranteed"))
        assertFalse(joined.contains("quantity available"))
        assertTrue(ui.manualChecklistAvailable)
    }

    @Test
    fun manualStoreSelectionAlwaysEnabled() {
        val state = facade.selectStoreManually(store)
        assertTrue(state.manualSelectionEnabled)
        assertEquals(store.storeId, state.currentStore?.storeId)
        val denied = facade.onSuggestionResult(StoreSuggestionResult(StoreSuggestionStatus.DENIED), LocationPermissionUiState.DENIED)
        assertTrue(denied.manualSelectionEnabled)
        val notAsked = facade.onSuggestionResult(StoreSuggestionResult(StoreSuggestionStatus.UNAVAILABLE), LocationPermissionUiState.NOT_ASKED)
        assertTrue(notAsked.manualSelectionEnabled)
    }

    @Test
    fun permissionDeniedOrNotAskedMapsToManualFlow() {
        val denied = facade.onSuggestionResult(StoreSuggestionResult(StoreSuggestionStatus.DENIED), LocationPermissionUiState.DENIED)
        assertTrue(DiagnosticsUiMapper.storeSuggestionLabel(denied.suggestionStatus, denied.locationPermission).contains("Manual", ignoreCase = true))
        val notAsked = facade.onSuggestionResult(StoreSuggestionResult(StoreSuggestionStatus.UNAVAILABLE), LocationPermissionUiState.NOT_ASKED)
        assertTrue(DiagnosticsUiMapper.storeSuggestionLabel(notAsked.suggestionStatus, notAsked.locationPermission).contains("manual", ignoreCase = true))
    }

    @Test
    fun diagnosticsMessagesContainNoRawGpsSecretsOrPii() {
        val sensitive = listOf(
            "lat=60.1699 lon=24.9384",
            "token=abc123secret",
            "api_key: xyz",
            "https://internal.example/api?password=hunter2",
            "endpoint /private/store",
        )
        sensitive.forEach { raw ->
            val sanitized = DiagnosticsUiMapper.sanitizeUserText(raw)
            assertEquals("Details unavailable for privacy.", sanitized, "Must not expose: $raw")
            val fallback = DiagnosticsUiMapper.fallbackMessage(FallbackReason("X", raw, raw))
            assertFalse(fallback!!.userSafeText.contains("60.1699"))
            assertFalse(fallback.userSafeText.contains("abc123"))
            assertFalse(fallback.userSafeText.contains("hunter2"))
        }
    }

    @Test
    fun presenceReasonMappingStaysUserSafe() {
        val row = DiagnosticsUiMapper.presenceRow("i1", "Milk", StorePresenceStatus.UNKNOWN, "gps 60.1,24.9 timeout")
        assertEquals("UNKNOWN", row.statusLabel())
        assertEquals("Reason: Details unavailable for privacy.", row.reasonLabel)
    }
}
