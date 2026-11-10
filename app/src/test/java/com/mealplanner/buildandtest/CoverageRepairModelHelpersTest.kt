package com.mealplanner.buildandtest

import com.mealplanner.domain.model.PrismaStoreRef
import com.mealplanner.domain.model.ShoppingListItem
import com.mealplanner.domain.model.StoreChain
import com.mealplanner.domain.model.effectiveDisplayText
import com.mealplanner.domain.model.effectiveQuantityText
import com.mealplanner.domain.model.toStoreRef
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CoverageRepairModelHelpersTest {
    @Test
    fun `shopping item effective helpers fall back`() {
        val item = ShoppingListItem(
            itemId = "i1",
            displayName = "Milk",
            displayText = "",
            quantityText = null,
            amountText = "1 l",
        )
        assertEquals("Milk", item.effectiveDisplayText())
        assertEquals("1 l", item.effectiveQuantityText())
        val item2 = item.copy(displayText = "Whole milk", quantityText = "2 l")
        assertEquals("Whole milk", item2.effectiveDisplayText())
        assertEquals("2 l", item2.effectiveQuantityText())
        val item3 = item.copy(displayText = "M", amountText = null, quantityText = null)
        assertNull(item3.effectiveQuantityText())
    }

    @Test
    fun `prisma store ref maps to store ref`() {
        val ref = PrismaStoreRef(storeId = "s1", displayName = "Prisma Sello")
        val mapped = ref.toStoreRef()
        assertEquals("s1", mapped.storeId)
        assertEquals("Prisma Sello", mapped.displayName)
        assertEquals(StoreChain.PRISMA, mapped.chain)
        val noName = PrismaStoreRef(storeId = "s2", displayName = null).toStoreRef()
        assertEquals("s2", noName.displayName)
    }
}
