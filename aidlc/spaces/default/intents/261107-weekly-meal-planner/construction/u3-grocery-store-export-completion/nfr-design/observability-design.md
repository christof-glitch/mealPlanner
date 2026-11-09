# Observability Design — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Date:** 2026-11-09

## Upstream
- [observability-requirements.md](../nfr-requirements/observability-requirements.md) (NFR2.7, NFR3.7, NFR5.6)
- [nfr-design-questions.md](nfr-design-questions.md) (Q4=A)

## Design

| Concern | Pattern | Addresses | Notes |
|---|---|---|---|
| Domain status visibility | Use typed result/status fields (`GenerationResult`, `PresenceCheckBatchResult`, `PresenceCheckResult`, `ExportResult`, stale/availability reasons/instructions) as primary observability surface for tests and U4. | NFR2.7 | No external APM required. |
| Privacy-safe reasons | Reason codes/messages coarse and non-sensitive; no raw GPS, secrets, tokens, or unnecessary personal identifiers. | NFR3.7 | Follows NFR3.5. |
| Investigation outcome mapping | `ExportResult.investigationOutcome` coarse values (`USED|PARTIALLY_USED|NOT_FEASIBLE_CONFIRMED|UNKNOWN`/equivalent) must be explicitly mapped to C6/A3 adapter outcomes in implementation (e.g., official `ok` => `USED`; fallback after attempt => `PARTIALLY_USED` or `FALLBACK_USED` context; not feasible => `NOT_FEASIBLE_CONFIRMED`; unresolved => `UNKNOWN`). | NFR3.7, BR3.8 | Addresses NFR requirements review minor. |
| Long-running representability | Adapter-facing contracts support `PARTIAL|UNAVAILABLE|FAILED_MAPPED|DENIED|UNKNOWN` outcomes so U4 can show progress/status without blocking local edits/fallback export. | NFR5.6 | Async mechanism deferred to adapter/U4. |