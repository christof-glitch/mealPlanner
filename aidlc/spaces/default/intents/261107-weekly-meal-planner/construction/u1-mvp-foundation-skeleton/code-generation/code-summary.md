# Code Summary — U1 `u1-mvp-foundation-skeleton`

**Intent:** `261107-weekly-meal-planner` (mvp, default space)  
**Unit:** U1 MVP foundation + integration slice  
**Date:** 2026-11-09 (rebuild verification)

## What was built
Greenfield Android Kotlin/Jetpack Compose foundation (single `:app` module, modularized packages) implementing baseline C1–C6/A1–A4 contracts for the Meal Planner MVP:

- **Domain models (ENT-U1 baseline):** plan/slots/lock snapshot/context, recipe/ingredient refs, exclusion/source/fallback statuses, shopping list/generation results, store presence batch/apply contracts (R-02 split), export payload/result.
- **Planning/lock rules (ADR-006/R-01–R-03):** exact stale warning text `Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.` (defined in `com.mealplanner.domain.planning.Adr006LockRules`); snapshot stores IDs/selections + plan context only (no ingredient freeze); explicit generate/regenerate; no auto-regeneration; regeneration with existing edits requires explicit confirmation (`NEEDS_CONFIRMATION`); plan changes mark `STALE_LOCK`.
- **Weekday eligibility (BR1.1–BR1.2):** 7-day/16-slot required structure (Sat/Sun B,L,D; Mon–Fri B,D), empty slots visible; weekday >30 min => `WEEKDAY_TIME_OVER_30`; missing/ambiguous time => `WEEKDAY_TIME_AMBIGUOUS` with explanation fields.
- **Shopping merge/re-resolution (BR1.8, BR1.10):** re-resolution/merge baseline with normalized name + compatible unit/amount merge; incompatible/ambiguous kept separate or explicit; missing/changed ingredient data becomes explicit unresolved with no fabricated qty/unit.
- **Presence split (R-02, BR1.12):** C5 `PresenceMappingBoundary` / `MapIngredientsToStorePresenceBatch` contract+fallback stub returns name/presence projections only (`PRESENT|NOT_FOUND|UNKNOWN`), never merge/stale; C4 `ShoppingListRules.applyBatchPresenceToList` applies projections/remap and sets unknown after ambiguous mapping/edit/merge context; C4 performs no PRISMA lookup.
- **Export fallback (BR1.11, R-03):** `ExportFallbackUseCase` builds payload from current edited list and returns typed `SHARED|COPIED|CANCELLED|FAILED|FALLBACK_INSTRUCTIONS_SHOWN`; official S-ostoslista path absent uses mandatory share/copy/manual fallback; extraction seam deferred to U3.
- **Persistence baseline (FR11/FR12/BR1.13, NFR1.1):** Room entities/DAO/database + DataStore repository split; minimal continuity mappers for plan/snapshot/shopping list/metadata (simple encoding for foundation); repository interface for plan/snapshot/list/store/season continuity.
- **Adapters/platform/diagnostics (NFR1.2, NFR2.1, NFR3.2, NFR4.3–NFR4.6):** C2/C5 fallback-safe boundary stubs (no live Sydänmerkki/PRISMA calls), A4 platform abstractions (permission/share/clipboard/date/location coarse signals, no continuous tracking), `withTypedTimeout` bounded timeout + typed fallback helper, privacy-safe diagnostics sanitizing forbidden coordinate/personal-ID key fragments.
- **UI shell:** minimal Compose root showing foundation state and exact stale warning contract; U4 owns full UX.

## Files created/modified
See `source-manifest.json` (47 application-source/config paths). Key paths:
- Root: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, `gradle/wrapper/gradle-wrapper.jar`, `gradlew`, `gradlew.bat`.
- App: `app/build.gradle.kts`, manifest/resources, `com.mealplanner.domain.*`, `adapters.*`, `data.persistence.*`, `platform.*`, `diagnostics.*`, `ui.*`, and 9 test classes under `app/src/test/java/com/mealplanner/...`.
- Stage record (updated in this rebuild): `code-generation-plan.md` (all 8 steps verified/ticked), `unit-test-instructions.md`, `code-summary.md`, `source-manifest.json`, `traceability.json`.

