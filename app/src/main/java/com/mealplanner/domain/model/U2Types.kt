package com.mealplanner.domain.model

/**
 * U2 additive domain contract types (ENT-U2-001..012, C2/C3 + A1).
 * Canonical slot reference is [PlanSlotRef]; no MealSlotRef alias.
 * No fabricated values; missing/ambiguous data uses explicit typed states.
 */

data class RecipeFilters(
    val mealTypes: List<MealSlotType>? = null,
    val maxTotalTimeMinutes: Int? = null,
    val requireSydanmerkkiCompatible: Boolean? = null,
    val seasonFitPreference: SeasonFitPreference? = null,
    val includeUnknownSeason: Boolean? = null,
    val nutritionAttributeFilter: List<String>? = null,
    val sourceTypes: List<String>? = null,
)

enum class SeasonFitPreference { IN_SEASON_ONLY, PREFER_IN_SEASON, ANY }

enum class RecipeSourceQueryType { CATALOG, BY_ID, BY_IDS, ALTERNATIVES, ELIGIBILITY }

/** ENT-U2-001 */
data class RecipeSourceAdapterRequest(
    val queryType: RecipeSourceQueryType,
    val recipeIds: List<RecipeId>? = null,
    val slotContext: PlanSlotRef? = null,
    val seasonContext: SeasonContext? = null,
    val filters: RecipeFilters? = null,
    /** true only when feasibility/terms/allowed access are confirmed (OQ1/FQ1=A). */
    val allowLiveSource: Boolean,
)

enum class RecipeAdapterStatus {
    ok, fallback_used, unavailable, restricted, timeout, parse_error_mapped, not_supported
}

enum class A1InvestigationOutcome {
    feasible_used, feasible_limited, not_feasible_or_allowed, unknown
}

enum class RecipeDataCompleteness { COMPLETE, PARTIAL, MINIMAL }

/** ENT-U2-003 */
data class RecipeDetail(
    val recipeId: RecipeId,
    val title: String,
    val summary: RecipeSummary,
    val ingredients: List<IngredientLine>? = null,
    val preparationNotes: List<String>? = null,
    val cookingGuidance: List<String>? = null,
    val seasonTags: List<String>? = null,
    val nutrition: NutritionProfile? = null,
    val dataCompleteness: RecipeDataCompleteness,
    val sourceStatus: RecipeSourceStatus,
)

/** ENT-U2-002 */
data class RecipeSourceAdapterResult(
    val status: RecipeAdapterStatus,
    val investigationOutcome: A1InvestigationOutcome,
    val recipes: List<RecipeDetail>? = null,
    val summaries: List<RecipeSummary>? = null,
    val sourceStatus: RecipeSourceStatus,
    val fallbackReason: FallbackReason? = null,
    val userSafeMessageKey: String? = null,
    val partialDataUsable: Boolean? = null,
)

/** ENT-U2-004 */
data class RecipeTimeNormalization(
    val totalTimeMinutes: Int? = null,
    val timeVerifiability: TimeConfidence,
    val weekdayEligible: Boolean? = null,
    val exclusionReason: ExclusionReason? = null,
    val rawTimeText: String? = null,
    val normalizationRule: String? = null,
)

enum class SeasonFitLabel { IN_SEASON, OUT_OF_SEASON, SHOULDER, SEASON_FIT_UNKNOWN }
enum class SeasonMappingSource { RECIPE_TAG, CURATED_MAPPING, NONE }

/** ENT-U2-005 */
data class SeasonFitEvaluation(
    val seasonFitLabel: SeasonFitLabel,
    val effectiveSeasonResolution: EffectiveSeasonResolution,
    val matchedTags: List<String>? = null,
    val mappingSource: SeasonMappingSource,
    val explanationKey: String? = null,
)

enum class NutritionSourceConfidence { HIGH, MEDIUM, LOW, UNKNOWN }

object NutritionLabels {
    const val UNAVAILABLE = "Nutrition information unavailable"
}

/** ENT-U2-006 */
data class NutritionProfile(
    val nutritionAvailability: NutritionAvailability,
    val energyKcal: Double? = null,
    val proteinG: Double? = null,
    val carbG: Double? = null,
    val fatG: Double? = null,
    val fiberG: Double? = null,
    val sydanmerkkiNutritionFlags: List<String>? = null,
    val sourceConfidence: NutritionSourceConfidence? = null,
    val unavailableLabel: String? = NutritionLabels.UNAVAILABLE.takeIf { nutritionAvailability == NutritionAvailability.UNAVAILABLE },
)

enum class NutritionCueStatus { AVAILABLE, PARTIAL, UNAVAILABLE }

/** ENT-U2-012 */
data class WeeklyNutritionBalanceCueRequest(
    val weekOrPlanId: PlanId? = null,
    val selectedRecipeRefs: List<RecipeId>,
    val nutritionProfilesByRecipeId: Map<RecipeId, NutritionProfile>? = null,
    val includePartialProfiles: Boolean,
)

/** ENT-U2-007 */
data class WeeklyNutritionBalanceCue(
    val status: NutritionCueStatus,
    val selectedRecipeCount: Int,
    val recipesWithNutritionCount: Int,
    val averageEnergyKcal: Double? = null,
    val tagCounts: Map<String, Int>? = null,
    val excludedFromCalculationCount: Int,
    val explanationKey: String,
)

/** ENT-U2-008 */
data class AlternativeRecipesRequest(
    val slotRef: PlanSlotRef,
    val currentRecipeId: RecipeId? = null,
    val seasonContext: SeasonContext,
    val effectiveSeasonResolution: EffectiveSeasonResolution? = null,
    val limit: Int? = null,
    val includeFallbackSources: Boolean,
)

/** ENT-U2-009 */
data class AlternativeRecipesResult(
    val status: AlternativeRecipesStatus,
    val alternatives: List<RecipeSummary> = emptyList(),
    val excludedCandidates: List<ExcludedRecipeCandidate>? = null,
    val sourceStatus: RecipeSourceStatus,
    val emptyReason: String? = null,
)

enum class AlternativeRecipesStatus { OK, EMPTY, FALLBACK, UNAVAILABLE }

enum class SlotSelectionOperation { SET_SELECTION, REPLACE_WITH_ALTERNATIVE, CLEAR_SELECTION }
enum class SlotSelectionInitiatedBy { USER }

/** ENT-U2-010 */
data class SlotSelectionChangeRequest(
    val slotRef: PlanSlotRef,
    val operation: SlotSelectionOperation,
    val recipeId: RecipeId? = null,
    val alternativeRecipeId: RecipeId? = null,
    val initiatedBy: SlotSelectionInitiatedBy = SlotSelectionInitiatedBy.USER,
)

enum class SlotSelectionResultStatus { APPLIED, REJECTED, NO_CHANGE }

/** ENT-U2-011 */
data class SlotSelectionChangeResult(
    val status: SlotSelectionResultStatus,
    val weeklyPlanState: WeeklyPlanState,
    val changedSlotRef: PlanSlotRef,
    val lockStaleStatus: LockStaleStatus? = null,
    val rejectionReason: String? = null,
)

/** U2 lock stale projection compatible with C3 consumers. */
data class LockStaleStatus(
    val snapshotId: LockSnapshotId? = null,
    val isStale: Boolean,
    val staleWarning: String? = null,
    val staleReason: String? = null,
)
