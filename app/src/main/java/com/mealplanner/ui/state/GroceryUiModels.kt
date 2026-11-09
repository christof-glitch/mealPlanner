package com.mealplanner.ui.state

import com.mealplanner.domain.model.ExportMethod
import com.mealplanner.domain.model.ExportResult
import com.mealplanner.domain.model.ShoppingListAggregate
import com.mealplanner.domain.model.SostoslistaAttemptStatus
import com.mealplanner.domain.model.StorePresenceStatus
import com.mealplanner.domain.model.StoreRef
import com.mealplanner.domain.planning.Adr006LockRules

data class GroceryItemUiModel(
    val itemId: String,
    val displayText: String,
    val quantityText: String? = null,
    val checked: Boolean = false,
    val availabilityStatus: StorePresenceStatus = StorePresenceStatus.NOT_CHECKED,
    val availabilityReason: String? = null,
    val removable: Boolean = true,
)

data class GroceryScreenState(
    val recipesLocked: Boolean = false,
    val lockSummary: String = "Recipes not locked",
    val canGenerate: Boolean = false,
    val hasList: Boolean = false,
    val shoppingListId: String? = null,
    val hasUserEdits: Boolean = false,
    val isStale: Boolean = false,
    val staleWarningText: String? = null,
    val items: List<GroceryItemUiModel> = emptyList(),
    val selectedStore: StoreRef? = null,
    val presenceStatusLabel: String? = null,
    val exportMessage: String? = null,
    val exportInstructions: String? = null,
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val showRegenerateConfirmation: Boolean = false,
) {
    companion object {
        const val LOCK_REQUIRED_MESSAGE = "Lock recipes in Plan before generating your shopping list."
        val EXACT_ADR006_STALE_WARNING: String = Adr006LockRules.STALE_WARNING_TEXT
        const val LOCKED_RECIPES_CHANGED_STATUS = "Locked recipes changed — regenerate to update the shopping list."
    }
}

sealed interface GroceryIntent {
    data object Generate : GroceryIntent
    data object RequestRegenerate : GroceryIntent
    data object ConfirmRegenerateAndReplace : GroceryIntent
    data object CancelRegenerate : GroceryIntent
    data class ToggleChecked(val itemId: String) : GroceryIntent
    data class EditItem(val itemId: String, val displayText: String, val quantityText: String?) : GroceryIntent
    data class AddItem(val displayText: String, val quantityText: String?) : GroceryIntent
    data class RemoveItem(val itemId: String) : GroceryIntent
    data object ExportOfficialSostoslista : GroceryIntent
    data object ShareFallback : GroceryIntent
    data object CopyTextFallback : GroceryIntent
}

data class GroceryDomainSnapshot(
    val aggregate: ShoppingListAggregate? = null,
    val recipesLocked: Boolean = false,
    val lockSummary: String = "Recipes not locked",
    val currentEditedList: ShoppingListAggregate? = aggregate,
)
