package com.mealplanner.ui.platform

import com.mealplanner.domain.export.ShareExportPort
import com.mealplanner.domain.model.ExportMethod
import com.mealplanner.domain.model.ExportResult
import com.mealplanner.domain.model.ExportResultStatus
import com.mealplanner.domain.model.ShoppingListExportDto

/**
 * Platform boundary for share/copy fallback (FR10/OQ4, BR3.9). Domain receives typed outcomes only.
 * Real Android share sheet/clipboard launching is provided by runtime wiring; this default is safe/offline.
 */
class ShareCopyPlatformBoundary : ShareExportPort {
    override fun exportFallback(payload: ShoppingListExportDto, title: String): ExportResult = ExportResult(
        status = ExportResultStatus.FALLBACK_USED,
        methodUsed = ExportMethod.SHARE_SHEET,
        message = "Share/copy fallback prepared for the current edited shopping list.",
        instructions = "Use your device share sheet or copy the shopping list text into S-ostoslista or another app.",
        payload = payload,
    )

    fun copyTextFallback(payload: ShoppingListExportDto): ExportResult = ExportResult(
        status = ExportResultStatus.FALLBACK_USED,
        methodUsed = ExportMethod.COPY_TEXT,
        message = "Shopping list text is ready to copy.",
        instructions = "Copy the current edited shopping list text and paste it where needed.",
        payload = payload,
    )
}
