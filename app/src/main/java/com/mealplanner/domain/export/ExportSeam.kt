package com.mealplanner.domain.export

import com.mealplanner.domain.model.ExportInvestigationOutcome
import com.mealplanner.domain.model.ExportMethod
import com.mealplanner.domain.model.ExportRequest
import com.mealplanner.domain.model.ExportResult
import com.mealplanner.domain.model.ExportResultStatus
import com.mealplanner.domain.model.ShareBoundaryReason
import com.mealplanner.domain.model.ShoppingListExportDto
import com.mealplanner.domain.model.SostoslistaAttemptStatus

/**
 * BR3.8/BR3.9/OQ4 export seam (inward dependencies): domain use case + DTO + platform/share ports.
 * No Android intent types in domain. Mandatory share/copy fallback; official S-ostoslista investigational.
 */
data class OfficialExportAttempt(
    val status: SostoslistaAttemptStatus,
    val message: String? = null,
    val userActionRequired: String? = null,
)

interface SostoslistaOfficialExportPort {
    fun attemptOfficialExport(payload: ShoppingListExportDto): OfficialExportAttempt
}

interface ShareExportPort {
    fun exportFallback(payload: ShoppingListExportDto, title: String): ExportResult
}

interface ExportShoppingListUseCase {
    fun export(request: ExportRequest): ExportResult
}

class DefaultExportShoppingListUseCase(
    private val officialPort: SostoslistaOfficialExportPort? = null,
    private val sharePort: ShareExportPort,
) : ExportShoppingListUseCase {
    override fun export(request: ExportRequest): ExportResult {
        val payload = request.payload
        if (request.preferredMethod == ExportMethod.S_OSTOSLISTA_OFFICIAL_IF_AVAILABLE && officialPort != null) {
            val attempt = officialPort.attemptOfficialExport(payload)
            when (attempt.status) {
                SostoslistaAttemptStatus.SUCCESS -> return ExportResult(
                    status = ExportResultStatus.SUCCESS,
                    methodUsed = ExportMethod.S_OSTOSLISTA_OFFICIAL_IF_AVAILABLE,
                    message = attempt.message ?: "Exported via official S-ostoslista path.",
                    investigationOutcome = ExportInvestigationOutcome.USED,
                    attemptStatus = attempt.status,
                    payload = payload,
                )

                SostoslistaAttemptStatus.NOT_SUPPORTED -> return fallback(
                    payload = payload,
                    title = request.title,
                    attempt = attempt,
                    outcome = ExportInvestigationOutcome.NOT_FEASIBLE_CONFIRMED,
                    reasonCode = "SOSTOSLISTA_NOT_SUPPORTED",
                    reasonMessage = attempt.message ?: "Official S-ostoslista export not supported/feasible in this build.",
                )

                SostoslistaAttemptStatus.UNAVAILABLE,
                SostoslistaAttemptStatus.FAILED_MAPPED -> return fallback(
                    payload = payload,
                    title = request.title,
                    attempt = attempt,
                    outcome = ExportInvestigationOutcome.PARTIALLY_USED,
                    reasonCode = if (attempt.status == SostoslistaAttemptStatus.UNAVAILABLE) "SOSTOSLISTA_UNAVAILABLE" else "SOSTOSLISTA_FAILED_MAPPED",
                    reasonMessage = attempt.message ?: "Official S-ostoslista export unavailable; fallback used.",
                )
            }
        }
        return fallback(
            payload = payload,
            title = request.title,
            attempt = null,
            outcome = ExportInvestigationOutcome.UNKNOWN,
            reasonCode = "OFFICIAL_EXPORT_NOT_REQUESTED",
            reasonMessage = "Official export not attempted; using share/copy fallback.",
        )
    }

    private fun fallback(
        payload: ShoppingListExportDto,
        title: String,
        attempt: OfficialExportAttempt?,
        outcome: ExportInvestigationOutcome,
        reasonCode: String,
        reasonMessage: String,
    ): ExportResult {
        val base = sharePort.exportFallback(payload, title)
        val instructions = base.instructions?.takeIf { it.isNotBlank() }
            ?: attempt?.userActionRequired?.takeIf { it.isNotBlank() }
            ?: "Official S-ostoslista export is not available. Use share or copy the current edited shopping list."
        return base.copy(
            status = if (base.status == ExportResultStatus.SUCCESS) ExportResultStatus.FALLBACK_USED else base.status,
            methodUsed = base.methodUsed,
            instructions = instructions,
            message = base.message ?: attempt?.message ?: reasonMessage,
            investigationOutcome = outcome,
            attemptStatus = attempt?.status,
            fallbackReason = base.fallbackReason ?: ShareBoundaryReason(
                code = reasonCode,
                message = reasonMessage,
                userActionRequired = instructions,
            ),
            payload = base.payload ?: payload,
        )
    }
}
