# Performance Requirements — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`, C2/C3 + A1)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-09

## Sources
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (NFR5)
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (A1 bounded adapter)
- [nfr-requirements-questions.md](nfr-requirements-questions.md) (Q1=A, Q2=A)

## Requirements

| ID | Requirement | Target / measure | Inherited from | Notes |
|---|---|---|---|---|
| NFR5.1 | On-device recipe/planning operations remain responsive | Local catalog query, weekday eligibility normalization (BR2.3), season fit evaluation (BR2.4), alternatives selection (BR2.6), recipe normalization, and C3 slot state updates complete for a typical MVP curated dataset (assumption: up to ~500 recipes / ~3,500 ingredient lines) without ANR or user-visible freeze on supported Android devices. Exact p95/p99 ms targets deferred to NFR Design/testing. | NFR5 | Applies to C2/C3 domain logic; no unbounded loops over untrusted input. |
| NFR5.2 | External/adapter operations do not block local interactions | A1 (Sydänmerkki/recipe-source) calls, when enabled by OQ1 confirmation, run off the UI thread with bounded per-attempt timeout (target 5–10s), limited safe/idempotent read retries (0–2 with backoff) only for confirmed/allowed live path. Any timeout/restriction/parse/unavailable result maps to typed status and triggers mandatory curated fallback; unrelated local planning/slot operations remain usable. | NFR5, NFR2 | Q1=A; A1 contract-summary behavior. |
| NFR5.3 | Nutrition/season/alternative computations use available data only | `WeeklyNutritionBalanceCue` (C2, from `WeeklyNutritionBalanceCueRequest`) and season/eligibility computations process only supplied/available records; missing profiles/data are excluded/counted, not retried as expensive recovery. No false precision. | NFR5, FR6 | Prevents wasted work and misleading output. |

## Benchmarks / verification (deferred detail)
- NFR Design/build-and-test define concrete profiling/test harness for curated dataset size and mid-range device class.
- A1 adapter timeout/retry behavior verified by unit tests with fake adapters (no live network required for CI).