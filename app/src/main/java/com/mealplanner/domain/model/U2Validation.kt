package com.mealplanner.domain.model

/** Boundary validation for U2 contracts (BR2.2, BR2.9, NFR3.3, NFR3.6). Typed, no fabrication. */
object U2Validation {
    data class ValidationFailure(val code: String, val message: String)

    fun validatePlanSlotRef(ref: PlanSlotRef?): ValidationFailure? {
        if (ref == null) return ValidationFailure("INVALID_SLOT_REF", "PlanSlotRef is required.")
        if (ref.planId.isBlank()) return ValidationFailure("INVALID_SLOT_REF", "planId is required.")
        return null
    }

    fun validateRecipeId(id: RecipeId?): ValidationFailure? {
        if (id.isNullOrBlank()) return ValidationFailure("INVALID_RECIPE_ID", "RecipeId is required.")
        return null
    }

    fun validateAdapterRequest(request: RecipeSourceAdapterRequest?): ValidationFailure? {
        if (request == null) return ValidationFailure("INVALID_REQUEST", "RecipeSourceAdapterRequest is required.")
        when (request.queryType) {
            RecipeSourceQueryType.BY_ID -> if (request.recipeIds.isNullOrEmpty()) return ValidationFailure("INVALID_REQUEST", "recipeIds required for BY_ID.")
            RecipeSourceQueryType.BY_IDS -> if (request.recipeIds == null) return ValidationFailure("INVALID_REQUEST", "recipeIds required for BY_IDS.")
            RecipeSourceQueryType.ALTERNATIVES, RecipeSourceQueryType.ELIGIBILITY -> {
                val slotError = validatePlanSlotRef(request.slotContext)
                if (slotError != null) return slotError
            }
            RecipeSourceQueryType.CATALOG -> Unit
        }
        return null
    }

    fun validateSlotSelectionRequest(request: SlotSelectionChangeRequest?): ValidationFailure? {
        if (request == null) return ValidationFailure("INVALID_REQUEST", "SlotSelectionChangeRequest is required.")
        val slotError = validatePlanSlotRef(request.slotRef)
        if (slotError != null) return slotError
        return when (request.operation) {
            SlotSelectionOperation.SET_SELECTION ->
                validateRecipeId(request.recipeId)?.let { ValidationFailure("INVALID_SELECTION", "recipeId required for SET_SELECTION.") }
            SlotSelectionOperation.REPLACE_WITH_ALTERNATIVE ->
                validateRecipeId(request.alternativeRecipeId ?: request.recipeId)?.let { ValidationFailure("INVALID_SELECTION", "alternative recipeId required for REPLACE_WITH_ALTERNATIVE.") }
            SlotSelectionOperation.CLEAR_SELECTION -> null
        }
    }

    fun validateNutritionCueRequest(request: WeeklyNutritionBalanceCueRequest?): ValidationFailure? {
        if (request == null) return ValidationFailure("INVALID_REQUEST", "WeeklyNutritionBalanceCueRequest is required.")
        if (request.selectedRecipeRefs.any { it.isBlank() }) return ValidationFailure("INVALID_REQUEST", "selectedRecipeRefs must not contain blank IDs.")
        return null
    }
}
