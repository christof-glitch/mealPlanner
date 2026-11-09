package com.mealplanner.diagnostics

import java.time.Instant

/**
 * Privacy-safe diagnostics (NFR3.2): no coordinates, no raw personal identifiers.
 */
data class DiagnosticEvent(
    val name: String,
    val at: Instant = Instant.now(),
    val category: DiagnosticCategory = DiagnosticCategory.APP,
    val attributes: Map<String, String> = emptyMap(),
)

enum class DiagnosticCategory { APP, INTEGRATION_FALLBACK, PERSISTENCE, EXPORT }

interface DiagnosticsSink {
    fun record(event: DiagnosticEvent)
}

class NoOpDiagnosticsSink : DiagnosticsSink {
    override fun record(event: DiagnosticEvent) = Unit
}

object PrivacySafeDiagnostics {
    private val forbiddenKeyFragments = listOf("lat", "lon", "coord", "gps", "email", "phone", "name", "address", "userIdRaw")

    fun sanitizeAttributes(attributes: Map<String, String>): Map<String, String> = attributes.filterKeys { key ->
        val lower = key.lowercase()
        forbiddenKeyFragments.none { lower.contains(it) }
    }

    fun event(name: String, category: DiagnosticCategory = DiagnosticCategory.APP, attributes: Map<String, String> = emptyMap()): DiagnosticEvent =
        DiagnosticEvent(name = name, category = category, attributes = sanitizeAttributes(attributes))
}
