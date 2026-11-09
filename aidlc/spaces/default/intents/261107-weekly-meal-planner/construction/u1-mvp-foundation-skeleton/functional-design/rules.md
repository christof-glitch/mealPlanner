# U1 Functional Design — Rules (`u1-mvp-foundation-skeleton`)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U1 — MVP foundation + integration slice  
**Stage:** Functional Design (construction, per-unit)  
**Date:** 2026-11-08

## Sources
- [entities.md](entities.md)
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (FR1–FR12, NFR1–NFR5)
- [stories.md](../../../inception/user-stories/stories.md) (US1.1–US5.6)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (C1–C6, A1–A4, R-01–R-04)
- [components.md](../../../inception/domain-design/components.md) (ADR-006/R-01–R-03)
- [functional-design-questions.md](functional-design-questions.md) (Q1=A,Q2=C,Q3=B,Q4=A,Q5=B,Q6=A,Q7=A,Q8=A)

## Rules — source of truth

```yaml
rules:
  - id: BR1.1
    title: Weekly schedule structure
    scope: C1
    type: validation
    requirement_refs: [FR1]
    story_refs: [US1.1]
    statement: "The plan model must represent exactly 7 days (Saturday–Friday context) with required slots: Saturday/Sunday = Breakfast,Lunch,Dinner; Monday–Friday = Breakfast,Dinner. Missing required slots are invalid plan state; unfilled slots may be EMPTY but must remain visible/addressable."
    error_or_fallback: "Reject invalid slot structure at domain boundary; preserve explicit EMPTY state."

  - id: BR1.2
    title: Weekday time eligibility baseline
    scope: C1/C2 baseline
    type: business
    requirement_refs: [FR2, FR3]
    story_refs: [US2.2]
    statement: "Recipes assigned/suggested for Monday–Friday meal slots must have total prep+cooking time <= 30 minutes when time data is available. If time data is missing or ambiguous, recipe is ineligible for weekday slots and must carry an ExclusionReason (AC2.2.3 baseline)."
    error_or_fallback: "Do not silently include ineligible recipes; provide ExclusionReason with reasonCode WEEKDAY_TIME_OVER_30 or WEEKDAY_TIME_AMBIGUOUS and affected slot/candidate refs."

  - id: BR1.3
    title: Sydänmerkki-attempt and fallback visibility
    scope: C2 baseline
    type: integration
    requirement_refs: [FR4]
    story_refs: [US2.3]
    statement: "Recipe acquisition must attempt Sydänmerkki-compatible source path (sydanmerkki.fi when feasible/allowed) or use curated/imported Sydänmerkki fallback. Every recipe/result set exposed to C1/C4 must carry DataSourceStatus indicating live/fallback/unavailable and fallback reason when applicable."
    error_or_fallback: "If live query fails/unavailable, use curated/imported fallback or explicit unavailable state; never fabricate Sydänmerkki compliance or recipe data."

  - id: BR1.4
    title: Season context, unknown fit, and override
    scope: C3 baseline
    type: business
    requirement_refs: [FR5]
    story_refs: [US2.4]
    statement: "Season evaluation uses available local/date/location context and recipe season metadata/tags/curated mapping. Missing season evidence yields `SEASON_FIT_UNKNOWN`, labeled as such. User season override/default/reset must be representable and take precedence for effective season context until reset."
    error_or_fallback: "No silent guessing; unknown fit remains explicit."

  - id: BR1.5
    title: Store selection and PRISMA name/presence only
    scope: C5 baseline
    type: integration
    requirement_refs: [FR7, FR8]
    story_refs: [US4.2, US5.1]
    statement: "Nearest-store suggestion is best effort and only with location permission; manual store selection is always available. PRISMA checks are best-effort ingredient name/presence matches for the selected store only and must return PRESENT, NOT_FOUND, or UNKNOWN with reason. Stock quantity, aisle certainty, and guaranteed availability are prohibited outputs."
    error_or_fallback: "Permission denied, store unknown, lookup failure, or ambiguous match => UNKNOWN/NOT_FOUND with explicit A2 reason and manual checklist fallback path."

  - id: BR1.6
    title: Lock snapshot stores IDs/selections + context only (ADR-006/R-01)
    scope: C4
    type: state
    requirement_refs: [FR9]
    story_refs: [US3.1, US3.2]
    statement: "Lock/finalize captures an immutable snapshot containing locked recipe IDs/selections and minimal plan context (selected store ref, season context/override, schedule policy version as applicable). Snapshot must not freeze ingredient lines as authoritative truth."
    error_or_fallback: "If required snapshot context cannot be captured, lock/generation request fails with typed reason and no fabricated snapshot."

  - id: BR1.7
    title: Shopping list generated only by explicit action
    scope: C4
    type: workflow
    requirement_refs: [FR9]
    story_refs: [US3.1, US3.2]
    statement: "Shopping list generation/regeneration occurs only after recipes are locked/finalized and only when the user explicitly invokes Generate or Regenerate. No background auto-generation or auto-regeneration is allowed."
    error_or_fallback: "If preconditions fail, return typed result NEEDS_CONFIRMATION/FAILED with userActionRequired."

  - id: BR1.8
    title: Ingredient re-resolution and missing data handling (ADR-006/R-03)
    scope: C4/C2
    type: business
    requirement_refs: [FR9]
    story_refs: [US3.1, US3.2]
    statement: "At generation/regeneration, C4 re-resolves ingredient data from C2 for locked recipe IDs using snapshot context. Missing, changed, or unavailable ingredient data must be surfaced explicitly as unresolved/unknown items or reasons. The system must not fabricate quantities, units, or ingredients."
    error_or_fallback: "Return unresolvedIngredients/fallback reasons; keep list usable with explicit gaps."

  - id: BR1.9
    title: Stale lock state and exact warning text (ADR-006/R-02)
    scope: C1/C4
    type: state
    requirement_refs: [FR9]
    story_refs: [US3.2]
    statement: "Any plan change affecting a locked context marks lock/list state stale (e.g., STALE_LOCK) and presents the exact warning text: `Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.` The system must not auto-regenerate. Regeneration with existing list edits requires explicit user confirmation before replacing edited content."
    error_or_fallback: "If stale state cannot be persisted, still block silent regeneration and require explicit user action on next interaction."

  - id: BR1.10
    title: Measurable ingredient merge baseline
    scope: C4
    type: business
    requirement_refs: [FR9]
    story_refs: [US3.1]
    statement: "When generating/regenerating, combine same ingredient by normalized name + compatible units/amounts. If units/amounts are incompatible or normalization confidence is insufficient, keep separate items and expose reason/ambiguity as needed. No silent quantity fabrication."
    error_or_fallback: "Separate items; preserve source recipe refs where available."

  - id: BR1.11
    title: Export uses current edited list (U1 fallback boundary)
    scope: C6
    type: workflow
    requirement_refs: [FR10]
    story_refs: [US3.3, US4.1]
    statement: "Share/copy export operates on the current edited shopping list state at export time. Share-sheet/copy-text fallback is mandatory in U1. S-ostoslista official-compatible method is not required by U1 baseline; extraction seam is deferred to U3 (Q3=B/Q7=A)."
    error_or_fallback: "If share sheet/copy fails, return FAILED/FALLBACK_INSTRUCTIONS_SHOWN with user-actionable instructions and no data loss."

  - id: BR1.12
    title: Batch presence split operations and coordination (R-02, Q2=C/Q6=A)
    scope: C4/C5
    type: integration
    requirement_refs: [FR8]
    story_refs: [US4.2]
    statement: "C5 owns `MapIngredientsToStorePresenceBatch` (ingredient refs/query context + selected store + best-effort lookup => batch StorePresenceResult projections). C4 owns `ApplyBatchPresenceToList` (current list/items + mapped projections => updated items with explicit unknown/reason/remap state). C4 must never perform PRISMA lookup; C5 must never own list merge or stale semantics. Unknown-after-edit/merge-regenerate remains C4-owned. U3 orchestrates."
    error_or_fallback: "Mapping ambiguity or missing projection => UNKNOWN presence with reason; do not fabricate PRESENT/NOT_FOUND."

  - id: BR1.13
    title: Explicit fallback states and local continuity metadata
    scope: A1–A3/C1–C6
    type: reliability
    requirement_refs: [FR11, FR12, NFR1, NFR4]
    story_refs: [US1.1, US5.1, US5.2, US5.3, US5.4, US5.5, US5.6]
    statement: "U1 baseline contracts must support typed results with explicit unavailable/fallback/unknown states and next actions where applicable. Logical local persistence (Q4=A) must preserve plan state, lock snapshot context, shopping list/edits, selected store, season override, and source/fallback/status metadata needed for Today/Plan continuity. Location permission is optional and manual store selection remains available."
    error_or_fallback: "On partial local data, show available data plus explicit unavailable/fallback state; no fabricated data."

  - id: BR1.14
    title: Nutrition and medical-scope baseline
    scope: C2/A1 baseline
    type: compliance
    requirement_refs: [FR6, NFR5]
    story_refs: [US2.1, US2.5]
    statement: "Nutrition cues/info may be shown only when data is available; otherwise label `Nutrition information unavailable`. Nutrition Balance Cue, when implemented by downstream units, is view-only and non-medical (no diagnoses/treatment/personalized medical dosing). U1 contracts must allow missing nutrition data without blocking core plan/list flows."
    error_or_fallback: "Missing nutrition => unavailable label; never infer medical advice."
```
