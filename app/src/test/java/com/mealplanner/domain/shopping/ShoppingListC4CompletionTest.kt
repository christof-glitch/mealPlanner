package com.mealplanner.domain.shopping

import com.mealplanner.domain.model.ApplyPresenceCheckBatchRequest
import com.mealplanner.domain.model.ApplyPresenceStatus
import com.mealplanner.domain.model.BatchPresenceResult
import com.mealplanner.domain.model.IngredientAvailabilityStatus
import com.mealplanner.domain.model.IngredientLine
import com.mealplanner.domain.model.PresenceBatchOverallStatus
import com.mealplanner.domain.model.PresenceSource
import com.mealplanner.domain.model.ShoppingItemPatch
import com.mealplanner.domain.model.ShoppingItemPatchOperation
import com.mealplanner.domain.model.ShoppingListAggregate
import com.mealplanner.domain.model.ShoppingListGenerationOperation
import com.mealplanner.domain.model.ShoppingListGenerationOutcome
import com.mealplanner.domain.model.ShoppingListGenerationRequest
import com.mealplanner.domain.model.ShoppingListItem
import com.mealplanner.domain.model.ShoppingListPatchRequest
import com.mealplanner.domain.model.ShoppingListStaleStatus
import com.mealplanner.domain.model.ShoppingListStaleWarning
import com.mealplanner.domain.model.StorePresenceResult
import com.mealplanner.domain.model.StorePresenceStatus
import com.mealplanner.domain.model.StoreRef
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShoppingListC4CompletionTest {
    private val store = StoreRef(storeId = "prisma-1", displayName = "Prisma Test")

    @Test
    fun `exact stale warning constant matches ADR-006`() {
        assertEquals(
            "Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.",
            ShoppingListStaleWarning.EXACT_TEXT,
        )
        val stale = ShoppingListStaleStatus(isStale = true, snapshotId = "snap-1")
        assertEquals(ShoppingListStaleWarning.EXACT_TEXT, stale.staleWarning)
    }

    @Test
    fun `generation request carries locked snapshot and confirmation semantics`() {
        val request = ShoppingListGenerationRequest(
            snapshotId = "snap-locked-1",
            operation = ShoppingListGenerationOperation.REGENERATE,
            confirmRegenerateIfEdited = false,
        )
        assertEquals("snap-locked-1", request.snapshotId)
        assertFalse(request.confirmRegenerateIfEdited)
    }

    @Test
    fun `edited aggregate requires confirmation before destructive rebuild intent`() {
        val aggregate = ShoppingListAggregate(
            shoppingListId = "list-1",
            snapshotId = "snap-1",
            hasUserEdits = true,
            items = listOf(ShoppingListItem(itemId = "i1", displayName = "Tomato", checked = true, manuallyEdited = true)),
        )
        val outcome = ShoppingListRules.evaluateRegenerationRequest(
            current = aggregate,
            request = ShoppingListGenerationRequest(snapshotId = "snap-1", operation = ShoppingListGenerationOperation.REGENERATE),
        )
        assertEquals(ShoppingListGenerationOutcome.CONFIRMATION_REQUIRED, outcome.status)
        assertEquals(aggregate, outcome.shoppingList)
    }

    @Test
    fun `applyPresenceCheckBatch remaps ambiguous identity to UNKNOWN with reason and preserves unrelated edits`() {
        val aggregate = ShoppingListAggregate(
            shoppingListId = "list-1",
            selectedStoreRef = store,
            items = listOf(
                ShoppingListItem(itemId = "i1", displayName = "Tomato", normalizedName = "tomato", presenceQueryKey = "tomato", checked = true, manuallyEdited = true),
                ShoppingListItem(itemId = "i2", displayName = "Milk", normalizedName = "milk", presenceQueryKey = "milk", checked = false),
            ),
        )
        val batch = BatchPresenceResult(
            storeRef = store,
            results = listOf(
                StorePresenceResult(queryText = "tomato", storeRef = store, status = StorePresenceStatus.PRESENT, source = PresenceSource.PRISMA),
                StorePresenceResult(queryText = "milk", storeRef = store, status = StorePresenceStatus.NOT_FOUND, source = PresenceSource.PRISMA),
            ),
            overallStatus = PresenceBatchOverallStatus.COMPLETE,
        )
        val edited = aggregate.copy(
            items = aggregate.items.map { if (it.itemId == "i1") it.copy(displayName = "Tomato diced", displayText = "Tomato diced", normalizedName = "tomato diced", presenceQueryKey = "tomato diced", manuallyEdited = true, userEdited = true) else it },
            hasUserEdits = true,
        )
        val result = ShoppingListRules.applyPresenceCheckBatch(
            ApplyPresenceCheckBatchRequest(
                shoppingListId = "list-1",
                storeId = store.storeId,
                shoppingList = edited,
                batch = batch,
                storeRef = store,
            ),
        )
        val tomato = result.shoppingList?.items?.first { it.itemId == "i1" }
        val milk = result.shoppingList?.items?.first { it.itemId == "i2" }
        assertNotNull(tomato)
        assertNotNull(milk)
        assertEquals(StorePresenceStatus.UNKNOWN, tomato.availabilityStatus)
        assertTrue(tomato.checked)
        assertEquals(StorePresenceStatus.NOT_FOUND, milk.availabilityStatus)
        assertTrue(tomato.unresolved || tomato.availabilityReason != null)
    }

    @Test
    fun `missing or changed source data is explicit and never fabricated`() {
        val lines = listOf(
            IngredientLine(name = "Changed item", availabilityStatus = IngredientAvailabilityStatus.CHANGED, sourceRecipeId = "r1"),
            IngredientLine(name = "Known", amount = 1.0, unit = "pcs", availabilityStatus = IngredientAvailabilityStatus.KNOWN, sourceRecipeId = "r2"),
        )
        val (items, unresolved) = ShoppingListRules.resolveIngredientLines(lines)
        assertEquals(1, items.size)
        assertEquals(1, unresolved.size)
        assertEquals(IngredientAvailabilityStatus.CHANGED, unresolved.first().availabilityStatus)
        assertTrue(items.none { it.displayName.contains("Changed") })
    }

    @Test
    fun `patch operations mark aggregate as user edited`() {
        val aggregate = ShoppingListAggregate(
            shoppingListId = "list-1",
            items = listOf(ShoppingListItem(itemId = "i1", displayName = "Tomato")),
        )
        val patched = ShoppingListRules.applyItemPatch(
            aggregate,
            ShoppingListPatchRequest(patch = ShoppingItemPatch(itemId = "i1", operation = ShoppingItemPatchOperation.SET_CHECKED, checked = true)),
        )
        assertTrue(patched.hasUserEdits)
        assertTrue(patched.items.first().checked)
        assertNotNull(patched.lastEditedAt)
    }
}
