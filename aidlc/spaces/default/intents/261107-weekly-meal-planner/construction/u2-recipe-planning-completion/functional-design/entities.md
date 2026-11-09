# U2 Functional Design — Entities (`u2-recipe-planning-completion`)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`)  
**Stage:** Functional Design (construction, per-unit)  
**Date:** 2026-11-09

## Sources
- [unit-of-work.md](../../../inception/units-generation/unit-of-work.md) (U2 scope/responsibilities, C2/C3 completion)
- [U1 entities](../../u1-mvp-foundation-skeleton/functional-design/entities.md), [rules](../../u1-mvp-foundation-skeleton/functional-design/rules.md), [functional-spec](../../u1-mvp-foundation-skeleton/functional-design/functional-spec.md)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (C2/C3 baseline/completion, A1)
- [components.md](../../../inception/domain-design/components.md) (RecipeCatalogComponent, MealPlanningComponent; ADR-005/006)
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (FR2–FR6, FR11, FR12, NFR1–NFR5, OQ1, OQ5–OQ7)
- [stories.md](../../../inception/user-stories/stories.md) (US1.1, US1.2, US2.1–US2.5, US5.4–US5.6)
- [functional-design-questions.md](functional-design-questions.md) (Q1=B resolved by FQ1=A, Q2=A, Q3=A, Q4=A, Q5=A, Q6=A)

## Scope
U2 completes C2 (RecipeCatalog) and C3 (MealPlanning) on top of U1 baselines. It defines domain contract shapes for recipe normalization/eligibility/season/nutrition/source-fallback, alternatives/replacement, and Today/Plan synchronization semantics. U4 owns presentation. Additive to U1 contracts (C1–C6/A1–A4); no frozen ingredient data in snapshots (ADR-006). No fabricated data.

## Type sources and baseline aliases
- **Canonical slot reference:** `PlanSlotRef` (U1 ENT-U1-001, C1 baseline) is the canonical slot type for U2 entities/workflows/rules. No `MealSlotRef` alias is introduced in U2.
- **Inherited from U1 baseline / contract summary:** `PlanId`, `PlanSlotRef`, `MealSlotType`, `PlanSlotState`, `WeeklyPlanState`, `RecipeId`, `ShoppingListId`, `ShoppingListItemId`, `LockSnapshotId`, `RecipeSummary`, `IngredientLine`, `IngredientRef`, `RecipeSourceStatus`, `DataSourceStatus`, `FallbackReason`, `ExclusionReason`, `SeasonContext`, `SeasonOverride`, `EffectiveSeasonResolution`, `LockStaleStatus`, `StoreRef`, `StorePresenceResult` and related status enums/shapes as defined in U1 functional design and contract-summary.
- **U2 minimum definition:** `RecipeFilters` is defined minimally here for U2 queries and may be extended additively by U2/U4.

```yaml
types:
  - name: RecipeFilters
    owner: C2
    description: Minimum optional filters for recipe catalog/alternatives queries.
    fields:
      - { name: mealTypes, type: "list<MealSlotType>", required: false }
      - { name: maxTotalTimeMinutes, type: integer, required: false }
      - { name: requireSydanmerkkiCompatible, type: boolean, required: false }
      - { name: seasonFitPreference, type: "IN_SEASON_ONLY|PREFER_IN_SEASON|ANY", required: false }
      - { name: includeUnknownSeason, type: boolean, required: false }
      - { name: nutritionAttributeFilter, type: "list<string>", required: false }
      - { name: sourceTypes, type: "list<string>", required: false }
    status_values: []
    notes: "All fields optional; omitted filters impose no constraint. nutritionAttributeFilter uses reliably-available attributes only (OQ6) and never medical targets."
```

## Entities — source of truth

