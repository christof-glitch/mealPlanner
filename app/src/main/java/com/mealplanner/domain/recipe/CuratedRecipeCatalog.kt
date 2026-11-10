package com.mealplanner.domain.recipe

import com.mealplanner.domain.model.Freshness
import com.mealplanner.domain.model.IngredientLine
import com.mealplanner.domain.model.RecipeDataCompleteness
import com.mealplanner.domain.model.RecipeDetail
import com.mealplanner.domain.model.RecipeSourceStatus
import com.mealplanner.domain.model.RecipeSourceType
import com.mealplanner.domain.model.RecipeSummary
import com.mealplanner.domain.model.SydanmerkkiStatus
import com.mealplanner.domain.model.TimeConfidence

/**
 * Minimal bundled/curated recipe catalog for local planning and week generation.
 * Provides breakfast and non-breakfast options so "generate week plan" can fill slots
 * without live integrations (OQ1/OQ2/OQ4 remain gated/investigational).
 */
object CuratedRecipeCatalog {
    private val curatedSourceStatus = RecipeSourceStatus(
        sourceType = RecipeSourceType.IMPORTED,
        freshness = Freshness.UNKNOWN,
        fallbackUsed = true,
        fallbackReason = null,
        userActionRequired = null,
    )

    fun summaries(): List<RecipeSummary> = details().map { it.summary }

    fun details(): List<RecipeDetail> = listOf(
        detail("brk-1", "Oatmeal porridge", 10, true),
        detail("brk-2", "Yogurt and berries", 5, true),
        detail("brk-3", "Smoothie bowl", 10, true),
        detail("brk-4", "Wholegrain toast breakfast", 10, true),
        detail("brk-5", "Banana pancakes", 20, true),
        detail("brk-6", "Egg omelet breakfast", 15, true),
        detail("brk-7", "Muesli with fruit", 5, true),
        detail("din-1", "Salmon and vegetables", 25),
        detail("din-2", "Chicken pasta", 25),
        detail("din-3", "Vegetable soup", 30),
        detail("din-4", "Beef stir-fry", 20),
        detail("din-5", "Lentil curry", 30),
        detail("din-6", "Turkey salad", 20),
        detail("din-7", "Baked fish and potatoes", 30),
        detail("din-8", "Tofu noodle bowl", 25),
        detail("din-9", "Tomato pasta", 20),
        detail("din-10", "Chicken and rice", 30),
        detail("din-11", "Vegetable omelet dinner", 20),
        detail("din-12", "Bean chili", 30),
        detail("din-13", "Shrimp and quinoa", 25),
        detail("din-14", "Mushroom risotto", 30),
    )

    private fun detail(id: String, title: String, minutes: Int, @Suppress("UNUSED_PARAMETER") breakfast: Boolean = false): RecipeDetail {
        val summary = RecipeSummary(
            recipeId = id,
            title = title,
            totalTimeMinutes = minutes,
            timeConfidence = TimeConfidence.KNOWN,
            sydanmerkkiStatus = SydanmerkkiStatus.UNAVAILABLE,
            sourceStatus = curatedSourceStatus,
            seasonFit = null,
            nutritionAvailability = null,
        )
        return RecipeDetail(
            recipeId = id,
            title = title,
            summary = summary,
            ingredients = listOf(
                IngredientLine(
                    ingredientRef = "$id-ing-1",
                    name = "Ingredient for $title",
                    normalizedName = "ingredient for ${title.lowercase()}",
                    amount = null,
                    unit = null,
                    amountText = null,
                    availabilityStatus = com.mealplanner.domain.model.IngredientAvailabilityStatus.KNOWN,
                    sourceRecipeId = id,
                ),
            ),
            preparationNotes = emptyList(),
            cookingGuidance = emptyList(),
            seasonTags = emptyList(),
            nutrition = null,
            dataCompleteness = RecipeDataCompleteness.MINIMAL,
            sourceStatus = curatedSourceStatus,
        )
    }

    fun isBreakfastTitle(title: String): Boolean {
        val t = title.lowercase()
        return listOf(
            "breakfast", "aamiainen", "aamupala", "puuro", "oat", "porridge",
            "smoothie", "jogurtti", "yogurt", "mysli", "muesli", "granola",
            "pancake", "lettu", "munakas", "omelet", "croissant", "toast breakfast",
        ).any { it in t }
    }
}
