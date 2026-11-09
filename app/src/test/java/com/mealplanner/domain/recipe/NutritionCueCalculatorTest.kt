package com.mealplanner.domain.recipe

import com.mealplanner.domain.model.NutritionAvailability
import com.mealplanner.domain.model.NutritionCueStatus
import com.mealplanner.domain.model.NutritionLabels
import com.mealplanner.domain.model.NutritionProfile
import com.mealplanner.domain.model.WeeklyNutritionBalanceCueRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NutritionCueCalculatorTest {
    @Test
    fun `missing nutrition is explicit and unavailable cue never fabricates values`() {
        val req = WeeklyNutritionBalanceCueRequest(
            selectedRecipeRefs = listOf("r1"),
            nutritionProfilesByRecipeId = mapOf("r1" to NutritionProfile(NutritionAvailability.UNAVAILABLE)),
            includePartialProfiles = true,
        )
        val cue = NutritionCueCalculator.calculate(req)
        assertEquals(NutritionCueStatus.UNAVAILABLE, cue.status)
        assertEquals(NutritionLabels.UNAVAILABLE, NutritionProfile(NutritionAvailability.UNAVAILABLE).unavailableLabel)
        assertEquals(1, cue.excludedFromCalculationCount)
        assertEquals(0, cue.recipesWithNutritionCount)
    }

    @Test
    fun `cue aggregates only from request inputs and reports exclusions`() {
        val req = WeeklyNutritionBalanceCueRequest(
            selectedRecipeRefs = listOf("r1", "r2", "r3"),
            nutritionProfilesByRecipeId = mapOf(
                "r1" to NutritionProfile(NutritionAvailability.AVAILABLE, energyKcal = 400.0),
                "r2" to NutritionProfile(NutritionAvailability.PARTIAL, energyKcal = 600.0),
                "r3" to NutritionProfile(NutritionAvailability.UNAVAILABLE),
            ),
            includePartialProfiles = true,
        )
        val cue = NutritionCueCalculator.calculate(req)
        assertEquals(NutritionCueStatus.PARTIAL, cue.status)
        assertEquals(3, cue.selectedRecipeCount)
        assertEquals(2, cue.recipesWithNutritionCount)
        assertEquals(1, cue.excludedFromCalculationCount)
        assertEquals(500.0, cue.averageEnergyKcal)
        assertFalse(cue.explanationKey.contains("diagnos", ignoreCase = true))
        assertFalse(cue.explanationKey.contains("dose", ignoreCase = true))
    }

    @Test
    fun `profiles missing from map are excluded explicitly`() {
        val req = WeeklyNutritionBalanceCueRequest(
            selectedRecipeRefs = listOf("r1", "r2"),
            nutritionProfilesByRecipeId = mapOf("r1" to NutritionProfile(NutritionAvailability.AVAILABLE, energyKcal = 300.0)),
            includePartialProfiles = false,
        )
        val cue = NutritionCueCalculator.calculate(req)
        assertEquals(1, cue.excludedFromCalculationCount)
        assertEquals(1, cue.recipesWithNutritionCount)
        assertEquals(NutritionCueStatus.PARTIAL, cue.status)
        assertEquals(300.0, cue.averageEnergyKcal)
    }
}
