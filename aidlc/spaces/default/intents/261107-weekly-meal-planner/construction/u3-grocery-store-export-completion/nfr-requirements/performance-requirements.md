# Performance Requirements — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [requirements.md](../../../inception/requirements-analysis/requirements.md) — inception **NFR5 Performance and responsiveness (MVP baseline)**.
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md) (BR3.1–BR3.5, BR3.8).
- [nfr-requirements-questions.md](nfr-requirements-questions.md) (Q1=A, Q2=A).

## Requirements

| ID | Inherits | Requirement | Target / constraint | Verification note |
|---|---|---|---|---|
| NFR5.1 | NFR5 | Locked-snapshot generation and deterministic merge run as bounded local operations for MVP scale. | For a typical 7-day locked plan (tens to low hundreds of ingredient lines/items), generation/merge completes without forcing app close and without requiring network access. | Unit tests with representative fixture; manual/device smoke in U4/build-and-test. |
| NFR5.2 | NFR5 | Shopping-list edits and availability projection remain responsive. | `applyPresenceCheckBatch`, check/edit/add/remove, and remap-to-Unknown decisions complete locally for current list size without blocking unrelated interactions; no unbounded loops over external data. | Unit tests for batch apply/remap; code review for complexity. |
| NFR5.3 | NFR5 | Export payload construction uses current edited state efficiently. | `ShoppingListExportDto`/text payload built from current in-memory/local aggregate without requiring pre-edit copies or extra network calls; share/copy fallback remains available. | Unit/integration tests verify current edited state exported. |
| NFR5.4 | NFR5 | External adapter checks (PRISMA/S-ostoslista) do not degrade core local responsiveness. | A2/A3 calls are adapter concerns but contracts/results are consumed asynchronously/bounded when implemented; local list edit and fallback export remain usable if checks are slow/unavailable. Typed timeout/partial/failed outcomes preferred. | Adapter tests with fake delays/failures; U4 integration behavior. |
| NFR5.5 | NFR5 | No premature hard latency SLO. | Precise millisecond thresholds deferred until implementation/measurement exists (NFR Design/build-and-test). MVP acceptance uses NFR5 baseline behavior. | Record measured hotspots if found later. |