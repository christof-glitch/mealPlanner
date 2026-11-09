package com.mealplanner.domain.shopping

import com.mealplanner.adapters.FallbackPresenceMappingBoundary
import com.mealplanner.domain.model.ApplyBatchPresenceToListRequest
import com.mealplanner.domain.model.ApplyPresenceStatus
import com.mealplanner.domain.model.MapIngredientsToStorePresenceBatchRequest
import com.mealplanner.domain.model.PresenceQueryItem
import com.mealplanner.domain.model.PresenceSource
import com.mealplanner.domain.model.ShoppingListItem
import com.mealplanner.domain.model.StorePresenceResult
import com.mealplanner.domain.model.StorePresenceStatus
import com.mealplanner.domain.model.StoreRef
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplyBatchPresenceToListTest {
    private val store = StoreRef(storeId = "s1", displayName = "Prisma X")

    private fun item(id: String, name: String, normalized: String = name.lowercase(), manuallyEdited: Boolean = false, unresolved: Boolean = false) =
        ShoppingListItem(
            itemId = id,
            displayName = name,
            normalizedName = normalized,
            manuallyEdited = manuallyEdited,
            unresolved = unresolved,
        )

    private fun projection(query: String, status: StorePresenceStatus) = StorePresenceResult(
        queryText = query,
        storeRef = store,
        status = status,
        source = PresenceSource.PRISMA,
    )

    @Test
    fun `C5 mapping stub returns name presence projections only with UNKNOWN fallback`() = runTest {
        val boundary = FallbackPresenceMappingBoundary()
        val result = boundary.mapIngredientsToStorePresenceBatch(
            MapIngredientsToStorePresenceBatchRequest(
                queryItems = listOf(PresenceQueryItem(queryText = "tomato", normalizedName = "tomato")),
                storeRef = store,
            ),
        )
        assertEquals(1, result.batchResult.results.size)
        val p = result.batchResult.results.first()
        assertEquals(StorePresenceStatus.UNKNOWN, p.status)
        assertEquals("tomato", p.queryText)
        assertEquals(store, p.storeRef)
        // Name/presence only: no stock/guarantee fields exist on StorePresenceResult.
        assertTrue(result.batchResult.overallStatus.name in setOf("COMPLETE", "PARTIAL", "FAILED"))
    }

    @Test
    fun `C4 applies mapped projections to matching items`() {
        val result = ShoppingListRules.applyBatchPresenceToList(
            ApplyBatchPresenceToListRequest(
                items = listOf(item("1", "Tomato", "tomato")),
                projections = listOf(projection("tomato", StorePresenceStatus.PRESENT)),
                storeRef = store,
            ),
        )
        assertEquals(ApplyPresenceStatus.APPLIED, result.status)
        assertEquals(StorePresenceStatus.PRESENT, result.items.first().presence)
        assertEquals(0, result.unknownOrRemapCount)
    }

    @Test
    fun `C4 never fabricates presence when projection is missing`() {
        val result = ShoppingListRules.applyBatchPresenceToList(
            ApplyBatchPresenceToListRequest(
                items = listOf(item("1", "Tomato", "tomato")),
                projections = emptyList(),
                storeRef = store,
            ),
        )
        assertEquals(ApplyPresenceStatus.FAILED, result.status)
        assertEquals(StorePresenceStatus.UNKNOWN, result.items.first().presence)
        assertTrue(result.unknownOrRemapCount >= 1)
    }

    @Test
    fun `ambiguous mapping becomes UNKNOWN after edit or merge context`() {
        val result = ShoppingListRules.applyBatchPresenceToList(
            ApplyBatchPresenceToListRequest(
                items = listOf(item("1", "Tomato", "tomato", manuallyEdited = true)),
                projections = listOf(
                    projection("tomato", StorePresenceStatus.PRESENT),
                    projection("tomato", StorePresenceStatus.NOT_FOUND),
                ),
                storeRef = store,
            ),
        )
        assertEquals(StorePresenceStatus.UNKNOWN, result.items.first().presence)
        assertTrue(result.items.first().unresolved)
    }

    @Test
    fun `manually edited item without mapping becomes UNKNOWN`() {
        val result = ShoppingListRules.applyBatchPresenceToList(
            ApplyBatchPresenceToListRequest(
                items = listOf(item("1", "Custom item", "custom item", manuallyEdited = true)),
                projections = listOf(projection("tomato", StorePresenceStatus.PRESENT)),
                storeRef = store,
            ),
        )
        assertEquals(StorePresenceStatus.UNKNOWN, result.items.first().presence)
        assertEquals("UNKNOWN_AFTER_EDIT_NO_MAPPING", result.items.first().unresolvedReason)
    }

    @Test
    fun `unresolved merged item is not upgraded to fabricated present`() {
        val result = ShoppingListRules.applyBatchPresenceToList(
            ApplyBatchPresenceToListRequest(
                items = listOf(item("1", "Herb mix", "herb mix", unresolved = true)),
                projections = listOf(projection("herb mix", StorePresenceStatus.PRESENT)),
                storeRef = store,
            ),
        )
        assertEquals(StorePresenceStatus.UNKNOWN, result.items.first().presence)
        assertTrue(result.items.first().unresolved)
    }
}
