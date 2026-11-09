package com.mealplanner.domain.recipe

import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.ExclusionReason
import com.mealplanner.domain.model.ExclusionReasonCode
import com.mealplanner.domain.model.ExclusionSourceMarker
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.RecipeSummary
import com.mealplanner.domain.model.RecipeTimeNormalization
import com.mealplanner.domain.model.TimeConfidence
import com.mealplanner.domain.model.dayKind
import com.mealplanner.domain.model.DayKind

/** BR2.3: deterministic weekday time normalization without guessing (FR3, OQ7). */
object RecipeTimeNormalizationEvaluator {
    fun normalize(
        day: DayOfWeek,
        mealType: MealSlotType,
        recipe: RecipeSummary,
        rawTimeText: String? = null,
    ): RecipeTimeNormalization {
        val weekday = day.dayKind() == DayKind.WEEKDAY
        val confidence = recipe.timeConfidence
        val total = recipe.totalTimeMinutes
        return when {
            !weekday -> RecipeTimeNormalization(
                totalTimeMinutes = total.takeIf { confidence == TimeConfidence.KNOWN },
                timeVerifiability = confidence,
                weekdayEligible = null,
                rawTimeText = rawTimeText,
                normalizationRule = "WEEKEND_NOT_SUBJECT_TO_WEEKDAY_30_MIN",
            )
            confidence == TimeConfidence.KNOWN && total != null && total <= 30 -> RecipeTimeNormalization(
                totalTimeMinutes = total,
                timeVerifiability = TimeConfidence.KNOWN,
                weekdayEligible = true,
                rawTimeText = rawTimeText,
                normalizationRule = "KNOWN_TOTAL_LE_30",
            )
            confidence == TimeConfidence.KNOWN && total != null && total > 30 -> RecipeTimeNormalization(
                totalTimeMinutes = total,
                timeVerifiability = TimeConfidence.KNOWN,
                weekdayEligible = false,
                exclusionReason = exclusion(recipe.recipeId, ExclusionReasonCode.WEEKDAY_TIME_OVER_30, ExclusionSourceMarker.RULE),
                rawTimeText = rawTimeText,
                normalizationRule = "KNOWN_TOTAL_GT_30",
            )
            confidence == TimeConfidence.KNOWN && total == null -> ambiguous(recipe.recipeId, rawTimeText, "KNOWN_CONFIDENCE_MISSING_TOTAL")
            confidence == TimeConfidence.AMBIGUOUS -> ambiguous(recipe.recipeId, rawTimeText, "AMBIGUOUS_TIME_NOT_GUESSED")
            confidence == TimeConfidence.MISSING -> missing(recipe.recipeId, rawTimeText)
            else -> ambiguous(recipe.recipeId, rawTimeText, "UNPARSEABLE_TIME_NOT_GUESSED")
        }
    }

    private fun missing(recipeId: String, rawTimeText: String?) = RecipeTimeNormalization(
        totalTimeMinutes = null,
        timeVerifiability = TimeConfidence.MISSING,
        weekdayEligible = false,
        exclusionReason = exclusion(recipeId, ExclusionReasonCode.WEEKDAY_TIME_AMBIGUOUS, ExclusionSourceMarker.DATA),
        rawTimeText = rawTimeText,
        normalizationRule = "MISSING_TIME_NOT_GUESSED",
    )

    private fun ambiguous(recipeId: String, rawTimeText: String?, rule: String) = RecipeTimeNormalization(
        totalTimeMinutes = null,
        timeVerifiability = TimeConfidence.AMBIGUOUS,
        weekdayEligible = false,
        exclusionReason = exclusion(recipeId, ExclusionReasonCode.WEEKDAY_TIME_AMBIGUOUS, ExclusionSourceMarker.DATA),
        rawTimeText = rawTimeText,
        normalizationRule = rule,
    )

    private fun exclusion(recipeId: String, code: ExclusionReasonCode, marker: ExclusionSourceMarker) = ExclusionReason(
        reasonCode = code,
        messageKeyOrTextSource = code.name,
        recipeOrCandidateRef = recipeId,
        sourceMarker = marker,
    )
}
