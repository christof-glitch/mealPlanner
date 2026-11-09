# Scalability Requirements — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`, on-device)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-09

## Sources
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (NFR1, MVP scope)
- [nfr-requirements-questions.md](nfr-requirements-questions.md) (Q4=A)

## Requirements

| ID | Requirement | Target / measure | Inherited from | Notes |
|---|---|---|---|---|
| NFR1.1 | No server scalability targets for U2 MVP | U2 runs as in-app/library logic in a single Android app; no backend service, no horizontal scaling, no multi-tenant load targets. Capacity is bounded by on-device resources and MVP dataset assumptions. | NFR1 | Consistent with Android-only MVP and C2/C3/A1 boundary scope. |
| NFR1.2 | Bounded on-device data growth assumptions | Curated/imported fallback catalog, season mappings, and nutrition/tag metadata remain bounded for MVP (planning assumption aligned to Q2/A: ~500 recipes / ~3,500 ingredient lines). Data structures and queries avoid unnecessary duplication; revisit if sync/backend or large catalogs are introduced. | NFR1, NFR5 | Not a hard production SLA; design guardrail. |
| NFR1.3 | Concurrency limited to local async boundaries | Concurrency is limited to structured async (e.g., coroutines/Flow) for A1 adapter calls, state observation, and local operations; no distributed concurrency/partitioning requirements. | NFR1 | Implementation detail refined in NFR Design. |

## Non-goals (MVP)
- Cloud autoscaling, sharding, multi-region replication, or backend throughput targets.
- Load testing beyond local/per-unit performance validation appropriate for Android app scope.