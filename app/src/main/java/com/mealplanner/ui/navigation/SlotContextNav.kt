package com.mealplanner.ui.navigation

import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.PlanSlotRef
import com.mealplanner.domain.model.RecipeId
import com.mealplanner.ui.state.SlotActionContext
import com.mealplanner.ui.state.SlotOrigin
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Stable R-06 slot context encoding for navigation args/back-stack.
 * Format: planId|dayOfWeek|mealType|recipeIdOrEmpty|origin
 */
object SlotContextNav {
    fun encode(context: SlotActionContext): String = listOf(
        context.slotRef.planId,
        context.slotRef.dayOfWeek.name,
        context.slotRef.mealType.name,
        context.currentRecipeId ?: "",
        context.origin.name,
    ).joinToString("|") { URLEncoder.encode(it, StandardCharsets.UTF_8) }

    fun decode(raw: String?): SlotActionContext? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            val parts = raw.split("|").map { URLDecoder.decode(it, StandardCharsets.UTF_8) }
            if (parts.size < 5) return null
            val planId = parts[0]
            val day = DayOfWeek.valueOf(parts[1])
            val mealType = MealSlotType.valueOf(parts[2])
            val recipeId: RecipeId? = parts[3].ifBlank { null }
            val origin = runCatching { SlotOrigin.valueOf(parts[4]) }.getOrDefault(SlotOrigin.PLAN)
            val slotRef = PlanSlotRef(planId = planId, dayOfWeek = day, mealType = mealType)
            SlotActionContext(
                slotRef = slotRef,
                currentRecipeId = recipeId,
                origin = origin,
            )
        }.getOrNull()
    }

    fun stableSlotKey(context: SlotActionContext): String = context.slotId
}
