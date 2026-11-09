package com.mealplanner.domain.planning

import com.mealplanner.domain.model.LockStaleStatus
import com.mealplanner.domain.model.PlanLockState
import com.mealplanner.domain.model.SlotSelectionChangeRequest
import com.mealplanner.domain.model.SlotSelectionChangeResult
import com.mealplanner.domain.model.SlotSelectionOperation
import com.mealplanner.domain.model.SlotSelectionResultStatus
import com.mealplanner.domain.model.SlotStatus
import com.mealplanner.domain.model.U2Validation
import com.mealplanner.domain.model.WeeklyPlanState

/**
 * BR2.7/BR2.8/R-06/ADR-006: target-slot-only changes, stale signaling when lock/list context active.
 * No auto-regeneration. C3 remains authoritative for WeeklyPlanState.
 */
class C3SlotSelectionService {
    fun apply(plan: WeeklyPlanState, request: SlotSelectionChangeRequest, lockOrListActive: Boolean): SlotSelectionChangeResult {
        val validation = U2Validation.validateSlotSelectionRequest(request)
        if (validation != null) {
            return SlotSelectionChangeResult(
                status = SlotSelectionResultStatus.REJECTED,
                weeklyPlanState = plan,
                changedSlotRef = request.slotRef,
                rejectionReason = validation.code,
            )
        }
        val slotIndex = plan.slots.indexOfFirst { it.slotRef == request.slotRef }
        if (slotIndex < 0) {
            return SlotSelectionChangeResult(
                status = SlotSelectionResultStatus.REJECTED,
                weeklyPlanState = plan,
                changedSlotRef = request.slotRef,
                rejectionReason = "SLOT_NOT_FOUND",
            )
        }
        val newRecipeId = when (request.operation) {
            SlotSelectionOperation.SET_SELECTION -> request.recipeId
            SlotSelectionOperation.REPLACE_WITH_ALTERNATIVE -> request.alternativeRecipeId ?: request.recipeId
            SlotSelectionOperation.CLEAR_SELECTION -> null
        }
        val updatedSlots = plan.slots.toMutableList()
        val current = updatedSlots[slotIndex]
        if (current.assignedRecipeId == newRecipeId && request.operation != SlotSelectionOperation.CLEAR_SELECTION) {
            return SlotSelectionChangeResult(SlotSelectionResultStatus.NO_CHANGE, plan, request.slotRef)
        }
        updatedSlots[slotIndex] = current.copy(
            assignedRecipeId = newRecipeId,
            slotStatus = if (newRecipeId == null) SlotStatus.EMPTY else SlotStatus.SELECTED,
            lastUpdatedAt = null,
        )
        var updated = plan.copy(slots = updatedSlots)
        var stale: LockStaleStatus? = null
        if (lockOrListActive || plan.lockState != PlanLockState.UNLOCKED || plan.activeLockSnapshotId != null) {
            updated = Adr006LockRules.markPlanStale(updated)
            stale = LockStaleStatus(
                snapshotId = updated.activeLockSnapshotId,
                isStale = true,
                staleWarning = Adr006LockRules.STALE_WARNING_TEXT,
                staleReason = "SLOT_SELECTION_CHANGED",
            )
        }
        return SlotSelectionChangeResult(
            status = SlotSelectionResultStatus.APPLIED,
            weeklyPlanState = updated,
            changedSlotRef = request.slotRef,
            lockStaleStatus = stale,
        )
    }
}
