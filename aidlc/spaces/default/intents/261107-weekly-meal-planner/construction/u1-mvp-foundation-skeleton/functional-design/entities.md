# U1 Functional Design — Entities (`u1-mvp-foundation-skeleton`)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U1 — MVP foundation + integration slice  
**Stage:** Functional Design (construction, per-unit)  
**Date:** 2026-11-08

## Sources
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (C1–C6, A1–A4, R-01–R-04)
- [components.md](../../../inception/domain-design/components.md), [decisions.md](../../../inception/domain-design/decisions.md) (ADR-001–ADR-006)
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (FR1–FR12, NFR1–NFR5)
- [stories.md](../../../inception/user-stories/stories.md) (US1.1–US5.6), [unit-of-work.md](../../../inception/units-generation/unit-of-work.md)
- [functional-design-questions.md](functional-design-questions.md) (Q1=A,Q2=C,Q3=B,Q4=A,Q5=B,Q6=A,Q7=A,Q8=A; summary confirmed `Looks correct`)

## Scope and defers (R-01)
U1 defines minimum required baseline shapes for C1 and the C2–C6/A1–A3 types it touches. Some fields are intentionally minimal for foundation; U2/U3/U4 refine within additive-first contract policy.

| Area | Status | Owner | Minimum required now | Defer notes |
|---|---|---|---|---|
| C1 plan/slot/local state contracts | Defined | U1 | `PlanId`, `PlanSlotRef`, `MealSlotType`, `PlanSlotState`, `WeeklyPlanState`, `DayKind` | U2 completes plan UX-facing behavior details. |
| C2 recipe/ingredient query DTOs used by C1/C4 | Defined minimum | U1 establishes baseline; U2 completes | `RecipeSummary`, `RecipeCandidateResult`, `IngredientLine`, `IngredientRef`, `RecipeSourceStatus` (minimum shapes in `ENT-U1-016`–`ENT-U1-019`) | Full recipe detail, candidate ranking, Sydänmerkki completeness/metadata verification deferred to U2. |
| C3 season/override DTOs | Defined minimum | U1 establishes baseline; U2 completes | `SeasonContext`, `SeasonOverride`, `EffectiveSeasonResolution` (minimum shapes in `ENT-U1-020`–`ENT-U1-022`) | Suggestion/ranking behavior deferred to U2. |
| C4 lock/snapshot/generation results (ADR-006) | Defined minimum | U1 establishes baseline; U3 completes | `LockSnapshotId`, `PlanSnapshotContext`, `LockedRecipeSelection`, `ShoppingListGenerationRequest`, `ShoppingListGenerationResult`, stale-state fields, exact stale warning text constant | Aggregation merge algorithm details beyond baseline rules deferred to U3; generation trigger remains explicit. |
| C5 store/presence results | Defined minimum | U1 establishes baseline; U2/U3 complete | `StoreRef`, `StoreSelectionSource`, `StorePresenceStatus`, `IngredientPresenceResult`, `BatchPresenceResult`, mapping key refs, `A2`/`A3` refs | PRISMA lookup implementation/official adapter deferred; name/presence only invariant mandatory. |
| C6 share payload/fallback results | Defined minimum (fallback boundary) | U1 establishes fallback boundary; U3 owns extraction seam | `SharePayload`, `ShareResult`, `ShareMethod`, copy-text/share-sheet fallback states, instructions metadata | Extraction seam deferred to U3: `ExportShoppingListUseCase` or equivalent, `ShoppingExportPayload`, `ShoppingExportAdapter`/platform share port, dependencies inward, share-sheet/copy-text default path, official-compatible S-ostoslista adapter behind same port. |
| A1–A3 reason/status shapes | Defined minimum | U1 establishes; U2/U3 extend | `OperationStatus`, `DataSourceStatus`, `FallbackReason`, `ExclusionReason`, `UserSafeMessageRef` | U2 may generalize to reusable `ExclusionOrWarningReason`; U4 owns presentation copy/layout. |
| C4 `ApplyBatchPresenceToList` + C5 `MapIngredientsToStorePresenceBatch` (R-02, Q2=C,Q6=A) | Defined contract names/ownership | C4/C5; U3 orchestrates | Inputs/outputs and coordination rules; C4 never PRISMA lookup; C5 never list merge/stale semantics | Concrete adapter/orchestration implementation deferred to U2/U3 as applicable. |

