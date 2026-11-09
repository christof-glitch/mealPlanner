package com.mealplanner.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** RM-01–RM-09 navigation routes (U4). Bottom nav + pushed/full-screen flows. R-06: slot context carried in args. */
sealed interface MealPlannerRoute {
    val route: String

    data object Onboarding : MealPlannerRoute { override val route = "onboarding" }
    data object Today : MealPlannerRoute { override val route = "today" }
    data object Plan : MealPlannerRoute { override val route = "plan" }
    data object Grocery : MealPlannerRoute { override val route = "grocery" }
    data object User : MealPlannerRoute { override val route = "user" }

    data class RecipeDetail(val recipeId: String, val slotContextKey: String? = null) : MealPlannerRoute {
        override val route = buildString {
            append("recipe/")
            append(URLEncoder.encode(recipeId, StandardCharsets.UTF_8))
            if (!slotContextKey.isNullOrBlank()) {
                append("?slotContextKey=")
                append(URLEncoder.encode(slotContextKey, StandardCharsets.UTF_8))
            }
        }
        companion object {
            const val ARG_RECIPE_ID = "recipeId"
            const val ARG_SLOT_CONTEXT = "slotContextKey"
            const val PATTERN = "recipe/{recipeId}?slotContextKey={slotContextKey}"
        }
    }

    data class ReplaceAlternatives(val slotContextKey: String) : MealPlannerRoute {
        override val route = "replace/${URLEncoder.encode(slotContextKey, StandardCharsets.UTF_8)}"
        companion object {
            const val ARG_SLOT_CONTEXT = "slotContextKey"
            const val PATTERN = "replace/{slotContextKey}"
        }
    }

    data class SelectPrismaStore(val currentStoreId: String? = null) : MealPlannerRoute {
        override val route = buildString {
            append("store/select")
            if (!currentStoreId.isNullOrBlank()) {
                append("?currentStoreId=")
                append(URLEncoder.encode(currentStoreId, StandardCharsets.UTF_8))
            }
        }
        companion object {
            const val ARG_CURRENT_STORE_ID = "currentStoreId"
            const val PATTERN = "store/select?currentStoreId={currentStoreId}"
        }
    }
}

val BottomNavRoutes = listOf(MealPlannerRoute.Today, MealPlannerRoute.Plan, MealPlannerRoute.Grocery, MealPlannerRoute.User)

