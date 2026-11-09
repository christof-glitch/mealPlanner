# Tech Stack Decisions — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [U1 tech-stack-decisions.md](../../u1-mvp-foundation-skeleton/nfr-requirements/tech-stack-decisions.md)
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (C2/C3/A1)

## Decisions

| Area | Decision | Status | Rationale | Confirm/defer |
|---|---|---|---|---|
| Platform/language/build | Inherit U1 baseline: Android app module(s), Kotlin, Gradle Kotlin DSL, AndroidX | Decided (baseline, inherited) | U2 extends C2/C3 within the same Android app; consistency reduces integration risk. | Toolchain/plugin versions confirmed in NFR Design/Code Generation. |
| UI toolkit | N/A for U2 domain logic | Decided | U2 is `library`/domain+adapter boundary; U4 owns UI. Contracts remain UI-toolkit-agnostic. | N/A. |
| Async/concurrency | Structured async for A1 adapter and observable state where needed (Kotlin coroutines/Flow or equivalent) | Accepted baseline | Matches U1 direction and A1 bounded non-blocking requirement (NFR5.2). C2 pure normalization/evaluation can remain synchronous where appropriate. | Confirm exact patterns in NFR Design/Code Generation. |
| A1 adapter abstraction | Interface/port behind C2/A1 boundary with injectable fake/stub implementations | Decided (required) | Enables FQ1=A gated live behavior, mandatory curated fallback, and CI-safe tests without live network. | NFR Design/Code Generation. |
| Curated/imported fallback data | Packaged app content / local data source behind adapter/repository contract | Accepted baseline | Mandatory fallback (BR2.1, FR4); OQ1 may later enable live source. | Exact format/storage refined in NFR Design. |
| Time/season normalization | Kotlin/domain pure functions for parsing/evaluation with explicit typed results | Accepted baseline | Supports deterministic no-guess behavior (BR2.3/BR2.4) and unit testing. | OQ5/OQ7 mapping details refined later. |
| Observability hooks | Typed results + optional structured logging hooks (no PII/secrets) | Accepted baseline | Satisfies NFR5.4–NFR5.6 without external telemetry dependency. | Hook abstraction in NFR Design. |
| Testing | Unit tests for normalization/eligibility/season/nutrition/cue/alternatives/slot state; adapter tests with fakes; integration tests as codebase matures | Decided (policy baseline, inherited) | Key behaviors and fallback paths must be testable (NFR4/team posture). | Frameworks/CI details in NFR Design/build-and-test. |

## Testability / Quality Gates (inherited NFR4)

| ID | Requirement | Target / measure | Notes |
|---|---|---|---|
| NFR4.1 | Automated tests for key U2 behaviors where practical | Unit tests cover BR2.* key behaviors: A1 adapter status mapping/gated live default-off + curated fallback (`ok/fallback_used/restricted/unavailable/timeout/parse_error_mapped/not_supported`), weekday time normalization/no-guess eligibility (FR3/BR2.3), season fit/unknown/override precedence contracts as applicable to U2 (BR2.4), nutrition availability labels and `WeeklyNutritionBalanceCueRequest` aggregation boundaries (BR2.5), alternatives eligibility/fallback (BR2.6), per-slot state changes and stale signaling semantics (BR2.7/BR2.8, R-06/ADR-006). | Uses fakes/stubs for A1; no live network required for CI. |
| NFR4.2 | CI-safe adapter and fallback verification | A1 live path remains disabled by default in tests unless explicitly configured for controlled scenarios; curated/imported fallback is always testable. Adapter tests assert typed mapped outcomes (no raw exceptions across boundaries) and minimal-data request construction assumptions. | Supports NFR3.2/NFR3.3/NFR3.6. |
| NFR4.3 | Quality-gate posture preserved | Critical tests and CI checks run for established codebase; existing tests remain green; coverage/quality targets are not weakened to pass. Regressions for fixed BR2.* findings and review-identified risks receive targeted tests where practical. | Inherits NFR4/team testing posture. |

## Constraints
- No hardcoded secrets/endpoints/keys.
- Live A1 execution only when OQ1 confirms feasibility/terms/allowed access (FQ1=A), with default-off auditable gate (NFR3.2).
- No MealPlanner account; no unnecessary personal data.
- Preserve ADR-006/R-06 invariants in C3-facing behavior.