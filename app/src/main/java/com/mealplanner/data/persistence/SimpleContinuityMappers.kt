package com.mealplanner.data.persistence

import com.mealplanner.domain.model.DataSourceStatus
import com.mealplanner.domain.model.Freshness
import com.mealplanner.domain.model.LockSnapshot
import com.mealplanner.domain.model.LockedRecipeSelection
import com.mealplanner.domain.model.PlanLockState
import com.mealplanner.domain.model.PlanSnapshotContext
import com.mealplanner.domain.model.PlanSlotRef
import com.mealplanner.domain.model.PlanSlotState
import com.mealplanner.domain.model.ShoppingListItem
import com.mealplanner.domain.model.SourceName
import com.mealplanner.domain.model.StorePresenceStatus
import com.mealplanner.domain.model.WeeklyPlanState
import java.time.Instant

/**
 * Minimal continuity mappers for U1 foundation.
 * Uses simple pipe-delimited encoding to avoid runtime JSON dependency in domain tests;
 * production serialization can be replaced with kotlinx.serialization/Moshi in later units.
 */
object SimpleContinuityMappers {
    fun encodePlan(entity: WeeklyPlanState, updatedAt: Instant = Instant.now()): WeeklyPlanEntity =
        WeeklyPlanEntity(
            planId = entity.planId,
            weekStartContext = entity.weekStartContext,
            lockState = entity.lockState.name,
            activeLockSnapshotId = entity.activeLockSnapshotId,
            staleWarning = entity.staleWarning,
            slotsJson = entity.slots.joinToString("\n") { encodeSlot(it) },
            lockedRecipeIdsJson = entity.lockedRecipeIds.joinToString(","),
            updatedAtEpochMs = updatedAt.toEpochMilli(),
        )

    fun decodePlan(entity: WeeklyPlanEntity): WeeklyPlanState = WeeklyPlanState(
        planId = entity.planId,
        weekStartContext = entity.weekStartContext,
        lockState = runCatching { PlanLockState.valueOf(entity.lockState) }.getOrDefault(PlanLockState.UNLOCKED),
        activeLockSnapshotId = entity.activeLockSnapshotId,
        staleWarning = entity.staleWarning,
        slots = if (entity.slotsJson.isBlank()) emptyList() else entity.slotsJson.lines().filter { it.isNotBlank() }.map { decodeSlot(it) },
        lockedRecipeIds = if (entity.lockedRecipeIdsJson.isBlank()) emptyList() else entity.lockedRecipeIdsJson.split(",").filter { it.isNotBlank() },
    )

    fun encodeSnapshot(snapshot: LockSnapshot): LockSnapshotEntity = LockSnapshotEntity(
        snapshotId = snapshot.snapshotId,
        planId = snapshot.planId,
        createdAtEpochMs = snapshot.createdAt.toEpochMilli(),
        selectionsJson = snapshot.lockedRecipeSelections.joinToString("\n") {
            listOf(it.slotRef.dayOfWeek.name, it.slotRef.mealType.name, it.recipeId, it.selectionSource.name).joinToString("|")
        },
        planContextJson = listOf(
            snapshot.planContext.selectedStoreRef?.displayName.orEmpty(),
            snapshot.planContext.seasonContext?.resolvedSeason.orEmpty(),
            snapshot.planContext.seasonOverride?.overrideSeason.orEmpty(),
            snapshot.planContext.schedulePolicyVersion.orEmpty(),
        ).joinToString("|"),
        sourceCatalogVersion = snapshot.sourceCatalogVersion,
    )

