# U2 Functional Design — Rules (`u2-recipe-planning-completion`)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`)  
**Stage:** Functional Design (construction, per-unit)  
**Date:** 2026-11-09

## Sources
- [entities.md](entities.md)
- [U1 rules](../../u1-mvp-foundation-skeleton/functional-design/rules.md) (BR1.1–BR1.14; preserve invariants)
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (FR2–FR6, FR11, FR12, NFR1–NFR5, OQ1, OQ5–OQ7)
- [stories.md](../../../inception/user-stories/stories.md) (US1.1, US1.2, US2.1–US2.5, US5.4–US5.6)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (C2/C3, A1)
- [functional-design-questions.md](functional-design-questions.md) (Q1=B+FQ1=A, Q2–Q6=A)

## Rules — source of truth

```yaml
rules:
  - id: BR2.1
    title: Sydänmerkki adapter path, gated live execution, mandatory curated fallback
    scope: C2/A1
    type: integration
    requirement_refs: [FR4, FR11, NFR2, NFR3]
    story_refs: [US2.5, US5.4]
    statement: "U2 defines/implements the Sydänmerkki recipe-source adapter path behind A1/C2 port and returns typed outcomes (`ok|fallback_used|unavailable|restricted|timeout|parse_error_mapped|not_supported`) plus investigation outcome. Live sydanmerkki.fi query execution is attempted only when feasibility/terms/allowed access are confirmed (OQ1); otherwise adapter must not scrape/violate terms and must return typed restricted/unavailable/not_supported with mandatory curated/imported Sydänmerkki-aligned fallback as runnable source. Fallback is not emergency-only. Every exposed recipe/result set carries clear RecipeSourceStatus/fallback reason."
    error_or_fallback: "On restriction/failure/unconfirmed access, use curated/imported fallback or explicit unavailable state; never fabricate Sydänmerkki compliance, fields, or recipes."
    source: "FR4; FQ1=A"

  - id: BR2.2
    title: Recipe normalization and safe partial-data presentation contracts
    scope: C2
    type: validation
    requirement_refs: [FR2, FR4, FR5, FR6, NFR2]
    story_refs: [US2.1, US2.5]
    statement: "C2 normalizes recipe details to expose minimum decision data when available: title, ingredients, preparation/cooking guidance, total-time verifiability, season fit inputs/label, nutrition availability/cues, and source/fallback status. Missing fields are explicitly labeled/unavailable in contract fields (never fabricated). Recipes remain viewable/selectable unless another explicit rule excludes them (e.g., weekday time eligibility for a specific slot context)."
    error_or_fallback: "Partial/missing fields => `dataCompleteness` PARTIAL/MINIMAL, null/empty optional fields, explicit unavailable labels/statuses; preserve source status."
    source: "FR2, AC2.1.1-AC2.1.3"

  - id: BR2.3
    title: Weekday total-time normalization without guessing
    scope: C2
    type: business
    requirement_refs: [FR3, NFR2]
    story_refs: [US2.2]
    statement: "For Monday–Friday Breakfast/Dinner slot contexts, C2 computes `RecipeTimeNormalization`: `totalTimeMinutes` only when confidently parseable as integer minutes; `timeVerifiability` is `KNOWN|MISSING|AMBIGUOUS`. Ranges, conflicting prep/cook fields, unit confusion, and unparseable text are AMBIGUOUS and not guessed. `weekdayEligible` is true only when KNOWN and totalTimeMinutes <= 30. Recipes with totalTimeMinutes > 30 or AMBIGUOUS/MISSING time are excluded from weekday suggestions and carry ExclusionReason (`WEEKDAY_TIME_OVER_30` or `WEEKDAY_TIME_AMBIGUOUS`). U1/U2 own explanation data; U4 presents."
    error_or_fallback: "No silent inclusion/exclusion ambiguity; attach ExclusionReason with slot/candidate ref and source marker."
    source: "FR3, AC2.2.1-AC2.2.3, BR1.2, Q4=A, OQ7"

  - id: BR2.4
    title: Season fit resolution, curated mapping, and override precedence
    scope: C2/C3
    type: business
    requirement_refs: [FR5, FR12, NFR2, NFR3]
    story_refs: [US2.3]
    statement: "C3 resolves effective season context from device date and optional coarse location hint/area context by default; user override mode `DEFAULT|OVERRIDE|RESET` takes precedence until reset and is representable/persistable via preferences contract. C2 computes season fit using recipe season tags or curated mapping when present; if season fit cannot be determined, result is `SEASON_FIT_UNKNOWN` with clear explanation key. The system must not incorrectly claim a season match."
    error_or_fallback: "Missing evidence => `SEASON_FIT_UNKNOWN`; missing device/location signals => effective resolution may be UNKNOWN with safe fallback behavior."
    source: "FR5, AC2.3.1-AC2.3.4, BR1.4, Q2=A, OQ5"

  - id: BR2.5
    title: Nutrition cues and minimal non-medical planning aid
    scope: C2 (aggregation) / C3-U4 orchestration boundary (input assembly)
    type: business
    requirement_refs: [FR6, NFR2, NFR5]
    story_refs: [US2.4]
    statement: "C2 exposes nutrition attributes only when reliably available (e.g., energyKcal/proteinG/carbG/fatG/fiberG/sydanmerkkiNutritionFlags, sourceConfidence). Missing/partial data sets `nutritionAvailability` to `UNAVAILABLE|PARTIAL` and uses label `Nutrition information unavailable` where data is absent; recipe viewing/selection is not blocked. C2 computes `WeeklyNutritionBalanceCue` only from `WeeklyNutritionBalanceCueRequest` inputs supplied by the C3/U4 orchestration boundary (selected recipe IDs and/or resolved `NutritionProfile` data). C2 must not read `WeeklyPlanState` directly; C3 remains authoritative for selected slot/recipe state (R-06/BR2.7). The cue is a simple weekly balance view from available attributes only (counts/averages/tag counts); unavailable data is excluded from calculations and exclusion count is indicated. No medical targets, dosing, diagnoses, or treatment advice."
    error_or_fallback: "Partial/missing profiles => safe partial or UNAVAILABLE cue with explicit exclusions/counts; no false precision."
    source: "FR6, AC2.4.1-AC2.4.4, BR1.14, Q3=A, OQ6"

  - id: BR2.6
    title: Alternatives respect eligibility, season, and fallback status
    scope: C2
    type: business
    requirement_refs: [FR2, FR3, FR4, FR5, NFR2]
    story_refs: [US1.2, US2.1, US2.2, US2.3, US2.5]
    statement: "C2 `getAlternatives` returns slot-appropriate alternatives considering weekday eligibility/time verifiability, effective season fit context, and source/fallback status. Alternatives may be empty with explicit reason and excluded-candidate data where useful. Excluded weekday-time candidates carry ExclusionReason; fallback-sourced alternatives carry source status."
    error_or_fallback: "No eligible alternatives => `EMPTY`/`FALLBACK`/`UNAVAILABLE` result with reason; never fabricate alternatives."
    source: "US1.2 AC1.2.1, Q5=A"

  - id: BR2.7
    title: Single source of truth for Today/Plan and per-slot changes only
    scope: C3
    type: state
    requirement_refs: [FR1, FR2, FR9, NFR2]
    story_refs: [US1.1, US1.2]
    statement: "WeeklyPlanState/slot selections are the single source of truth shared by Today and Plan views (R-06). C3 slot operations (`setSlotSelection`, `replaceWithAlternative`, `clearSlotSelection` as applicable) affect only the target slot and preserve other slot selections. Updated observable state is emitted after applied changes."
    error_or_fallback: "Reject invalid slot refs/operations with typed rejection; do not mutate unrelated slots."
    source: "AC1.1.3, AC1.2.2-AC1.2.3, R-06, Q5=A"

  - id: BR2.8
    title: Slot changes mark lock stale without auto-regeneration
    scope: C3/C4 boundary
    type: state
    requirement_refs: [FR9, NFR2]
    story_refs: [US1.2, US3.2]
    statement: "When a plan/slot selection changes and an active lock snapshot or generated shopping list context exists, C3/C1 state marks lock/list context stale per ADR-006/BR1.9 and exposes the exact stale warning semantics required downstream: `Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.` U2 must not trigger automatic regeneration."
    error_or_fallback: "If stale signaling cannot complete, preserve explicit stale state on next observable update; never auto-regenerate."
    source: "BR1.9, ADR-006/R-01-R-03, AC1.2.3"

  - id: BR2.9
    title: Local/fallback reliability and privacy-safe adapter behavior
    scope: C2/C3/A1
    type: reliability
    requirement_refs: [FR11, FR12, NFR2, NFR3, NFR5]
    story_refs: [US5.4, US5.5, US5.6]
    statement: "Recipe/planning completion contracts must support typed fallback/unknown/unavailable states and keep core local planning behavior usable. Adapter calls are bounded/tolerant and map failures to explicit statuses; curated/imported/local data can satisfy MVP flows when live sources fail. No MealPlanner account; only necessary data used; no secrets or unnecessary personal data in contract requirements."
    error_or_fallback: "Live source failure => fallback/partial/empty with status and next action metadata; preserve current plan/list state."
    source: "FR11, FR12, BR1.13"
```

## Rule notes
- BR2.* completes U2 scope; BR1.* remain authoritative for shared invariants (ADR-006 stale warning exact text, no fabrication, R-02 split, PRISMA name/presence only where relevant, U1 fallback boundaries).
- IDs use `BR{group}.{seq}` for traceability sensor compatibility.