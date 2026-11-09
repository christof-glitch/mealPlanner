package com.mealplanner.domain.shopping

import com.mealplanner.adapters.RecipeSourceBoundary
import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.Freshness
import com.mealplanner.domain.model.IngredientAvailabilityStatus
import com.mealplanner.domain.model.IngredientLine
import com.mealplanner.domain.model.LockSnapshot
import com.mealplanner.domain.model.LockedRecipeSelection
import com.mealplanner.domain.model.PlanSlotRef
import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.RecipeCandidateResult
import com.mealplanner.domain.model.RecipeId
import com.mealplanner.domain.model.RecipeQueryStatus
import com.mealplanner.domain.model.RecipeSourceStatus
import com.mealplanner.domain.model.RecipeSourceType
import com.mealplanner.domain.model.RecipeSummary
import com.mealplanner.domain.model.ShoppingListGenerationOperation
import com.mealplanner.domain.model.ShoppingListGenerationOutcome
import com.mealplanner.domain.model.ShoppingListGenerationRequest
import com.mealplanner.domain.model.ShoppingListStaleStatus
import com.mealplanner.domain.model.ShoppingListStaleWarning
import java.time.Instant
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ShoppingListServiceTest {
    private fun snapshot(vararg recipeIds: String) = LockSnapshot(
        snapshotId = "snap-1",
        planId = "plan-1",
        lockedRecipeSelections = recipeIds.mapIndexed { _, id ->
            LockedRecipeSelection(
                slotRef = PlanSlotRef(planId = "plan-1", dayOfWeek = DayOfWeek.MONDAY, mealType = MealSlotType.DINNER),
                recipeId = id,
            )
        },
        createdAt = Instant.parse("2026-11-09T10:00:00Z"),
    )

    private fun fakeResolver(
        resolved: Map<RecipeId, List<IngredientLine>>,
    ): RecipeSourceBoundary = object : RecipeSourceBoundary {
        private val status = RecipeSourceStatus(RecipeSourceType.CURATED_SYDANMERKKI_FALLBACK, Freshness.CURRENT, false)
        override suspend fun findCandidates(query: Map<String, String>) = RecipeCandidateResult(status = RecipeQueryStatus.UNAVAILABLE, sourceStatus = status)
        override suspend fun getRecipeSummary(recipeId: RecipeId): RecipeSummary? = null
        override suspend fun resolveIngredients(recipeIds: List<RecipeId>): Map<RecipeId, List<IngredientLine>> =
            recipeIds.associateWith { id -> resolved[id].orEmpty() }
        override fun sourceStatus(): RecipeSourceStatus = status
    }

    @Test
    fun `generate from locked snapshot re-resolves ingredients via port and never fabricates missing source`() = runTest {
        val service = ShoppingListService(fakeResolver(mapOf(
            "r1" to listOf(IngredientLine(name = "Tomato", amount = 2.0, unit = "pcs", sourceRecipeId = "r1")),
            "r2" to emptyList(), // missing/changed source => explicit, no fabrication
        )))
        val result = service.generateFromLockSnapshot(
            request = ShoppingListGenerationRequest(snapshotId = "snap-1"),
            snapshot = snapshot("r1", "r2"),
            staleStatus = ShoppingListStaleStatus(isStale = false),
        )
        assertEquals(ShoppingListGenerationOutcome.PARTIAL, result.status)
        assertNotNull(result.shoppingList)
        assertTrue(result.missingOrChangedRecipeIds.contains("r2"))
        assertTrue(result.shoppingList!!.items.any { it.displayName.contains("Tomato") })
        assertTrue(result.shoppingList!!.generationNotes.any { it.contains("r2") || it.contains("missing", true) || it.contains("changed", true) })
    }

    @Test
    fun `invalid snapshot returns typed INVALID_SNAPSHOT`() = runTest {
        val service = ShoppingListService(fakeResolver(emptyMap()))
        val result = service.generateFromLockSnapshot(
            request = ShoppingListGenerationRequest(snapshotId = "snap-missing"),
            snapshot = null,
            staleStatus = ShoppingListStaleStatus(),
        )
        assertEquals(ShoppingListGenerationOutcome.INVALID_SNAPSHOT, result.status)
        assertEquals(null, result.shoppingList)
    }

    @Test
    fun `regeneration with edits and no confirmation preserves list`() = runTest {
        val service = ShoppingListService(fakeResolver(mapOf("r1" to listOf(IngredientLine(name = "Tomato", sourceRecipeId = "r1")))))
        val generated = service.generateFromLockSnapshot(ShoppingListGenerationRequest("snap-1"), snapshot("r1"), ShoppingListStaleStatus())
        val edited = generated.shoppingList!!.copy(hasUserEdits = true)
        val result = service.regenerateFromLockSnapshot(
            request = ShoppingListGenerationRequest(snapshotId = "snap-1", operation = ShoppingListGenerationOperation.REGENERATE, confirmRegenerateIfEdited = false),
            snapshot = snapshot("r1"),
            currentList = edited,
            staleStatus = ShoppingListStaleStatus(isStale = true, staleWarning = ShoppingListStaleWarning.EXACT_TEXT),
        )
        assertEquals(ShoppingListGenerationOutcome.CONFIRMATION_REQUIRED, result.status)
        assertEquals(edited, result.shoppingList)
    }

    @Test
    fun `stale status is reflected without auto-regeneration`() = runTest {
        val service = ShoppingListService(fakeResolver(mapOf("r1" to listOf(IngredientLine(name = "Tomato", sourceRecipeId = "r1")))))
        val stale = ShoppingListStaleStatus(isStale = true, snapshotId = "snap-1")
        val result = service.generateFromLockSnapshot(ShoppingListGenerationRequest("snap-1"), snapshot("r1"), stale)
        assertEquals(ShoppingListStaleWarning.EXACT_TEXT, result.staleStatus.staleWarning)
        assertTrue(result.staleStatus.isStale)
    }

    @Test
    fun `deterministic merge does not fabricate incompatible quantities`() = runTest {
        val service = ShoppingListService(fakeResolver(mapOf(
            "r1" to listOf(IngredientLine(name = "Milk", amount = 1.0, unit = "l", sourceRecipeId = "r1")),
            "r2" to listOf(IngredientLine(name = "Milk", amount = 2.0, unit = "dl", sourceRecipeId = "r2")),
        )))
        val result = service.generateFromLockSnapshot(ShoppingListGenerationRequest("snap-1"), snapshot("r1", "r2"), ShoppingListStaleStatus())
        val milkItems = result.shoppingList!!.items.filter { it.normalizedName?.contains("milk") == true || it.displayName.contains("Milk", true) }
        assertEquals(2, milkItems.size)
        assertFalse(milkItems.any { it.amount == 3.0 })
    }

    @Test
    fun `export current state preparation uses edited aggregate`() = runTest {
        val service = ShoppingListService(fakeResolver(mapOf("r1" to listOf(IngredientLine(name = "Tomato", sourceRecipeId = "r1")))))
        val generated = service.generateFromLockSnapshot(ShoppingListGenerationRequest("snap-1"), snapshot("r1"), ShoppingListStaleStatus())
        val edited = generated.shoppingList!!.copy(
            hasUserEdits = true,
            items = generated.shoppingList!!.items.map { it.copy(displayName = "Tomato (edited)", displayText = "Tomato (edited)", checked = true, manuallyEdited = true, userEdited = true) },
        )
        val exportState = service.prepareCurrentExportState(edited)
        assertTrue(exportState.items.any { it.displayText.contains("edited") && it.checked })
        assertTrue(exportState.items.none { it.displayText == "Tomato" && !it.checked && edited.items.any { e -> e.displayText.contains("edited") } })
    }
}
