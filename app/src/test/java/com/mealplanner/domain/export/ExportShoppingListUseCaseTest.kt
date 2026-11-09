package com.mealplanner.domain.export

import com.mealplanner.domain.model.ExportInvestigationOutcome
import com.mealplanner.domain.model.ExportLineItem
import com.mealplanner.domain.model.ExportMethod
import com.mealplanner.domain.model.ExportRequest
import com.mealplanner.domain.model.ExportResult
import com.mealplanner.domain.model.ExportResultStatus
import com.mealplanner.domain.model.ShoppingListAggregate
import com.mealplanner.domain.model.ShoppingListExportDto
import com.mealplanner.domain.model.ShoppingListItem
import com.mealplanner.domain.model.SostoslistaAttemptStatus
import com.mealplanner.domain.shopping.ShoppingListService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExportShoppingListUseCaseTest {
    private fun dto(vararg items: ExportLineItem) = ShoppingListExportDto(
        shoppingListId = "list-1",
        snapshotId = "snap-1",
        items = items.toList(),
    )

    @Test
    fun `export uses current edited list DTO`() {
        val aggregate = ShoppingListAggregate(
            shoppingListId = "list-1",
            items = listOf(
                ShoppingListItem(itemId = "i1", displayName = "Tomato", displayText = "Tomato edited", checked = true, manuallyEdited = true, userEdited = true),
                ShoppingListItem(itemId = "i2", displayName = "Milk", removed = true),
            ),
            hasUserEdits = true,
        )
        val service = ShoppingListService(object : com.mealplanner.adapters.RecipeSourceBoundary {
            override suspend fun findCandidates(query: Map<String, String>) = throw UnsupportedOperationException()
            override suspend fun getRecipeSummary(recipeId: String) = null
            override suspend fun resolveIngredients(recipeIds: List<String>) = emptyMap<String, List<com.mealplanner.domain.model.IngredientLine>>()
            override fun sourceStatus() = throw UnsupportedOperationException()
        })
        val prepared = service.prepareCurrentExportState(aggregate)
        assertEquals(1, prepared.items.size)
        assertEquals("Tomato edited", prepared.items.first().displayText)
        assertTrue(prepared.items.first().checked)
    }

    @Test
    fun `mandatory share or copy fallback is returned with instructions when official not feasible`() {
        val official = object : SostoslistaOfficialExportPort {
            override fun attemptOfficialExport(payload: ShoppingListExportDto): OfficialExportAttempt =
                OfficialExportAttempt(status = SostoslistaAttemptStatus.NOT_SUPPORTED, message = "OQ4 not feasible")
        }
        val share = object : ShareExportPort {
            override fun exportFallback(payload: ShoppingListExportDto, title: String): ExportResult =
                ExportResult(ExportResultStatus.FALLBACK_USED, ExportMethod.COPY_TEXT, instructions = "Copy and paste into S-ostoslista", payload = payload, investigationOutcome = ExportInvestigationOutcome.NOT_FEASIBLE_CONFIRMED)
        }
        val useCase = DefaultExportShoppingListUseCase(officialPort = official, sharePort = share)
        val result = useCase.export(ExportRequest(payload = dto(ExportLineItem("Tomato")), preferredMethod = ExportMethod.S_OSTOSLISTA_OFFICIAL_IF_AVAILABLE))
        assertEquals(ExportResultStatus.FALLBACK_USED, result.status)
        assertTrue(!result.instructions.isNullOrBlank())
        assertEquals(ExportInvestigationOutcome.NOT_FEASIBLE_CONFIRMED, result.investigationOutcome)
    }

    @Test
    fun `official success maps to USED without fabricated fallback success`() {
        val official = object : SostoslistaOfficialExportPort {
            override fun attemptOfficialExport(payload: ShoppingListExportDto): OfficialExportAttempt =
                OfficialExportAttempt(status = SostoslistaAttemptStatus.SUCCESS, message = "Exported")
        }
        val share = object : ShareExportPort {
            override fun exportFallback(payload: ShoppingListExportDto, title: String): ExportResult =
                throw AssertionError("fallback must not run on official success")
        }
        val useCase = DefaultExportShoppingListUseCase(official, share)
        val result = useCase.export(ExportRequest(payload = dto(ExportLineItem("Tomato")), preferredMethod = ExportMethod.S_OSTOSLISTA_OFFICIAL_IF_AVAILABLE))
        assertEquals(ExportResultStatus.SUCCESS, result.status)
        assertEquals(ExportInvestigationOutcome.USED, result.investigationOutcome)
    }

    @Test
    fun `official unavailable or failed maps to fallback with coarse investigation outcome`() {
        val official = object : SostoslistaOfficialExportPort {
            override fun attemptOfficialExport(payload: ShoppingListExportDto): OfficialExportAttempt =
                OfficialExportAttempt(status = SostoslistaAttemptStatus.FAILED_MAPPED, message = "Network mapped failure")
        }
        val share = object : ShareExportPort {
            override fun exportFallback(payload: ShoppingListExportDto, title: String): ExportResult =
                ExportResult(ExportResultStatus.FALLBACK_USED, ExportMethod.SHARE_SHEET, instructions = "Share list manually", payload = payload)
        }
        val useCase = DefaultExportShoppingListUseCase(official, share)
        val result = useCase.export(ExportRequest(payload = dto(ExportLineItem("Tomato")), preferredMethod = ExportMethod.S_OSTOSLISTA_OFFICIAL_IF_AVAILABLE))
        assertEquals(ExportResultStatus.FALLBACK_USED, result.status)
        assertEquals(ExportInvestigationOutcome.PARTIALLY_USED, result.investigationOutcome)
        assertTrue(!result.instructions.isNullOrBlank())
    }

    @Test
    fun `legacy export fallback use case remains compatible`() {
        val legacy = ExportFallbackUseCase(UnavailableShareSheetBoundary(), UnavailableClipboardBoundary())
        val result = legacy.exportCurrentEditedList("list-1", "Groceries", listOf(ShoppingListItem(itemId = "i1", displayName = "Tomato")))
        assertEquals(com.mealplanner.domain.model.ShareStatus.FALLBACK_INSTRUCTIONS_SHOWN, result.status)
    }
}
