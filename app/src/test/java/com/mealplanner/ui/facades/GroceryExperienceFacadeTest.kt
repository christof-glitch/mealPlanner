package com.mealplanner.ui.facades

import com.mealplanner.domain.export.DefaultExportShoppingListUseCase
import com.mealplanner.domain.export.ExportShoppingListUseCase
import com.mealplanner.domain.export.OfficialExportAttempt
import com.mealplanner.domain.export.ShareExportPort
import com.mealplanner.domain.export.SostoslistaOfficialExportPort
import com.mealplanner.domain.model.ExportInvestigationOutcome
import com.mealplanner.domain.model.ExportLineItem
import com.mealplanner.domain.model.ExportMethod
import com.mealplanner.domain.model.ExportRequest
import com.mealplanner.domain.model.ExportResult
import com.mealplanner.domain.model.ExportResultStatus
import com.mealplanner.domain.model.ShoppingListAggregate
import com.mealplanner.domain.model.ShoppingListExportDto
import com.mealplanner.domain.model.ShoppingListGenerationOutcome
import com.mealplanner.domain.model.ShoppingListGenerationOutcomeResult
import com.mealplanner.domain.model.ShoppingListItem
import com.mealplanner.domain.model.ShoppingListStaleStatus
import com.mealplanner.domain.model.ShoppingListStateStatus
import com.mealplanner.domain.model.SostoslistaAttemptStatus
import com.mealplanner.domain.model.effectiveDisplayText
import com.mealplanner.domain.model.effectiveQuantityText
import com.mealplanner.domain.planning.Adr006LockRules
import com.mealplanner.domain.shopping.ShoppingListRules
import com.mealplanner.ui.state.GroceryDomainSnapshot
import com.mealplanner.ui.state.GroceryIntent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Step 4 (test-first, key behavior): ADR-006/R-03 grocery invariants, exact stale warning,
 * regenerate confirmation when hasUserEdits, export current edited list, FR10 fallback mapping.
 */
class GroceryExperienceFacadeTest {
    private fun item(id: String, name: String, qty: String? = null, checked: Boolean = false) = ShoppingListItem(
        itemId = id,
        displayName = name,
        displayText = name,
        quantityText = qty,
        checked = checked,
        amountText = qty,
    )

    private fun aggregate(
        hasEdits: Boolean = false,
        stale: Boolean = false,
        items: List<ShoppingListItem> = listOf(item("i1", "Milk", "1 l"), item("i2", "Eggs", "12 pcs")),
        snapshotId: String = "snap-1",
    ) = ShoppingListAggregate(
        shoppingListId = "list-1",
        snapshotId = snapshotId,
        items = items,
        listStatus = ShoppingListStateStatus.GENERATED,
        staleStatus = ShoppingListStaleStatus(snapshotId = snapshotId, isStale = stale),
        hasUserEdits = hasEdits,
    )

    private fun sharePort(captured: MutableList<ShoppingListExportDto> = mutableListOf()) = object : ShareExportPort {
        override fun exportFallback(payload: ShoppingListExportDto, title: String): ExportResult {
            captured += payload
            return ExportResult(
                status = ExportResultStatus.SUCCESS,
                methodUsed = ExportMethod.SHARE_SHEET,
                instructions = "Share or copy the current edited shopping list into S-ostoslista.",
                payload = payload,
            )
        }
    }

    private fun prepareExport(agg: ShoppingListAggregate): ShoppingListExportDto = ShoppingListExportDto(
        shoppingListId = agg.shoppingListId,
        snapshotId = agg.snapshotId,
        items = agg.items.filterNot { it.removed }.map {
            ExportLineItem(it.effectiveDisplayText(), it.checked, it.effectiveQuantityText(), it.availabilityStatus.name, it.availabilityReason, it.itemId)
        },
        staleWarning = agg.staleStatus.staleWarning,
    )

    private fun facade(
        exportUseCase: ExportShoppingListUseCase? = null,
        generate: (() -> ShoppingListGenerationOutcomeResult)? = null,
        regenerate: ((Boolean) -> ShoppingListGenerationOutcomeResult)? = null,
    ) = DefaultGroceryExperienceFacade(
        exportUseCase = exportUseCase,
        prepareExport = ::prepareExport,
        generate = generate,
        regenerate = regenerate,
    )

    @Test
    fun generateOnlyWhenDomainReportsReadyExplicit() {
        val generated = aggregate()
        val f = facade(generate = { ShoppingListGenerationOutcomeResult(ShoppingListGenerationOutcome.GENERATED, generated) })
        f.refresh(GroceryDomainSnapshot(aggregate = null, recipesLocked = false, lockSummary = "Recipes not locked"))
        val notReady = f.handle(GroceryIntent.Generate)
        assertFalse(notReady.canGenerate)
        assertEquals("Lock recipes in Plan before generating your shopping list.", notReady.lockSummary)

        f.refresh(GroceryDomainSnapshot(aggregate = null, recipesLocked = true, lockSummary = "Recipes locked: 3"))
        val ready = f.handle(GroceryIntent.Generate)
        assertTrue(ready.hasList)
        assertEquals("list-1", ready.shoppingListId)
    }

