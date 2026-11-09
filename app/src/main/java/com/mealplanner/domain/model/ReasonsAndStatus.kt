package com.mealplanner.domain.model

/** Cross-cutting typed fallback reason (A1–A3 minimum baseline). */
data class FallbackReason(
    val code: String,
    val message: String? = null,
    val userActionRequired: String? = null,
    val technicalDetail: String? = null,
)

/** ENT-U1-014 minimum weekday-time exclusion/explanation data (Q5=B/Q8=A). */
data class ExclusionReason(
    val reasonCode: ExclusionReasonCode,
    val messageKeyOrTextSource: String,
    val slotRef: PlanSlotRef? = null,
    val recipeOrCandidateRef: String? = null,
    val sourceMarker: ExclusionSourceMarker,
)

enum class ExclusionReasonCode {
    WEEKDAY_TIME_OVER_30,
    WEEKDAY_TIME_AMBIGUOUS,
    DATA_UNAVAILABLE,
    SOURCE_UNAVAILABLE,
    OTHER,
}

enum class ExclusionSourceMarker { RULE, DATA, FALLBACK, UNKNOWN }

/** ENT-U1-015 cross-cutting source/fallback status envelope. */
data class DataSourceStatus(
    val source: SourceName,
    val freshness: Freshness,
    val fallbackUsed: Boolean,
    val fallbackReason: FallbackReason? = null,
    val userActionRequired: String? = null,
)

/** ENT-U1-019 recipe-facing minimum source status mapped to A1. */
data class RecipeSourceStatus(
    val sourceType: RecipeSourceType,
    val freshness: Freshness,
    val fallbackUsed: Boolean,
    val fallbackReason: FallbackReason? = null,
    val userActionRequired: String? = null,
)

/** A2 reason wrapper for PRISMA/presence boundaries (name/presence only). */
data class PresenceLookupReason(
    val code: String,
    val message: String? = null,
    val userActionRequired: String? = null,
)

/** A3 reason wrapper for export/share boundaries. */
data class ShareBoundaryReason(
    val code: String,
    val message: String? = null,
    val userActionRequired: String? = null,
)
