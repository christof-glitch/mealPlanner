package com.mealplanner.domain.model

/** Logical ID aliases (ENT-U1 baseline). Concrete persistence IDs refined in later units. */
typealias PlanId = String
typealias RecipeId = String
typealias IngredientRefId = String
typealias ShoppingListId = String
typealias LockSnapshotId = String
typealias ShoppingListItemId = String

enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

enum class MealSlotType { BREAKFAST, LUNCH, DINNER }

enum class DayKind { WEEKDAY, WEEKEND }

fun DayOfWeek.dayKind(): DayKind = when (this) {
    DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> DayKind.WEEKEND
    else -> DayKind.WEEKDAY
}

enum class SlotStatus { EMPTY, SUGGESTED, SELECTED, LOCKED }

enum class PlanLockState { UNLOCKED, LOCKED, STALE_LOCK }

enum class SelectionSource { USER_SELECTED, SUGGESTED_ACCEPTED, FALLBACK }

enum class GenerationStatus { GENERATED, REGENERATED, FAILED, CANCELLED, NEEDS_CONFIRMATION }

/** C5 raw/name-presence result states (FR8). `NOT_CHECKED` is a C4 projection pre-check state. */
enum class StorePresenceStatus { PRESENT, NOT_FOUND, UNKNOWN, NOT_CHECKED }

/** Legacy U1 batch status; U3 adds [PresenceBatchStatus] with C5/A2 mapped outcomes. */
enum class PresenceBatchOverallStatus { COMPLETE, PARTIAL, FAILED }

/** U3/C5 batch mapped outcomes (BR3.7): name/presence only, typed fallback reasons. */
enum class PresenceBatchStatus { OK, PARTIAL, UNAVAILABLE, DENIED, FAILED_MAPPED }

enum class ApplyPresenceStatus { APPLIED, PARTIAL, FAILED }

enum class ShareStatus { SHARED, COPIED, CANCELLED, FAILED, FALLBACK_INSTRUCTIONS_SHOWN }

enum class ShareMethod { ANDROID_SHARE_SHEET, COPY_TEXT, MANUAL_INSTRUCTIONS }

enum class TimeConfidence { KNOWN, MISSING, AMBIGUOUS }

enum class SydanmerkkiStatus { VERIFIED, CURATED_FALLBACK, UNAVAILABLE }

enum class SeasonFit { IN_SEASON, OUT_OF_SEASON, SEASON_FIT_UNKNOWN }

enum class NutritionAvailability { AVAILABLE, UNAVAILABLE, PARTIAL }

enum class IngredientAvailabilityStatus { KNOWN, MISSING, CHANGED, UNAVAILABLE }

enum class RecipeQueryStatus { OK, PARTIAL, FALLBACK, UNAVAILABLE }

enum class StoreChain { PRISMA, OTHER }

enum class StoreSelectionSource { MANUAL, SUGGESTED, PREVIOUS }

enum class SeasonResolutionSource { DEVICE_DATE, DEVICE_LOCATION, DEFAULT, OVERRIDE, UNKNOWN }

enum class SeasonOverrideMode { DEFAULT, OVERRIDE, RESET }

enum class EffectiveSeasonStatus { RESOLVED, UNKNOWN, OVERRIDDEN }

enum class SourceName {
    SYDANMERKKI_LIVE, SYDANMERKKI_FALLBACK, PRISMA, SEASON_LOCAL, LOCAL_CACHE, MANUAL, NONE
}

enum class Freshness { CURRENT, CACHED, STALE, UNKNOWN }

enum class RecipeSourceType {
    SYDANMERKKI, CURATED_SYDANMERKKI_FALLBACK, LOCAL_CACHE, IMPORTED, NONE
}
