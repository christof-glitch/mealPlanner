package com.mealplanner.domain.shopping

import com.mealplanner.domain.model.IngredientAvailabilityStatus
import com.mealplanner.domain.model.IngredientLine
import com.mealplanner.domain.model.StorePresenceStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShoppingListGenerationRulesTest {
    @Test
    fun `compatible ingredients merge by normalized name and compatible unit`() {
        val lines = listOf(
            IngredientLine(name = "Tomato", normalizedName = "tomato", amount = 2.0, unit = "pcs", sourceRecipeId = "r1"),
            IngredientLine(name = "tomato", amount = 3.0, unit = "pcs", sourceRecipeId = "r2"),
        )
        val (items, unresolved) = ShoppingListRules.resolveIngredientLines(lines)
        assertTrue(unresolved.isEmpty())
        assertEquals(1, items.size)
        assertEquals(5.0, items.first().amount)
        assertEquals("pcs", items.first().unit)
        assertEquals(setOf("r1", "r2"), items.first().sourceRecipeIds.toSet())
    }

    @Test
    fun `incompatible units keep items separate without fabricating quantities`() {
        val lines = listOf(
            IngredientLine(name = "Milk", amount = 1.0, unit = "l", sourceRecipeId = "r1"),
            IngredientLine(name = "Milk", amount = 2.0, unit = "dl", sourceRecipeId = "r2"),
        )
        val (items, unresolved) = ShoppingListRules.resolveIngredientLines(lines)
        assertTrue(unresolved.isEmpty())
        assertEquals(2, items.size)
        assertTrue(items.none { it.amount == 3.0 || it.amount == 1.2 })
    }

    @Test
    fun `missing or changed ingredient data becomes explicit unresolved without fabrication`() {
        val lines = listOf(
            IngredientLine(name = "Mystery spice", amount = null, unit = null, availabilityStatus = IngredientAvailabilityStatus.MISSING, sourceRecipeId = "r1"),
            IngredientLine(name = "Onion", amount = null, unit = null, availabilityStatus = IngredientAvailabilityStatus.CHANGED, sourceRecipeId = "r2"),
            IngredientLine(name = "Carrot", amount = 1.0, unit = "pcs", availabilityStatus = IngredientAvailabilityStatus.KNOWN, sourceRecipeId = "r3"),
        )
        val (items, unresolved) = ShoppingListRules.resolveIngredientLines(lines)
        assertEquals(2, unresolved.size)
        assertTrue(unresolved.any { it.name == "Mystery spice" && it.amount == null && it.unit == null })
        assertTrue(unresolved.any { it.availabilityStatus == IngredientAvailabilityStatus.CHANGED })
        assertEquals(1, items.size)
        assertEquals("Carrot", items.first().displayName)
    }

    @Test
    fun `blank name known ingredient is not fabricated into resolved item`() {
        val lines = listOf(
            IngredientLine(name = " ", availabilityStatus = IngredientAvailabilityStatus.KNOWN, sourceRecipeId = "r1"),
        )
        val (items, unresolved) = ShoppingListRules.resolveIngredientLines(lines)
        assertTrue(items.isEmpty())
        assertEquals(1, unresolved.size)
        assertEquals(IngredientAvailabilityStatus.MISSING, unresolved.first().availabilityStatus)
    }

    @Test
    fun `known resolvable items do not invent amount or unit`() {
        val lines = listOf(
            IngredientLine(name = "Salt", amount = null, unit = null, amountText = "to taste", availabilityStatus = IngredientAvailabilityStatus.KNOWN, sourceRecipeId = "r1"),
        )
        val (items, unresolved) = ShoppingListRules.resolveIngredientLines(lines)
        assertTrue(unresolved.isEmpty())
        assertEquals(1, items.size)
        assertNull(items.first().amount)
        assertNull(items.first().unit)
        assertEquals("to taste", items.first().amountText)
    }

    @Test
    fun `same normalized name with missing amounts is kept separate and explicit`() {
        val lines = listOf(
            IngredientLine(name = "Herb mix", amount = null, unit = null, availabilityStatus = IngredientAvailabilityStatus.KNOWN, sourceRecipeId = "r1"),
            IngredientLine(name = "herb  mix", amount = null, unit = null, availabilityStatus = IngredientAvailabilityStatus.KNOWN, sourceRecipeId = "r2"),
        )
        val (items, _) = ShoppingListRules.resolveIngredientLines(lines)
        assertEquals(2, items.size)
        assertTrue(items.all { it.amount == null })
    }
}
