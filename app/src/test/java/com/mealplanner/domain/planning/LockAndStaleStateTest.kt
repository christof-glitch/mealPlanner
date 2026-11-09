package com.mealplanner.domain.planning

import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.GenerationStatus
import com.mealplanner.domain.model.IngredientAvailabilityStatus
import com.mealplanner.domain.model.IngredientLine
import com.mealplanner.domain.model.LockSnapshot
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.PlanLockState
import com.mealplanner.domain.model.PlanSlotRef
import com.mealplanner.domain.model.PlanSlotState
import com.mealplanner.domain.model.PlanSnapshotContext
import com.mealplanner.domain.model.SlotStatus
import com.mealplanner.domain.model.WeeklyPlanState
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LockAndStaleStateTest {
    private val service = PlanLockService()
    private val now = Instant.parse("2026-11-08T10:00:00Z")

    private fun planWithSelection(): WeeklyPlanState {
        val base = WeekdayEligibilityEvaluator.createEmptyPlan("plan-lock")
        val slot = base.slots.first().slotRef
        return base.copy(
            slots = base.slots.map {
                if (it.slotRef == slot) it.copy(assignedRecipeId = "recipe-1", slotStatus = SlotStatus.SELECTED) else it
            },
        )
    }

    @Test
    fun `lock snapshot stores IDs selections and context only`() {
        val (locked, snapshot) = service.lockPlan(
            planWithSelection(),
            now = now,
            planContext = PlanSnapshotContext(selectedStoreRef = com.mealplanner.domain.model.StoreRef(displayName = "Prisma Test")),
            snapshotId = "snap-1",
        )
        assertEquals("snap-1", snapshot.snapshotId)
        assertEquals("plan-lock", snapshot.planId)
        assertEquals(1, snapshot.lockedRecipeSelections.size)
        assertEquals("recipe-1", snapshot.lockedRecipeSelections.first().recipeId)
        assertEquals("Prisma Test", snapshot.planContext.selectedStoreRef?.displayName)
        assertEquals(PlanLockState.LOCKED, locked.lockState)
        assertEquals("snap-1", locked.activeLockSnapshotId)
        // ADR-006/R-01: no ingredient freeze field exists; snapshot carries selections+context only.
        assertTrue(Adr006LockRules.snapshotHasNoFrozenIngredients(snapshot))
    }

    @Test
    fun `plan change after lock marks STALE_LOCK with exact warning text`() {
        val (locked, _) = service.lockPlan(planWithSelection(), now = now)
        val stale = service.markPlanChanged(locked)
        assertEquals(PlanLockState.STALE_LOCK, stale.lockState)
        assertEquals(Adr006LockRules.STALE_WARNING_TEXT, stale.staleWarning)
        assertEquals(
            "Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.",
            stale.staleWarning,
        )
    }

    @Test
    fun `unlocked plan without snapshot is not marked stale`() {
        val plan = WeekdayEligibilityEvaluator.createEmptyPlan("p")
        val result = service.markPlanChanged(plan)
        assertEquals(PlanLockState.UNLOCKED, result.lockState)
        assertNull(result.staleWarning)
    }

    @Test
    fun `generate requires explicit locked snapshot`() {
        val plan = planWithSelection()
        val result = service.generateShoppingList(
            plan = plan,
            snapshot = null,
            hasExistingUserEdits = false,
            confirmedReplaceEdits = false,
            isRegenerate = false,
            resolvedItems = emptyList(),
        )
        assertEquals(GenerationStatus.FAILED, result.status)
        assertEquals("LOCK_AND_EXPLICIT_GENERATE_REQUIRED", result.userActionRequired)
    }

    @Test
    fun `explicit generate with locked snapshot succeeds`() {
        val (locked, snapshot) = service.lockPlan(planWithSelection(), now = now)
        val result = service.generateShoppingList(
            plan = locked,
            snapshot = snapshot,
            hasExistingUserEdits = false,
            confirmedReplaceEdits = false,
            isRegenerate = false,
            resolvedItems = emptyList(),
        )
        assertEquals(GenerationStatus.GENERATED, result.status)
        assertEquals(snapshot.snapshotId, result.snapshotId)
    }

    @Test
    fun `regenerate with existing edits requires explicit confirmation`() {
        val (locked, snapshot) = service.lockPlan(planWithSelection(), now = now)
        val stale = Adr006LockRules.markPlanStale(locked)
        val result = service.generateShoppingList(
            plan = stale,
            snapshot = snapshot,
            hasExistingUserEdits = true,
            confirmedReplaceEdits = false,
            isRegenerate = true,
            resolvedItems = emptyList(),
        )
        assertEquals(GenerationStatus.NEEDS_CONFIRMATION, result.status)
        assertEquals("CONFIRM_REPLACE_EDITED_LIST", result.userActionRequired)
        assertNotNull(result.fallbackReason)
    }

    @Test
    fun `regenerate with edits succeeds only after explicit confirmation`() {
        val (locked, snapshot) = service.lockPlan(planWithSelection(), now = now)
        val result = service.generateShoppingList(
            plan = locked,
            snapshot = snapshot,
            hasExistingUserEdits = true,
            confirmedReplaceEdits = true,
            isRegenerate = true,
            resolvedItems = emptyList(),
        )
        assertEquals(GenerationStatus.REGENERATED, result.status)
    }

    @Test
    fun `stale state does not auto regenerate`() {
        val (locked, snapshot) = service.lockPlan(planWithSelection(), now = now)
        val stale = service.markPlanChanged(locked)
        // Domain exposes no auto-regenerate path; explicit call still requires confirmation when edits exist.
        val result = service.generateShoppingList(stale, snapshot, true, false, true, emptyList())
        assertEquals(GenerationStatus.NEEDS_CONFIRMATION, result.status)
        assertFalse(result.status == GenerationStatus.REGENERATED)
    }
}
