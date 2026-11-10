package com.mealplanner.domain.recipe

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CuratedRecipeCatalogTest {
    @Test
    fun summariesAndDetailsHaveUniqueIds() {
        val summaries = CuratedRecipeCatalog.summaries()
        assertTrue(summaries.isNotEmpty())
        val ids = summaries.map { it.recipeId }
        assertEquals(ids.size, ids.toSet().size)
        assertEquals(summaries.size, CuratedRecipeCatalog.details().size)
    }

    @Test
    fun breakfastHeuristicDetectsBreakfastTitles() {
        val breakfast = CuratedRecipeCatalog.summaries().first { it.title.contains("Oat", ignoreCase = true) }
        assertTrue(CuratedRecipeCatalog.isBreakfastTitle(breakfast.title))
        val dinner = CuratedRecipeCatalog.summaries().first { it.title.contains("Salmon", ignoreCase = true) }
        assertTrue(!CuratedRecipeCatalog.isBreakfastTitle(dinner.title))
    }
}
