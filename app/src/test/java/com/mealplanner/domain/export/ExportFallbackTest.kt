package com.mealplanner.domain.export

import com.mealplanner.domain.model.ShareMethod
import com.mealplanner.domain.model.SharePayload
import com.mealplanner.domain.model.ShareResult
import com.mealplanner.domain.model.ShareStatus
import com.mealplanner.domain.model.ShoppingListItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExportFallbackTest {
    private fun item(id: String, name: String, checked: Boolean = false, amount: Double? = null, unit: String? = null) =
        ShoppingListItem(itemId = id, displayName = name, checked = checked, amount = amount, unit = unit)

    @Test
    fun `payload is built from current edited list including checked state choice`() {
        val useCase = ExportFallbackUseCase()
        val items = listOf(item("1", "Tomato", amount = 2.0, unit = "pcs"), item("2", "Milk", checked = true))
        val payload = useCase.buildPayload("list-1", "Groceries", items)
        assertTrue(payload.generatedFromEditedList)
        assertEquals("list-1", payload.shoppingListId)
        assertTrue(payload.bodyText.contains("Tomato"))
        assertTrue(payload.bodyText.contains("Milk"))
    }

    @Test
    fun `official S-ostoslista path absent uses fallback instructions`() {
        val useCase = ExportFallbackUseCase(
            shareSheet = UnavailableShareSheetBoundary(),
            clipboard = UnavailableClipboardBoundary(),
        )
        val result = useCase.exportCurrentEditedList("list-1", "Groceries", listOf(item("1", "Tomato")))
        assertEquals(ShareStatus.FALLBACK_INSTRUCTIONS_SHOWN, result.status)
        assertEquals(ShareMethod.MANUAL_INSTRUCTIONS, result.method)
        assertTrue(result.payload?.instructionsText?.isNotBlank() == true)
    }

    @Test
    fun `share sheet success returns SHARED`() {
        val useCase = ExportFallbackUseCase(
            shareSheet = object : ShareSheetBoundary {
                override fun share(payload: SharePayload) = ShareResult(ShareStatus.SHARED, ShareMethod.ANDROID_SHARE_SHEET, payload)
            },
            clipboard = UnavailableClipboardBoundary(),
        )
        val result = useCase.exportCurrentEditedList("list-1", "Groceries", listOf(item("1", "Tomato")))
        assertEquals(ShareStatus.SHARED, result.status)
    }

    @Test
    fun `share failure falls back to copy with COPIED`() {
        val useCase = ExportFallbackUseCase(
            shareSheet = UnavailableShareSheetBoundary(),
            clipboard = object : ClipboardBoundary {
                override fun copy(text: String): Boolean = text.contains("Tomato")
            },
        )
        val result = useCase.exportCurrentEditedList("list-1", "Groceries", listOf(item("1", "Tomato")))
        assertEquals(ShareStatus.COPIED, result.status)
        assertEquals(ShareMethod.COPY_TEXT, result.method)
    }

    @Test
    fun `cancelled share is preserved and not treated as fallback failure`() {
        val useCase = ExportFallbackUseCase(
            shareSheet = object : ShareSheetBoundary {
                override fun share(payload: SharePayload) = ShareResult(ShareStatus.CANCELLED, ShareMethod.ANDROID_SHARE_SHEET, payload)
            },
            clipboard = object : ClipboardBoundary {
                override fun copy(text: String): Boolean = true
            },
        )
        val result = useCase.exportCurrentEditedList("list-1", "Groceries", listOf(item("1", "Tomato")))
        assertEquals(ShareStatus.CANCELLED, result.status)
    }

    @Test
    fun `failed share and failed copy return explicit fallback instructions`() {
        val useCase = ExportFallbackUseCase(UnavailableShareSheetBoundary(), UnavailableClipboardBoundary())
        val result = useCase.exportCurrentEditedList("list-1", "Groceries", listOf(item("1", "Tomato")))
        assertTrue(result.status in setOf(ShareStatus.FAILED, ShareStatus.FALLBACK_INSTRUCTIONS_SHOWN))
        assertTrue(result.userActionRequired?.isNotBlank() == true || result.fallbackReason?.userActionRequired?.isNotBlank() == true)
    }

    @Test
    fun `payload reflects current edited quantities not stale source`() {
        val useCase = ExportFallbackUseCase()
        val edited = listOf(item("1", "Tomato", amount = 5.0, unit = "pcs"))
        val payload = useCase.buildPayload("list-1", "Groceries", edited)
        assertTrue(payload.bodyText.contains("5.0"))
        assertFalse(payload.bodyText.contains("2.0"))
    }
}
