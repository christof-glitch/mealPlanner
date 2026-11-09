package com.mealplanner.domain.recipe

import com.mealplanner.domain.model.NutritionAvailability
import com.mealplanner.domain.model.NutritionCueStatus
import com.mealplanner.domain.model.WeeklyNutritionBalanceCue
import com.mealplanner.domain.model.WeeklyNutritionBalanceCueRequest

/**
 * BR2.5: weekly non-medical nutrition cue computed only from request inputs.
 * Never reads WeeklyPlanState (R-06/BR2.7). No medical targets/dosing/diagnoses.
 */
object NutritionCueCalculator {
    fun calculate(request: WeeklyNutritionBalanceCueRequest): WeeklyNutritionBalanceCue {
        val selected = request.selectedRecipeRefs
        val profiles = request.nutritionProfilesByRecipeId.orEmpty()
        val usable = selected.mapNotNull { id ->
            val p = profiles[id] ?: return@mapNotNull null
            when (p.nutritionAvailability) {
                NutritionAvailability.AVAILABLE -> p
                NutritionAvailability.PARTIAL -> if (request.includePartialProfiles) p else null
                NutritionAvailability.UNAVAILABLE -> null
            }
        }
        val excluded = selected.size - usable.size
        val kcalValues = usable.mapNotNull { it.energyKcal }
        val status = when {
            usable.isEmpty() -> NutritionCueStatus.UNAVAILABLE
            excluded > 0 -> NutritionCueStatus.PARTIAL
            else -> NutritionCueStatus.AVAILABLE
        }
        val explanation = when (status) {
            NutritionCueStatus.UNAVAILABLE -> "NUTRITION_CUE_UNAVAILABLE_EXCLUDES_MISSING"
            NutritionCueStatus.PARTIAL -> "NUTRITION_CUE_PARTIAL_EXCLUDES_MISSING"
            NutritionCueStatus.AVAILABLE -> "NUTRITION_CUE_AVAILABLE_NON_MEDICAL"
        }
        return WeeklyNutritionBalanceCue(
            status = status,
            selectedRecipeCount = selected.size,
            recipesWithNutritionCount = usable.size,
            averageEnergyKcal = if (kcalValues.isNotEmpty()) kcalValues.average() else null,
            tagCounts = null,
            excludedFromCalculationCount = excluded,
            explanationKey = explanation,
        )
    }
}
