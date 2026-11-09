# Performance Design — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Date:** 2026-11-09

## Upstream
- [performance-requirements.md](../nfr-requirements/performance-requirements.md) (NFR5.1–NFR5.5)
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md)
- [nfr-design-questions.md](nfr-design-questions.md) (Q2=A, Q5=A)

## Design

| Concern | Pattern | Addresses | Notes |
|---|---|---|---|
| Generation/merge | Deterministic local pipeline: load snapshot -> re-resolve ingredients via C2 port -> normalize candidates -> group by merge key -> emit `ShoppingItem` + `MergeDecision`/notes. O(n) over ingredient candidates for MVP scale. | NFR5.1, BR3.1, BR3.2 | No network required; no fabrication. |
| Edits/projection | In-memory/local aggregate mutation returns new state; `applyPresenceCheckBatch` indexes batch by stable item/query key scoped to list+store and applies in one pass, preserving unrelated fields. | NFR5.2, BR3.3, BR3.5 | Avoid repeated full scans where trivial; exact data structures deferred to implementation. |
| Export DTO | Build `ShoppingListExportDto`/text from current aggregate in a single traversal; no pre-edit snapshot copies. | NFR5.3, BR3.8 | Share/copy fallback remains local. |
| External checks | A2/A3 boundaries consumed as typed results; caller may execute off main thread (U4/adapter concern). U3 domain must not block local edit/fallback export paths by design. | NFR5.4 | Timeout/batching/cancellation details deferred to adapter implementation (OQ2–OQ4). |
| SLO posture | No hard ms SLO now; measure hotspots if observed in build-and-test/device validation. | NFR5.5 | MVP acceptance follows NFR5 baseline. |