## Entities — source of truth

```yaml
entities:
  - id: ENT-U1-001
    name: PlanSlotRef
    owner: C1
    description: Stable reference to a weekly plan slot.
    fields:
      - { name: planId, type: PlanId, required: true }
      - { name: dayOfWeek, type: "MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY", required: true }
      - { name: mealType, type: "BREAKFAST|LUNCH|DINNER", required: true }
    status_values: []
    notes: "Day/meal combinations must satisfy FR1/FR2 schedule rules."

  - id: ENT-U1-002
    name: PlanSlotState
    owner: C1
    description: Current state of one plan slot, including lock visibility.
    fields:
      - { name: slotRef, type: PlanSlotRef, required: true }
      - { name: assignedRecipeId, type: RecipeId, required: false }
      - { name: slotStatus, type: "EMPTY|SUGGESTED|SELECTED|LOCKED", required: true }
      - { name: exclusionReason, type: ExclusionReason, required: false }
      - { name: dataSource, type: DataSourceStatus, required: false }
      - { name: lastUpdatedAt, type: Instant, required: false }
    status_values: ["EMPTY", "SUGGESTED", "SELECTED", "LOCKED"]
    notes: "Unfilled slots remain EMPTY (FR11). Exclusion reason used for US2.2 AC2.2.3 baseline."

  - id: ENT-U1-003
    name: WeeklyPlanState
    owner: C1
    description: Aggregate 7-day plan state persisted locally for Today/Plan continuity.
    fields:
      - { name: planId, type: PlanId, required: true }
      - { name: weekStartContext, type: string, required: false }
      - { name: slots, type: "list<PlanSlotState>", required: true }
      - { name: lockedRecipeIds, type: "list<RecipeId>", required: true }
      - { name: lockState, type: "UNLOCKED|LOCKED|STALE_LOCK", required: true }
      - { name: activeLockSnapshotId, type: LockSnapshotId, required: false }
      - { name: staleWarning, type: string, required: false }
    status_values: ["UNLOCKED", "LOCKED", "STALE_LOCK"]
    notes: "When STALE_LOCK, staleWarning must be exact ADR-006 text when shown: `Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.`"

  - id: ENT-U1-004
    name: LockSnapshot
    owner: C4 (baseline contract); C1 references active snapshot
    description: Immutable shopping-generation snapshot per ADR-006/R-01.
    fields:
      - { name: snapshotId, type: LockSnapshotId, required: true }
      - { name: planId, type: PlanId, required: true }
      - { name: lockedRecipeSelections, type: "list<LockedRecipeSelection>", required: true }
      - { name: planContext, type: PlanSnapshotContext, required: true }
      - { name: createdAt, type: Instant, required: true }
      - { name: sourceCatalogVersion, type: string, required: false }
    status_values: []
    notes: "Stores locked recipe IDs/selections + plan context only; ingredients are not frozen except through re-resolution rules."

  - id: ENT-U1-005
    name: LockedRecipeSelection
    owner: C4
    description: Recipe selection captured in lock snapshot.
    fields:
      - { name: slotRef, type: PlanSlotRef, required: true }
      - { name: recipeId, type: RecipeId, required: true }
      - { name: selectionSource, type: "USER_SELECTED|SUGGESTED_ACCEPTED|FALLBACK", required: true }
    status_values: ["USER_SELECTED", "SUGGESTED_ACCEPTED", "FALLBACK"]

  - id: ENT-U1-006
    name: PlanSnapshotContext
    owner: C4
    description: Minimal plan context required to interpret/regenerate from a snapshot.
    fields:
      - { name: selectedStoreRef, type: StoreRef, required: false }
      - { name: seasonContext, type: SeasonContext, required: false }
      - { name: seasonOverride, type: SeasonOverride, required: false }
      - { name: schedulePolicyVersion, type: string, required: false }
    status_values: []
    notes: "No fabricated values; missing context must be explicit."

  - id: ENT-U1-007
    name: ShoppingListGenerationResult
    owner: C4
    description: Typed result of explicit Generate/Regenerate action.
    fields:
      - { name: status, type: "GENERATED|REGENERATED|FAILED|CANCELLED|NEEDS_CONFIRMATION", required: true }
      - { name: snapshotId, type: LockSnapshotId, required: false }
      - { name: shoppingListId, type: ShoppingListId, required: false }
      - { name: items, type: "list<ShoppingListItem>", required: false }
      - { name: unresolvedIngredients, type: "list<IngredientRef>", required: false }
      - { name: fallbackReason, type: FallbackReason, required: false }
      - { name: userActionRequired, type: string, required: false }
    status_values: ["GENERATED", "REGENERATED", "FAILED", "CANCELLED", "NEEDS_CONFIRMATION"]
    notes: "Generation only by explicit action (FR9). Regeneration with edits requires confirmation before destructive replace."

  - id: ENT-U1-008
    name: ShoppingListItem
    owner: C4
    description: Editable shopping-list line item; may carry store presence projection.
    fields:
      - { name: itemId, type: ShoppingListItemId, required: true }
      - { name: displayName, type: string, required: true }
      - { name: normalizedName, type: string, required: false }
      - { name: amount, type: number, required: false }
      - { name: unit, type: string, required: false }
      - { name: amountText, type: string, required: false }
      - { name: checked, type: boolean, required: true }
      - { name: sourceRecipeIds, type: "list<RecipeId>", required: false }
      - { name: presence, type: StorePresenceStatus, required: false }
      - { name: presenceReason, type: A2, required: false }
      - { name: manuallyEdited, type: boolean, required: true }
      - { name: mergeGroupKey, type: string, required: false }
    status_values: []

  - id: ENT-U1-009
    name: StoreRef
    owner: C5
    description: Selected/suggested store reference (manual selection always available).
    fields:
      - { name: storeId, type: string, required: false }
      - { name: displayName, type: string, required: true }
      - { name: chain, type: "PRISMA|OTHER", required: false }
      - { name: selectionSource, type: "MANUAL|SUGGESTED|PREVIOUS", required: true }
      - { name: isConfirmed, type: boolean, required: true }
    status_values: ["MANUAL", "SUGGESTED", "PREVIOUS"]

  - id: ENT-U1-010
    name: StorePresenceResult
    owner: C5
    description: Best-effort name/presence result for one ingredient/query.
    fields:
      - { name: ingredientRef, type: IngredientRef, required: false }
      - { name: queryText, type: string, required: true }
      - { name: storeRef, type: StoreRef, required: true }
      - { name: status, type: "PRESENT|NOT_FOUND|UNKNOWN", required: true }
      - { name: reason, type: A2, required: false }
      - { name: checkedAt, type: Instant, required: false }
      - { name: source, type: "PRISMA|MANUAL_FALLBACK|CACHE|UNAVAILABLE", required: true }
    status_values: ["PRESENT", "NOT_FOUND", "UNKNOWN"]
    notes: "Name/presence only; never stock quantity or guaranteed availability."

  - id: ENT-U1-011
    name: BatchPresenceResult
    owner: C5
    description: Batch output from `MapIngredientsToStorePresenceBatch`.
    fields:
      - { name: storeRef, type: StoreRef, required: true }
      - { name: results, type: "list<StorePresenceResult>", required: true }
      - { name: overallStatus, type: "COMPLETE|PARTIAL|FAILED", required: true }
      - { name: fallbackReason, type: FallbackReason, required: false }
    status_values: ["COMPLETE", "PARTIAL", "FAILED"]

  - id: ENT-U1-012
    name: SharePayload
    owner: C6
    description: Text/export payload used by share-sheet/copy fallback in U1.
    fields:
      - { name: shoppingListId, type: ShoppingListId, required: true }
      - { name: title, type: string, required: true }
      - { name: bodyText, type: string, required: true }
      - { name: includesCheckedItems, type: boolean, required: true }
      - { name: generatedFromEditedList, type: boolean, required: true }
      - { name: instructionsText, type: string, required: false }
    status_values: []
    notes: "U1 fallback boundary only; extraction seam deferred to U3 (Q3=B/Q7=A)."

  - id: ENT-U1-013
    name: ShareResult
    owner: C6
    description: Typed result of fallback share/copy action.
    fields:
      - { name: status, type: "SHARED|COPIED|CANCELLED|FAILED|FALLBACK_INSTRUCTIONS_SHOWN", required: true }
      - { name: method, type: "ANDROID_SHARE_SHEET|COPY_TEXT|MANUAL_INSTRUCTIONS", required: true }
      - { name: payload, type: SharePayload, required: false }
      - { name: fallbackReason, type: A3, required: false }
      - { name: userActionRequired, type: string, required: false }
    status_values: ["SHARED", "COPIED", "CANCELLED", "FAILED", "FALLBACK_INSTRUCTIONS_SHOWN"]

  - id: ENT-U1-014
    name: ExclusionReason
    owner: U1/U2 explanation data; U4 presentation
    description: Minimum U1 weekday-time exclusion/explanation data (Q5=B/Q8=A).
    fields:
      - { name: reasonCode, type: "WEEKDAY_TIME_OVER_30|WEEKDAY_TIME_AMBIGUOUS|DATA_UNAVAILABLE|SOURCE_UNAVAILABLE|OTHER", required: true }
      - { name: messageKeyOrTextSource, type: string, required: true }
      - { name: slotRef, type: PlanSlotRef, required: false }
      - { name: recipeOrCandidateRef, type: string, required: false }
      - { name: sourceMarker, type: "rule|data|fallback|unknown", required: true }
    status_values: ["WEEKDAY_TIME_OVER_30", "WEEKDAY_TIME_AMBIGUOUS", "DATA_UNAVAILABLE", "SOURCE_UNAVAILABLE", "OTHER"]
    notes: "Extensible; U2 may generalize to reusable `ExclusionOrWarningReason`. No fabricated reason details."

  - id: ENT-U1-015
    name: DataSourceStatus
    owner: A1
    description: Cross-cutting source/fallback status used by C2/C3/C5/C6 baselines.
    fields:
      - { name: source, type: "SYDANMERKKI_LIVE|SYDANMERKKI_FALLBACK|PRISMA|SEASON_LOCAL|LOCAL_CACHE|MANUAL|NONE", required: true }
      - { name: freshness, type: "CURRENT|CACHED|STALE|UNKNOWN", required: true }
      - { name: fallbackUsed, type: boolean, required: true }
      - { name: fallbackReason, type: FallbackReason, required: false }
      - { name: userActionRequired, type: string, required: false }
    status_values: []
    notes: "C2 mapping: SYDANMERKKI_LIVE <= recipe source `SYDANMERKKI` with current/verified result; SYDANMERKKI_FALLBACK <= curated/imported/local fallback carrying Sydänmerkki-compatible baseline; NONE/LOCAL_CACHE/MANUAL used only with explicit fallback/unknown semantics. C2 `RecipeSourceStatus` (ENT-U1-019) is the recipe-facing minimum; `DataSourceStatus` is the cross-cutting envelope."

  - id: ENT-U1-016
    name: RecipeSummary
    owner: C2
    description: Minimum recipe summary used by C1/C4 baseline.
    fields:
      - { name: recipeId, type: RecipeId, required: true }
      - { name: title, type: string, required: true }
      - { name: totalTimeMinutes, type: int, required: false }
      - { name: timeConfidence, type: "KNOWN|MISSING|AMBIGUOUS", required: true }
      - { name: sydanmerkkiStatus, type: "VERIFIED|CURATED_FALLBACK|UNAVAILABLE", required: true }
      - { name: sourceStatus, type: RecipeSourceStatus, required: true }
      - { name: seasonFit, type: "IN_SEASON|OUT_OF_SEASON|SEASON_FIT_UNKNOWN", required: false }
      - { name: nutritionAvailability, type: "AVAILABLE|UNAVAILABLE|PARTIAL", required: false }
    status_values: []

  - id: ENT-U1-017
    name: IngredientLine
    owner: C2
    description: Minimum ingredient line re-resolved by C4 at generation.
    fields:
      - { name: ingredientRef, type: IngredientRef, required: false }
      - { name: name, type: string, required: true }
      - { name: normalizedName, type: string, required: false }
      - { name: amount, type: number, required: false }
      - { name: unit, type: string, required: false }
      - { name: amountText, type: string, required: false }
      - { name: availabilityStatus, type: "KNOWN|MISSING|CHANGED|UNAVAILABLE", required: true }
      - { name: sourceRecipeId, type: RecipeId, required: true }
    status_values: ["KNOWN", "MISSING", "CHANGED", "UNAVAILABLE"]
    notes: "No fabricated amount/unit; missing/changed/unavailable must be explicit."

  - id: ENT-U1-018
    name: RecipeCandidateResult
    owner: C2
    description: Minimum query/candidate envelope used by C1 planning hooks, including AC2.2.3 exclusion data.
    fields:
      - { name: queryContext, type: "object", required: false }
      - { name: candidates, type: "list<RecipeSummary>", required: true }
      - { name: excludedCandidates, type: "list<ExcludedRecipeCandidate>", required: false }
      - { name: status, type: "OK|PARTIAL|FALLBACK|UNAVAILABLE", required: true }
      - { name: sourceStatus, type: RecipeSourceStatus, required: true }
      - { name: fallbackReason, type: FallbackReason, required: false }
    status_values: ["OK", "PARTIAL", "FALLBACK", "UNAVAILABLE"]

  - id: ENT-U1-019
    name: RecipeSourceStatus
    owner: C2
    description: Recipe-facing minimum source/fallback status mapped to A1.
    fields:
      - { name: sourceType, type: "SYDANMERKKI|CURATED_SYDANMERKKI_FALLBACK|LOCAL_CACHE|IMPORTED|NONE", required: true }
      - { name: freshness, type: "CURRENT|CACHED|STALE|UNKNOWN", required: true }
      - { name: fallbackUsed, type: boolean, required: true }
      - { name: fallbackReason, type: FallbackReason, required: false }
      - { name: userActionRequired, type: string, required: false }
    status_values: []
    notes: "Maps to ENT-U1-015 as described there; Sydänmerkki attempt/fallback visibility required by FR4/BR1.3."

  - id: ENT-U1-020
    name: SeasonContext
    owner: C3
    description: Minimum effective season context input.
    fields:
      - { name: localDate, type: string, required: false }
      - { name: locationHint, type: string, required: false }
      - { name: resolvedSeason, type: string, required: false }
      - { name: resolutionSource, type: "DEVICE_DATE|DEVICE_LOCATION|DEFAULT|OVERRIDE|UNKNOWN", required: true }
    status_values: []

  - id: ENT-U1-021
    name: SeasonOverride
    owner: C3
    description: User season override/default/reset state.
    fields:
      - { name: mode, type: "DEFAULT|OVERRIDE|RESET", required: true }
      - { name: overrideSeason, type: string, required: false }
      - { name: updatedAt, type: Instant, required: false }
    status_values: ["DEFAULT", "OVERRIDE", "RESET"]

  - id: ENT-U1-022
    name: EffectiveSeasonResolution
    owner: C3
    description: Minimum effective season resolution result used by C1/C2 hooks.
    fields:
      - { name: effectiveContext, type: SeasonContext, required: true }
      - { name: override, type: SeasonOverride, required: false }
      - { name: status, type: "RESOLVED|UNKNOWN|OVERRIDDEN", required: true }
      - { name: fallbackReason, type: FallbackReason, required: false }
    status_values: ["RESOLVED", "UNKNOWN", "OVERRIDDEN"]

  - id: ENT-U1-023
    name: ExcludedRecipeCandidate
    owner: C2/C1
    description: Candidate exclusion record carrying AC2.2.3 minimum data.
    fields:
      - { name: recipeSummary, type: RecipeSummary, required: false }
      - { name: recipeOrCandidateRef, type: string, required: false }
      - { name: exclusionReason, type: ExclusionReason, required: true }
    status_values: []

  - id: ENT-U1-024
    name: ApplyBatchPresenceToListRequest
    owner: C4
    description: Minimum input for C4-owned `ApplyBatchPresenceToList` (R-02, Q2=C/Q6=A).
    fields:
      - { name: shoppingListId, type: ShoppingListId, required: false }
      - { name: items, type: "list<ShoppingListItem>", required: true }
      - { name: projections, type: "list<StorePresenceResult>", required: true }
      - { name: storeRef, type: StoreRef, required: true }
      - { name: mappingContext, type: "object", required: false }
    status_values: []
    notes: "C4 receives mapped projections only; never performs PRISMA lookup."

  - id: ENT-U1-025
    name: ApplyBatchPresenceToListResult
    owner: C4
    description: Minimum output for `ApplyBatchPresenceToList`.
    fields:
      - { name: items, type: "list<ShoppingListItem>", required: true }
      - { name: status, type: "APPLIED|PARTIAL|FAILED", required: true }
      - { name: unknownOrRemapCount, type: int, required: false }
      - { name: fallbackReason, type: FallbackReason, required: false }
    status_values: ["APPLIED", "PARTIAL", "FAILED"]
    notes: "Unresolved mappings become UNKNOWN with reason; C4 owns unknown-after-edit/merge-regenerate behavior."

  - id: ENT-U1-026
    name: MapIngredientsToStorePresenceBatchRequest
    owner: C5
    description: Minimum input for C5-owned `MapIngredientsToStorePresenceBatch`.
    fields:
      - { name: ingredientRefs, type: "list<IngredientRef>", required: false }
      - { name: queryItems, type: "list<object>", required: true }
      - { name: storeRef, type: StoreRef, required: true }
      - { name: lookupContext, type: "object", required: false }
    status_values: []
    notes: "Best-effort name/presence mapping only; no stock quantity/guaranteed availability."

  - id: ENT-U1-027
    name: MapIngredientsToStorePresenceBatchResult
    owner: C5
    description: Minimum output alias/wrapper for `MapIngredientsToStorePresenceBatch`.
    fields:
      - { name: batchResult, type: BatchPresenceResult, required: true }
      - { name: mappingWarnings, type: "list<A2>", required: false }
    status_values: []
    notes: "C5 never owns list merge/stale semantics; returns projections for C4."
```

## Domain notes
- `RecipeId`, `IngredientRef`, `ShoppingListId`, `PlanId`, `LockSnapshotId`, `ShoppingListItemId`, `SeasonContext`, `SeasonOverride`, and `Instant` are logical type aliases/placeholders; concrete Kotlin types are refined in NFR Design/Code Generation.
- All external/integration outcomes must remain typed and explicit; `UNKNOWN`/fallback is preferred over fabrication.
