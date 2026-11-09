# Code Summary — U4 `u4-android-ui-experience`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Unit:** U4 — Android UI experience (`ui`)  
**Stage:** Code Generation (construction, per-unit)  
**Date:** 2026-11-09

## What was implemented
- **UI skeleton & state/contracts (Steps 1,3-5):** `com.mealplanner.ui` packages for navigation/state/facades/mappers/screens/components/platform. Thin orchestration over U1–U3 (ADR-005) via `PlanExperienceFacade`, `GroceryExperienceFacade`, `StoreAvailabilityFacade`, and `DiagnosticsUiMapper`. State models for slots/plan/today/grocery/store/user.
- **Navigation shell & primitives (Step 6):** `MealPlannerScaffold` + bottom nav (Today/Plan/Grocery/User), pushed `RecipeDetail`, full-screen `ReplaceAlternatives`/`SelectPrismaStore`; `SlotContextNav` stable R-06 slot-context encoding; route arg wiring. Shared primitives: status/error/R-05 panels, slot card, nutrition cue, season indicator, recipe badges, FR8 presence row, regenerate confirmation dialog, grocery checklist row (accessibility labels/testTag, non-color-only text).
- **Screens (Steps 7-9):** RM-01 onboarding/permissions (location purpose, manual alternative, R-05 next actions), RM-02 Today and RM-03 Plan (shared slot state, unfilled slots remain empty, lock control linking to Grocery without auto-generation, season context), RM-04 Recipe detail and RM-05 Alternatives (slot context/back-stack, domain-provided labels/eligibility explanations only), RM-06 Grocery (readiness/explicit generate, regenerate confirmation when `hasUserEdits`, exact ADR-006 stale warning/no auto-regeneration, edit/check/add/remove, FR8 presence display, RM-08 export actions using current edited list with official S-ostoslista investigational + share/copy fallback), RM-07 store selection (manual always available, suggestion best-effort), RM-09 User (season override/default/reset, permission/privacy).
- **Platform/wiring (Step 10):** `PermissionStatusProvider`/`DefaultPermissionStatusProvider`, `ShareCopyPlatformBoundary` (`ShareExportPort`) confined to UI/platform boundary (typed outcomes only, no domain Android types). `MealPlannerRoot` launches `MealPlannerScaffold`; `MainActivity`/`MealPlannerApp` entry preserved. No secrets/endpoints hardcoded.
- **Build config (Steps 6,12):** Added `androidx.navigation:navigation-compose` and `androidx.compose.material:material-icons-core` (minimal, required by U4 shell).

## Key invariants preserved
- ADR-005 thin UI: no duplication of R-03/FR3/FR9 business rules.
- R-06: shared Today/Plan slot state, slot context carried in nav/actions, unfilled slots remain empty.
- R-05: RM-01/RM-02/RM-03 render explicit next actions (retry/bundled fallback/manual planning where feasible).
- ADR-006/R-01-R-03: explicit generate/regenerate, confirmation when `hasUserEdits`, exact stale warning `Adr006LockRules.STALE_WARNING_TEXT`, no auto-regeneration, export current edited list.
- FR8: PRISMA name/presence only (`PRESENT|NOT_FOUND|UNKNOWN` + reason), never stock/guaranteed availability; manual checklist fallback.
- FR10/OQ4: official S-ostoslista investigational; mandatory share/copy fallback with instructions.
- NFR3: no MealPlanner account/raw GPS/PII in UI diagnostics; privacy-safe mapping/sanitization.

## Tests written/run
- **Key behavior (test-first where practical):** `PlanExperienceFacadeTest` (5), `GroceryExperienceFacadeTest` (5), `StoreAvailabilityAndDiagnosticsTest` (5) — Red→Green cycles recorded in plan.
- **Supporting (Step 11):** `SupportingUiBehaviorTest` (7) covering label contracts, FR8 unknown/manual fallback, export preserves current edited list, settings defaults/reset/privacy, exact ADR-006 stale warning state, accessibility/non-color label smoke.
- **Runner/smoke:** `UiRunnerSmokeTest` (1).
- **Executed:** `./gradlew :app:testDebugUnitTest --tests "com.mealplanner.ui.*" --console=plain` — **BUILD SUCCESSFUL** (2026-11-09, repeated during Steps 2–12).
- **Not executed in this step:** full-project `testDebugUnitTest` / assembled 80% line-coverage floor and CI-before-merge verification (additive project obligations checked by Build and Test on full suite). No thresholds weakened.

## Deviations / notes
- Compose instrumentation/UI rendering tests were not added (unit/fake-backed scope per plan; `accessibility/content-description smoke` covered practically via state/mapper contracts and testTag/semantics in UI).
- `GroceryExperienceFacade` uses local editable aggregate as current edited list for display/export; persistence/save path boundary remains explicit per BR3.9/NFR carry-forward and can be wired to C4 persistence in integration.
- `MealPlannerScaffold` currently renders with default empty states; Step 10 preserves entry wiring and full facade/DI/state-holder wiring is intentionally minimal for MVP brownfield slice.
- `material-icons-core` added; bottom-nav Plan icon uses `Icons.AutoMirrored.Filled.List` (core-safe).
- I did not modify `MainActivity.kt` (entry behavior preserved through `MealPlannerRoot()`).
