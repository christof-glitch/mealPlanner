package com.mealplanner.data.persistence

import com.mealplanner.domain.model.ShoppingListItem
import com.mealplanner.domain.model.StorePresenceStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Integration stub/boundary test for shopping list/edit continuity and fallback metadata intent. */
class ShoppingListPersistenceBoundaryTest {
    @Test
    fun `shopping list persistence boundary preserves edits and unknown presence`() = runTest {
        val repo = InMemoryContinuityRepository()
        val items = listOf(
            ShoppingListItem("i1", "Tomato", checked = true, manuallyEdited = true),
            ShoppingListItem("i2", "Unknown item", presence = StorePresenceStatus.UNKNOWN, unresolved = true),
        )
        repo.saveShoppingList("p1", "l1", "s1", items)
        val loaded = repo.loadShoppingList("p1")
        assertEquals(2, loaded.size)
        assertTrue(loaded.first().manuallyEdited)
        assertEquals(StorePresenceStatus.UNKNOWN, loaded.last().presence)
    }
}
