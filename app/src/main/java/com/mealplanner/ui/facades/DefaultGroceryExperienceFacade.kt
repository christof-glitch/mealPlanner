package com.mealplanner.ui.facades

import com.mealplanner.domain.export.ExportShoppingListUseCase
import com.mealplanner.domain.model.ExportMethod
import com.mealplanner.domain.model.ExportRequest
import com.mealplanner.domain.model.ExportResult
import com.mealplanner.domain.model.ExportResultStatus
import com.mealplanner.domain.model.ShareBoundaryReason
import com.mealplanner.domain.model.ShoppingListAggregate
import com.mealplanner.domain.model.ShoppingListExportDto
import com.mealplanner.domain.model.ShoppingListGenerationOperation
import com.mealplanner.domain.model.ShoppingListGenerationOutcome
import com.mealplanner.domain.model.ShoppingListGenerationOutcomeResult
import com.mealplanner.domain.model.ShoppingListGenerationRequest
import com.mealplanner.domain.model.ShoppingListItem
import com.mealplanner.domain.model.effectiveDisplayText
import com.mealplanner.domain.model.effectiveQuantityText
import com.mealplanner.domain.planning.Adr006LockRules
import com.mealplanner.domain.shopping.ShoppingListRules
import com.mealplanner.ui.state.GroceryDomainSnapshot
import com.mealplanner.ui.state.GroceryIntent
import com.mealplanner.ui.state.GroceryItemUiModel
import com.mealplanner.ui.state.GroceryScreenState

/**
 * Thin grocery orchestration (ADR-005, ADR-006/R-01-R-03, BR3.8).
 * Local editable UI state = current edited list for display/export; C4/domain own generation/merge rules.
 */
