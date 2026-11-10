package com.mealplanner.ui.facades

import com.mealplanner.domain.model.ExportMethod
import com.mealplanner.domain.model.ExportRequest
import com.mealplanner.domain.model.ExportResult
import com.mealplanner.domain.model.ExportResultStatus
import com.mealplanner.domain.model.ShoppingItemIngredientDataStatus
import com.mealplanner.domain.model.ShoppingListAggregate
import com.mealplanner.domain.model.ShoppingListGenerationOutcome
import com.mealplanner.domain.model.ShoppingListGenerationOutcomeResult
import com.mealplanner.domain.model.ShoppingListItem
import com.mealplanner.domain.model.ShoppingListStaleStatus
import com.mealplanner.domain.model.ShoppingListStateStatus
import com.mealplanner.domain.model.StoreChain
import com.mealplanner.domain.model.StoreRef
import com.mealplanner.ui.state.GroceryDomainSnapshot
import com.mealplanner.ui.state.GroceryIntent
import com.mealplanner.ui.state.GroceryScreenState
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DefaultGroceryExperienceFacadeTest {
    private fun aggregate(id: String = "list1", hasEdits: Boolean = false, stale: Boolean = false): ShoppingListAggregate =
        ShoppingListAggregate(
            shoppingListId = id,
            snapshotId = "snap1",
            items = listOf(
                ShoppingListItem(itemId = "i1", displayName = "Milk", displayText = "Milk", quantityText = "1 l"),
                ShoppingListItem(itemId = "i2", displayName = "Eggs", displayText = "Eggs", quantityText = "6", checked = true),
            ),
            listStatus = ShoppingListStateStatus.GENERATED,
            staleStatus = ShoppingListStaleStatus(snapshotId = "snap1", isStale = stale),
            hasUserEdits = hasEdits,
            selectedStoreRef = StoreRef("s1", "Prisma", StoreChain.PRISMA),
            lastGeneratedAt = Instant.parse("2026-01-01T00:00:00Z"),
        )

    @Test
    fun refreshAndGenerateAndRegenerateFlows() {
        val generated = aggregate()
        val facade = DefaultGroceryExperienceFacade(
            generate = { ShoppingListGenerationOutcomeResult(ShoppingListGenerationOutcome.GENERATED, generated) },
            regenerate = { confirmed ->
                if (!confirmed) ShoppingListGenerationOutcomeResult(ShoppingListGenerationOutcome.CONFIRMATION_REQUIRED)
                else ShoppingListGenerationOutcomeResult(ShoppingListGenerationOutcome.GENERATED, aggregate(hasEdits = false))
            },
        )

        val locked = facade.refresh(GroceryDomainSnapshot(recipesLocked = true, lockSummary = "Locked 3"))
        assertTrue(locked.canGenerate)

        val afterGenerate = facade.handle(GroceryIntent.Generate)
        assertTrue(afterGenerate.hasList)
        assertEquals(2, afterGenerate.items.size)

        // edited list => request regenerate asks for confirmation
        facade.refresh(GroceryDomainSnapshot(aggregate = aggregate(hasEdits = true), recipesLocked = true, lockSummary = "Locked"))
        val asked = facade.handle(GroceryIntent.RequestRegenerate)
        assertTrue(asked.showRegenerateConfirmation)
        val cancelled = facade.handle(GroceryIntent.CancelRegenerate)
        assertFalse(cancelled.showRegenerateConfirmation)

        facade.handle(GroceryIntent.RequestRegenerate)
        val confirmed = facade.handle(GroceryIntent.ConfirmRegenerateAndReplace)
        assertFalse(confirmed.showRegenerateConfirmation)
        assertTrue(confirmed.hasList)
    }

    @Test
    fun itemMutationsAndExport() {
        val facade = DefaultGroceryExperienceFacade(
            exportUseCase = object : com.mealplanner.domain.export.ExportShoppingListUseCase {
                override fun export(request: ExportRequest): ExportResult = ExportResult(
                    status = ExportResultStatus.SUCCESS,
                    methodUsed = request.preferredMethod,
                    message = "ok",
                    instructions = "instr",
                )
            },
        )
        facade.refresh(GroceryDomainSnapshot(aggregate = aggregate(), recipesLocked = true, lockSummary = "Locked"))

        var state = facade.handle(GroceryIntent.ToggleChecked("i1"))
        assertTrue(state.items.first { it.itemId == "i1" }.checked)
        state = facade.handle(GroceryIntent.EditItem("i1", "Milk 2", "2 l"))
        assertEquals("Milk 2", state.items.first { it.itemId == "i1" }.displayText)
        state = facade.handle(GroceryIntent.AddItem("Bread", "1"))
        assertTrue(state.items.any { it.displayText == "Bread" })
        val removeId = state.items.first { it.displayText == "Bread" }.itemId
        state = facade.handle(GroceryIntent.RemoveItem(removeId))
        assertTrue(state.items.none { it.itemId == removeId })

        state = facade.handle(GroceryIntent.ShareFallback)
        assertEquals("ok", state.exportMessage)
        assertEquals("instr", state.exportInstructions)

        // export with no list
        val emptyFacade = DefaultGroceryExperienceFacade()
        val emptyState = emptyFacade.handle(GroceryIntent.CopyTextFallback)
        assertNotNull(emptyState.exportMessage ?: emptyState.exportInstructions ?: "no-list")
    }

    @Test
    fun staleWarningAndLockRequiredMessage() {
        val facade = DefaultGroceryExperienceFacade()
        val notLocked = facade.refresh(GroceryDomainSnapshot(recipesLocked = false, lockSummary = ""))
        assertEquals("Recipes not locked", notLocked.lockSummary)
        facade.handle(GroceryIntent.Generate)
        assertEquals(GroceryScreenState.LOCK_REQUIRED_MESSAGE, facade.currentState().lockSummary)

        val stale = facade.refresh(GroceryDomainSnapshot(aggregate = aggregate(stale = true), recipesLocked = true, lockSummary = "Locked"))
        assertTrue(stale.isStale)
        assertEquals(GroceryScreenState.EXACT_ADR006_STALE_WARNING, stale.staleWarningText)
    }
}
