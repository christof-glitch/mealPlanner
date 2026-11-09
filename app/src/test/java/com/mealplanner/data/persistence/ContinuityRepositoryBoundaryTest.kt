package com.mealplanner.data.persistence

import com.mealplanner.domain.model.LockSnapshot
import com.mealplanner.domain.model.PlanSnapshotContext
import com.mealplanner.domain.model.ShoppingListItem
import com.mealplanner.domain.planning.WeekdayEligibilityEvaluator
import java.time.Instant
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration stub/boundary test: runnable in local JVM with in-memory fake repository.
 * Exercises continuity contract (Room/DataStore boundary intent) without Android device/framework.
 */
class ContinuityRepositoryBoundaryTest {
    @Test
    fun `saving and loading plan preserves continuity metadata`() = runTest {
        val repo = InMemoryContinuityRepository()
        val plan = WeekdayEligibilityEvaluator.createEmptyPlan("plan-r").copy(weekStartContext = "2026-W45")
        repo.savePlan(plan)
        val loaded = repo.loadPlan("plan-r")
        assertEquals(plan.planId, loaded?.planId)
        assertEquals("2026-W45", loaded?.weekStartContext)
        assertEquals(16, loaded?.slots?.size)
    }

    @Test
    fun `missing plan returns explicit null not fabricated state`() = runTest {
        val repo = InMemoryContinuityRepository()
        assertNull(repo.loadPlan("missing"))
    }
}

class InMemoryContinuityRepository : ContinuityRepository {
    private val plans = mutableMapOf<String, com.mealplanner.domain.model.WeeklyPlanState>()
    private val snapshots = mutableMapOf<String, LockSnapshot>()
    private val lists = mutableMapOf<String, List<ShoppingListItem>>()
    private var store: String? = null
    private var season: String? = null
    override suspend fun loadPlan(planId: String) = plans[planId]
    override suspend fun savePlan(plan: com.mealplanner.domain.model.WeeklyPlanState) { plans[plan.planId] = plan }
    override suspend fun loadSnapshot(snapshotId: String) = snapshots[snapshotId]
    override suspend fun saveSnapshot(snapshot: LockSnapshot) { snapshots[snapshot.snapshotId] = snapshot }
    override suspend fun loadShoppingList(planId: String) = lists[planId].orEmpty()
    override suspend fun saveShoppingList(planId: String, shoppingListId: String, snapshotId: String?, items: List<ShoppingListItem>, statusMetadataJson: String?) { lists[planId] = items }
    override suspend fun getSelectedStore() = store
    override suspend fun setSelectedStore(storeDisplayName: String?) { store = storeDisplayName }
    override suspend fun getSeasonOverride() = season
    override suspend fun setSeasonOverride(override: String?) { season = override }
}