    fun decodeSnapshot(entity: LockSnapshotEntity): LockSnapshot {
        val selections = if (entity.selectionsJson.isBlank()) emptyList() else entity.selectionsJson.lines().filter { it.isNotBlank() }.map { line ->
            val p = line.split("|")
            LockedRecipeSelection(
                slotRef = PlanSlotRef(entity.planId, enumValueOf(p[0]), enumValueOf(p[1])),
                recipeId = p[2],
                selectionSource = runCatching { enumValueOf<com.mealplanner.domain.model.SelectionSource>(p[3]) }.getOrDefault(com.mealplanner.domain.model.SelectionSource.USER_SELECTED),
            )
        }
        val ctx = entity.planContextJson.split("|")
        return LockSnapshot(
            snapshotId = entity.snapshotId,
            planId = entity.planId,
            lockedRecipeSelections = selections,
            planContext = PlanSnapshotContext(
                selectedStoreRef = ctx.getOrNull(0)?.takeIf { it.isNotBlank() }?.let { com.mealplanner.domain.model.StoreRef(displayName = it) },
                seasonContext = ctx.getOrNull(1)?.takeIf { it.isNotBlank() }?.let { com.mealplanner.domain.model.SeasonContext(resolvedSeason = it) },
                seasonOverride = ctx.getOrNull(2)?.takeIf { it.isNotBlank() }?.let { com.mealplanner.domain.model.SeasonOverride(mode = com.mealplanner.domain.model.SeasonOverrideMode.OVERRIDE, overrideSeason = it) },
                schedulePolicyVersion = ctx.getOrNull(3)?.takeIf { it.isNotBlank() },
            ),
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMs),
            sourceCatalogVersion = entity.sourceCatalogVersion,
        )
    }

    fun encodeShoppingItems(items: List<ShoppingListItem>): String = items.joinToString("\n") { item ->
        listOf(
            item.itemId,
            escape(item.displayName),
            item.normalizedName.orEmpty(),
            item.amount?.toString().orEmpty(),
            item.unit.orEmpty(),
            item.checked.toString(),
            item.sourceRecipeIds.joinToString(","),
            item.presence?.name.orEmpty(),
            item.manuallyEdited.toString(),
            item.unresolved.toString(),
            escape(item.unresolvedReason.orEmpty()),
        ).joinToString("|")
    }

    fun decodeShoppingItems(json: String): List<ShoppingListItem> = if (json.isBlank()) emptyList() else json.lines().filter { it.isNotBlank() }.map { line ->
        val p = line.split("|")
        ShoppingListItem(
            itemId = p.getOrElse(0) { "" },
            displayName = unescape(p.getOrElse(1) { "" }),
            normalizedName = p.getOrNull(2)?.takeIf { it.isNotBlank() },
            amount = p.getOrNull(3)?.takeIf { it.isNotBlank() }?.toDoubleOrNull(),
            unit = p.getOrNull(4)?.takeIf { it.isNotBlank() },
            checked = p.getOrNull(5)?.toBooleanStrictOrNull() ?: false,
            sourceRecipeIds = p.getOrNull(6)?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
            presence = p.getOrNull(7)?.takeIf { it.isNotBlank() }?.let { runCatching { StorePresenceStatus.valueOf(it) }.getOrNull() },
            manuallyEdited = p.getOrNull(8)?.toBooleanStrictOrNull() ?: false,
            unresolved = p.getOrNull(9)?.toBooleanStrictOrNull() ?: false,
            unresolvedReason = p.getOrNull(10)?.takeIf { it.isNotBlank() }?.let { unescape(it) },
        )
    }

    private fun encodeSlot(slot: PlanSlotState): String = listOf(
        slot.slotRef.dayOfWeek.name,
        slot.slotRef.mealType.name,
        slot.assignedRecipeId.orEmpty(),
        slot.slotStatus.name,
        slot.exclusionReason?.reasonCode?.name.orEmpty(),
        slot.dataSource?.source?.name.orEmpty(),
        slot.dataSource?.freshness?.name.orEmpty(),
        slot.dataSource?.fallbackUsed?.toString().orEmpty(),
    ).joinToString("|")

    private fun decodeSlot(line: String): PlanSlotState {
        val p = line.split("|")
        val slotRef = PlanSlotRef(planId = "decoded", dayOfWeek = enumValueOf(p[0]), mealType = enumValueOf(p[1]))
        val source = p.getOrNull(5)?.takeIf { it.isNotBlank() }?.let { runCatching { SourceName.valueOf(it) }.getOrNull() }
        val freshness = p.getOrNull(6)?.takeIf { it.isNotBlank() }?.let { runCatching { Freshness.valueOf(it) }.getOrNull() }
        return PlanSlotState(
            slotRef = slotRef,
            assignedRecipeId = p.getOrNull(2)?.takeIf { it.isNotBlank() },
            slotStatus = runCatching { enumValueOf<com.mealplanner.domain.model.SlotStatus>(p.getOrElse(3) { "EMPTY" }) }.getOrDefault(com.mealplanner.domain.model.SlotStatus.EMPTY),
            dataSource = if (source != null && freshness != null) DataSourceStatus(source, freshness, p.getOrNull(7)?.toBooleanStrictOrNull() ?: false) else null,
        )
    }

    private fun escape(s: String): String = s.replace("\\", "\\\\").replace("|", "\\p").replace("\n", "\\n")
    private fun unescape(s: String): String = s.replace("\\n", "\n").replace("\\p", "|").replace("\\\\", "\\")
}
