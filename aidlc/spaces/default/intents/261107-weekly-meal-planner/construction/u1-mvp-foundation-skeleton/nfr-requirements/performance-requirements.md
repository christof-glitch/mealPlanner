# Performance Requirements — U1 `u1-mvp-foundation-skeleton`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U1 — MVP foundation + integration slice (Android app)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-08

## Upstream
- [requirements.md](../../../inception/requirements-analysis/requirements.md) — inception **NFR5 Performance and responsiveness** (primary). Local derived IDs in this file (`NFR3.x`) are a U1 artifact-local namespace; semantic mapping to inception NFR1–NFR5 is recorded in [traceability.json](traceability.json).
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md)
- [nfr-requirements-questions.md](nfr-requirements-questions.md) (Q1=A, summary confirmed)

## Requirements

| ID | Inherits | Category | Requirement | Target / Budget | Notes |
|---|---|---|---|---|---|
| NFR3.1 | NFR3 | Local responsiveness | Common local plan/shopping-list state updates (view state changes, check off, edit fields, local selection changes) feel responsive on a typical mid-range Android device. | Target `< 500 ms` for common local state updates (MVP reasonable default). | Measured on representative mid-range device/profile in testing; exact benchmark harness deferred to NFR Design/build-and-test. |
| NFR3.2 | NFR3 | External adapter boundedness | External adapter calls (Sydänmerkki/PRISMA when implemented by downstream units) must use bounded timeouts and must not block core local planning/shopping-list flows. | Bounded timeout required; exact values deferred to NFR Design/adapter config. Fallback path must be available within timeout/failure handling. | U1 provides typed fallback contracts; U2/U3 implement adapters. |
| NFR3.3 | NFR3 | Startup/first useful view | App should reach a usable local state without waiting for external integrations when local data exists. | Qualitative MVP target: first useful local view uses persisted/local state where available; external refresh is non-blocking where practical. | Exact cold/warm startup metrics deferred to NFR Design/testing. |

## Assumptions
- “Typical mid-range Android device” is an MVP assumption; specific device/profile matrix deferred to testing strategy.
- No backend service in MVP; performance focuses on on-device and adapter-boundary behavior.
