package com.mealplanner.buildandtest

import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.PresenceBatchStatus
import com.mealplanner.domain.model.StorePresenceStatus
import com.mealplanner.domain.model.StoreSuggestionStatus
import com.mealplanner.ui.mappers.DiagnosticsUiMapper
import com.mealplanner.ui.state.LocationPermissionUiState
import com.mealplanner.ui.state.R05NextAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CoverageRepairUiMapperTest {
    @Test
    fun `sanitizeUserText redacts sensitive fragments`() {
        assertNull(DiagnosticsUiMapper.sanitizeUserText(null))
        assertNull(DiagnosticsUiMapper.sanitizeUserText("  "))
        assertEquals("hello", DiagnosticsUiMapper.sanitizeUserText("hello"))
        assertEquals("Details unavailable for privacy.", DiagnosticsUiMapper.sanitizeUserText("lat=1"))
        assertEquals("Details unavailable for privacy.", DiagnosticsUiMapper.sanitizeUserText("contains https://x"))
        assertEquals("Details unavailable for privacy.", DiagnosticsUiMapper.sanitizeUserText("api_key=abc"))
    }

    @Test
    fun `r05 and fallback messages`() {
        val r05 = DiagnosticsUiMapper.r05NoLocalDataRemoteUnavailable(bundledFallbackAvailable = false)
        assertTrue(r05.visible)
        assertTrue(r05.nextActions.contains(R05NextAction.RETRY))
        assertFalse(r05.nextActions.contains(R05NextAction.USE_BUNDLED_FALLBACK_CATALOG))
        val withFallback = DiagnosticsUiMapper.r05NoLocalDataRemoteUnavailable(bundledFallbackAvailable = true)
        assertTrue(withFallback.nextActions.contains(R05NextAction.USE_BUNDLED_FALLBACK_CATALOG))
        assertNull(DiagnosticsUiMapper.fallbackMessage(null))
        val msg = DiagnosticsUiMapper.fallbackMessage(FallbackReason(code = "x", message = "safe", userActionRequired = "retry"))
        assertNotNull(msg)
        assertTrue(msg.hasNextAction)
    }

    @Test
    fun `presence rows and labels`() {
        val row = DiagnosticsUiMapper.presenceRow("i1", "milk", StorePresenceStatus.NOT_CHECKED, "token leaked")
        assertEquals(StorePresenceStatus.UNKNOWN, row.status)
        assertTrue(row.reasonLabel.contains("privacy") || row.reasonLabel.contains("Reason"))
        val present = DiagnosticsUiMapper.presenceRow("i2", "egg", StorePresenceStatus.PRESENT, "ok")
        assertEquals(StorePresenceStatus.PRESENT, present.status)
        DiagnosticsUiMapper.presenceBatchLabel(PresenceBatchStatus.OK)
        DiagnosticsUiMapper.presenceBatchLabel(PresenceBatchStatus.PARTIAL)
        DiagnosticsUiMapper.presenceBatchLabel(PresenceBatchStatus.UNAVAILABLE)
        DiagnosticsUiMapper.presenceBatchLabel(PresenceBatchStatus.DENIED)
        DiagnosticsUiMapper.presenceBatchLabel(PresenceBatchStatus.FAILED_MAPPED)
        DiagnosticsUiMapper.presenceBatchLabel(null)
    }

    @Test
    fun `store suggestion labels cover permission and status branches`() {
        DiagnosticsUiMapper.storeSuggestionLabel(null, LocationPermissionUiState.DENIED)
        DiagnosticsUiMapper.storeSuggestionLabel(StoreSuggestionStatus.DENIED, LocationPermissionUiState.GRANTED)
        DiagnosticsUiMapper.storeSuggestionLabel(StoreSuggestionStatus.UNAVAILABLE, LocationPermissionUiState.GRANTED)
        DiagnosticsUiMapper.storeSuggestionLabel(StoreSuggestionStatus.NOT_FOUND, LocationPermissionUiState.GRANTED)
        DiagnosticsUiMapper.storeSuggestionLabel(StoreSuggestionStatus.UNKNOWN, LocationPermissionUiState.GRANTED)
        DiagnosticsUiMapper.storeSuggestionLabel(StoreSuggestionStatus.OK, LocationPermissionUiState.GRANTED)
        DiagnosticsUiMapper.storeSuggestionLabel(null, LocationPermissionUiState.NOT_ASKED)
        DiagnosticsUiMapper.storeSuggestionLabel(null, LocationPermissionUiState.GRANTED)
    }
}
