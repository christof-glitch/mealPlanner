# Unit Test Instructions — U4 `u4-android-ui-experience`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Unit:** U4 — Android UI experience (`ui`)  
**Stage:** Code Generation (construction, per-unit)  
**Date:** 2026-11-09  
**Active test strategy:** Standard (5–8 tests per component, unit tests plus integration tests for key boundaries). **Methodology:** custom (key behavior test-first where practical; supporting behavior practical cadence).

## Test framework setup and configuration
- **Frameworks (existing brownfield):** JUnit4 (`testImplementation(libs.junit)`), kotlin.test assertions, `kotlinx-coroutines-test`, MockK, Turbine. Android unit tests run on JVM with `unitTests.isReturnDefaultValues = true`.
- **UI scope:** Prefer fake-backed facade/state-holder/ViewModel/navigation-state unit tests under `app/src/test/java/com/mealplanner/ui/**`. Compose instrumentation/UI tests are optional only if explicitly added and runnable; not required by this MVP plan.
- **Configuration:** No new test-runner bootstrap expected; reuse `app/build.gradle.kts` and version catalog dependencies. Do not change coverage-threshold configuration to make a unit-scoped run pass.

## How to run THIS UNIT's tests (exact, unit-scoped)
Run before the first key-behavior test-first cycle and after implementation/refactors:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.ui.*" --console=plain
```

Targeted runs (examples, exact file/package scope):

```bash
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.ui.state.*" --console=plain
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.ui.facades.*" --console=plain
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.ui.screens.*" --console=plain
```

These commands are scoped to U4 UI tests only. Build and Test executes every unit's commands; unscoped project-wide commands are intentionally omitted here.

## Expected coverage targets
- **Standard scope:** ~5–8 tests per meaningful U4 component/facade/state area, covering key boundaries (plan slot sync/R-06, grocery ADR-006 generate/regenerate confirmation/exact stale warning/export current edited list, store presence FR8/manual fallback, permission/privacy-safe diagnostics, export/share fallback).
- **MVP/Standard floor:** 80% line-coverage floor and CI execution before merge are project obligations (additive). **Build and Test checks the project coverage floor on the full test suite of the assembled project.** A unit-scoped run does not judge that whole-project floor. If a whole-project coverage-threshold setting causes a non-zero exit while every selected U4 test passes, record that outcome with U4 test results; it is not a failed test. Do not relax/lower/disable the threshold.
- **Regression:** Existing tests across `com.mealplanner.*` must remain green; full regression is verified in Build and Test / authorized verification flows.

## Mocking/stubbing guidance
- Mock/fake U1–U3 domain services/use cases/ports (`C2`–`C6`, planning, shopping, export, store/presence, preferences/season/permission providers). Keep UI tests deterministic and offline.
- Do not mock away invariants under test: ADR-006 exact stale warning text, regenerate confirmation when `hasUserEdits`, export current edited list, `PRESENT|NOT_FOUND|UNKNOWN` name/presence-only labels, manual store/share-copy fallback availability.
- Use `kotlinx-coroutines-test` dispatchers/schedulers for async state; use MockK/Turbine only where they simplify assertions.
- Treat external adapter data as untrusted display input; assert user-safe labels and absence of raw GPS/secrets/PII.

## Test data management
- Use small in-memory fixtures for weekly slots (weekday/weekend FR1 shapes), recipes (time/season/nutrition metadata variants), shopping items, store refs, and export payloads.
- Reuse canonical constants where available (e.g., `Adr006LockRules.STALE_WARNING_TEXT`) rather than duplicating warning text literals except when asserting the exact user-facing string.
- Keep fixtures privacy-safe (no real secrets, tokens, endpoints, or precise coordinates).
- For R-05, include no-local-data + remote-unavailable fallback fixtures with explicit next actions. For FR10/OQ4, include official S-ostoslista `NOT_SUPPORTED/UNAVAILABLE/FAILED_MAPPED/SUCCESS` mappings to fallback/success states.
