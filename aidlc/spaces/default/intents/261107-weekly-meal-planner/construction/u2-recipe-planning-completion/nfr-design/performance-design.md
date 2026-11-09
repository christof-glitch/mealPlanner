# Performance Design — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`, C2/C3 + A1)  
**Stage:** NFR Design (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [performance-requirements.md](../nfr-requirements/performance-requirements.md) (NFR5.1–NFR5.3)
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md) (BR2.3–BR2.6)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (A1 bounded adapter)
- [nfr-design-questions.md](nfr-design-questions.md) (Q1=A, Q2=A)

## Design goals
- Keep on-device C2/C3 operations responsive for the MVP curated dataset (assumption: ~500 recipes / ~3,500 ingredient lines) without ANR or user-visible freeze (NFR5.1).
- Ensure A1 operations never block unrelated local planning interactions (NFR5.2).
- Avoid wasted computation and never fabricate precision when data is missing (NFR5.3).

## Patterns and decisions

| Area | Design | Addresses | Notes |
|---|---|---|---|
| Pure domain computations | Implement weekday time normalization (BR2.3), season fit evaluation (BR2.4), nutrition/cue aggregation inputs (BR2.5), alternatives filtering (BR2.6), and C3 slot-state transitions as deterministic, side-effect-light domain functions where possible. | NFR5.1, NFR2.5 | Pure functions are cheap to test and avoid hidden I/O. |
| Bounded dataset assumption | Design queries/filters over curated catalog with linear/bounded scans acceptable for MVP dataset assumption; avoid unbounded loops over untrusted input. Add indexes/normalized lookup maps only if implementation evidence shows need. | NFR5.1, NFR1.2 | Exact p95/ms targets deferred to build-and-test profiling. |
| No persistent U2 cache (MVP) | No persistent cache in U2 domain layer. Optional short-lived in-memory memoization is allowed only for repeated catalog scans in the same operation scope if profiling proves need; memo keys must be bounded and derived from validated inputs. | NFR5.1 | Q2=A. U1/U4 own persistence. |
| Non-blocking A1 adapter | A1 calls run off the UI thread via structured async (coroutines/dispatcher boundary). Adapter port returns typed `RecipeSourceAdapterResult`; callers may observe progress/status through existing typed states. Local C3 slot operations and curated catalog browsing remain available while A1 is pending/failing. | NFR5.2 | Q1=A; boundary owned by app/U4 composition but U2 contract must be async-friendly. |
| A1 timeout/retry budget | Per live attempt uses bounded timeout (target 5–10s). Limited safe/idempotent read retries (0–2 with backoff) only for confirmed/allowed live path. Timeout/restriction/parse/unavailable/not_supported maps to typed status and curated fallback; never blocks indefinitely. | NFR5.2, NFR2.1 | FQ1=A: live path default-off; retries never used as a bypass. |
| Missing-data short-circuit | Nutrition aggregation processes only `NutritionProfile` records supplied in `WeeklyNutritionBalanceCueRequest` (C3/U4 assembled context); missing/unavailable profiles are counted/excluded, not recovered by extra lookups. Season evaluation returns `SEASON_FIT_UNKNOWN` when evidence insufficient (BR2.4). | NFR5.3, FR6, FR5 | No false precision, no wasted recovery work. |
| Cancellation/cooperation | Async A1 operations accept cancellation where the platform/runtime supports it; cancellation maps to a typed non-fabricated unavailable/fallback outcome as appropriate, not a crash. | NFR5.2, NFR2.4 | Refined in Code Generation with Kotlin coroutine patterns. |

## Illustrative boundary (non-normative)
```kotlin
suspend fun fetch(request: RecipeSourceQuery): RecipeSourceAdapterResult = withContext(ioDispatcher) {
  if (!gate.allowLiveSource()) return curatedFallback(request, reason = "live_disabled_default")
  withTimeout(budgetMs) { retryingIdempotentRead(maxRetries = 2) { liveClient.query(request) } }
    .mapToTypedResultOrFallback(request)
}
```

## Risks and follow-ups
- **Dataset growth:** if curated catalog exceeds MVP assumption, revisit indexing/memoization (NFR1.2).
- **Exact thresholds:** set concrete device/profile targets and profiling method in build-and-test; do not weaken quality targets.
- **A1 live feasibility:** OQ1 remains open; fallback path must stay fast and mandatory.