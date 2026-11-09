package com.mealplanner.ui.state

import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.PlanSlotRef
import com.mealplanner.domain.model.RecipeId

/** R-06 slot context carried by all slot actions (origin preserved for focus/back-stack). */
data class SlotActionContext(
    val slotRef: PlanSlotRef,
    val dayOfWeek: DayOfWeek = slotRef.dayOfWeek,
    val mealType: MealSlotType = slotRef.mealType,
    val slotId: String = "${slotRef.planId}:${slotRef.dayOfWeek}:${slotRef.mealType}",
    val currentRecipeId: RecipeId? = null,
    val origin: SlotOrigin = SlotOrigin.PLAN,
)

enum class SlotOrigin { TODAY, PLAN }

data class MealSlotUiModel(
    val context: SlotActionContext,
    val recipeTitle: String? = null,
    val timeLabel: String? = null,
    val nutritionLabel: String? = null,
    val sourceLabel: String? = null,
    val badges: List<String> = emptyList(),
    val isEmpty: Boolean = recipeTitle == null,
    val statusLabels: List<String> = emptyList(),
)

enum class R05NextAction { RETRY, USE_BUNDLED_FALLBACK_CATALOG, CONTINUE_WITH_MANUAL_PLANNING }

data class R05State(
    val visible: Boolean = false,
    val message: String = "No local data available and remote data is unavailable.",
    val nextActions: List<R05NextAction> = listOf(R05NextAction.RETRY, R05NextAction.USE_BUNDLED_FALLBACK_CATALOG, R05NextAction.CONTINUE_WITH_MANUAL_PLANNING),
)

data class PlanScreenState(
    val planId: String = "plan-1",
    val weekSlots: List<MealSlotUiModel> = emptyList(),
    val lockStateLabel: String = "Recipes not locked",
    val lockSummary: String? = null,
    val isLocked: Boolean = false,
    val nutritionCueLabel: String? = null,
    val seasonContextLabel: String = "Using device date and location",
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val fallbackMessage: String? = null,
    val r05State: R05State? = null,
    val lastFocusedSlotId: String? = null,
    val statusMessage: String? = null,
)

data class TodayScreenState(
    val dateLabel: String = "",
    val todaySlots: List<MealSlotUiModel> = emptyList(),
    val nutritionCueLabel: String? = null,
    val seasonContextLabel: String = "Using device date and location",
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val fallbackMessage: String? = null,
    val r05State: R05State? = null,
    val lastFocusedSlotId: String? = null,
    val statusMessage: String? = null,
)

sealed interface PlanIntent {
    data class SelectOrReplace(val context: SlotActionContext, val recipeId: String?) : PlanIntent
    data class ViewRecipe(val context: SlotActionContext) : PlanIntent
    data class ChooseRecipe(val context: SlotActionContext) : PlanIntent
    data object LockRecipesForShopping : PlanIntent
    data object Retry : PlanIntent
    data object UseBundledFallbackCatalog : PlanIntent
    data object ContinueWithManualPlanning : PlanIntent
}
