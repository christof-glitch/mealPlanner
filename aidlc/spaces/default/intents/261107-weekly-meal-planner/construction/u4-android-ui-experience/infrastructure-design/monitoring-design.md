# Monitoring Design — U4 `u4-android-ui-experience`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Unit:** U4 — Android UI experience (`ui`)  
**Stage:** Infrastructure Design (construction, per-unit)  
**Date:** 2026-11-09

## Scope and upstream
- [performance-design.md](../nfr-design/performance-design.md), [security-design.md](../nfr-design/security-design.md)
- [functional-spec.md](../functional-design/functional-spec.md) (R-05 fallback states, typed UI status)
- [infrastructure-design-questions.md](infrastructure-design-questions.md) (Q3 resolved A)

U4 has **no AWS/CloudWatch monitoring stack**. Monitoring is app/client-level and privacy-safe for MVP.

## Metrics & KPIs

| Metric | Source | Threshold | Why it matters |
|---|---|---|---|
| ANR/crash-free user experience (release process) | Android vitals / release QA tooling if adopted; local debug observation in MVP | No reproducible ANR in primary RM-01–RM-09 flows during MVP verification | NFR1.1/NFR1.4 responsiveness and no dead-end interactions. |
| UI state correctness for fallback flows | Automated UI/state tests with fake facades + manual smoke | All required R-05/FR7/FR8/FR10 fallback states render next actions | NFR2 reliability from user perspective; no silent failure. |
| Core action success (manual verification) | Test scenarios/manual checklist | Generate/regenerate confirmation gate, export current edited list, share/copy fallback, manual store selection work as designed | Prevents ADR-006/FR10 regressions. |
| Privacy safety in diagnostics/UI | Code review + tests for `DiagnosticsUiMapper`/state models | Zero raw GPS coordinates, secrets, endpoints, or PII in UI/logging hooks | NFR3.3 compliance. |

## Alerts

| Alert | Condition | Severity | Routes to |
|---|---|---|---|
| CI unit-test failure | `./gradlew :app:testDebugUnitTest --console=plain` (or layout-explicit equivalent) fails | High | Developer/PR author; blocks merge/release candidate for affected change. |
| Accessibility/regression finding in review | WCAG 2.2 AA practical issue or missing fallback label/action found in review/manual test | Medium-High | Developer; fix before approval/merge. |
| Privacy leak finding | Raw GPS/secret/PII/stock-guarantee claim observed in UI state, copy, or diagnostics | Critical | Developer; must fix before merge/release. |
| Production crash/ANR alert (if release monitoring added later) | Tooling threshold exceeded in production/internal track | High | Release owner/developer; out of U4 infra scope to provision now. |

## SLIs / SLOs

| SLI | SLO target | Measurement window |
|---|---|---|
| Primary local interaction responsiveness | MVP target: no blocking main-thread behavior/ANR in primary flows on supported test profile | Per verification run / pre-release smoke |
| Fallback availability for core flows | 100% of designed fallback paths present (manual planning/store/checklist/share-copy) in acceptance scenarios | Per PR/test cycle |
| Privacy-safe UI/diagnostics | 0 known instances of raw GPS/secrets/PII exposure in reviewed UI code paths | Per PR/review cycle |

## Logs & Tracing
- **App-level:** Prefer typed UI/domain result states over ad hoc logs. Any diagnostics hooks added during implementation must pass through privacy-safe mapping (`DiagnosticsUiMapper` pattern), exclude raw coordinates/secrets/endpoints/PII, and be disabled or guarded appropriately.
- **Tracing:** No distributed tracing infrastructure. Local coroutine/flow debugging uses standard Android/Kotlin tooling in development only.
- **Dashboards:** No cloud dashboard. Verification evidence is captured via test results (`BUILD SUCCESSFUL`/failures), AI-DLC review/checkpoint receipts, and release QA notes if used.
