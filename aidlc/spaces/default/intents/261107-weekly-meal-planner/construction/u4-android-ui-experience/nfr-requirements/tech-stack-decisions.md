# Tech Stack Decisions — U4 `u4-android-ui-experience`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Unit:** U4 — Android UI experience (`ui`)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [functional-spec.md](../functional-design/functional-spec.md) (Compose/MVVM thin UI architecture)
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (Android MVP, NFR1–NFR5)

## Decisions

| Area | Decision | Rationale | Alternatives considered | Source |
|---|---|---|---|---|
| UI toolkit | **Jetpack Compose + Material 3** (existing brownfield stack) | Already configured (`compose = true`, Material 3 dependency); fits RM component/state-driven UI and accessibility semantics. | XML Views — rejected for new UI work to avoid mixed paradigms in MVP. | `app/build.gradle.kts`, RM Q1=A, project tech stack |
| Language/runtime | **Kotlin/JVM target 17**, Android `minSdk 26`, `targetSdk/compileSdk 35` | Matches existing app config and domain Kotlin code. | Raise minSdk/target changes — out of scope unless required by dependencies. | `app/build.gradle.kts` |
| Architecture | **MVVM-style state holders/facades over U1–U3 domain use cases** (thin UI) | Preserves ADR-005; enables fake-backed UI/state tests; aligns with Compose state. | Put rules in ViewModels/composables — rejected (duplicates domain). | ADR-005, U4 functional spec |
| Navigation | **Compose Navigation with bottom-nav top level + pushed/full-screen routes** (implementation detail finalized in code generation) | Matches RM-01–RM-09 and Q3=B full-screen flows. | Single-activity fragment nav — unnecessary given Compose stack. | RM mockups/interaction spec |
| Async/state | **Kotlin coroutines/Flow + lifecycle ViewModel Compose** (existing deps) | Already present; supports off-main-thread work and observable UI state. | RxJava — not present; unnecessary. | `build.gradle.kts` |
| Persistence/preferences integration | **Existing Room/DataStore/domain repositories via facades** | Brownfield continuity; no new storage tech in U4. | New persistence layer — rejected. | U1–U3 brownfield |
| Permissions | **Android runtime permission APIs via platform boundary** (Activity Result / accompanist-free approach as chosen in implementation) | Purpose-limited location with manual fallback; testable via abstraction. | Hard dependency on third-party permission lib — not required. | FR7/FR12, NFR3.2 |
| Share/export | **Android share sheet + clipboard/copy via existing `ShareExportPort`/platform boundaries**; S-ostoslista official attempt behind domain port (OQ4) | Mandatory fallback; keeps Android intent details out of domain (BR3.9). | Direct deep-link coupling in UI/domain — rejected until OQ4 feasible. | FR10, BR3.9 |
| Testing | **JUnit4 + kotlin.test + coroutines-test + MockK + Turbine** (existing); Compose UI tests added only if needed by plan | Aligns with existing test stack and MVP pragmatism. | New UI test framework migration — not required now. | `build.gradle.kts`, team testing posture |

## Open constraints
- OQ1–OQ4/OQ5–OQ8 remain; UI must render investigational/fallback states. No new external SDKs added in NFR stage without explicit need.