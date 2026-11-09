# Performance Design — U1 `u1-mvp-foundation-skeleton`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U1 — MVP foundation + integration slice (Android app)  
**Stage:** NFR Design (construction, per-unit)  
**Date:** 2026-11-08

## Upstream
- [performance-requirements.md](../nfr-requirements/performance-requirements.md) (local `NFR3.1`–`NFR3.3`; maps to inception NFR5)
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (C1–C6, A1–A4)
- [nfr-design-questions.md](nfr-design-questions.md) (Q1=A Compose, Q2=A Room+DataStore, Q3=A bounded adapters)

## Design goals
- Common local interactions feel responsive (target `< 500 ms` for common local state updates on typical mid-range Android device).
- External adapters never block core local planning/shopping-list flows.
- First useful local view uses persisted/local state where available.

## Patterns and decisions

| Area | Design | Addresses | Notes |
|---|---|---|---|
| Local state rendering | Jetpack Compose state hoisting with immutable UI state models derived from domain/use-case results; avoid heavy synchronous work on main thread. | NFR3.1 | Compose details refined in Code Generation; U1 contracts remain UI-toolkit-aware only at implementation boundary. |
| Local persistence reads/writes | Room for structured plan/list/snapshot entities; DataStore/preferences for small settings (selected store, season override). Use background dispatchers for I/O; expose observed state via `Flow`/equivalent. | NFR3.1, NFR3.3, NFR1.1 | Exact schema/split refined in Code Generation; writes must preserve minimal data and explicit states. |
| External adapter calls | Bounded timeout per adapter call (value in config; deferred). Execute off main thread; map timeout/failure to typed fallback/unknown state quickly. | NFR3.2 | Applies to A1 Sydänmerkki, A2 PRISMA, A3 S-ostoslista official attempt when implemented by U2/U3. |
| No core-flow blocking | Core plan/list/edit/export-fallback flows operate on local/domain state. Network/lookup results enrich state asynchronously and never fabricate data. | NFR3.2, NFR3.3 | Share sheet/copy-text fallback (C6/A4) remains available without official S-ostoslista success. |
| Optional safe retries | Limited retry only for safe idempotent reads (e.g., lookup/query), with backoff cap; never retry non-idempotent actions without explicit design. | NFR3.2 | Default conservative for MVP. |
| Startup | Load persisted plan/list/settings first; render explicit empty/loading/known states; trigger external refreshes opportunistically and non-blocking. | NFR3.3 | Cold/warm startup metrics deferred to build-and-test. |
| Performance budget | Local update budget `< 500 ms` for common interactions; adapter timeout budget required but value deferred. | NFR3.1, NFR3.2 | Benchmark harness/device profile deferred to build-and-test. |

## Deferred
- Exact timeout values, retry counts, dispatcher configuration.
- Startup metric targets and benchmark method/device matrix.
- Compose performance tuning (recomposition profiling) in Code Generation/testing.
