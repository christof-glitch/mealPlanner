# Tech Stack Decisions — U1 `u1-mvp-foundation-skeleton`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U1 — MVP foundation + integration slice (Android app)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-08

## Upstream
- [requirements.md](../../../inception/requirements-analysis/requirements.md)
- [functional-spec.md](../functional-design/functional-spec.md)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md)
- [nfr-requirements-questions.md](nfr-requirements-questions.md) (Q5=A, summary confirmed)

## Decisions

| Area | Decision | Status | Rationale | Confirm/defer |
|---|---|---|---|---|
| Platform | Android app (MVP) | Decided | Intent and scope are Android-only MVP. | Confirmed. |
| Language | Kotlin | Decided (baseline) | Modern Android standard; supports typed contracts/null-safety and coroutines/Flow for async boundaries. | Confirm in NFR Design/Code Generation (version/toolchain). |
| Build system | Gradle Kotlin DSL | Decided (baseline) | Standard for modern Android/Kotlin projects. | Confirm versions/plugins in Code Generation. |
| Core libraries | AndroidX | Decided (baseline) | Standard Android foundation. | Confirm specific artifacts in NFR Design/Code Generation. |
| UI toolkit | Jetpack Compose or Views | **To confirm in NFR Design** | Both viable; choice affects implementation patterns and U4 work. U1 contracts remain UI-toolkit-agnostic where possible. | NFR Design decision with rationale. |
| Async/concurrency | Kotlin coroutines / Flow (or equivalent structured async) | Accepted baseline | Fits adapter boundaries and local state observation; exact usage patterns refined later. | Confirm in NFR Design/Code Generation. |
| Local persistence | Mechanism deferred | **To confirm in NFR Design** | Must satisfy NFR1.1/NFR4.2 (plan/snapshot/list/edits/store/season override/status metadata). Candidates (e.g., Room/DataStore/files) evaluated later. | NFR Design decision. |
| Networking/adapters | Deferred to downstream adapter implementation | Deferred | Sydänmerkki/PRISMA integration details depend on access methods (OQ1/OQ2); U1 provides contracts/fallback types. | NFR Design/U2/U3. |
| Dependency injection | Deferred | **To confirm in NFR Design** | Helpful for modular C1–C6 boundaries/testing; not required to lock now. | NFR Design/Code Generation. |
| Testing frameworks | Deferred (aligned to team/testing posture) | **To confirm in NFR Design/Code Generation** | Must support unit/integration/UI testing as codebase is established; CI checks required by team practices. | NFR Design/build-and-test. |
| Testability/quality gates (inception NFR4) | Baseline required | Decided (policy baseline) | Key behaviors (plan structure/time rule, recipe selection, shopping-list aggregation/editing, adapters/fallbacks, privacy-sensitive permission handling) must have automated tests where practical; critical tests/CI checks run; existing tests remain green; quality targets not weakened to pass (team posture). | Concrete frameworks/CI pipeline confirmed in NFR Design/build-and-test/Code Generation. |
| Architecture style | Modular boundaries aligned to C1–C6 within single Android app | Decided (baseline) | Preserves contract ownership and future extraction options without premature backend/services. | Refine module/package boundaries in NFR Design/Code Generation. |

## Constraints
- No hardcoded secrets/endpoints/keys.
- No MealPlanner account/auth in MVP.
- Official S-ostoslista method investigational; share-sheet/copy-text fallback mandatory.
- PRISMA checks name/presence only; never stock quantity/guaranteed availability.
