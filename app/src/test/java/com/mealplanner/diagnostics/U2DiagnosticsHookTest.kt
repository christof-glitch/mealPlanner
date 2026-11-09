package com.mealplanner.diagnostics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class U2DiagnosticsHookTest {
    @Test
    fun `no-op diagnostics is safe and fake hook captures structured fields`() {
        val sink = CapturingSink()
        val event = PrivacySafeDiagnostics.event(
            "a1.test",
            DiagnosticCategory.INTEGRATION_FALLBACK,
            mapOf("op" to "catalog", "status" to "fallback_used", "count" to "2"),
        )
        sink.record(event)
        NoOpDiagnosticsSink().record(event)
        assertEquals(1, sink.events.size)
        assertEquals("a1.test", sink.events.single().name)
        assertTrue(sink.events.single().attributes["status"] == "fallback_used")
    }

    private class CapturingSink : DiagnosticsSink {
        val events = mutableListOf<DiagnosticEvent>()
        override fun record(event: DiagnosticEvent) { events += event }
    }
}