## Key decisions
- **Gradle/JUnit:** JUnit4 + `kotlin-test` + MockK/coroutines-test/Turbine for JVM-runnable domain tests; AGP `:app:testDebugUnitTest` unit-scoped filters.
- **No ingredient freeze in snapshot:** enforced by type design (LockSnapshot contains selections/context only) and ADR-006 tests.
- **R-02 split preserved:** `PresenceMappingBoundary`/C5 stub returns projections only; C4 `applyBatchPresenceToList` consumes projections and owns remap/unknown/merge-adjacent application; no PRISMA lookup in C4.
- **No fabrication:** merge/re-resolution/presence paths return explicit unresolved/unknown/fallback states; amounts/units not invented.
- **Privacy:** no secrets/endpoints hardcoded; diagnostics sanitize coordinate/personal-ID-like keys; location abstraction is coarse/optional.
- **U3 extraction seam deferred:** export implemented behind simple C6 boundary with fallback payload/result; no `ExportShoppingListUseCase`/adapter port extraction claimed.

## Testing posture (authoritative contract markers)
- **strategy:** `standard`
- **methodology:** `custom` (source: `team`)
- **ordering:** For key behavior, write tests before implementation (TDD/BDD style where practical), then implement to satisfy those tests; for supporting or lower-risk behavior, use the most practical test cadence and keep tests runnable in CI.
- **methodology_evidence:** Human affirmed Q3 for this learning/ASAP Android MVP. “Custom” reflects a practical mix (test-first for key behavior, flexible where practical) rather than a single pure cadence.
- **affirmed_at:** 2026-11-07 (team.md practices-discovery affirmation gate context; testing posture carried into this intent)
- **unit coverage baseline:** 41 tests across 9 classes for U1 key components and persistence boundaries (34 domain + 7 persistence/boundary); mvp 80% line-coverage floor and CI execution before merge remain Build-and-Test/CI obligations on the full suite — this unit-scoped run does not judge that floor. **Quality/coverage targets were not weakened.**
- **scope/project:** `mvp`, greenfield.

## Tests written / executed
- **Written:** 41 tests in 9 classes (7 plan/eligibility, 8 lock/stale, 6 merge/re-resolution, 6 presence split, 7 export fallback, 3 persistence mapper, 4 persistence boundary stub tests across 3 classes). Meets standard strategy volume for key components and includes key-boundary integration stubs.
- **Executed (2026-11-09):** `./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.*" --tests "com.mealplanner.data.persistence.*" --console=plain` -> **BUILD SUCCESSFUL**. XML results (`app/build/test-results/testDebugUnitTest/`): **41 tests, 0 failures, 0 errors, 0 skipped, 9 classes**.
  - `ContinuityRepositoryBoundaryTest` (2), `PlanPersistenceMapperTest` (3), `ShoppingListPersistenceBoundaryTest` (1), `SnapshotPersistenceBoundaryTest` (1), `ExportFallbackTest` (7), `LockAndStaleStateTest` (8), `WeeklyPlanStateTest` (7), `ApplyBatchPresenceToListTest` (6), `ShoppingListGenerationRulesTest` (6).
- **Custom ordering / Red-Green evidence:** tests and satisfying implementation already existed in this rebuild context; tests were executed and passed. **Red/failing-before-Green evidence was not produced in this session** and is not claimed.
- **Not executed (expected/deferred):** instrumented/Compose UI device tests (U4/full UX/deferred), performance benchmarks, CI 80% line-coverage floor on full suite (Build and Test/CI).

## Deviations and notes
- **Red/Green evidence not re-produced:** custom methodology requires test-first for key behavior where practical and Red evidence before Green when tests can run. Existing key-behavior tests were executed and passed in this session; a Red/Green cycle was not re-run because the behavior-defining tests and implementation were already present. All required key behaviors remain covered by explicit tests.
- **Coverage/quality targets not weakened:** no threshold configured to pass/fail unit-scoped runs inappropriately; 80% line-coverage floor remains a Build-and-Test/CI gate.

## Traceability
- Plan/eligibility: FR1–FR3, BR1.1–BR1.2, US1.1/US2.2.
- Lock/generate/regenerate/stale: FR9, ADR-006/R-01–R-03, BR1.6–BR1.10, US3.1/US3.2.
- Presence split: FR7–FR8, BR1.5, BR1.12, R-02, US4.2/US5.1.
- Export fallback: FR10, BR1.11, R-03, US3.3/US4.1.
- Reliability/privacy/local continuity: FR11–FR12, NFR1–NFR5, BR1.13–BR1.14, US5.1–US5.6.
- `traceability.json` regenerated by `aidlc engine sensor-traceability` and targets set to `OK`/`Deferred` for existing workspace-relative files.