    @Test
    fun regenerateWithEditsRequiresExplicitConfirmationNoSilentReplacement() {
        val edited = aggregate(hasEdits = true)
        var replaced = false
        val f = facade(regenerate = { confirm ->
            val req = com.mealplanner.domain.model.ShoppingListGenerationRequest(
                snapshotId = "snap-1",
                operation = com.mealplanner.domain.model.ShoppingListGenerationOperation.REGENERATE,
                confirmRegenerateIfEdited = confirm,
            )
            val decision = ShoppingListRules.evaluateRegenerationRequest(edited, req)
            if (decision.status == ShoppingListGenerationOutcome.GENERATED) {
                replaced = true
                ShoppingListGenerationOutcomeResult(ShoppingListGenerationOutcome.GENERATED, aggregate(hasEdits = false))
            } else decision
        })
        f.refresh(GroceryDomainSnapshot(aggregate = edited, recipesLocked = true, lockSummary = "Recipes locked: 1", currentEditedList = edited))
        val asked = f.handle(GroceryIntent.RequestRegenerate)
        assertTrue(asked.showRegenerateConfirmation)
        assertFalse(replaced)
        assertEquals(edited.items.size, asked.items.size, "Current list must remain until explicit confirmation")

        val cancelled = f.handle(GroceryIntent.CancelRegenerate)
        assertFalse(cancelled.showRegenerateConfirmation)
        assertFalse(replaced)
        assertEquals(edited.items.size, cancelled.items.size)

        val confirmed = f.handle(GroceryIntent.ConfirmRegenerateAndReplace)
        assertTrue(replaced)
        assertFalse(confirmed.showRegenerateConfirmation)
    }

    @Test
    fun staleKeepsCurrentListAndExposesExactAdr006WarningNoAutoRegeneration() {
        val stale = aggregate(hasEdits = true, stale = true)
        val f = facade()
        val state = f.refresh(GroceryDomainSnapshot(aggregate = stale, recipesLocked = true, lockSummary = "Recipes locked: 1", currentEditedList = stale))
        assertTrue(state.isStale)
        assertEquals(Adr006LockRules.STALE_WARNING_TEXT, state.staleWarningText)
        assertEquals("Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.", state.staleWarningText)
        assertEquals(stale.items.size, state.items.size, "Current list must remain; no auto-regeneration")
        assertTrue(state.items.any { it.displayText == "Milk" })
    }

    @Test
    fun exportUsesCurrentEditedListPayload() {
        val edited = aggregate(hasEdits = true, items = listOf(item("i1", "Milk (edited)", "2 l", checked = true)))
        val captured = mutableListOf<ShoppingListExportDto>()
        val export = DefaultExportShoppingListUseCase(sharePort = sharePort(captured))
        val f = facade(exportUseCase = export)
        f.refresh(GroceryDomainSnapshot(aggregate = edited, recipesLocked = true, currentEditedList = edited))
        val result = f.exportCurrentEditedList(GroceryIntent.ShareFallback)
        assertEquals(ExportResultStatus.FALLBACK_USED, result.status)
        val payload = captured.single()
        assertTrue(payload.items.any { it.displayText.contains("edited") && it.checked })
        assertFalse(payload.items.any { it.displayText == "Milk" && !it.displayText.contains("edited") })
    }

    @Test
    fun officialSostoslistaAttemptMapsToFallbackStatesWithInstructions() {
        val base = aggregate()
        val dto = prepareExport(base)
        fun facadeWithOfficial(status: SostoslistaAttemptStatus, message: String?): DefaultGroceryExperienceFacade {
            val official = object : SostoslistaOfficialExportPort {
                override fun attemptOfficialExport(payload: ShoppingListExportDto) = OfficialExportAttempt(status, message, "Use share/copy fallback.")
            }
            return facade(exportUseCase = DefaultExportShoppingListUseCase(officialPort = official, sharePort = sharePort()))
        }
        f_refresh_base(facadeWithOfficial(SostoslistaAttemptStatus.NOT_SUPPORTED, "Not feasible"), base).let { f ->
            val r = f.exportCurrentEditedList(GroceryIntent.ExportOfficialSostoslista)
            assertEquals(ExportResultStatus.FALLBACK_USED, r.status)
            assertEquals(ExportInvestigationOutcome.NOT_FEASIBLE_CONFIRMED, r.investigationOutcome)
            assertNotNull(r.instructions)
        }
        f_refresh_base(facadeWithOfficial(SostoslistaAttemptStatus.UNAVAILABLE, "Unavailable"), base).let { f ->
            val r = f.exportCurrentEditedList(GroceryIntent.ExportOfficialSostoslista)
            assertEquals(ExportResultStatus.FALLBACK_USED, r.status)
            assertEquals(SostoslistaAttemptStatus.UNAVAILABLE, r.attemptStatus)
            assertNotNull(r.instructions)
        }
        f_refresh_base(facadeWithOfficial(SostoslistaAttemptStatus.FAILED_MAPPED, "Failed"), base).let { f ->
            val r = f.exportCurrentEditedList(GroceryIntent.ExportOfficialSostoslista)
            assertEquals(ExportResultStatus.FALLBACK_USED, r.status)
            assertEquals(ExportInvestigationOutcome.PARTIALLY_USED, r.investigationOutcome)
            assertNotNull(r.instructions)
        }
    }

    private fun f_refresh_base(f: DefaultGroceryExperienceFacade, base: ShoppingListAggregate): DefaultGroceryExperienceFacade {
        f.refresh(GroceryDomainSnapshot(aggregate = base, recipesLocked = true, currentEditedList = base))
        return f
    }
}
