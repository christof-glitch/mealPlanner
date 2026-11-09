package com.mealplanner.data.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Minimal continuity set (NFR1.1/FR11/FR12/BR1.13). */
@Entity(tableName = "weekly_plan_state")
data class WeeklyPlanEntity(
    @PrimaryKey val planId: String,
    val weekStartContext: String?,
    val lockState: String,
    val activeLockSnapshotId: String?,
    val staleWarning: String?,
    val slotsJson: String,
    val lockedRecipeIdsJson: String,
    val updatedAtEpochMs: Long,
)

@Entity(tableName = "lock_snapshot")
data class LockSnapshotEntity(
    @PrimaryKey val snapshotId: String,
    val planId: String,
    val createdAtEpochMs: Long,
    val selectionsJson: String,
    val planContextJson: String,
    val sourceCatalogVersion: String?,
)

@Entity(tableName = "shopping_list")
data class ShoppingListEntity(
    @PrimaryKey val shoppingListId: String,
    val planId: String,
    val snapshotId: String?,
    val itemsJson: String,
    val statusMetadataJson: String?,
    val updatedAtEpochMs: Long,
)

@Entity(tableName = "app_preferences")
data class PreferenceEntity(
    @PrimaryKey val key: String,
    val valueJson: String,
    val updatedAtEpochMs: Long,
)
