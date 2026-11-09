package com.mealplanner.data.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mealplanner.domain.model.LockSnapshot
import com.mealplanner.domain.model.ShoppingListItem
import com.mealplanner.domain.model.WeeklyPlanState
import kotlinx.coroutines.flow.first
import java.time.Instant

interface ContinuityRepository {
    suspend fun loadPlan(planId: String): WeeklyPlanState?
    suspend fun savePlan(plan: WeeklyPlanState)
    suspend fun loadSnapshot(snapshotId: String): LockSnapshot?
    suspend fun saveSnapshot(snapshot: LockSnapshot)
    suspend fun loadShoppingList(planId: String): List<ShoppingListItem>
    suspend fun saveShoppingList(planId: String, shoppingListId: String, snapshotId: String?, items: List<ShoppingListItem>, statusMetadataJson: String? = null)
    suspend fun getSelectedStore(): String?
    suspend fun setSelectedStore(storeDisplayName: String?)
    suspend fun getSeasonOverride(): String?
    suspend fun setSeasonOverride(override: String?)
}

class RoomAndDataStoreContinuityRepository(
    private val dao: MealPlannerDao,
    private val dataStore: DataStore<Preferences>,
) : ContinuityRepository {
    override suspend fun loadPlan(planId: String): WeeklyPlanState? = dao.getPlan(planId)?.let { SimpleContinuityMappers.decodePlan(it).copy(planId = planId) }
    override suspend fun savePlan(plan: WeeklyPlanState) { dao.upsertPlan(SimpleContinuityMappers.encodePlan(plan)) }
    override suspend fun loadSnapshot(snapshotId: String): LockSnapshot? = dao.getSnapshot(snapshotId)?.let(SimpleContinuityMappers::decodeSnapshot)
    override suspend fun saveSnapshot(snapshot: LockSnapshot) { dao.upsertSnapshot(SimpleContinuityMappers.encodeSnapshot(snapshot)) }
    override suspend fun loadShoppingList(planId: String): List<ShoppingListItem> =
        dao.getLatestShoppingList(planId)?.let { SimpleContinuityMappers.decodeShoppingItems(it.itemsJson) } ?: emptyList()

    override suspend fun saveShoppingList(planId: String, shoppingListId: String, snapshotId: String?, items: List<ShoppingListItem>, statusMetadataJson: String?) {
        dao.upsertShoppingList(
            ShoppingListEntity(
                shoppingListId = shoppingListId,
                planId = planId,
                snapshotId = snapshotId,
                itemsJson = SimpleContinuityMappers.encodeShoppingItems(items),
                statusMetadataJson = statusMetadataJson,
                updatedAtEpochMs = Instant.now().toEpochMilli(),
            ),
        )
    }

    override suspend fun getSelectedStore(): String? = dataStore.data.first()[SELECTED_STORE]
    override suspend fun setSelectedStore(storeDisplayName: String?) {
        dataStore.edit { prefs ->
            if (storeDisplayName == null) prefs.remove(SELECTED_STORE) else prefs[SELECTED_STORE] = storeDisplayName
        }
    }

    override suspend fun getSeasonOverride(): String? = dataStore.data.first()[SEASON_OVERRIDE]
    override suspend fun setSeasonOverride(override: String?) {
        dataStore.edit { prefs ->
            if (override == null) prefs.remove(SEASON_OVERRIDE) else prefs[SEASON_OVERRIDE] = override
        }
    }

    companion object {
        private val SELECTED_STORE = stringPreferencesKey("selected_store")
        private val SEASON_OVERRIDE = stringPreferencesKey("season_override")
    }
}
