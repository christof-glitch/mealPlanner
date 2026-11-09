# Observability Requirements — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [requirements.md](../../../inception/requirements-analysis/requirements.md) — inception **NFR2 fallback clarity** and **NFR5 responsiveness** (typed status/progress suitability); no dedicated observability platform NFR.
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md).
- [nfr-requirements-questions.md](nfr-requirements-questions.md) (Q3=A).

## Requirements

| ID | Inherits | Requirement | Target / constraint | Notes |
|---|---|---|---|---|
| NFR2.7 | NFR2 | Domain results carry explicit status/reason metadata. | `GenerationResult`, `PresenceCheckBatchResult`/`PresenceCheckResult`, `ExportResult`, `ShoppingListStaleStatus`, and availability reason fields expose typed status and human-actionable reason/message/instructions where applicable. | Enables U4 fallback states and deterministic tests. |
| NFR3.7 | NFR3 | Diagnostics are privacy-safe. | Status reasons/messages avoid raw location coordinates, secrets, and unnecessary personal identifiers. Presence/export investigation outcomes use coarse typed values (`USED|PARTIALLY_USED|NOT_FEASIBLE_CONFIRMED|UNKNOWN`/equivalent). | Aligns with NFR3.5. |
| NFR5.6 | NFR5 | Long-running adapter operations are representable. | Contracts allow in-progress/partial/unavailable/failed mapped outcomes for adapter checks; UI can show status without blocking unrelated local edits. Implementation mechanism (coroutines callbacks/Flow) deferred to NFR Design/U4. | No external APM/telemetry required for MVP. |