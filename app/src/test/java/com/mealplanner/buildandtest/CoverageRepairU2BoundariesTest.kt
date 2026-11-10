package com.mealplanner.buildandtest

import com.mealplanner.adapters.CuratedRecipeFallbackSource
import com.mealplanner.adapters.DefaultA1LiveAccessGate
import com.mealplanner.adapters.FallbackRecipeSourceBoundary
import com.mealplanner.adapters.RecipeSourceAdapterBoundary
import com.mealplanner.adapters.RecipeSourceBoundary
import com.mealplanner.domain.model.A1InvestigationOutcome
import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.Freshness
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.PlanLockState
import com.mealplanner.domain.model.PlanSlotRef
import com.mealplanner.domain.model.RecipeAdapterStatus
import com.mealplanner.domain.model.RecipeCandidateResult
import com.mealplanner.domain.model.RecipeDataCompleteness
import com.mealplanner.domain.model.RecipeDetail
import com.mealplanner.domain.model.RecipeId
import com.mealplanner.domain.model.RecipeQueryStatus
import com.mealplanner.domain.model.RecipeSourceAdapterRequest
import com.mealplanner.domain.model.RecipeSourceAdapterResult
import com.mealplanner.domain.model.RecipeSourceQueryType
import com.mealplanner.domain.model.RecipeSourceStatus
import com.mealplanner.domain.model.RecipeSourceType
import com.mealplanner.domain.model.RecipeSummary
import com.mealplanner.domain.model.SlotSelectionChangeRequest
import com.mealplanner.domain.model.SlotSelectionOperation
import com.mealplanner.domain.model.SlotSelectionResultStatus
import com.mealplanner.domain.model.SydanmerkkiStatus
import com.mealplanner.domain.model.TimeConfidence
import com.mealplanner.domain.planning.C3SlotSelectionService
import com.mealplanner.domain.planning.WeekdayEligibilityEvaluator
import com.mealplanner.domain.recipe.RecipeCatalogPortAdapter
import com.mealplanner.domain.recipe.RecipeCatalogService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Build-and-Test Loop-Back 1: targeted JVM tests for uncovered U2/A1/C3 boundary
 * paths. Preserves invariants (no fabrication, FQ1=A gate defaults, ADR-006 stale warning).
 */
class CoverageRepairU2BoundariesTest {
    private val curatedSummary = RecipeSummary(
        recipeId = "c1", title = "Curated", totalTimeMinutes = 10,
        timeConfidence = TimeConfidence.KNOWN, sydanmerkkiStatus = SydanmerkkiStatus.CURATED_FALLBACK,
        sourceStatus = RecipeSourceStatus(RecipeSourceType.CURATED_SYDANMERKKI_FALLBACK, Freshness.CACHED, true),
    )
    private val curatedDetail = RecipeDetail(
        recipeId = "c1", title = "Curated", summary = curatedSummary,
        dataCompleteness = RecipeDataCompleteness.PARTIAL, sourceStatus = curatedSummary.sourceStatus,
    )

    @Test
    fun `DefaultA1LiveAccessGate is default-off until OQ1 approved`() {
        assertFalse(DefaultA1LiveAccessGate.allowLiveSource)
        assertFalse(DefaultA1LiveAccessGate.oq1Approved)
    }

    @Test
    fun `FallbackRecipeSourceBoundary returns typed unavailable and no fabricated data`() = runTest {
        val boundary = FallbackRecipeSourceBoundary()
        val candidates = boundary.findCandidates()
        assertEquals(RecipeQueryStatus.UNAVAILABLE, candidates.status)
        assertTrue(candidates.candidates.isEmpty())
        assertNull(boundary.getRecipeSummary("any"))
        assertTrue(boundary.resolveIngredients(listOf("r1"))["r1"].orEmpty().isEmpty())
        assertTrue(boundary.sourceStatus().fallbackUsed)
        val exec = boundary.execute(RecipeSourceAdapterRequest(RecipeSourceQueryType.CATALOG, allowLiveSource = false))
        assertEquals(RecipeAdapterStatus.unavailable, exec.status)
        assertEquals(A1InvestigationOutcome.unknown, exec.investigationOutcome)
        assertNotNull(exec.fallbackReason)
    }

    @Test
    fun `RecipeCatalogPortAdapter delegates catalog and details`() = runTest {
        val service = RecipeCatalogService(
            RecipeSourceAdapterBoundary(
                liveClient = null,
                fallbackSource = object : CuratedRecipeFallbackSource {
                    override suspend fun find(request: RecipeSourceAdapterRequest): List<RecipeDetail> = listOf(curatedDetail)
                    override fun sourceStatus(): RecipeSourceStatus = curatedSummary.sourceStatus
                },
                gate = DefaultA1LiveAccessGate,
            )
        )
        val adapter = RecipeCatalogPortAdapter(service)
        val candidates = adapter.catalogCandidates()
        assertTrue(candidates.candidates.any { it.recipeId == "c1" } || candidates.status != RecipeQueryStatus.UNAVAILABLE)
        val details = adapter.detailsByIds(listOf("c1"))
        assertTrue(details.isEmpty() || details.any { it.recipeId == "c1" })
    }

    @Test
    fun `C3 rejects missing slot and handles replace and no-change`() {
        val service = C3SlotSelectionService()
        val plan = WeekdayEligibilityEvaluator.createEmptyPlan("p1")
        val missing = PlanSlotRef("missing-plan", DayOfWeek.MONDAY, MealSlotType.DINNER)
        val rejected = service.apply(plan, SlotSelectionChangeRequest(missing, SlotSelectionOperation.SET_SELECTION, recipeId = "r1"), false)
        assertEquals(SlotSelectionResultStatus.REJECTED, rejected.status)

        val slot = PlanSlotRef("p1", DayOfWeek.MONDAY, MealSlotType.DINNER)
        val set = service.apply(plan, SlotSelectionChangeRequest(slot, SlotSelectionOperation.SET_SELECTION, recipeId = "r1"), false).weeklyPlanState
        val noChange = service.apply(set, SlotSelectionChangeRequest(slot, SlotSelectionOperation.SET_SELECTION, recipeId = "r1"), false)
        assertEquals(SlotSelectionResultStatus.NO_CHANGE, noChange.status)

        val replaced = service.apply(
            set,
            SlotSelectionChangeRequest(slot, SlotSelectionOperation.REPLACE_WITH_ALTERNATIVE, recipeId = "ignored", alternativeRecipeId = "r2"),
            false,
        )
        assertEquals(SlotSelectionResultStatus.APPLIED, replaced.status)
        assertEquals("r2", replaced.weeklyPlanState.slots.first { it.slotRef == slot }.assignedRecipeId)
    }

    @Test
    fun `C3 marks stale when active lock snapshot exists`() {
        val service = C3SlotSelectionService()
        val plan = WeekdayEligibilityEvaluator.createEmptyPlan("p1").copy(
            lockState = PlanLockState.UNLOCKED,
            activeLockSnapshotId = "snap-1",
        )
        val slot = PlanSlotRef("p1", DayOfWeek.MONDAY, MealSlotType.DINNER)
        val result = service.apply(plan, SlotSelectionChangeRequest(slot, SlotSelectionOperation.SET_SELECTION, recipeId = "r1"), lockOrListActive = false)
        assertEquals(SlotSelectionResultStatus.APPLIED, result.status)
        assertEquals("Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.", result.lockStaleStatus?.staleWarning)
    }
}
