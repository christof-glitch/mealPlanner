package com.mealplanner.data.persistence

import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.LockedRecipeSelection
import com.mealplanner.domain.model.LockSnapshot
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.PlanLockState
import com.mealplanner.domain.model.PlanSlotRef
import com.mealplanner.domain.model.PlanSnapshotContext
import com.mealplanner.domain.model.SelectionSource
import com.mealplanner.domain.model.ShoppingListItem
import com.mealplanner.domain.model.StorePresenceStatus
import com.mealplanner.domain.model.StoreRef
import com.mealplanner.domain.planning.WeekdayEligibilityEvaluator
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PlanPersistenceMapperTest {
    @Test
    fun `weekly plan round trip preserves lock state slots and stale warning`() {
        val plan = WeekdayEligibilityEvaluator.createEmptyPlan("plan-p").copy(
            lockState = PlanLockState.STALE_LOCK,
            activeLockSnapshotId = "snap-1",
            staleWarning = com.mealplanner.domain.planning.Adr006LockRules.STALE_WARNING_TEXT,
            lockedRecipeIds = listOf("r1"),
        )
        val encoded = SimpleContinuityMappers.encodePlan(plan, Instant.parse("2026-11-08T00:00:00Z"))
        val decoded = SimpleContinuityMappers.decodePlan(encoded)
        assertEquals(plan.planId, decoded.planId)
        assertEquals(PlanLockState.STALE_LOCK, decoded.lockState)
        assertEquals(plan.staleWarning, decoded.staleWarning)
        assertEquals(16, decoded.slots.size)
        assertEquals(listOf("r1"), decoded.lockedRecipeIds)
    }

    @Test
    fun `lock snapshot round trip preserves selections and context without ingredient freeze`() {
        val snapshot = LockSnapshot(
            snapshotId = "snap-2",
            planId = "plan-p",
            lockedRecipeSelections = listOf(
                LockedRecipeSelection(PlanSlotRef("plan-p", DayOfWeek.MONDAY, MealSlotType.DINNER), "r1", SelectionSource.USER_SELECTED),
            ),
            planContext = PlanSnapshotContext(selectedStoreRef = StoreRef(displayName = "Prisma A"), schedulePolicyVersion = "v1"),
            createdAt = Instant.parse("2026-11-08T01:00:00Z"),
        )
        val decoded = SimpleContinuityMappers.decodeSnapshot(SimpleContinuityMappers.encodeSnapshot(snapshot))
        assertEquals(snapshot.snapshotId, decoded.snapshotId)
        assertEquals(1, decoded.lockedRecipeSelections.size)
        assertEquals("r1", decoded.lockedRecipeSelections.first().recipeId)
        assertEquals("Prisma A", decoded.planContext.selectedStoreRef?.displayName)
        // No ingredient payload encoded - ADR-006/R-01.
        assertNotNull(decoded.planContext)
    }

    @Test
    fun `shopping items round trip preserves edits presence and unresolved state`() {
        val items = listOf(
            ShoppingListItem("i1", "Tomato", normalizedName = "tomato", amount = 2.0, unit = "pcs", checked = true, sourceRecipeIds = listOf("r1"), presence = StorePresenceStatus.PRESENT, manuallyEdited = true),
            ShoppingListItem("i2", "Mystery", unresolved = true, unresolvedReason = "MISSING_DATA"),
        )
        val decoded = SimpleContinuityMappers.decodeShoppingItems(SimpleContinuityMappers.encodeShoppingItems(items))
        assertEquals(2, decoded.size)
        assertTrue(decoded[0].checked)
        assertTrue(decoded[0].manuallyEdited)
        assertEquals(StorePresenceStatus.PRESENT, decoded[0].presence)
        assertTrue(decoded[1].unresolved)
        assertEquals("MISSING_DATA", decoded[1].unresolvedReason)
    }
}
