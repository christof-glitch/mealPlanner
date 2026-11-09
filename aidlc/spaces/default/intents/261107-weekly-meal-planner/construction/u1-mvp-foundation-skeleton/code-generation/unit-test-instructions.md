# Unit Test Instructions — U1 `u1-mvp-foundation-skeleton`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Unit:** U1 — MVP foundation + integration slice (Android, greenfield)  
**Stage:** Code Generation (construction, per-unit)  
**Test strategy:** Standard (5–8 tests per component, unit tests + integration stubs for key boundaries). **Scope floor (mvp):** 80% line-coverage floor and CI execution before merge when CI pipeline exists; neither replaces the selected strategy.

## Scope of tests for this unit
This unit's tests cover U1 foundation behavior only (C1–C6/A1–A4 baseline contracts and domain rules):
- Weekly plan slot structure and weekday time eligibility/exclusion reasons (BR1.1–BR1.2, FR1–FR3).
- ADR-006 lock snapshot, explicit generate/regenerate, stale warning, no auto-regeneration, confirmation before replacing edits (BR1.6–BR1.9, FR9).
- Ingredient re-resolution, merge baseline, no fabrication, unresolved/unknown handling (BR1.8, BR1.10).
- R-02 presence split: C5 batch presence mapping (name/presence only) and C4 apply/remap/unknown semantics (BR1.5, BR1.12, FR7–FR8).
- Export uses current edited list with mandatory share/copy fallback result states (BR1.11, FR10).
- Persistence boundary mapping/continuity metadata and privacy-safe fallback states (BR1.13, NFR1.1, NFR4.2).

Out of scope for execution in this unit: full U4 UI flows, live Sydänmerkki/PRISMA/S-ostoslista network integration, instrumented device/end-to-end UI automation (unless environment provides it), and Build-and-Test full-project coverage gate.

## Test framework setup and configuration
- **Runner:** Kotlin/JUnit unit tests for Android module (`:app`) using the project's configured JUnit variant (JUnit4/AndroidX test compatibility or JUnit5 via configured plugin) plus `kotlin-test` assertions as needed.
- **Libraries (planned, minimal):** `kotlinx-coroutines-test` for coroutine use cases, `MockK` (or equivalent) for boundary doubles, `Turbine` optional for Flow assertions, AndroidX test/core/Robolectric only if needed for local Android framework behavior. Room/DataStore integration stubs may use in-memory Room or local test doubles depending on feasibility in local JVM.
- **Configuration files:** `app/build.gradle.kts` test dependencies/tasks; optional `gradle.properties` test flags. Do not weaken coverage/quality thresholds to make a unit-scoped run pass.
- **Test data:** deterministic fixtures/fakes for recipes, plan slots, lock snapshots, ingredient refs, presence results, and share/export payloads. No real secrets, no live external endpoints, no precise personal location data.

## Exact unit-scoped commands (runnable before first test-first cycle)
> **All commands are scoped to this unit/module.** Do not use bare `./gradlew test`. A bare project-wide command would rerun unrelated tests and, in unit isolation, can misattribute coverage-threshold failures.

Run from workspace root after Gradle wrapper/project skeleton exists:

```bash
# Primary unit-scoped command for U1 domain/unit tests (exact package filter)
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.*" --tests "com.mealplanner.data.persistence.*" --console=plain
```

As tests are created, use exact class filters when debugging/regression scope is needed (examples; replace with actual class names created by this unit):

```bash
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.planning.WeeklyPlanStateTest" --console=plain
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.planning.LockAndStaleStateTest" --console=plain
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.shopping.ShoppingListGenerationRulesTest" --console=plain
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.shopping.ApplyBatchPresenceToListTest" --console=plain
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.export.ExportFallbackTest" --console=plain
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.data.persistence.PlanPersistenceMapperTest" --console=plain
```

If a selected test run exits non-zero only because a project-wide coverage threshold applies to all measured source while every selected test passes, record that with the unit test results; it is not a failed test for this unit. Do not put a later required step after such a threshold-failing command in the same command chain.

## How to run this unit's tests during Code Generation
1. After Step 2 bootstrap, run the primary unit-scoped command; it should execute the test classes present for U1.
2. For key behavior (Step 3), write failing tests first where practical, run the exact class/package command, record Red/failing output, implement minimal code (Step 4), rerun until green, then refactor.
3. For supporting behavior (persistence/platform abstractions), use the most practical cadence but keep tests runnable with the same unit-scoped command.

## Expected coverage targets
- **Standard strategy:** 5–8 tests per key component (planning/lock, recipe eligibility/source baseline, shopping merge/regenerate, presence application, export fallback, persistence mapping/boundaries) plus integration stubs for key Room/DataStore boundaries.
- **Key behavior emphasis:** ADR-006 stale warning exact text, no auto-regeneration, confirmation before replacing edits, no fabrication, PRISMA name/presence only, explicit fallback/unknown states.
- **Project coverage floor (mvp):** 80% line coverage is a project floor measured by **Build and Test** on the full assembled test suite. Unit-scoped runs in this stage do not judge that floor. Do not weaken threshold settings in build/test configuration to make unit-scoped runs pass.

