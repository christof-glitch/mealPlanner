package com.mealplanner.data.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WeeklyPlanEntity::class, LockSnapshotEntity::class, ShoppingListEntity::class, PreferenceEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class MealPlannerDatabase : RoomDatabase() {
    abstract fun dao(): MealPlannerDao
}
