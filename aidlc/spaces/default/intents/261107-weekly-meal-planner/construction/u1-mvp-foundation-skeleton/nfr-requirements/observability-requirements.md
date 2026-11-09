# Observability Requirements — U1 `u1-mvp-foundation-skeleton`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U1 — MVP foundation + integration slice (Android app)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-08

## Upstream
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (no dedicated inception observability NFR ID)
- [nfr-requirements-questions.md](nfr-requirements-questions.md) (Q4=A, summary confirmed)

## Applicability
Distributed tracing, backend SLO dashboards, and centralized observability platform requirements are **`N/A`** for U1 MVP baseline (single-user on-device Android app, no backend service). Lightweight local diagnostics and user-visible fallback states are required.

## Requirements

| ID | Inherits | Category | Requirement | Target / Behavior | Notes |
|---|---|---|---|---|---|
| NFR2.4 | NFR2 | User-visible state observability | Fallback/unknown/stale/integration states are observable to the user through typed UI-facing state contracts. | U1 contracts expose explicit status/reason/next-action fields (A1–A3, DataSourceStatus, stale warning). | Presentation owned by U4. |
| NFR4.6 | NFR4 | Privacy-safe diagnostics | Local diagnostics/errors, if captured, must avoid personal data and remain minimal. | No location coordinates/raw personal identifiers in logs/diagnostics; optional crash/error reporting hook deferred with privacy review. | Supports NFR4.5. |
| NFR3.5 | NFR3 | Performance diagnosability (deferred) | Measurable performance diagnostics/benchmarks are deferred to NFR Design/build-and-test. | Define benchmark method/device profile when tests are established. | NFR3.1 target exists; instrumentation method TBD. |

## N/A
- Distributed tracing: `N/A` (no distributed service topology).
- Backend SLO/alerting: `N/A` (no backend service in MVP).
