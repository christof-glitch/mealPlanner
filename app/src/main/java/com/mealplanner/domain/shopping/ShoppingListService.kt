package com.mealplanner.domain.shopping

import com.mealplanner.adapters.RecipeSourceBoundary
import com.mealplanner.domain.model.ExportLineItem
import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.IngredientAvailabilityStatus
import com.mealplanner.domain.model.IngredientLine
import com.mealplanner.domain.model.LockSnapshot
import com.mealplanner.domain.model.ShoppingItemIngredientDataStatus
import com.mealplanner.domain.model.ShoppingListAggregate
import com.mealplanner.domain.model.ShoppingListExportDto
import com.mealplanner.domain.model.ShoppingListGenerationOperation
import com.mealplanner.domain.model.ShoppingListGenerationOutcome
import com.mealplanner.domain.model.ShoppingListGenerationOutcomeResult
import com.mealplanner.domain.model.ShoppingListGenerationRequest
import com.mealplanner.domain.model.ShoppingListItem
import com.mealplanner.domain.model.ShoppingListStaleStatus
import com.mealplanner.domain.model.ShoppingListStateStatus
import com.mealplanner.domain.model.StorePresenceStatus
import com.mealplanner.domain.model.effectiveDisplayText
import com.mealplanner.domain.model.effectiveQuantityText
import java.time.Instant
import java.util.UUID

/**
 * C4 shopping list use-case/service (BR3.1-BR3.4, BR3.8).
 * Re-resolves ingredients from C2 at generation time (ADR-006/R-01). No fabrication.
 */
class ShoppingListService(
    private val recipeResolver: RecipeSourceBoundary,
) {
    suspend fun generateFromLockSnapshot(
        request: ShoppingListGenerationRequest,
        snapshot: LockSnapshot?,
        staleStatus: ShoppingListStaleStatus = request.staleStatus,
        now: Instant = Instant.now(),
    ): ShoppingListGenerationOutcomeResult {
        if (snapshot == null || snapshot.snapshotId != request.snapshotId) {
            return ShoppingListGenerationOutcomeResult(
                status = ShoppingListGenerationOutcome.INVALID_SNAPSHOT,
                message = "Locked snapshot missing or invalid.",
                staleStatus = staleStatus,
                fallbackReason = FallbackReason("INVALID_SNAPSHOT", "Cannot generate without a valid locked snapshot."),
            )
        }
        val lockedRecipeIds = snapshot.lockedRecipeSelections.map { it.recipeId }.distinct()
        val resolved = recipeResolver.resolveIngredients(lockedRecipeIds)
        val missingOrChanged = mutableListOf<String>()
        val allLines = mutableListOf<IngredientLine>()
        lockedRecipeIds.forEach { recipeId ->
            val lines = resolved[recipeId]
            if (lines.isNullOrEmpty()) {
                missingOrChanged += recipeId
                allLines += IngredientLine(
                    name = "Missing recipe ingredients",
                    availabilityStatus = IngredientAvailabilityStatus.MISSING,
                    sourceRecipeId = recipeId,
                )
            } else {
                lines.forEach { line ->
                    allLines += line
                    if (line.availabilityStatus == IngredientAvailabilityStatus.CHANGED ||
                        line.availabilityStatus == IngredientAvailabilityStatus.MISSING ||
                        line.availabilityStatus == IngredientAvailabilityStatus.UNAVAILABLE
                    ) {
                        if (recipeId !in missingOrChanged) missingOrChanged += recipeId
                    }
                }
            }
        }
        val (items, unresolved) = ShoppingListRules.resolveIngredientLines(allLines)
        val notes = buildList {
            if (missingOrChanged.isNotEmpty()) add("Missing or changed source data for recipes: ${missingOrChanged.joinToString(", ")}")
            if (unresolved.isNotEmpty()) add("${unresolved.size} ingredient line(s) unresolved or incomplete; no fabrication applied.")
        }
        val status = when {
            items.isEmpty() && unresolved.isNotEmpty() -> ShoppingListGenerationOutcome.PARTIAL
            missingOrChanged.isNotEmpty() || unresolved.isNotEmpty() -> ShoppingListGenerationOutcome.PARTIAL
            else -> ShoppingListGenerationOutcome.GENERATED
        }
        val aggregate = ShoppingListAggregate(
            shoppingListId = "shopping-${snapshot.snapshotId}-${UUID.randomUUID()}",
            snapshotId = snapshot.snapshotId,
            sourceRecipeIds = lockedRecipeIds,
            items = items.map { item ->
                item.copy(
                    selectedStoreId = snapshot.planContext.selectedStoreRef?.storeId,
                    availabilityStatus = item.availabilityStatus.takeUnless { it == StorePresenceStatus.NOT_CHECKED } ?: StorePresenceStatus.NOT_CHECKED,
                    ingredientDataStatus = item.ingredientDataStatus,
                )
            },
            listStatus = if (status == ShoppingListGenerationOutcome.PARTIAL) ShoppingListStateStatus.PARTIAL else ShoppingListStateStatus.GENERATED,
            staleStatus = staleStatus,
            hasUserEdits = false,
            generationNotes = notes,
            lastGeneratedAt = now,
            selectedStoreRef = snapshot.planContext.selectedStoreRef,
        )
        return ShoppingListGenerationOutcomeResult(
            status = status,
            shoppingList = aggregate,
            missingOrChangedRecipeIds = missingOrChanged,
            message = notes.joinToString(" ").ifBlank { "Shopping list generated from locked snapshot." },
            staleStatus = staleStatus,
        )
    }

    suspend fun regenerateFromLockSnapshot(
        request: ShoppingListGenerationRequest,
        snapshot: LockSnapshot?,
        currentList: ShoppingListAggregate?,
        staleStatus: ShoppingListStaleStatus = request.staleStatus,
        now: Instant = Instant.now(),
    ): ShoppingListGenerationOutcomeResult {
        val confirmation = ShoppingListRules.evaluateRegenerationRequest(currentList, request.copy(operation = ShoppingListGenerationOperation.REGENERATE))
        if (confirmation.status == ShoppingListGenerationOutcome.CONFIRMATION_REQUIRED) {
            return confirmation.copy(staleStatus = staleStatus)
        }
        return generateFromLockSnapshot(request.copy(operation = ShoppingListGenerationOperation.REGENERATE), snapshot, staleStatus, now)
    }

    /** BR3.8: export uses current edited list state. */
    fun prepareCurrentExportState(aggregate: ShoppingListAggregate): ShoppingListExportDto {
        return ShoppingListExportDto(
            shoppingListId = aggregate.shoppingListId,
            snapshotId = aggregate.snapshotId,
            items = aggregate.items.filterNot { it.removed }.map { item ->
                ExportLineItem(
                    displayText = item.effectiveDisplayText(),
                    checked = item.checked,
                    quantityText = item.effectiveQuantityText(),
                    availabilityStatus = item.availabilityStatus.name,
                    availabilityReason = item.availabilityReason,
                    itemId = item.itemId,
                )
            },
            selectedStore = aggregate.selectedStoreRef,
            staleWarning = aggregate.staleStatus.staleWarning,
            notes = aggregate.generationNotes,
        )
    }
}
