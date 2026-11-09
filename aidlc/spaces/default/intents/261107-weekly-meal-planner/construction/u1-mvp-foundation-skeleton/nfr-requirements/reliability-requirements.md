# Reliability Requirements — U1 `u1-mvp-foundation-skeleton`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U1 — MVP foundation + integration slice (Android app)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-08

## Upstream
- [requirements.md](../../../inception/requirements-analysis/requirements.md) — supports inception **NFR2 Usability/clarity of fallback states** and local continuity concerns. Local derived IDs in this file (`NFR1.x`, `NFR2.x`) are U1 artifact-local namespaces; semantic mapping to inception NFR1–NFR5 is recorded in [traceability.json](traceability.json).
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md) (BR1.3, BR1.5, BR1.8, BR1.9, BR1.11, BR1.13)
- [nfr-requirements-questions.md](nfr-requirements-questions.md) (Q3=A, summary confirmed)

## Requirements

| ID | Inherits | Category | Requirement | Target / Behavior | Notes |
|---|---|---|---|---|---|
| NFR1.1 | NFR1 | Local continuity | Core planning and shopping-list baseline remains usable with previously available/local data where possible. | Plan state, lock snapshot context, shopping list/edits, selected store, season override, and source/fallback/status metadata persisted per Q4=A Functional Design. | Storage mechanism deferred to NFR Design/Code Generation. |
| NFR1.2 | NFR1 | Graceful degradation | Missing integrations/data sources degrade gracefully to curated/imported/local fallback or manual path. | Sydänmerkki/PRISMA/S-ostoslista failures produce explicit fallback or manual checklist/share-copy paths; no hard failure for core local continuity where local data exists. | First-run seed-data gaps documented as known risk where applicable. |
| NFR2.1 | NFR2 | Explicit states | External/data-source outcomes use typed explicit states with next action where applicable. | States include available/cached/stale/unknown/fallback (or equivalent typed results); user-facing next action provided where relevant. | No fabricated data (BR1.13). |
| NFR2.2 | NFR2 | State consistency | Today/Plan-related baseline state remains consistent with persisted lock/stale metadata. | Stale lock uses exact ADR-006 warning semantics; no auto-regeneration. | UI synchronization completed in U4; U1 provides contracts/state. |
| NFR2.3 | NFR2 | Recovery behavior | On partial local data or transient adapter failure, show available data plus explicit unavailable/fallback state. | No silent data loss; export fallback preserves actionable path. | Error boundaries and retry UX refined in NFR Design/Code Generation. |
