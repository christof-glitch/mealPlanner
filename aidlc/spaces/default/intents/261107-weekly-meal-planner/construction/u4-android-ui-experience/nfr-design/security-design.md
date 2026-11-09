# NFR Security & Privacy Design — U4 `u4-android-ui-experience`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U4 — Android UI experience (`ui`)  
**Stage:** NFR Design (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [security-requirements.md](../nfr-requirements/security-requirements.md) (NFR3.1-U4–NFR3.6-U4)
- [functional-spec.md](../functional-design/functional-spec.md) (RM-01/RM-06–RM-09)
- [tech-stack-decisions.md](../nfr-requirements/tech-stack-decisions.md)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (A2/A3/C6 boundaries)

## Design decisions

| Requirement | Design pattern/decision | Verification hook |
|---|---|---|
| NFR3.1-U4 | No auth/account/navigation paths. App state is local/on-device MVP; no user identity fields in UI models. | Code review; UI route/state tests. |
| NFR3.2-U4 | Permission handling behind `PermissionStatusProvider`/platform boundary. RM-01/RM-07/RM-09 render purpose text and always expose manual store selection. Denied/not-asked states map to manual-only flow; no location permission required for core planning/grocery. | Fake permission provider tests; manual device checks. |
| NFR3.3-U4 | UI diagnostics use typed status/reason enums and user-safe messages. `DiagnosticsUiMapper` strips/avoids raw coordinates, secrets, endpoints, PII. Logging hooks (if added) must be privacy-safe and disabled/guarded for sensitive payloads. | Code review; tests for mapped messages; no raw-GPS/secret strings. |
| NFR3.4-U4 | Export/share is explicit user action from current edited list state. `GroceryExperienceFacade`/export UI invokes C6 use case; official S-ostoslista investigational result shown, mandatory share/copy fallback always available. Cancel/failure preserves list. Android intents/clipboard confined to platform/share boundary (BR3.9). | Fake export-port tests; UI tests for fallback/cancel. |
| NFR3.5-U4 | `StorePresenceStatusRow`/models render only `PRESENT|NOT_FOUND|UNKNOWN` + reason; copy must never mention stock quantity/guaranteed availability. Manual checklist fallback always available. | State/render tests; copy review. |
| NFR3.6-U4 | Missing-data labels are explicit and non-medical (`Nutrition information unavailable`, `Some nutrition details unavailable`, `Season fit unknown`, `Unknown`+reason). WCAG 2.2 AA practical baseline: contrast, labels, focus order, announcements, dialog focus, non-color-only status. | Accessibility/manual review; UI tests for labels/focus behavior where practical. |

## Threat/failure considerations (UI)
- Treat external adapter outputs as untrusted display data; render as text, do not interpret as commands/URLs without validation.
- Permission denial, adapter failure, and missing data must degrade to safe manual/local flows (NFR2). No sensitive data in screenshots/logs by default beyond normal Android behavior; future telemetry requires review.