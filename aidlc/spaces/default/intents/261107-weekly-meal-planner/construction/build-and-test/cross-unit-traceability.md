# Cross-Unit Traceability — Build and Test

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Stage:** Build and Test (Construction 3.6)  
**Date:** 2026-11-09  
**Source:** `aidlc engine sensor-traceability --stage build-and-test` (`/tmp/mealplanner-trace3.json`, `pass:false`) + manual verification of unit code-generation summaries/traceability artifacts and executed tests.

## Sensor result (verbatim summary)
- **pass:** `false`
- **uncovered:** `NFR1`, `NFR2`, `NFR3`, `NFR4`, `NFR5`
- **reason (condensed):** U2 code-generation traceability reports NFR1–NFR5 target files as “target file does not exist” for paths such as `app/src/main/java/com/mealplanner/domain/recipe/RecipeCatalogService.kt`, `.../AlternativeRecipesSelector.kt`, `.../RecipeTimeNormalizationEvaluator.kt`, `app/src/test/java/com/mealplanner/domain/recipe/RecipeTimeNormalizationEvaluatorTest.kt`, `app/src/main/java/com/mealplanner/adapters/RecipeSourceBoundary.kt`, `.../A1RecipeSourceAdapter.kt`, and related U2 C2/C3/A1/NFR targets. **Manual verification (2026-11-09):** these files exist under `app/src/...` and were normalized in U2 `traceability.json`; sensor path-resolution behavior remains unresolved and is surfaced at the gate.

## FR/AC coverage (manual cross-unit view)
The sensor coverage output includes FR1–FR12 and AC groups across units (U1 foundation + U2–U4 completion). No uncovered FR/AC IDs were reported by the sensor in the `uncovered` list (only NFR1–NFR5). Manual mapping by unit responsibilities:

| Area | Units | Evidence |
|---|---|---|
| FR1 week/slot structure | U1, U2, U4 | `Adr006LockRules`/planning state, C3 slot selection, UI navigation/slot context; `:app:testDebugUnitTest` success. |
| FR2 weekday ≤30 total prep+cook, missing/ambiguous excluded | U1, U2, U4 | `RecipeTimeNormalizationEvaluator` + tests, C3/UI mapping, exclusion reasons; fake-backed tests. |
| FR3 generate/lock/select alternatives/manual, unfilled allowed | U1, U2, U4 | C3/lock/snapshot rules, alternative selector, plan UI facades; ADR-006 exact stale warning preserved. |
| FR4 Sydänmerkki attempted, FQ1=A gated live, curated fallback mandatory | U1, U2 | A1 adapter boundary/gate/fallback, default-off live path, no scraping/terms assumptions; tests use fakes (no live call). |
| FR5 season fit metadata + date/location mapping, unknown allowed, override | U1, U2, U4 | `SeasonFitEvaluator`/catalog/mappers, UI indicators/user override plumbing per unit summaries. |
| FR6 nutrition cues/cue levels/unavailable, minimal non-medical balance cue | U1, U2, U4 | `NutritionCueCalculator`, UI cue components/mappers; no diagnosis/treatment claims. |
| FR7 PRISMA nearest best effort, manual store, permission optional | U1, U3, U4 | Store availability/store selection contracts, manual fallback, location optional; no live PRISMA. |
| FR8 name/presence only `PRESENT|NOT_FOUND|UNKNOWN`, no stock fabrication | U1, U3, U4 | R-02/BR3.5 presence projection, C5 batch contracts, ShoppingList owns apply; tests verify no C5 mutation of items. |
| FR9 shopping list generate/regenerate/merge/edits, ADR-006/R-03, exact stale warning | U1, U3, U4 | `ShoppingListRules`/service, explicit generate/regenerate, confirmation when edits exist, export current edited list, no auto-regeneration, exact stale text in `Adr006LockRules`/UI. |
| FR10 export via OQ4-investigational official path + mandatory share/copy fallback | U1, U3, U4 | C6/A3 export use case/payload/ports, S-ostoslista investigational, share/copy fallback; no live S-ostoslista. |
| FR11 reliability/fallback states/next actions/on-device local where possible, R-05/R-06 | U1–U4 | Typed fallback/unknown states, shared slot context, explicit next actions in UI primitives/facades; fake-backed tests. |
| FR12 privacy/minimal data/no MealPlanner account/no hardcoded secrets | U1–U4 | No secrets/endpoints added; location optional/manual; diagnostics evidence bounded per unit summaries. |

## NFR coverage (unresolved)
| ID | Sensor | Status | Notes |
|---|---|---|---|
| NFR1 Performance/scalability (MVP on-device/bounded) | uncovered | **Unresolved at sensor** | Evidence in U1–U4 NFR/perf artifacts and deterministic domain tests; U2 target-resolution messages affect reporting. No load testing executed. |
| NFR2 Reliability/fallbacks | uncovered | **Unresolved at sensor** | Typed fallbacks and fake-backed tests cover A1/A2/A3/C2–C6 boundaries; sensor blocked by U2 path-resolution reporting. |
| NFR3 Security/privacy/GDPR-minimal | uncovered | **Unresolved at sensor** | No secrets/MealPlanner account/raw GPS; gated live paths default-off/auditable where designed. Evidence split across NFR/security artifacts and code/tests. |
| NFR4 Maintainability/testability | uncovered | **Unresolved at sensor** | Modular packages, fake ports, Standard test coverage attempted (58.55% line measured, below MVP floor). |
| NFR5 Usability/transparency/fallback clarity | uncovered | **Unresolved at sensor** | Exact stale warning, `Nutrition information unavailable`, `Season fit unknown`, FR8 labels, R-05 next actions represented in domain/UI artifacts and tests. |

## Required action
1. **Coverage (blocking):** add targeted tests across U1–U4 to meet/exceed MVP 80% line-coverage floor on full assembled suite without weakening thresholds (loop-back to code-generation).
2. **Traceability (blocking for sensor pass):** during code-generation replay, align per-unit NFR traceability targets/evidence format to what `aidlc engine sensor-traceability` resolves (especially U2 NFR paths) and ensure cross-unit NFR1–NFR5 evidence is discoverable; keep claims factual.
3. Re-run `:app:testDebugUnitTest`, JaCoCo report, and `aidlc engine sensor-traceability --stage build-and-test` after fixes; update `test-results.md`/matrix and re-present gate only when passing or per protocol.
