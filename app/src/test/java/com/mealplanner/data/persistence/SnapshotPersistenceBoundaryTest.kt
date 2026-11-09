package com.mealplanner.data.persistence

import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.LockSnapshot
import com.mealplanner.domain.model.LockedRecipeSelection
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.PlanSlotRef
import com.mealplanner.domain.model.PlanSnapshotContext
import java.time.Instant
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Integration stub/boundary test for snapshot continuity (ADR-006 IDs/selections+context). */
class SnapshotPersistenceBoundaryTest {
    @Test
    fun `snapshot persistence boundary preserves recipe selections and context`() = runTest {
        val repo = InMemoryContinuityRepository()
        val snapshot = LockSnapshot(
            snapshotId = "s1",
            planId = "p1",
            lockedRecipeSelections = listOf(LockedRecipeSelection(PlanSlotRef("p1", DayOfWeek.MONDAY, MealSlotType.DINNER), "r1")),
            planContext = PlanSnapshotContext(schedulePolicyVersion = "v1"),
            createdAt = Instant.parse("2026-11-08T00:00:00Z"),
        )
        repo.saveSnapshot(snapshot)
        val loaded = repo.loadSnapshot("s1")
        assertEquals(1, loaded?.lockedRecipeSelections?.size)
        assertEquals("v1", loaded?.planContext?.schedulePolicyVersion)
    }
}
