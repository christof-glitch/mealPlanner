# Reliability Requirements — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [requirements.md](../../../inception/requirements-analysis/requirements.md) — inception **NFR2 Usability and clarity of fallback states** (primary for U3 reliability/fallback behavior).
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md) (BR3.1–BR3.8, BR3.10), ADR-006/R-01–R-03.

## Requirements

| ID | Inherits | Requirement | Target / constraint | Verification note |
|---|---|---|---|---|
| NFR2.1 | NFR2 | Generation is explicit and snapshot-safe. | `generate/regenerate` occurs only by explicit action from valid `LockedRecipeSnapshot`; invalid snapshot returns `INVALID_SNAPSHOT`; missing/changed recipe data yields `PARTIAL`/notes with no fabrication. | Unit tests for valid/invalid/missing/changed sources. |
| NFR2.2 | NFR2 | Regeneration confirmation protects user edits. | If list has edits and confirmation is absent, return `CONFIRMATION_REQUIRED` and preserve current edited list unchanged. Confirmed regeneration rebuilds from locked snapshot per ADR-006. | Unit tests for edited/non-edited regenerate paths. |
| NFR2.3 | NFR2 | Stale-state semantics are preserved exactly. | Exact stale warning `Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.` is preserved in contracts; no auto-regeneration. | Contract/constant tests. |
| NFR2.4 | NFR2 | Availability projection is safe after change. | `applyPresenceCheckBatch` applies only matching results; ambiguous/edit/merge/split/regenerate identity changes remap to `UNKNOWN` or `NOT_CHECKED` with reason. ShoppingList owns projection; C5 never mutates items. | Unit tests for remap, identity change, partial batch, idempotent same batch/scope where applicable. |
| NFR2.5 | NFR2 | PRISMA/S-ostoslista failures degrade safely. | Presence returns `UNKNOWN`/typed batch failures with reasons; never stock/guaranteed availability. Official S-ostoslista attempt may return `NOT_SUPPORTED|UNAVAILABLE|FAILED_MAPPED`; mandatory share/copy fallback returns `FALLBACK_USED`/instructions as applicable. | Adapter-fake tests for denied/unavailable/partial/failed cases. |
| NFR2.6 | NFR2 | Export uses current edited list and is always possible via fallback. | `ShoppingListExportDto` reflects current checked/edited/added/removed state; share sheet/copyable text path remains available even when official export is not feasible. | Unit tests compare DTO/text to edited aggregate state. |