# Unit Test Instructions — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, test strategy `Standard`)  
**Unit:** U2 — Recipe + planning completion (`library`, C2/C3 + A1)  
**Stage:** Code Generation (construction)  
**Date:** 2026-11-09

## Scope
Requirement/key-behavior coverage for BR2.1–BR2.9 and NFR4.1–NFR4.3 (Standard strategy: ~5–8 tests per component/key area, with integration-like boundary tests using fakes/stubs; additive MVP floor/CI posture preserved). All commands below are **unit-scoped** (exact class/package filters or module tasks) to avoid rerunning the whole suite per unit in Build and Test.

## Test framework and setup
- **Runner:** JUnit (Kotlin/JVM unit tests) via Android Gradle/Kotlin test setup already established by U1 (`app/src/test/...`, `kotlin.test`).
- **Fakes/stubs:** Use in-test fakes for `RecipeSourceBoundary`/A1 adapter ports, curated fallback sources, and diagnostics hooks; **no live Sydänmerkki/network in CI**.
- **Config:** Reuse existing test configuration. If a new U2 module is introduced, add minimal test task/config and record the exact command here and in the plan.

## Exact runnable commands (before first test-first cycle, and for verification)
Run from project root. Use `--console=plain`. These commands are scoped to U2-relevant tests; they **always include U2 tests regardless of module layout** (per infrastructure-design gate). Keep existing tests green; do not weaken thresholds.

```bash
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.recipe.*" --tests "com.mealplanner.domain.planning.SlotSelection*" --tests "com.mealplanner.domain.planning.C3*" --tests "com.mealplanner.adapters.A1*" --tests "com.mealplanner.adapters.RecipeSourceAdapter*" --console=plain
```

If U2 code is split into a dedicated module later, add that module’s unit-test task (scoped to U2 packages/classes) alongside the command above and update this file.

## Expected coverage targets
- **Project floor:** MVP/Standard additive floor is 80% line coverage (team/org posture). **Build and Test checks the project coverage floor on the full assembled test suite.** A unit-scoped run does not judge the whole-project floor; do not change coverage thresholds to make a unit-scoped run pass. If a unit-scoped command exits non-zero only because a whole-project coverage threshold fails while all selected tests pass, record that result for this unit—it is not a failed test (run such commands without later steps that the threshold exit would stop).
- **U2 target:** Meaningful coverage for BR2.1–BR2.9 key behaviors (adapter gate/fallback/status mapping, normalization no-guess, season fit/unknown, nutrition unavailable/cue boundaries, alternatives, C3 slot state/stale semantics). Add targeted regressions for review findings.

## Mocking/stubbing guidance
- **A1 live gate:** default `allowLiveSource=false` and OQ1 not approved in tests unless a controlled scenario explicitly injects an approved gate; assert no live client invocation when gated off.
- **Adapter outcomes:** assert typed statuses `ok|fallback_used|unavailable|restricted|timeout|parse_error_mapped|not_supported` and safe mapped reasons; no raw exceptions across boundaries.
- **Time/season/nutrition:** pure deterministic inputs; no fabricated values for missing/ambiguous data.
- **ADR-006/R-06:** assert exact stale warning `"Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."` and no auto-regeneration; only target slot mutates.

## Test data management
- Use small in-memory fixtures for curated recipe catalogs, ingredient lines, season contexts, and nutrition profiles.
- Keep fixtures minimal and readable; prefer builders/helpers in test scope.
- No PII/secrets in fixtures or logs. No external network/resources.