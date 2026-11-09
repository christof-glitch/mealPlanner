# Scalability Design — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`, on-device)  
**Stage:** NFR Design (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [scalability-requirements.md](../nfr-requirements/scalability-requirements.md) (NFR1.1–NFR1.3)
- [performance-design.md](performance-design.md) (bounded dataset, no persistent cache by default)
- [nfr-design-questions.md](nfr-design-questions.md) (Q2=A)

## Design goals
- Make MVP scalability constraints explicit: no server scaling, bounded on-device data, limited local concurrency.
- Keep design simple enough for a library unit while avoiding patterns that block future growth decisions.

## Patterns and decisions

| Area | Design | Addresses | Notes |
|---|---|---|---|
| No server scalability | U2 has no backend components, queues, shards, or multi-tenant scaling targets. All C2/C3/A1-boundary logic runs within the Android app process/model. | NFR1.1 | If sync/backend is introduced later, revisit as a separate intent/decision. |
| Bounded on-device data | Curated/imported fallback catalog, season mappings, and nutrition/tag metadata are bounded by MVP assumptions (~500 recipes / ~3,500 ingredient lines). Repository/query interfaces accept filters and return typed results without requiring whole-dataset copies unless needed. | NFR1.2, NFR5.1 | Avoid premature optimization; measure in build-and-test. |
| Structured local concurrency | Use structured async only for A1 adapter calls and observable async boundaries; C2 normalization/evaluation can be synchronous/pure. No distributed locks, partitioning, or parallel fan-out beyond bounded local needs. | NFR1.3, NFR5.2 | Cancellation and typed failure mapping required (performance/security/reliability designs). |
| Growth guardrails | Keep data models/provenance explicit (`RecipeSourceStatus`, fallback reasons, typed statuses) so future catalog expansion or optional live source does not require unsafe implicit behavior. | NFR1.2, NFR2.2 | OQ1/OQ5–OQ7 may affect future scale/data shape. |

## Risks and follow-ups
- **Catalog expansion:** larger datasets may require indexed local storage/search; deferred to later units/intents.
- **Live A1 enablement:** if OQ1 is resolved and live usage grows, add quotas/rate awareness at adapter config level; not required for MVP default-off path.