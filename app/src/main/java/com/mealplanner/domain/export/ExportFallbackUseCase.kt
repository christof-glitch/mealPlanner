package com.mealplanner.domain.export

import com.mealplanner.domain.model.ShareBoundaryReason
import com.mealplanner.domain.model.ShareMethod
import com.mealplanner.domain.model.SharePayload
import com.mealplanner.domain.model.ShareResult
import com.mealplanner.domain.model.ShareStatus
import com.mealplanner.domain.model.ShoppingListItem

/**
 * C6 baseline: export uses current edited shopping list with mandatory share/copy fallback.
 * Extraction seam deferred to U3 (R-03/Q7=A).
 */
class ExportFallbackUseCase(
    private val shareSheet: ShareSheetBoundary = UnavailableShareSheetBoundary(),
    private val clipboard: ClipboardBoundary = UnavailableClipboardBoundary(),
) {
    fun buildPayload(
        shoppingListId: String,
        title: String,
        items: List<ShoppingListItem>,
        includeCheckedItems: Boolean = true,
    ): SharePayload {
        val effective = if (includeCheckedItems) items else items.filterNot { it.checked }
        val body = effective.joinToString(separator = "\n") { item ->
            val qty = listOfNotNull(item.amount?.toString(), item.unit).joinToString(" ").trim()
            val prefix = if (qty.isBlank()) "- " else "- $qty "
            prefix + item.displayName
        }.ifBlank { "(no items)" }
        return SharePayload(
            shoppingListId = shoppingListId,
            title = title,
            bodyText = body,
            includesCheckedItems = includeCheckedItems,
            generatedFromEditedList = true,
            instructionsText = "Official S-ostoslista export is not available in this foundation build. Use share or copy the list text and paste it into your preferred app.",
        )
    }

    fun exportCurrentEditedList(
        shoppingListId: String,
        title: String,
        items: List<ShoppingListItem>,
        preferShareSheet: Boolean = true,
    ): ShareResult {
        val payload = buildPayload(shoppingListId, title, items)
        if (preferShareSheet) {
            val shared = shareSheet.share(payload)
            if (shared.status == ShareStatus.SHARED || shared.status == ShareStatus.CANCELLED) return shared
        }
        val copied = clipboard.copy(payload.bodyText)
        if (copied) {
            return ShareResult(
                status = ShareStatus.COPIED,
                method = ShareMethod.COPY_TEXT,
                payload = payload,
            )
        }
        return ShareResult(
            status = ShareStatus.FALLBACK_INSTRUCTIONS_SHOWN,
            method = ShareMethod.MANUAL_INSTRUCTIONS,
            payload = payload,
            fallbackReason = ShareBoundaryReason(
                code = "SHARE_AND_COPY_UNAVAILABLE",
                message = "Share sheet and copy actions are unavailable.",
                userActionRequired = payload.instructionsText,
            ),
            userActionRequired = payload.instructionsText,
        )
    }
}

interface ShareSheetBoundary {
    fun share(payload: SharePayload): ShareResult
}

interface ClipboardBoundary {
    fun copy(text: String): Boolean
}

class UnavailableShareSheetBoundary : ShareSheetBoundary {
    override fun share(payload: SharePayload): ShareResult = ShareResult(
        status = ShareStatus.FAILED,
        method = ShareMethod.ANDROID_SHARE_SHEET,
        payload = payload,
        fallbackReason = ShareBoundaryReason("SHARE_SHEET_UNAVAILABLE", "Android share sheet unavailable in this context."),
    )
}

class UnavailableClipboardBoundary : ClipboardBoundary {
    override fun copy(text: String): Boolean = false
}
