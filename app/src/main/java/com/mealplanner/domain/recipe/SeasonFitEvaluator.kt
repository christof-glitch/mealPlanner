package com.mealplanner.domain.recipe

import com.mealplanner.domain.model.EffectiveSeasonResolution
import com.mealplanner.domain.model.EffectiveSeasonStatus
import com.mealplanner.domain.model.RecipeDetail
import com.mealplanner.domain.model.SeasonFitEvaluation
import com.mealplanner.domain.model.SeasonFitLabel
import com.mealplanner.domain.model.SeasonMappingSource

/** BR2.4: season fit from tags/curated mapping; UNKNOWN when evidence insufficient. No false matches. */
object SeasonFitEvaluator {
    private val seasonAliases = mapOf(
        "winter" to setOf("winter", "talvi"),
        "spring" to setOf("spring", "kevät", "kevat"),
        "summer" to setOf("summer", "kesä", "kesa"),
        "autumn" to setOf("autumn", "fall", "syksy"),
    )

    fun evaluate(recipe: RecipeDetail, effectiveResolution: EffectiveSeasonResolution): SeasonFitEvaluation {
        val resolved = normalizeSeason(effectiveResolution.effectiveContext.resolvedSeason)
        if (effectiveResolution.status == EffectiveSeasonStatus.UNKNOWN || resolved == null) {
            return SeasonFitEvaluation(
                seasonFitLabel = SeasonFitLabel.SEASON_FIT_UNKNOWN,
                effectiveSeasonResolution = effectiveResolution,
                matchedTags = recipe.seasonTags?.takeIf { it.isNotEmpty() },
                mappingSource = if (recipe.seasonTags.isNullOrEmpty()) SeasonMappingSource.NONE else SeasonMappingSource.RECIPE_TAG,
                explanationKey = "SEASON_FIT_UNKNOWN_NO_EFFECTIVE_SEASON",
            )
        }
        val tags = recipe.seasonTags.orEmpty().map { it.trim().lowercase() }.filter { it.isNotBlank() }
        if (tags.isEmpty()) {
            return SeasonFitEvaluation(
                seasonFitLabel = SeasonFitLabel.SEASON_FIT_UNKNOWN,
                effectiveSeasonResolution = effectiveResolution,
                mappingSource = SeasonMappingSource.NONE,
                explanationKey = "SEASON_FIT_UNKNOWN_NO_MAPPING",
            )
        }
        val current = seasonAliases[resolved] ?: setOf(resolved)
        val matched = tags.filter { tag -> current.any { it == tag } }
        val otherSeason = tags.any { tag -> seasonAliases.entries.any { (season, aliases) -> season != resolved && aliases.any { it == tag } } }
        val label = when {
            matched.isNotEmpty() && !otherSeason -> SeasonFitLabel.IN_SEASON
            matched.isNotEmpty() && otherSeason -> SeasonFitLabel.SHOULDER
            otherSeason -> SeasonFitLabel.OUT_OF_SEASON
            else -> SeasonFitLabel.SEASON_FIT_UNKNOWN
        }
        return SeasonFitEvaluation(
            seasonFitLabel = label,
            effectiveSeasonResolution = effectiveResolution,
            matchedTags = matched.ifEmpty { null },
            mappingSource = SeasonMappingSource.RECIPE_TAG,
            explanationKey = if (label == SeasonFitLabel.SEASON_FIT_UNKNOWN) "SEASON_FIT_UNKNOWN_NO_MAPPING" else null,
        )
    }

    private fun normalizeSeason(raw: String?): String? {
        val value = raw?.trim()?.lowercase()?.takeIf { it.isNotBlank() } ?: return null
        return seasonAliases.entries.firstOrNull { (k, v) -> k == value || v.contains(value) }?.key ?: value
    }
}