```yaml
entities:
  - id: ENT-U2-001
    name: RecipeSourceAdapterRequest
    owner: C2/A1
    description: Input to Sydänmerkki/recipe-source adapter path (live path required; execution gated by confirmed feasibility/terms per FQ1=A).
    fields:
      - { name: queryType, type: "CATALOG|BY_ID|BY_IDS|ALTERNATIVES|ELIGIBILITY", required: true }
      - { name: recipeIds, type: "list<RecipeId>", required: false }
      - { name: slotContext, type: PlanSlotRef, required: false }
      - { name: seasonContext, type: SeasonContext, required: false }
      - { name: filters, type: RecipeFilters, required: false }
      - { name: allowLiveSource, type: boolean, required: true }
    status_values: ["CATALOG", "BY_ID", "BY_IDS", "ALTERNATIVES", "ELIGIBILITY"]
    notes: "Uses canonical PlanSlotRef (U1 ENT-U1-001). allowLiveSource is true only when feasibility/terms/allowed access are confirmed (OQ1). Otherwise adapter returns typed restricted/unavailable/not_supported and uses curated fallback."

  - id: ENT-U2-002
    name: RecipeSourceAdapterResult
    owner: C2/A1
    description: Typed outcome from A1 adapter path with investigation/fallback visibility.
    fields:
      - { name: status, type: "ok|fallback_used|unavailable|restricted|timeout|parse_error_mapped|not_supported", required: true }
      - { name: investigationOutcome, type: "feasible_used|feasible_limited|not_feasible_or_allowed|unknown", required: true }
      - { name: recipes, type: "list<RecipeDetail>", required: false }
      - { name: summaries, type: "list<RecipeSummary>", required: false }
      - { name: sourceStatus, type: RecipeSourceStatus, required: true }
      - { name: fallbackReason, type: FallbackReason, required: false }
      - { name: userSafeMessageKey, type: string, required: false }
      - { name: partialDataUsable, type: boolean, required: false }
    status_values: ["ok", "fallback_used", "unavailable", "restricted", "timeout", "parse_error_mapped", "not_supported"]
    notes: "Mandatory curated/imported Sydänmerkki-aligned fallback remains available. `parse_error_mapped`: if live data is partially parseable and safe, adapter may return normalized partial data only when fields are explicitly marked/complete per record and must attempt curated fallback to fill missing records where possible without fabricating; report `fallback_used` when fallback contributed, else `parse_error_mapped` with explicit partial status. Hard parse failure/unusable live result => typed failure status and curated fallback if available, else unavailable. No scraping/terms assumptions."

  - id: ENT-U2-003
    name: RecipeDetail
    owner: C2
    description: Normalized recipe detail used for selection, eligibility, alternatives, and C4 re-resolution.
    fields:
      - { name: recipeId, type: RecipeId, required: true }
      - { name: title, type: string, required: true }
      - { name: summary, type: RecipeSummary, required: true }
      - { name: ingredients, type: "list<IngredientLine>", required: false }
      - { name: preparationNotes, type: "list<string>", required: false }
      - { name: cookingGuidance, type: "list<string>", required: false }
      - { name: seasonTags, type: "list<string>", required: false }
      - { name: nutrition, type: NutritionProfile, required: false }
      - { name: dataCompleteness, type: "COMPLETE|PARTIAL|MINIMAL", required: true }
      - { name: sourceStatus, type: RecipeSourceStatus, required: true }
    status_values: ["COMPLETE", "PARTIAL", "MINIMAL"]

  - id: ENT-U2-004
    name: RecipeTimeNormalization
    owner: C2
    description: Typed weekday total-time normalization result (OQ7, FR3, AC2.2.3).
    fields:
      - { name: totalTimeMinutes, type: integer, required: false }
      - { name: timeVerifiability, type: "KNOWN|MISSING|AMBIGUOUS", required: true }
      - { name: weekdayEligible, type: boolean, required: false }
      - { name: exclusionReason, type: ExclusionReason, required: false }
      - { name: rawTimeText, type: string, required: false }
      - { name: normalizationRule, type: string, required: false }
    status_values: ["KNOWN", "MISSING", "AMBIGUOUS"]
    notes: "Ambiguous formats (ranges, conflicting prep+cook, unit confusion, unparseable) are not guessed; excluded from weekday suggestions with ExclusionReason."

  - id: ENT-U2-005
    name: SeasonFitEvaluation
    owner: C2/C3
    description: Recipe season fit computed from effective season context and recipe tags/curated mapping (OQ5, FR5).
    fields:
      - { name: seasonFitLabel, type: "IN_SEASON|OUT_OF_SEASON|SHOULDER|SEASON_FIT_UNKNOWN", required: true }
      - { name: effectiveSeasonResolution, type: EffectiveSeasonResolution, required: true }
      - { name: matchedTags, type: "list<string>", required: false }
      - { name: mappingSource, type: "RECIPE_TAG|CURATED_MAPPING|NONE", required: true }
      - { name: explanationKey, type: string, required: false }
    status_values: ["IN_SEASON", "OUT_OF_SEASON", "SHOULDER", "SEASON_FIT_UNKNOWN"]

  - id: ENT-U2-006
    name: NutritionProfile
    owner: C2
    description: Optional reliably-available nutrition attributes and minimal planning-aid inputs (OQ6, FR6, BR1.14).
    fields:
      - { name: nutritionAvailability, type: "AVAILABLE|UNAVAILABLE|PARTIAL", required: true }
      - { name: energyKcal, type: number, required: false }
      - { name: proteinG, type: number, required: false }
      - { name: carbG, type: number, required: false }
      - { name: fatG, type: number, required: false }
      - { name: fiberG, type: number, required: false }
      - { name: sydanmerkkiNutritionFlags, type: "list<string>", required: false }
      - { name: sourceConfidence, type: "HIGH|MEDIUM|LOW|UNKNOWN", required: false }
      - { name: unavailableLabel, type: string, required: false }
    status_values: ["AVAILABLE", "UNAVAILABLE", "PARTIAL"]
    notes: "Missing data labeled `Nutrition information unavailable` where applicable. No medical targets/dosing/diagnoses."

  - id: ENT-U2-007
    name: WeeklyNutritionBalanceCue
    owner: C2
    description: Minimal non-medical weekly planning aid computed by C2 from nutrition inputs supplied by C3/U4 orchestration boundary.
    fields:
      - { name: status, type: "AVAILABLE|PARTIAL|UNAVAILABLE", required: true }
      - { name: selectedRecipeCount, type: integer, required: true }
      - { name: recipesWithNutritionCount, type: integer, required: true }
      - { name: averageEnergyKcal, type: number, required: false }
      - { name: tagCounts, type: "map<string,integer>", required: false }
      - { name: excludedFromCalculationCount, type: integer, required: true }
      - { name: explanationKey, type: string, required: true }
    status_values: ["AVAILABLE", "PARTIAL", "UNAVAILABLE"]
    notes: "View-only planning cue. C2 computes aggregation only; it does not read WeeklyPlanState directly. C3/U4 orchestration provides selected recipe IDs and/or resolved NutritionProfile inputs (ENT-U2-012). Excludes unavailable data from calculations and states exclusion count."

  - id: ENT-U2-012
    name: WeeklyNutritionBalanceCueRequest
    owner: C2 (computation) / C3-U4 orchestration boundary (input assembly)
    description: Minimum input contract for computing WeeklyNutritionBalanceCue without C2 accessing plan state.
    fields:
      - { name: weekOrPlanId, type: PlanId, required: false }
      - { name: selectedRecipeRefs, type: "list<RecipeId>", required: true }
      - { name: nutritionProfilesByRecipeId, type: "map<RecipeId,NutritionProfile>", required: false }
      - { name: includePartialProfiles, type: boolean, required: true }
    status_values: []
    notes: "C3 owns selected slot/recipe state (R-06/BR2.7) and/or U4 orchestration resolves C2 recipe nutrition profiles for selected recipes, then passes this request to C2. C2 returns WeeklyNutritionBalanceCue using only supplied/available data; missing profiles count as excluded/unavailable."

  - id: ENT-U2-008
    name: AlternativeRecipesRequest
    owner: C2/C3
    description: Request for slot-specific replacement alternatives.
    fields:
      - { name: slotRef, type: PlanSlotRef, required: true }
      - { name: currentRecipeId, type: RecipeId, required: false }
      - { name: seasonContext, type: SeasonContext, required: true }
      - { name: limit, type: integer, required: false }
      - { name: includeFallbackSources, type: boolean, required: true }
    status_values: []

  - id: ENT-U2-009
    name: AlternativeRecipesResult
    owner: C2
    description: Alternatives respecting weekday eligibility, season fit, and source/fallback status.
    fields:
      - { name: status, type: "OK|EMPTY|FALLBACK|UNAVAILABLE", required: true }
      - { name: alternatives, type: "list<RecipeSummary>", required: true }
      - { name: excludedCandidates, type: "list<ExcludedRecipeCandidate>", required: false }
      - { name: sourceStatus, type: RecipeSourceStatus, required: true }
      - { name: emptyReason, type: string, required: false }
    status_values: ["OK", "EMPTY", "FALLBACK", "UNAVAILABLE"]

  - id: ENT-U2-010
    name: SlotSelectionChangeRequest
    owner: C3
    description: Per-slot selection/replacement operation affecting only the target slot (R-06).
    fields:
      - { name: slotRef, type: PlanSlotRef, required: true }
      - { name: operation, type: "SET_SELECTION|REPLACE_WITH_ALTERNATIVE|CLEAR_SELECTION", required: true }
      - { name: recipeId, type: RecipeId, required: false }
      - { name: alternativeRecipeId, type: RecipeId, required: false }
      - { name: initiatedBy, type: "USER", required: true }
    status_values: ["SET_SELECTION", "REPLACE_WITH_ALTERNATIVE", "CLEAR_SELECTION"]

  - id: ENT-U2-011
    name: SlotSelectionChangeResult
    owner: C3
    description: Observable state update after slot change; triggers stale-lock behavior per ADR-006/BR1.9.
    fields:
      - { name: status, type: "APPLIED|REJECTED|NO_CHANGE", required: true }
      - { name: weeklyPlanState, type: WeeklyPlanState, required: true }
      - { name: changedSlotRef, type: PlanSlotRef, required: true }
      - { name: lockStaleStatus, type: LockStaleStatus, required: false }
      - { name: rejectionReason, type: string, required: false }
    status_values: ["APPLIED", "REJECTED", "NO_CHANGE"]
    notes: "Changes affect only target slot. If a lock/list snapshot context is active, plan change marks stale per BR1.9; no auto-regeneration."
```

## Entity relationships (logical)
- `RecipeSourceAdapterResult` carries `RecipeDetail`/`RecipeSummary` items with `RecipeSourceStatus`.
- `RecipeDetail.summary` is the normalized `RecipeSummary` (U1 ENT-U1-016 extended by usage).
- `RecipeTimeNormalization` produces `ExclusionReason` data for AC2.2.3 (U1/U2 contract, U4 presentation).
- `SeasonFitEvaluation` depends on `SeasonContext`/`EffectiveSeasonResolution` (U1/C3 baseline).
- `WeeklyNutritionBalanceCue` aggregates from selected recipe `NutritionProfile` values only.
- `SlotSelectionChangeResult.weeklyPlanState` remains the single source for Today/Plan synchronization (R-06).