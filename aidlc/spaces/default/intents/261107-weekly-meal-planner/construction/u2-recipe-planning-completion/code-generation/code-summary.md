# Code Summary — U2 `u2-recipe-planning-completion` (Code Generation)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Unit:** U2 — Recipe + planning completion (C2/C3 + A1)  
**Stage:** Construction / Code Generation  
**Date:** 2026-11-09

## What was built
Implemented U2 domain/adapter behavior for recipe catalog normalization, weekday eligibility/time normalization, season fit, nutrition cues, alternatives, C3 slot-selection stale signaling, and A1 Sydänmerkki boundary with FQ1=A gating and mandatory curated fallback. Work was done in-place in the brownfield Android/Kotlin app under `app/` (no duplicate `*_modified` files, no app code under `aidlc/`).

### Key behavior delivered
- **A1 (BR2.1, FQ1=A, NFR2.1/NFR2.4/NFR3.1–3.3/NFR5.2):** `RecipeSourceBoundary`/`A1RecipeSourceAdapter` path includes default-off auditable live gate (`allowLiveSource` + OQ1-approved path), bounded/safe mapping, typed statuses (`ok|fallback_used|unavailable|restricted|timeout|parse_error_mapped|not_supported`/equivalent), and mandatory curated/imported fallback. No scraping/terms assumptions; no raw exception leakage.
- **C2 catalog/facade (BR2.2/BR2.9):** `RecipeCatalogService` exposes catalog/detail/ingredient resolution via boundary with explicit safe normalization; missing/partial data not fabricated.
- **Weekday time normalization (BR2.3, FR3):** `RecipeTimeNormalizationEvaluator` produces deterministic normalization for weekday Mon–Fri Breakfast/Dinner contexts: `KNOWN` total <=30 eligible, >30 => `WEEKDAY_TIME_OVER_30`; missing/ambiguous/unparseable/confusing => explicit `MISSING|AMBIGUOUS` and `WEEKDAY_TIME_AMBIGUOUS` exclusion, never guessed.
- **Season fit (BR2.4, FR5):** `SeasonFitEvaluator` computes `IN_SEASON|OUT_OF_SEASON|SHOULDER|SEASON_FIT_UNKNOWN` from tags/curated mapping and effective season resolution context; insufficient evidence yields unknown (no false match); override precedence carried through effective resolution.
- **Nutrition cue (BR2.5, FR6, OQ6):** `NutritionCueCalculator` computes `WeeklyNutritionBalanceCue` only from `WeeklyNutritionBalanceCueRequest` inputs (selected refs + profiles map), excludes missing/unsupported data with explicit counts/status, labels unavailable as `Nutrition information unavailable` semantics, and never reads `WeeklyPlanState`. Non-medical only.
- **Alternatives (BR2.6):** `AlternativeRecipesSelector` (with narrow catalog port) filters alternatives by slot/weekday eligibility, source/fallback status, and typed outcomes; empty/unavailable/fallback reasons explicit; no fabrication.
- **C3 slot selection + ADR-006/R-06 (BR2.7/BR2.8/BR1.9):** `C3SlotSelectionService` supports `SET_SELECTION|REPLACE_WITH_ALTERNATIVE|CLEAR_SELECTION` on target `PlanSlotRef` only, validates invalid refs/ops with typed rejection, preserves single `WeeklyPlanState` authority, and marks stale when lock/list context active using exact warning: `"Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."` No auto-regeneration.
- **Diagnostics (NFR5.4–5.6, NFR3.1):** uses existing `DiagnosticsSink`/`NoOpDiagnosticsSink`/`PrivacySafeDiagnostics` safe hook pattern; A1 boundary wired.

## Files created/modified (application)
See `source-manifest.json`. Notable U2 app files:
- Modified: `app/src/main/java/com/mealplanner/adapters/A1RecipeSourceAdapter.kt`, `adapters/RecipeSourceBoundary.kt`, `adapters/IntegrationStubs.kt` (as needed), `domain/model/*` additive/compatibility updates (`U2Types.kt`, `U2Validation.kt`, others as needed), `diagnostics/Diagnostics.kt` preserved/wired usage.
- Created/modified: `app/src/main/java/com/mealplanner/domain/recipe/{RecipeCatalogService,RecipeCatalogPortAdapter,RecipeTimeNormalizationEvaluator,SeasonFitEvaluator,NutritionCueCalculator,AlternativeRecipesSelector}.kt`
- Created: `app/src/main/java/com/mealplanner/domain/planning/C3SlotSelectionService.kt`
- Preserved ADR-006 behavior: `app/src/main/java/com/mealplanner/domain/planning/{Adr006LockRules,PlanLockService}.kt`

## Tests added/modified
- `app/src/test/java/com/mealplanner/adapters/A1RecipeSourceAdapterGateTest.kt`
- `app/src/test/java/com/mealplanner/domain/recipe/{U2ModelValidationTest,RecipeCatalogServiceTest,RecipeTimeNormalizationEvaluatorTest,SeasonFitEvaluatorTest,NutritionCueCalculatorTest,AlternativeRecipesServiceTest}.kt`
- `app/src/test/java/com/mealplanner/domain/planning/C3SlotSelectionServiceTest.kt`
- `app/src/test/java/com/mealplanner/diagnostics/U2DiagnosticsHookTest.kt`

