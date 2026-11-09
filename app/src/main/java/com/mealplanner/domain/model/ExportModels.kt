package com.mealplanner.domain.model

import java.time.Instant

/** ENT-U1-012: U1 fallback boundary; U3 adds seam DTOs below while preserving compatibility. */
data class SharePayload(
    val shoppingListId: ShoppingListId,
    val title: String,
    val bodyText: String,
    val includesCheckedItems: Boolean = true,
    val generatedFromEditedList: Boolean = true,
    val instructionsText: String? = null,
)

/** ENT-U1-013 */
data class ShareResult(
    val status: ShareStatus,
    val method: ShareMethod,
    val payload: SharePayload? = null,
    val fallbackReason: ShareBoundaryReason? = null,
    val userActionRequired: String? = null,
)

// --- U3 C6/A3 export seam (BR3.8/BR3.9, OQ4 investigational) ---

enum class ExportMethod {
    S_OSTOSLISTA_OFFICIAL_IF_AVAILABLE,
    SHARE_SHEET,
    COPY_TEXT,
}

/** Coarse investigation outcome; mapped to C6/A3 semantics without fabricating success. */
enum class ExportInvestigationOutcome {
    USED,
    PARTIALLY_USED,
    NOT_FEASIBLE_CONFIRMED,
    UNKNOWN,
}

enum class SostoslistaAttemptStatus {
    SUCCESS,
    NOT_SUPPORTED,
    UNAVAILABLE,
    FAILED_MAPPED,
}

enum class ExportResultStatus {
    SUCCESS,
    FALLBACK_USED,
    FAILED_MAPPED,
}

data class ExportLineItem(
    val displayText: String,
    val checked: Boolean = true,
    val quantityText: String? = null,
    val availabilityStatus: String? = null,
    val availabilityReason: String? = null,
    val itemId: String? = null,
)

/** BR3.9: stable grocery/export extraction seam DTO built from current edited list. */
data class ShoppingListExportDto(
    val shoppingListId: ShoppingListId,
    val snapshotId: LockSnapshotId? = null,
    val exportedAt: Instant? = null,
    val items: List<ExportLineItem> = emptyList(),
    val selectedStore: StoreRef? = null,
    val staleWarning: String? = null,
    val notes: List<String> = emptyList(),
    val includesCheckedItems: Boolean = true,
)

data class ExportRequest(
    val shoppingListId: ShoppingListId? = null,
    val payload: ShoppingListExportDto,
    val preferredMethod: ExportMethod = ExportMethod.SHARE_SHEET,
    val title: String = "Shopping list",
)

data class ExportResult(
    val status: ExportResultStatus,
    val methodUsed: ExportMethod,
    val instructions: String? = null,
    val message: String? = null,
    val investigationOutcome: ExportInvestigationOutcome? = null,
    val attemptStatus: SostoslistaAttemptStatus? = null,
    val fallbackReason: ShareBoundaryReason? = null,
    val payload: ShoppingListExportDto? = null,
)

