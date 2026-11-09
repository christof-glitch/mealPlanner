package com.mealplanner.data.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MealPlannerDao {
    @Query("SELECT * FROM weekly_plan_state WHERE planId = :planId LIMIT 1")
    suspend fun getPlan(planId: String): WeeklyPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlan(entity: WeeklyPlanEntity)

    @Query("SELECT * FROM lock_snapshot WHERE snapshotId = :snapshotId LIMIT 1")
    suspend fun getSnapshot(snapshotId: String): LockSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSnapshot(entity: LockSnapshotEntity)

    @Query("SELECT * FROM shopping_list WHERE planId = :planId ORDER BY updatedAtEpochMs DESC LIMIT 1")
    suspend fun getLatestShoppingList(planId: String): ShoppingListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertShoppingList(entity: ShoppingListEntity)

    @Query("SELECT * FROM app_preferences WHERE key = :key LIMIT 1")
    suspend fun getPreference(key: String): PreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreference(entity: PreferenceEntity)
}
