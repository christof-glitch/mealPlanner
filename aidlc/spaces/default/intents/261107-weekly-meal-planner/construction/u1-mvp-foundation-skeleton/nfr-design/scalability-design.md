# Scalability Design — U1 `u1-mvp-foundation-skeleton`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U1 — MVP foundation + integration slice (Android app)  
**Stage:** NFR Design (construction, per-unit)  
**Date:** 2026-11-08

## Upstream
- [scalability-requirements.md](../nfr-requirements/scalability-requirements.md) (local `NFR3.4`; service-style scalability `N/A`/limited)
- [nfr-design-questions.md](nfr-design-questions.md)

## Applicability
Service-style scalability (multi-tenant load, horizontal scaling, elastic throughput, sharding, load balancers) is **`N/A`** for U1 because the MVP is a single-user, on-device Android application with no backend service.

## Patterns and decisions

| Area | Design | Addresses | Notes |
|---|---|---|---|
| Local data growth | Bound persisted data to normal weekly usage: active weekly plan state, lock snapshot context, shopping list + edits, selected store, season override, source/fallback/status metadata. | NFR3.4 | Historical plan/snapshot retention or cleanup is a future decision if scope expands. |
| Computation scale | Keep merge/presence projection and state derivation efficient for expected list sizes (weekly household list). Avoid unbounded in-memory duplication. | NFR3.4 | No load target; optimize only if profiling shows issues. |
| Adapter concurrency | Bounded async calls; cancel superseded lookups where safe; no unbounded parallel external requests. | NFR3.2, NFR3.4 | Protects device/network resources, not server scale. |

## N/A
- Multi-tenant isolation: `N/A`.
- Horizontal/vertical server scaling: `N/A`.
- Distributed caching tiers/CDN for backend: `N/A`.
- Capacity thresholds/auto-scaling rules: `N/A`.
