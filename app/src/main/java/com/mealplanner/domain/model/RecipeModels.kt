package com.mealplanner.domain.model

/** ENT-U1-016 */
data class RecipeSummary(
    val recipeId: RecipeId,
    val title: String,
    val totalTimeMinutes: Int? = null,
    val timeConfidence: TimeConfidence = TimeConfidence.MISSING,
    val sydanmerkkiStatus: SydanmerkkiStatus = SydanmerkkiStatus.UNAVAILABLE,
    val sourceStatus: RecipeSourceStatus,
    val seasonFit: SeasonFit? = null,
    val nutritionAvailability: NutritionAvailability? = null,
)

/** ENT-U1-017 */
data class IngredientLine(
    val ingredientRef: IngredientRefId? = null,
    val name: String,
    val normalizedName: String? = null,
    val amount: Double? = null,
    val unit: String? = null,
    val amountText: String? = null,
    val availabilityStatus: IngredientAvailabilityStatus = IngredientAvailabilityStatus.KNOWN,
    val sourceRecipeId: RecipeId,
)

/** ENT-U1-023 */
data class ExcludedRecipeCandidate(
    val recipeSummary: RecipeSummary? = null,
    val recipeOrCandidateRef: String? = null,
    val exclusionReason: ExclusionReason,
)

/** ENT-U1-018 */
data class RecipeCandidateResult(
    val queryContext: Map<String, String> = emptyMap(),
    val candidates: List<RecipeSummary> = emptyList(),
    val excludedCandidates: List<ExcludedRecipeCandidate> = emptyList(),
    val status: RecipeQueryStatus = RecipeQueryStatus.UNAVAILABLE,
    val sourceStatus: RecipeSourceStatus,
    val fallbackReason: FallbackReason? = null,
)
