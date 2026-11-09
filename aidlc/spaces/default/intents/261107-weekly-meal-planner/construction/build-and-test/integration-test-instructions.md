# Integration Test Instructions — Meal Planner MVP

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, test strategy `Standard`)  
**Stage:** Build and Test (Construction 3.6)  
**Date:** 2026-11-09

## Scope
Standard strategy requires key boundary/cross-unit interaction integration tests. For this Android MVP, integration coverage is primarily provided by **JVM unit tests with fake/stub boundaries** (Room/DataStore boundary intent tests, adapter fakes, U1–U3 domain/use-case interactions, U4 thin-UI facades against fake U1–U3 ports). No live Sydänmerkki/PRISMA/S-ostoslista network calls are executed; OQ1–OQ4 remain investigational/gated with mandatory fallbacks.

## Test framework setup
- **Runner:** JUnit4/kotlin-test via `:app:testDebugUnitTest` (Android JVM unit tests, `unitTests.isReturnDefaultValues=true`).
- **Libraries:** `kotlinx-coroutines-test`, MockK, Turbine (as configured).
- **Boundaries/fakes:** curated recipe source/A1 gate, PRISMA presence/store fakes, export/share/copy ports, persistence mapper/boundary stubs, UI facades with fake domain services.
- **Determinism:** fixed fixtures/IDs; no real secrets, endpoints, tokens, or precise GPS/PII.

## Key boundary areas
| Boundary | Units | Evidence/tests |
|---|---|---|
| Planning/lock/stale (ADR-006/R-01–R-03/R-06) | U1, U2, U4 | `WeeklyPlanStateTest`, `LockAndStaleStateTest`, `C3SlotSelectionServiceTest`, `PlanExperienceFacadeTest`, `GroceryExperienceFacadeTest` |
| Recipe/catalog/time/season/nutrition/alternatives (C2/C3/A1, FQ1=A) | U2, U4 UI mapping | `A1RecipeSourceAdapterGateTest`, `RecipeCatalogServiceTest`, `RecipeTimeNormalizationEvaluatorTest`, `SeasonFitEvaluatorTest`, `NutritionCueCalculatorTest`, `AlternativeRecipesServiceTest` |
| Shopping merge/regenerate/edits/presence projection (R-02, BR3.1–BR3.5) | U1, U3, U4 | `ShoppingListGenerationRulesTest`, `ApplyBatchPresenceToListTest`, `ShoppingListC4CompletionTest`, `ShoppingListServiceTest`, `StoreAvailabilityAndDiagnosticsTest`, `GroceryExperienceFacadeTest` |
| Store presence FR8 name/presence only + manual fallback | U3, U4 | `StoreAvailabilityContractsTest`, `StoreAvailabilityAndDiagnosticsTest`, `SupportingUiBehaviorTest` |
| Export seam + S-ostoslista investigational/fallback (FR10/OQ4, BR3.8/BR3.9) | U1, U3, U4 | `ExportFallbackTest`, `ExportShoppingListUseCaseTest`, `GroceryExperienceFacadeTest`, `SupportingUiBehaviorTest` |
| Persistence continuity/privacy-safe boundaries | U1 | `ContinuityRepositoryBoundaryTest`, `PlanPersistenceMapperTest`, `SnapshotPersistenceBoundaryTest`, `ShoppingListPersistenceBoundaryTest` |

## How to run
```bash
# Full assembled JVM integration/unit suite (deduplicated project command)
./gradlew :app:testDebugUnitTest --console=plain

# Unit-scoped slices (diagnostic only; do not use for project coverage floor)
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.*" --tests "com.mealplanner.data.persistence.*" --console=plain
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.recipe.*" --tests "com.mealplanner.domain.planning.SlotSelection*" --tests "com.mealplanner.domain.planning.C3*" --tests "com.mealplanner.adapters.A1*" --tests "com.mealplanner.adapters.RecipeSourceAdapter*" --console=plain
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.shopping.*" --tests "com.mealplanner.domain.availability.*" --tests "com.mealplanner.domain.export.*" --console=plain
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.ui.*" --console=plain
```

## Coverage expectations
- Standard strategy: unit tests plus integration tests for key boundaries (satisfied by fake-backed boundary tests above).
- MVP additive floor: 80% line coverage on the full assembled test suite is verified by Build and Test/CI obligations; do not weaken thresholds. See `test-results.md`.

## Test data/environment
No live external services, no secrets, no MealPlanner account, no raw GPS/PII. Use in-memory fixtures and typed fallback/unknown outcomes.

## Not executed / deferred
- Real-device/instrumented Compose UI tests and end-to-end Android flows (not required by current unit scopes; UI evidence is fake-backed JVM tests).
- Live PRISMA/Sydänmerkki/S-ostoslista integration (OQ1–OQ4 investigational/gated; mandatory fallbacks exercised).
- Performance/security NFR execution beyond design/contract evidence (no separate performance/security instruction files generated for Standard unless required; see summary).