# Reliability Design — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`, C2/C3 + A1)  
**Stage:** NFR Design (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [reliability-requirements.md](../nfr-requirements/reliability-requirements.md) (NFR2.1–NFR2.5)
- [rules.md](../functional-design/rules.md) (BR2.1–BR2.9)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (A1, C3, ADR-006, R-06)
- [security-design.md](security-design.md), [performance-design.md](performance-design.md)

## Design goals
- Make A1 failures non-fatal and always recoverable through mandatory curated/imported fallback where available (NFR2.1).
- Expose explicit typed states; never silently fail or fabricate (NFR2.2).
- Preserve C3 plan-state integrity and ADR-006 stale semantics without auto-regeneration (NFR2.3).
- Keep deterministic normalization/eligibility behavior (NFR2.5).

## Patterns and decisions

| Area | Design | Addresses | Notes |
|---|---|---|---|
| A1 fallback chain | Adapter chain/decorator order: **gate check (default-off/OQ1)** → optional live query (bounded timeout, 0–2 idempotent read retries only when allowed) → parse/map → curated fallback resolver. Every path returns typed `RecipeSourceAdapterResult` with status/investigation outcome/fallback reason; no raw errors cross boundaries. | NFR2.1, NFR3.2, NFR3.3, NFR5.2 | Q1=A. FQ1=A; mandatory fallback always available/runnable for MVP. |
| Typed explicit states | Domain results carry explicit enums/statuses: time normalization (`KNOWN|MISSING|AMBIGUOUS`), season fit (`IN_SEASON|OUT_OF_SEASON|SEASON_FIT_UNKNOWN`), nutrition availability labels (`Nutrition information unavailable`), A1 statuses, slot/alternatives outcomes, exclusion reasons. UI-facing copy is U4 responsibility; U2 emits stable keys/types. | NFR2.2, FR3, FR5, FR6, FR11 | No guessing/fabrication. |
| Deterministic no-guess rules | Weekday eligibility: Mon–Fri Breakfast/Dinner eligible only when prep+cook total is KNOWN and <= 30 minutes; missing/ambiguous excluded with explanation. Season fit unknown when evidence insufficient. Override precedence per BR2.4. | NFR2.5, BR2.3, BR2.4 | Pure functions with unit-test fixtures. |
| C3 state integrity (R-06) | C3 slot operations are scoped to target `PlanSlotRef`; shared Today/Plan state model remains single source of truth for read surfaces (U4 owns persistence/view wiring). U2 never mutates unrelated slots. | NFR2.3, BR2.7, R-06 | Contract-level behavior; persistence refined with U1/U4. |
| ADR-006 stale signaling only | Plan changes affecting active lock/list context mark grocery/list context stale with exact warning `"Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."` (BR1.9 semantics preserved). U2 never triggers auto-regeneration and never performs ingredient freeze; snapshot IDs+context handled by U3/C4. | NFR2.3, BR2.8, ADR-006 | Regeneration requires explicit Generate/Regenerate + confirmation when edits exist (U3/U4 flow). |
| Graceful degradation | Live adapter failures/timeouts/parse issues/restrictions do not crash or block unrelated local planning. Curated/local data path remains usable. Cancellable async operations map cancellation to typed non-fabricated outcomes. | NFR2.4, NFR5.2 | Supports core on-device usability (FR11). |

## Illustrative fallback mapping (non-normative)
```kotlin
when (live) {
  is Timeout -> fallback(reason = "timeout")
  is ParseError -> fallback(reason = "parse_error_mapped")
  is Restricted -> typed(status = RESTRICTED, fallback = curatedIfAvailable())
  else -> mappedTypedResult()
}
```

## Risks and follow-ups
- **OQ1 unresolved:** live path remains disabled by default; curated fallback coverage/quality must be verified for MVP scenarios.
- **OQ5/OQ7 mapping details:** normalization/season mapping ambiguity handled by explicit unknown/typed outcomes until confirmed.
- **Persistence transactions:** U1/U4 own durable state; U2 must keep contract transitions deterministic for safe composition.