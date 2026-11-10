# Code Summary — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Unit:** U3 — Grocery + store + export completion (`library`, C4/C5/C6)  
**Stage:** Code Generation (construction, per-unit)  
**Date:** 2026-11-09  
**Testing contract:** `sha256:08f951c92a56228b04800253ce17226a44c030963a64e478b9006a6d4f7c3c01` (custom ordering, Standard strategy, MVP floor)

## What was built (brownfield-safe, additive)

- **C4 domain evolution (`shopping`/models):** Added/aligned C4 aggregate, patch, merge, stale-warning, generation/regeneration, and explicit BR3.5 presence-batch application types in `ShoppingModels.kt`/`CoreTypes.kt`. Implemented `ShoppingListRules.evaluateRegenerationRequest`, `applyItemPatch`, and `applyPresenceCheckBatch` (scoped by `shoppingListId` + store + stable item/query key precedence, ambiguous remap => `UNKNOWN`/`NOT_CHECKED` with reason, no item mutation by C5). Preserved legacy `applyBatchPresenceToList` behavior for existing tests (including `UNKNOWN_AFTER_EDIT_NO_MAPPING` and no fabricated present upgrade for unresolved merged items).
- **C4 use case/service:** Implemented `ShoppingListService` to generate/regenerate only from a valid locked snapshot (`LockSnapshot` IDs+plan context), re-resolve ingredients via injected `RecipeSourceBoundary` port (ADR-006/R-01), return typed partial/missing/invalid outcomes with no fabrication (BR3.1), deterministic merge via rules (BR3.2), require confirmation before rebuilding edited lists and never auto-regenerate (BR3.3/R-03), reflect stale status with exact ADR-006 warning text (BR3.4), and prepare `ShoppingListExportDto` from current edited aggregate (BR3.8).
- **C5/A2 availability + store contracts (`availability`/models):** Created `domain/availability` with `StorePresenceUseCase` and `StoreSelectionUseCase`. `StorePresenceUseCase` maps PRISMA name/presence batches to typed `PresenceCheckBatchResult` outcomes (`PRESENT|NOT_FOUND|UNKNOWN` only; `OK|PARTIAL|UNAVAILABLE|DENIED|FAILED_MAPPED`), privacy-safe mapped reasons, and never mutates shopping items. `StoreSelectionUseCase` keeps manual store selection always available and supports best-effort nearest suggestion via port with manual fallback; no raw GPS handling in domain. `PresenceModels.kt` evolved with C5 query/result/batch/store-selection DTOs.
- **C6/A3 export seam (`export`/models):** Created inward export seam in `ExportSeam.kt` (`ExportShoppingListUseCase`, `DefaultExportShoppingListUseCase`, `ShareExportPort`, `SostoslistaOfficialExportPort`, `OfficialExportAttempt`) using existing `ExportModels.kt` DTOs (`ShoppingListExportDto`, `ExportRequest/Result`, `ExportMethod`, `SostoslistaAttemptStatus`, `ExportInvestigationOutcome`). `DefaultExportShoppingListUseCase` exports current edited DTO, keeps mandatory share/copy fallback instructions, and maps S-ostoslista investigational outcomes explicitly without fabricating success: official `SUCCESS => USED`, `NOT_SUPPORTED => NOT_FEASIBLE_CONFIRMED` fallback, `UNAVAILABLE|FAILED_MAPPED => PARTIALLY_USED` fallback. No Android intent types in domain. Legacy `ExportFallbackUseCase`/share payload/result APIs remain compatible; existing export fallback tests pass.

## Tests and execution

**Executed (U3-scoped, not full suite):**
```bash
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.shopping.*" --tests "com.mealplanner.domain.availability.*" --tests "com.mealplanner.domain.export.*" --console=plain
```
- Result after Step 5: `BUILD SUCCESSFUL in 2s` (27 actionable tasks: 4 executed, 23 up-to-date).
- Result after Step 6: `BUILD SUCCESSFUL in 2s` (27 actionable tasks: 7 executed, 20 up-to-date).

**New tests added (key behavior, test-first where practical):**
- `app/src/test/java/com/mealplanner/domain/shopping/ShoppingListC4CompletionTest.kt` (6)
- `app/src/test/java/com/mealplanner/domain/shopping/ShoppingListServiceTest.kt` (6)
- `app/src/test/java/com/mealplanner/domain/availability/StoreAvailabilityContractsTest.kt` (5)
- `app/src/test/java/com/mealplanner/domain/export/ExportShoppingListUseCaseTest.kt` (5)

**Existing tests kept green (filtered):** `ShoppingListGenerationRulesTest`, `ApplyBatchPresenceToListTest`, `ExportFallbackTest`, and other tests under the filtered U3 packages.

**Not executed:** full project test suite / 80% line-coverage verification (Build and Test/full-suite responsibility). No live PRISMA or S-ostoslista integration executed (OQ2–OQ4 investigational; fake/port-backed only). No tests claimed beyond executed command above.

## Constraints/invariants preserved

- ADR-006/R-01–R-03: locked snapshot IDs+plan context; re-resolve at generation; explicit regenerate confirmation when edits exist; exact stale warning text preserved; no auto-regeneration.
- R-02/BR3.5: ShoppingList owns availability projection via explicit batch application; C5/A2 never mutates items; ambiguous identity remap to `UNKNOWN`/`NOT_CHECKED` with reason; scoped by list + store + stable item/query key precedence.
- FR8: PRISMA name/presence only (`PRESENT|NOT_FOUND|UNKNOWN`), never stock/guaranteed availability.
- FR9/FR10/BR3.8/BR3.9: deterministic merge/no fabrication; export uses current edited list; mandatory share/copy fallback instructions; S-ostoslista official method behind port/investigational; inward seam (domain use-case + DTO + platform/share port).
- NFR3/NFR4/NFR5: no MealPlanner account/secrets/raw GPS in U3 domain; privacy-safe typed diagnostics/reasons; existing tests green; quality targets not weakened.

## Loop-Back Repair Note (2026-11-10, Build-and-Test Loop-Back 1)

- Brownfield replay: U3 implementation/tests/artifacts already satisfied plan steps; no production behavior changes made in this pass.
- Verified U3-scoped: `./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.shopping.*" --tests "com.mealplanner.domain.availability.*" --tests "com.mealplanner.domain.export.*" --console=plain` → `BUILD SUCCESSFUL`.
- Preserved invariants: ADR-006/R-01–R-03 (locked snapshot IDs+context, re-resolve, exact stale warning, no auto-regeneration, explicit confirmation when edits exist), R-02/BR3.5 ShoppingList-owned presence projection/remap (C5 never mutates items), FR8 name/presence only (`PRESENT|NOT_FOUND|UNKNOWN`), FR10/BR3.9 mandatory share/copy fallback and inward export seam, OQ2–OQ4 investigational/fake-backed only, no MealPlanner account/secrets/raw GPS. Project-wide coverage and build-and-test cross-unit traceability/NFR evidence remain Build-and-Test concerns.

## Known limitations / follow-up

- OQ2–OQ4 remain investigational: no live PRISMA lookup, nearest-store provider, or official S-ostoslista transport implemented; ports/fakes and mandatory fallbacks are in place.
- UI/persistence wiring (U4) and full project coverage/CI verification are out of U3 scope.
- `ExportFallbackUseCase` legacy Android-share/copy boundaries remain as prior U1 baseline; U3 seam adds C6/A3 orchestration without requiring Android intent types in domain. Further U4/UI integration can adapt platform ports.
