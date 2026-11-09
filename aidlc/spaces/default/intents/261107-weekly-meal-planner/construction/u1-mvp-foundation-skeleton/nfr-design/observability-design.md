# Observability Design — U1 `u1-mvp-foundation-skeleton`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U1 — MVP foundation + integration slice (Android app)  
**Stage:** NFR Design (construction, per-unit)  
**Date:** 2026-11-08

## Upstream
- [observability-requirements.md](../nfr-requirements/observability-requirements.md) (local `NFR2.4`, `NFR4.6`, `NFR3.5`)
- [nfr-design-questions.md](nfr-design-questions.md) (Q5=A)

## Applicability
Distributed tracing, centralized backend SLO dashboards, and alerting platforms are **`N/A`** for U1 (single-user on-device Android MVP, no backend). Observability focuses on user-visible state and privacy-safe local diagnostics.

## Patterns and decisions

| Area | Design | Addresses | Notes |
|---|---|---|---|
| User-visible state observability | Expose typed status/reason/next-action in domain/UI state (A1–A3, `DataSourceStatus`, stale warning, presence/export results). UI renders explicit labels/states; no dead-end silent failures. | NFR2.4 | Presentation owned by U4; U1 provides contracts. |
| Local diagnostic events | Structured local diagnostic event model for key flows: component/contract, operation, result status, fallback reason category, duration bucket or coarse timing (optional), correlation/session-local ID (non-personal). | NFR2.4, NFR4.6 | Keep minimal; useful for development/support diagnostics. |
| Privacy-safe fields | Diagnostics must not include coordinates, precise/raw location, raw personal identifiers, account data, secrets, or un-sanitized payload text. | NFR4.6 | Enforced by logging policy in Code Generation. |
| Optional crash/error reporting hook | Define integration point only; actual SDK/vendor integration deferred until privacy review approves fields/config. | NFR4.6 | Not enabled by default in MVP design. |
| Performance diagnosability | No production APM required. Benchmark/test method and device profile deferred to build-and-test to evaluate NFR3.1 target. | NFR3.5 | Can use local profiling during development. |

## Suggested diagnostic event categories (privacy-safe)
- Plan load/save: `ok/cached/fallback/error_mapped`.
- Lock/generate/regenerate: `generated/regenerated/needs_confirmation/failed_mapped`, stale flag.
- Adapter lookup (A1/A2/A3): `ok/fallback/unknown/unavailable/timeout_mapped` (no endpoint/payload details).
- Export: `share_sheet/copy_text/official_attempt/fallback_used/cancelled/failed_mapped`.

## N/A
- Distributed tracing/correlation across services: `N/A`.
- SLI/SLO alerting/backend dashboards: `N/A`.
- Central log aggregation: `N/A` for MVP unless a privacy-reviewed crash tool is later added.
