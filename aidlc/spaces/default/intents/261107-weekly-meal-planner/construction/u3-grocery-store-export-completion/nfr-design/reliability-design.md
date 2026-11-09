# Reliability Design — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Date:** 2026-11-09

## Upstream
- [reliability-requirements.md](../nfr-requirements/reliability-requirements.md) (NFR2.1–NFR2.7)
- [rules.md](../functional-design/rules.md) (BR3.1–BR3.8), ADR-006/R-01–R-03
- [nfr-design-questions.md](nfr-design-questions.md) (Q1=A, Q2=A)

## Design

| Concern | Pattern | Addresses | Notes |
|---|---|---|---|
| Explicit generation | Use-case requires `ShoppingListGenerationRequest` from valid locked snapshot; validate snapshot presence/shape before re-resolution. | NFR2.1, BR3.1 | Missing/changed source => `PARTIAL`/notes, no fabrication. |
| Confirmation gate | `regenerate` checks `hasUserEdits`; without `confirmRegenerateIfEdited` returns `CONFIRMATION_REQUIRED` and leaves aggregate unchanged. | NFR2.2, BR3.3 | Confirmation is explicit user action (R-03). |
| Stale semantics | Expose `ShoppingListStaleStatus.staleWarning` using exact ADR-006 text constant: `Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.` No auto-regeneration path. | NFR2.3, BR3.4 | U2/C3 signals staleness; C4 reflects it. |
| Safe availability projection | `applyPresenceCheckBatch` (explicit C4 operation): match by stable key precedence (itemRef/query key + normalized query + selected store/scope as designed in implementation); apply only matches; preserve unrelated edits/checks; identity/query ambiguity after edit/merge/split/regenerate/manual add => `UNKNOWN`/`NOT_CHECKED` with reason. Same batch+scope application is idempotent where practical. | NFR2.4, BR3.5, R-02 | C5/A2 never mutates items. |
| External fallback-first | A2: `PRESENT|NOT_FOUND|UNKNOWN` + typed batch status/reason; never stock/guarantee. A3: official attempt only when feasible/confirmed; `NOT_SUPPORTED|UNAVAILABLE|FAILED_MAPPED` => mandatory share/copy fallback with instructions. | NFR2.5, BR3.6–BR3.8, OQ2–OQ4 | Manual store/checklist always usable. |
| Current edited export | `ShoppingListExportDto` built from current aggregate immediately before export; includes checked/edited/added/removed state and optional stale warning/notes. | NFR2.6, BR3.3, BR3.8 | No stale pre-edit copy. |
| Typed observability | All public results carry status/reason/instruction fields safe for U4/tests; coarse investigation outcome mapping documented. | NFR2.7, NFR3.7 | Supports fallback clarity without sensitive payloads. |