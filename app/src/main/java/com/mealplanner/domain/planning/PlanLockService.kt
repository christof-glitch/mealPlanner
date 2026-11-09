package com.mealplanner.domain.planning

import com.mealplanner.domain.model.GenerationStatus
import com.mealplanner.domain.model.LockSnapshot
import com.mealplanner.domain.model.LockedRecipeSelection
import com.mealplanner.domain.model.PlanLockState
import com.mealplanner.domain.model.PlanSnapshotContext
import com.mealplanner.domain.model.ShoppingListGenerationResult
import com.mealplanner.domain.model.SlotStatus
import com.mealplanner.domain.model.WeeklyPlanState
import java.time.Instant
import java.util.UUID

/**
 * C1/C3/C4 baseline lock and explicit generate/regenerate semantics (ADR-006).
 * U1 provides domain logic baseline; orchestration/UI owned by later units.
 */
class PlanLockService {
    fun lockPlan(
        plan: WeeklyPlanState,
        now: Instant = Instant.now(),
        planContext: PlanSnapshotContext = PlanSnapshotContext(),
        snapshotId: String = UUID.randomUUID().toString(),
    ): Pair<WeeklyPlanState, LockSnapshot> {
        val selections = plan.slots
            .filter { !it.assignedRecipeId.isNullOrBlank() }
            .map { slot ->
                LockedRecipeSelection(
                    slotRef = slot.slotRef,
                    recipeId = slot.assignedRecipeId!!,
                    selectionSource = com.mealplanner.domain.model.SelectionSource.USER_SELECTED,
                )
            }
        val snapshot = LockSnapshot(
            snapshotId = snapshotId,
            planId = plan.planId,
            lockedRecipeSelections = selections,
            planContext = planContext,
            createdAt = now,
        )
        val updated = plan.copy(
            lockState = PlanLockState.LOCKED,
            activeLockSnapshotId = snapshot.snapshotId,
            lockedRecipeIds = selections.map { it.recipeId }.distinct(),
            staleWarning = null,
            slots = plan.slots.map { slot ->
                if (slot.assignedRecipeId != null) slot.copy(slotStatus = SlotStatus.LOCKED) else slot
            },
        )
        return updated to snapshot
    }

    fun markPlanChanged(plan: WeeklyPlanState): WeeklyPlanState {
        if (plan.lockState == PlanLockState.UNLOCKED && plan.activeLockSnapshotId == null) return plan
        return Adr006LockRules.markPlanStale(plan)
    }

    fun generateShoppingList(
        plan: WeeklyPlanState,
        snapshot: LockSnapshot?,
        hasExistingUserEdits: Boolean,
        confirmedReplaceEdits: Boolean,
        isRegenerate: Boolean,
        resolvedItems: List<com.mealplanner.domain.model.ShoppingListItem>,
        unresolved: List<com.mealplanner.domain.model.IngredientLine> = emptyList(),
        shoppingListId: String = UUID.randomUUID().toString(),
    ): ShoppingListGenerationResult {
        if (plan.lockState == PlanLockState.UNLOCKED || snapshot == null) {
            return ShoppingListGenerationResult(
                status = GenerationStatus.FAILED,
                userActionRequired = "LOCK_AND_EXPLICIT_GENERATE_REQUIRED",
                fallbackReason = com.mealplanner.domain.model.FallbackReason("LOCK_REQUIRED", "Lock/finalize recipes before generating."),
            )
        }
        if (isRegenerate && hasExistingUserEdits && !confirmedReplaceEdits) {
            return ShoppingListGenerationResult(
                status = GenerationStatus.NEEDS_CONFIRMATION,
                snapshotId = snapshot.snapshotId,
                userActionRequired = "CONFIRM_REPLACE_EDITED_LIST",
                fallbackReason = com.mealplanner.domain.model.FallbackReason(
                    code = "REGENERATION_CONFIRMATION_REQUIRED",
                    message = "Regeneration can replace edited shopping list items. Explicit confirmation is required.",
                ),
            )
        }
        return ShoppingListGenerationResult(
            status = if (isRegenerate) GenerationStatus.REGENERATED else GenerationStatus.GENERATED,
            snapshotId = snapshot.snapshotId,
            shoppingListId = shoppingListId,
            items = resolvedItems,
            unresolvedIngredients = unresolved,
        )
    }
}