## Mocking/stubbing guidance
- Mock or fake external boundaries (A1 Sydänmerkki, A2 PRISMA, A3 S-ostoslista, A4 platform effects) behind interfaces; return typed fallback/unknown/error results.
- Use deterministic clocks/IDs only if needed; avoid time/network nondeterminism.
- Never mock the domain rule under test in a way that hides behavior (especially ADR-006 state transitions, merge/no-fabrication, and R-02 split responsibilities).

## Test data management
- Keep fixtures in test source sets under `app/src/test/` (or shared test fixtures package) with clear names.
- Use minimal, explicit data: slot refs, recipe IDs, ingredient refs, normalized names, compatible/incompatible units, presence results, edited list states.
- Do not include secrets, tokens, precise coordinates, or personal identifiers.

## Final test inventory (U1)
| Component | Test class | Tests (actual) | Key behavior |
|---|---|---:|---|
| Planning/lock/schedule | `com.mealplanner.domain.planning.WeeklyPlanStateTest` | 7 | BR1.1–BR1.2 slot structure, empty visibility, weekday time eligibility/exclusions |
| ADR-006 lock/stale | `com.mealplanner.domain.planning.LockAndStaleStateTest` | 8 | BR1.6–BR1.9 snapshot IDs+context only, exact stale warning, no auto-regenerate, explicit confirmation |
| Shopping merge/re-resolution | `com.mealplanner.domain.shopping.ShoppingListGenerationRulesTest` | 6 | BR1.8, BR1.10 merge baseline, no fabrication, unresolved explicit |
| Presence split (R-02) | `com.mealplanner.domain.shopping.ApplyBatchPresenceToListTest` | 6 | BR1.12 C5 projections name/presence only, C4 apply/remap/unknown, no fabricated presence |
| Export fallback | `com.mealplanner.domain.export.ExportFallbackTest` | 7 | BR1.11 current edited list, share/copy/fallback states, official path absent |
| Persistence mapping | `com.mealplanner.data.persistence.PlanPersistenceMapperTest` | 3 | Continuity metadata, snapshot context, list edits/presence/unresolved round-trip |
| Continuity boundary stub | `com.mealplanner.data.persistence.ContinuityRepositoryBoundaryTest` | 2 | Room/DataStore boundary intent, explicit null/no fabrication |
| Snapshot boundary stub | `com.mealplanner.data.persistence.SnapshotPersistenceBoundaryTest` | 1 | ADR-006 continuity/snapshot context boundary |
| Shopping boundary stub | `com.mealplanner.data.persistence.ShoppingListPersistenceBoundaryTest` | 1 | Edits/presence/unresolved continuity boundary |

**Total:** 41 tests across 9 test classes (includes 3 persistence boundary stub classes / 4 stub tests runnable in local JVM with in-memory fakes). All selected tests passed.

## Execution evidence (2026-11-09 rebuild verification)
- **Executed (unit-scoped):** `cd /Users/christfm/opencode/meal-planner && ./gradlew :app:testDebugUnitTest --tests 'com.mealplanner.domain.*' --tests 'com.mealplanner.data.persistence.*' --console=plain`
- **Result:** `BUILD SUCCESSFUL` (27 actionable tasks at first run; rerun UP-TO-DATE). XML results in `app/build/test-results/testDebugUnitTest/` report **9 suites, 41 tests, 0 failures, 0 errors, 0 skipped**: `ContinuityRepositoryBoundaryTest` (2), `PlanPersistenceMapperTest` (3), `ShoppingListPersistenceBoundaryTest` (1), `SnapshotPersistenceBoundaryTest` (1), `ExportFallbackTest` (7), `LockAndStaleStateTest` (8), `WeeklyPlanStateTest` (7), `ApplyBatchPresenceToListTest` (6), `ShoppingListGenerationRulesTest` (6).
- **Environment:** Gradle 8.14.6 wrapper present (including `gradle/wrapper/gradle-wrapper.jar`), macOS aarch64, JBR 21.0.11, Android SDK configured (`local.properties`). No project-wide coverage-threshold failure observed in this unit-scoped run.
- **Custom ordering evidence:** tests and satisfying implementation already existed in this rebuild context; tests were executed and passed. **Red/failing-before-Green evidence was not produced in this session** and is not claimed.
- **Not executed (expected/deferred):** instrumented/Compose UI device tests (U4), performance benchmarks, full CI 80% line-coverage/CI-before-merge gate. Do not claim those executed. Quality/coverage targets were not weakened.