class DefaultGroceryExperienceFacade(
    private val exportUseCase: ExportShoppingListUseCase? = null,
    private val prepareExport: ((ShoppingListAggregate) -> ShoppingListExportDto)? = null,
    private val generate: (() -> ShoppingListGenerationOutcomeResult)? = null,
    private val regenerate: ((confirm: Boolean) -> ShoppingListGenerationOutcomeResult)? = null,
) : GroceryExperienceFacade {
    private var recipesLocked: Boolean = false
    private var lockSummary: String = "Recipes not locked"
    private var currentAggregate: ShoppingListAggregate? = null
    private var showRegenerateConfirmation: Boolean = false
    private var exportMessage: String? = null
    private var exportInstructions: String? = null

    override fun currentState(): GroceryScreenState = toState()

    override fun refresh(snapshot: GroceryDomainSnapshot): GroceryScreenState {
        recipesLocked = snapshot.recipesLocked
        lockSummary = snapshot.lockSummary.ifBlank { if (recipesLocked) "Recipes locked" else "Recipes not locked" }
        currentAggregate = snapshot.currentEditedList ?: snapshot.aggregate
        showRegenerateConfirmation = false
        return toState()
    }

    override fun handle(intent: GroceryIntent): GroceryScreenState {
        when (intent) {
            GroceryIntent.Generate -> handleGenerate()
            GroceryIntent.RequestRegenerate -> handleRequestRegenerate()
            GroceryIntent.ConfirmRegenerateAndReplace -> handleRegenerateConfirmed()
            GroceryIntent.CancelRegenerate -> showRegenerateConfirmation = false
            is GroceryIntent.ToggleChecked -> toggle(intent.itemId)
            is GroceryIntent.EditItem -> edit(intent.itemId, intent.displayText, intent.quantityText)
            is GroceryIntent.AddItem -> add(intent.displayText, intent.quantityText)
            is GroceryIntent.RemoveItem -> remove(intent.itemId)
            GroceryIntent.ExportOfficialSostoslista, GroceryIntent.ShareFallback, GroceryIntent.CopyTextFallback -> {
                val result = exportCurrentEditedList(intent)
                exportMessage = result.message ?: result.status.name
                exportInstructions = result.instructions
            }
        }
        return toState()
    }

    override fun exportCurrentEditedList(intent: GroceryIntent): ExportResult {
        val aggregate = currentAggregate ?: return ExportResult(
            status = ExportResultStatus.FAILED_MAPPED,
            methodUsed = ExportMethod.COPY_TEXT,
            instructions = "No shopping list available to export.",
            fallbackReason = ShareBoundaryReason("NO_LIST", "No current edited shopping list.", "Generate a shopping list first."),
        )
        val payload = (prepareExport ?: ::defaultPrepareExport).invoke(aggregate)
        val preferred = when (intent) {
            GroceryIntent.ExportOfficialSostoslista -> ExportMethod.S_OSTOSLISTA_OFFICIAL_IF_AVAILABLE
            GroceryIntent.CopyTextFallback -> ExportMethod.COPY_TEXT
            else -> ExportMethod.SHARE_SHEET
        }
        val request = ExportRequest(
            shoppingListId = aggregate.shoppingListId,
            payload = payload,
            preferredMethod = preferred,
            title = "Shopping list",
        )
        return exportUseCase?.export(request) ?: ExportResult(
            status = ExportResultStatus.FALLBACK_USED,
            methodUsed = preferred,
            instructions = "Use share or copy the current edited shopping list.",
            payload = payload,
        )
    }

    private fun handleGenerate() {
        if (!recipesLocked) {
            lockSummary = GroceryScreenState.LOCK_REQUIRED_MESSAGE
            return
        }
        val result = generate?.invoke() ?: return
        if (result.status == ShoppingListGenerationOutcome.GENERATED || result.status == ShoppingListGenerationOutcome.PARTIAL) {
            currentAggregate = result.shoppingList ?: currentAggregate
        }
    }

    private fun handleRequestRegenerate() {
        val agg = currentAggregate ?: return
        if (agg.hasUserEdits) {
            showRegenerateConfirmation = true // explicit confirmation path, no silent replacement
            return
        }
        applyRegenerateResult(regenerate?.invoke(false))
    }

    private fun handleRegenerateConfirmed() {
        val result = regenerate?.invoke(true) ?: return
        if (result.status == ShoppingListGenerationOutcome.CONFIRMATION_REQUIRED) {
            showRegenerateConfirmation = true
            return
        }
        showRegenerateConfirmation = false
        applyRegenerateResult(result)
    }

    private fun applyRegenerateResult(result: ShoppingListGenerationOutcomeResult?) {
        if (result == null) return
        if (result.status == ShoppingListGenerationOutcome.GENERATED || result.status == ShoppingListGenerationOutcome.PARTIAL) {
            currentAggregate = result.shoppingList ?: currentAggregate
            showRegenerateConfirmation = false
        }
    }

    private fun toggle(itemId: String) {
        mutateItems { items -> items.map { if (it.itemId == itemId) it.copy(checked = !it.checked) else it } }
    }

    private fun edit(itemId: String, displayText: String, quantityText: String?) {
        mutateItems { items -> items.map { if (it.itemId == itemId) it.copy(displayName = displayText, displayText = displayText, quantityText = quantityText, amountText = quantityText, manuallyEdited = true, userEdited = true) else it } }
    }

    private fun add(displayText: String, quantityText: String?) {
        mutateItems { items ->
            val id = "manual-${items.size + 1}-${displayText.hashCode()}"
            items + ShoppingListItem(itemId = id, displayName = displayText, displayText = displayText, quantityText = quantityText, amountText = quantityText, manuallyEdited = true, userEdited = true, addedManually = true)
        }
    }

    private fun remove(itemId: String) {
        mutateItems { items -> items.filterNot { it.itemId == itemId } }
    }

    private fun mutateItems(transform: (List<ShoppingListItem>) -> List<ShoppingListItem>) {
        val agg = currentAggregate ?: return
        currentAggregate = agg.copy(items = transform(agg.items), hasUserEdits = true)
    }

    private fun toState(): GroceryScreenState {
        val agg = currentAggregate
        val stale = agg?.staleStatus?.isStale == true
        val staleWarning = when {
            !stale -> null
            !agg?.staleStatus?.staleWarning.isNullOrBlank() -> agg?.staleStatus?.staleWarning
            else -> Adr006LockRules.STALE_WARNING_TEXT
        }
        val items = agg?.items?.filterNot { it.removed }.orEmpty().map {
            GroceryItemUiModel(
                itemId = it.itemId,
                displayText = it.effectiveDisplayText(),
                quantityText = it.effectiveQuantityText(),
                checked = it.checked,
                availabilityStatus = it.availabilityStatus,
                availabilityReason = it.availabilityReason,
            )
        }
        return GroceryScreenState(
            recipesLocked = recipesLocked,
            lockSummary = if (!recipesLocked) lockSummary.ifBlank { GroceryScreenState.LOCK_REQUIRED_MESSAGE } else lockSummary,
            canGenerate = recipesLocked && agg == null,
            hasList = agg != null,
            shoppingListId = agg?.shoppingListId,
            hasUserEdits = agg?.hasUserEdits == true,
            isStale = stale,
            staleWarningText = staleWarning,
            items = items,
            selectedStore = agg?.selectedStoreRef,
            exportMessage = exportMessage,
            exportInstructions = exportInstructions,
            showRegenerateConfirmation = showRegenerateConfirmation,
        )
    }

    private fun defaultPrepareExport(aggregate: ShoppingListAggregate): ShoppingListExportDto = ShoppingListExportDto(
        shoppingListId = aggregate.shoppingListId,
        snapshotId = aggregate.snapshotId,
        items = aggregate.items.filterNot { it.removed }.map {
            com.mealplanner.domain.model.ExportLineItem(
                displayText = it.effectiveDisplayText(),
                checked = it.checked,
                quantityText = it.effectiveQuantityText(),
                availabilityStatus = it.availabilityStatus.name,
                availabilityReason = it.availabilityReason,
                itemId = it.itemId,
            )
        },
        staleWarning = aggregate.staleStatus.staleWarning,
    )
}
