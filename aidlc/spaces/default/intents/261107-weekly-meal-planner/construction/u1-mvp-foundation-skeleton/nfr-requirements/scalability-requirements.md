# Scalability Requirements — U1 `u1-mvp-foundation-skeleton`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U1 — MVP foundation + integration slice (Android app)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-08

## Upstream
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (no dedicated inception scalability NFR ID; Android MVP scope)
- [nfr-requirements-questions.md](nfr-requirements-questions.md) (Q4=A, summary confirmed)

## Applicability
Service-style scalability (multi-tenant load, horizontal scaling, throughput SLOs) is **`N/A`/limited** for U1 because this is a single-user on-device Android MVP slice with no backend service in MVP.

## Requirements

| ID | Inherits | Category | Requirement | Target / Rationale | Notes |
|---|---|---|---|---|---|
| NFR3.4 | NFR3 | Local data growth | Local persisted data remains reasonable for MVP usage (one active weekly plan context, lock snapshot context, shopping list/edits, selected store, season override, source/status metadata). | No multi-tenant/load target; data volume bounded by normal weekly planning usage. | If historical plans/snapshots are added later, retention/cleanup becomes an NFR Design decision. |

## N/A rationale
- No server-side concurrency, tenant isolation, or elastic scaling targets apply to U1 baseline.
