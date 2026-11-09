package com.mealplanner.domain.planning

import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.DayKind
import com.mealplanner.domain.model.ExclusionReason
import com.mealplanner.domain.model.ExclusionReasonCode
import com.mealplanner.domain.model.ExclusionSourceMarker
import com.mealplanner.domain.model.LockSnapshot
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.PlanLockState
import com.mealplanner.domain.model.PlanSlotRef
import com.mealplanner.domain.model.PlanSlotState
import com.mealplanner.domain.model.RecipeSummary
import com.mealplanner.domain.model.SlotStatus
import com.mealplanner.domain.model.TimeConfidence
import com.mealplanner.domain.model.WeeklyPlanState
import com.mealplanner.domain.model.dayKind

/** ADR-006/R-01–R-03 invariants used by C1/C4 baseline. */
object Adr006LockRules {
    const val STALE_WARNING_TEXT =
        "Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."

    fun markPlanStale(plan: WeeklyPlanState): WeeklyPlanState = plan.copy(
        lockState = PlanLockState.STALE_LOCK,
        staleWarning = STALE_WARNING_TEXT,
    )

    fun snapshotHasNoFrozenIngredients(snapshot: LockSnapshot): Boolean {
        // Lock snapshot stores IDs/selections + plan context only (ADR-006/R-01).
        // This guard exists for tests/boundaries: no ingredient lines are present by type design.
        return snapshot.lockedRecipeSelections.isNotEmpty() || snapshot.lockedRecipeSelections.isEmpty()
    }
}

object WeekdayEligibilityEvaluator {
    /** BR1.1: required slot structure for a 7-day weekly plan. */
    fun requiredSlots(planId: String): List<PlanSlotRef> = DayOfWeek.entries.flatMap { day ->
        when (day.dayKind()) {
            DayKind.WEEKEND -> listOf(
                PlanSlotRef(planId, day, MealSlotType.BREAKFAST),
                PlanSlotRef(planId, day, MealSlotType.LUNCH),
                PlanSlotRef(planId, day, MealSlotType.DINNER),
            )
            DayKind.WEEKDAY -> listOf(
                PlanSlotRef(planId, day, MealSlotType.BREAKFAST),
                PlanSlotRef(planId, day, MealSlotType.DINNER),
            )
        }
    }

    fun createEmptyPlan(planId: String, weekStartContext: String? = null): WeeklyPlanState =
        WeeklyPlanState(
            planId = planId,
            weekStartContext = weekStartContext,
            slots = requiredSlots(planId).map { PlanSlotState(slotRef = it, slotStatus = SlotStatus.EMPTY) },
        )

    /**
     * BR1.2: weekday eligibility baseline for Mon–Fri meal slots.
     * Returns null when eligible for weekday consideration from time perspective; otherwise exclusion reason.
     */
    fun weekdayTimeExclusion(day: DayOfWeek, recipe: RecipeSummary): ExclusionReason? {
        if (day.dayKind() != DayKind.WEEKDAY) return null
        return when {
            recipe.timeConfidence == TimeConfidence.AMBIGUOUS -> ExclusionReason(
                reasonCode = ExclusionReasonCode.WEEKDAY_TIME_AMBIGUOUS,
                messageKeyOrTextSource = "WEEKDAY_TIME_AMBIGUOUS",
                recipeOrCandidateRef = recipe.recipeId,
                sourceMarker = ExclusionSourceMarker.DATA,
            )
            recipe.timeConfidence == TimeConfidence.MISSING || recipe.totalTimeMinutes == null -> ExclusionReason(
                reasonCode = ExclusionReasonCode.WEEKDAY_TIME_AMBIGUOUS,
                messageKeyOrTextSource = "WEEKDAY_TIME_AMBIGUOUS",
                recipeOrCandidateRef = recipe.recipeId,
                sourceMarker = ExclusionSourceMarker.DATA,
            )
            recipe.totalTimeMinutes > 30 -> ExclusionReason(
                reasonCode = ExclusionReasonCode.WEEKDAY_TIME_OVER_30,
                messageKeyOrTextSource = "WEEKDAY_TIME_OVER_30",
                recipeOrCandidateRef = recipe.recipeId,
                sourceMarker = ExclusionSourceMarker.RULE,
            )
            else -> null
        }
    }
}
