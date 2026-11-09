package com.mealplanner.domain.model

import java.time.Instant

/** ENT-U1-001 */
data class PlanSlotRef(
    val planId: PlanId,
    val dayOfWeek: DayOfWeek,
    val mealType: MealSlotType,
)

/** ENT-U1-002 */
data class PlanSlotState(
    val slotRef: PlanSlotRef,
    val assignedRecipeId: RecipeId? = null,
    val slotStatus: SlotStatus = SlotStatus.EMPTY,
    val exclusionReason: ExclusionReason? = null,
    val dataSource: DataSourceStatus? = null,
    val lastUpdatedAt: Instant? = null,
)

/** ENT-U1-003 */
data class WeeklyPlanState(
    val planId: PlanId,
    val weekStartContext: String? = null,
    val slots: List<PlanSlotState>,
    val lockedRecipeIds: List<RecipeId> = emptyList(),
    val lockState: PlanLockState = PlanLockState.UNLOCKED,
    val activeLockSnapshotId: LockSnapshotId? = null,
    val staleWarning: String? = null,
)

/** ENT-U1-020 */
data class SeasonContext(
    val localDate: String? = null,
    val locationHint: String? = null,
    val resolvedSeason: String? = null,
    val resolutionSource: SeasonResolutionSource = SeasonResolutionSource.UNKNOWN,
)

/** ENT-U1-021 */
data class SeasonOverride(
    val mode: SeasonOverrideMode = SeasonOverrideMode.DEFAULT,
    val overrideSeason: String? = null,
    val updatedAt: Instant? = null,
)

/** ENT-U1-022 */
data class EffectiveSeasonResolution(
    val effectiveContext: SeasonContext,
    val override: SeasonOverride? = null,
    val status: EffectiveSeasonStatus = EffectiveSeasonStatus.UNKNOWN,
    val fallbackReason: FallbackReason? = null,
)
