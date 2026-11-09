package com.mealplanner.platform

import com.mealplanner.domain.model.FallbackReason
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

/** A4 platform boundaries: permission/share/clipboard/date/location signals (no continuous tracking). */
interface PermissionBoundary {
    suspend fun hasLocationPermission(): Boolean
    suspend fun requestLocationPermission(): Boolean
}

interface ShareSheetPlatform {
    suspend fun shareText(title: String, body: String): SharePlatformResult
}

interface ClipboardPlatform {
    suspend fun copyPlainText(label: String, text: String): Boolean
}

interface DateLocationSignalProvider {
    /** Coarse/local date context only; no precise continuous tracking. */
    fun currentLocalDateIso(): String?
    /** Best-effort coarse location hint for nearest-store suggestion; null when unavailable/denied. */
    suspend fun coarseLocationHint(): String?
}

sealed interface SharePlatformResult {
    data object Shared : SharePlatformResult
    data object Cancelled : SharePlatformResult
    data class Failed(val reason: FallbackReason) : SharePlatformResult
}

/** NFR1.2: bounded timeout + typed fallback off main thread. */
suspend fun <T> withTypedTimeout(
    timeoutMs: Long,
    fallback: (FallbackReason) -> T,
    block: suspend () -> T,
): T = try {
    withTimeout(timeoutMs) { block() }
} catch (_: TimeoutCancellationException) {
    fallback(FallbackReason("TIMEOUT", "Operation timed out.", "Try again or use fallback."))
} catch (e: Exception) {
    fallback(FallbackReason("FAILED", e.message ?: "Operation failed.", "Try again or use fallback."))
}