## Tests executed and results
Executed (unit-scoped, command from `unit-test-instructions.md`):
```bash
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.recipe.*" --tests "com.mealplanner.domain.planning.SlotSelection*" --tests "com.mealplanner.domain.planning.C3*" --tests "com.mealplanner.adapters.A1*" --tests "com.mealplanner.adapters.RecipeSourceAdapter*" --console=plain
```
**Result:** `BUILD SUCCESSFUL` (27 actionable tasks, up-to-date in final run). Selected U2 tests passed.

Additional verification during implementation: `WeeklyPlanStateTest` and targeted U2 suites were run successfully (`BUILD SUCCESSFUL`).

## Unexecuted tests / reasons
- Full-project unit test suite with 80% line-coverage floor (`./gradlew :app:testDebugUnitTest --console=plain` full assembled suite) was **not executed as the Build and Test whole-project coverage gate** in this Code Generation unit-scoped step. Rationale: unit-scoped execution per `unit-test-instructions.md`; Build and Test stage checks whole-project coverage/floors on full assembled suite. No quality targets were weakened.

## Deviations / risks
- Step 12 used existing diagnostics hook interfaces (`DiagnosticsSink`/`NoOpDiagnosticsSink`/`PrivacySafeDiagnostics`) rather than introducing a duplicate `U2Diagnostics` type; behavior satisfies safe structured/no-op/fake-capture intent.
- Some unit tests were added as practical TDD/green iterations due to existing partial U2 files from interrupted session; key behaviors covered with explicit tests and all selected suites green.
- **Adversarial review iteration 1 (NOT-READY) fixed:** `AlternativeRecipesSelector` now applies BR2.4-consistent season-fit context using `effectiveSeasonResolution` + recipe details/tags when available (excludes `OUT_OF_SEASON`; keeps `IN_SEASON`/`SHOULDER`/`SEASON_FIT_UNKNOWN`/no-evidence to avoid fabrication), honors `includeFallbackSources` with typed `UNAVAILABLE`/`FALLBACK_SOURCES_EXCLUDED` when fallback-only sources are excluded, and adds targeted tests for in-season/out-of-season/unknown and fallback exclusion. `RecipeCatalogPort`/adapter extended with `detailsByIds` for season evidence.
- PRISMA store/ingredient presence flows remain U3/U4-owned (U2 provides contracts only where relevant); U4 UI/permissions/orchestration and U3 C4 apply-batch/regeneration remain out of scope.

## Invariants preserved
- **FQ1=A:** Sydänmerkki live path required in boundary but default-off and only after confirmed OQ1 feasibility/terms/allowed access (auditable gate); mandatory curated/imported fallback always available/runnable; no scraping/terms assumptions.
- **No fabrication:** missing/ambiguous/unavailable/unknown/partial states explicit and typed; never guessed.
- **ADR-006 / R-06:** exact stale warning preserved: `"Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."`; no auto-regeneration in C3; snapshot IDs+context boundary respected; single `WeeklyPlanState`/C3 authority for target-slot changes.
- **R-06 nutrition cue:** computed only from `WeeklyNutritionBalanceCueRequest` inputs; never reads `WeeklyPlanState`.
- **Canonical slot ref:** `PlanSlotRef` only (no `MealSlotRef`).
- **S-ostoslista fallback / PRISMA boundaries:** unaffected by U2; PRISMA remains name/presence-only where applicable (U3/U4).
- **NFR4.3:** no quality/coverage targets weakened.

## Loop-Back Repair Note (2026-11-10, Build-and-Test Loop-Back 1)
- Brownfield replay: U2 implementation/tests already existed and passed scoped verification; plan steps remained satisfied.
- Added targeted JVM coverage-repair tests (no threshold/invariant weakening): `app/src/test/java/com/mealplanner/buildandtest/CoverageRepairU2BoundariesTest.kt` (FQ1=A default gate, `FallbackRecipeSourceBoundary` typed no-fabrication behavior, `RecipeCatalogPortAdapter` delegation, C3 missing-slot/no-change/replace/ADR-006 stale-when-active-snapshot cases).
- Verified: `./gradlew :app:testDebugUnitTest --tests "com.mealplanner.buildandtest.CoverageRepairU2BoundariesTest" --console=plain` → `BUILD SUCCESSFUL`; U2 scoped command from `unit-test-instructions.md` previously/currently `BUILD SUCCESSFUL` (27 tasks up-to-date). Project-wide coverage/traceability gaps remain Build-and-Test cross-unit concerns.

## Artifacts
- Plan: `aidlc/spaces/default/intents/261107-weekly-meal-planner/construction/u2-recipe-planning-completion/code-generation/code-generation-plan.md`
- Unit test instructions: `.../code-generation/unit-test-instructions.md`
- Traceability: `.../code-generation/traceability.json`
- Source manifest: `.../code-generation/source-manifest.json`
- Code summary: `.../code-generation/code-summary.md`
