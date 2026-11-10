# Code Generation Plan — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Unit:** U3 — Grocery + store + export completion (`library`, C4/C5/C6)  
**Stage:** Code Generation (construction, per-unit)  
**Date:** 2026-11-09

## Summary

- Builds: U3 grocery/shopping list completion (C4 locked-snapshot generation/re-resolve, deterministic merge, edits, regeneration confirmation, exact stale semantics, ShoppingList-owned `applyPresenceCheckBatch`/remap), store/availability contracts (C5/A2 name/presence only, `PRESENT|NOT_FOUND|UNKNOWN`, manual fallback), and export completion (C6/A3 DTO/use-case/share-port seam, mandatory share/copy fallback, S-ostoslista investigational) as brownfield-safe Kotlin domain/adapter code.
- Touches: `app/src/main/java/com/mealplanner/domain/model/{CoreTypes,ShoppingModels,PresenceModels,ExportModels}.kt`, `app/src/main/java/com/mealplanner/domain/shopping/**`, new `app/src/main/java/com/mealplanner/domain/availability/**`, `app/src/main/java/com/mealplanner/domain/export/**`, `app/src/main/java/com/mealplanner/adapters/{IntegrationStubs,PresenceMappingBoundary}.kt` (and only minimal compatibility updates where required), plus U3 unit tests under `app/src/test/java/com/mealplanner/domain/{shopping,availability,export}/**`.
- Tests: Standard strategy (5–8 tests per U3 component area where practical) + MVP scope floor obligations; test-first for key BR3/NFR behaviors, custom ordering preserved; targeted fake-backed adapter fallback tests; existing tests kept green.

## Testing Contract

```json
{
  "version": 1,
  "methodology": "custom",
  "source": "team",
  "ordering": "For key behavior, write tests before implementation (TDD/BDD style where practical), then implement to satisfy those tests; for supporting or lower-risk behavior, use the most practical test cadence and keep tests runnable in CI.",
  "scope": "mvp",
  "test_strategy": "standard",
  "project_type": "greenfield",
  "applicable_notes": [
    {
      "layer": "org",
      "text": "We treat tests as a first-class deliverable in every Bolt. The specific\nmethodology (TDD, BDD, ATDD, or classic test-after) is affirmed at\npractices-discovery and recorded in `team.md` under this heading with explicit\n`Methodology` and `Ordering` fields; Code Generation resolves those fields\nindependently from coverage, tooling, and scope notes.\n\nWhen no posture has been affirmed, our default per scope is:\n- **Methodology**: test-after\n- **Ordering**: implement each applicable testable layer, then write and run\n  that layer's tests.\n- `mvp`, `enterprise`, `feature`, `infra`, `classic` add an 80% line-coverage\n  floor and CI execution before merge.\n- `bugfix`, `security-patch` add a targeted regression for the specific\n  bug/vulnerability and require the existing suite to remain green.\n- `express` uses the Minimal strategy: requirement-driven unit tests (one per\n  requirement, with a happy-path floor per component); existing tests remain\n  green.\n- `poc`, `refactor`, `workshop` add no extra new-test floor and require the\n  existing suite to remain green.\n\nThe active `Test Strategy` still applies in every scope and determines test\nvolume/types. Scope floors are additive; they never reduce or replace the\nselected strategy.\n\nBuild and Test verifies defined coverage floors and affirmed quality targets;\nthey may not be weakened to make a step pass.\n\nAffirm a stricter posture in `team.md` if the team commits to one."
    },
    {
      "layer": "team",
      "text": "We treat tests as part of delivering MVP behavior, especially around meal-plan generation/selection, shopping-list aggregation, integration adapters/fallbacks, and privacy-sensitive data handling. [intent-statement, scope-document]\n\n- **Methodology**: custom\n- **Ordering**: For key behavior, write tests before implementation (TDD/BDD style where practical), then implement to satisfy those tests; for supporting or lower-risk behavior, use the most practical test cadence and keep tests runnable in CI.\n- **Methodology evidence**: Human affirmed Q3 for this learning/ASAP Android MVP. “Custom” reflects a practical mix (test-first for key behavior, flexible where practical) rather than a single pure cadence.\n- **Quality targets:** Keep critical tests and CI checks in place; keep existing tests green. Do not weaken quality targets to make a step pass. Coverage expectations and Android test tooling/CI details will be confirmed as the codebase and pipeline are established."
    }
  ],
  "obligations": {
    "strategy": "standard",
    "strategy_volume": [
      "Five to eight tests per component.",
      "Unit tests plus integration tests for key boundaries.",
      "Add E2E, performance, or security tests when requirements demand them."
    ],
    "scope_floor": [
      "Meet an 80% line-coverage floor.",
      "Run the selected tests in CI before merge."
    ],
    "combination_rule": "Apply every selected-strategy obligation and every scope-floor obligation; neither replaces the other, and a targeted scope regression may add the narrowest necessary test type beyond the strategy default."
  },
  "plan_profile": {
    "methodology": "custom",
    "runner_step": "Bootstrap the minimal test runner/configuration and record the exact unit-scoped command.",
    "runner_ready_before_first_test": true,
    "testable_layers": [
      "Data model / database behavior",
      "Repository / data access",
      "Business logic",
      "API / endpoint",
      "Frontend behavior"
    ],
    "steps": [
      "Project structure and production configuration skeleton.",
      "Bootstrap the minimal test runner/configuration and record the exact unit-scoped command.",
      "Custom ordering - For key behavior, write tests before implementation (TDD/BDD style where practical), then implement to satisfy those tests; for supporting or lower-risk behavior, use the most practical test cadence and keep tests runnable in CI.",
      "Implementation and tests - preserve that exact ordering; do not convert it to layer-local TDD.",
      "Environment/build configuration.",
      "Documentation and traceability."
    ]
  },
  "input_sha256": "sha256:59587920ee05541bac9d545befd046fa7ac9373c9a92b738b64c5406b0f6afde",
  "contract_sha256": "sha256:08f951c92a56228b04800253ce17226a44c030963a64e478b9006a6d4f7c3c01"
}
```

## Invariants and carry-forward constraints

- ADR-006/R-01–R-03: locked snapshot uses IDs+plan context only; re-resolve ingredients at generation; explicit regenerate confirmation when edits exist; exact stale warning text: `"Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."`; no auto-regeneration.
- R-02/BR3.5: ShoppingList owns availability projection and explicit `applyPresenceCheckBatch`; ambiguous edit/merge/split/regenerate/manual-add identity/query changes remap to `UNKNOWN`/`NOT_CHECKED` with reason; StoreAvailability/A2 never mutates items. Canonical presence-remap key precedence is scoped by `shoppingListId` + selected `storeId/scope` + stable item/query key (prefer explicit itemRef/query key, fall back to normalized query only when unambiguous; ambiguity => `UNKNOWN`).
- FR8: PRISMA name/presence only (`PRESENT|NOT_FOUND|UNKNOWN`), never stock/guaranteed availability. Manual checklist/manual store always usable.
- FR9/FR10/BR3.8/BR3.9: deterministic merge/no fabrication; export uses current edited list; mandatory Android share sheet/copyable text fallback with instructions; S-ostoslista official method investigational (OQ4) behind adapter/port; inward extraction seam (export use-case interface, `ShoppingListExportDto`, platform/share adapter port).
- NFR3/NFR4/NFR5: no MealPlanner account/secrets/raw GPS coords in U3; privacy-safe typed diagnostics; keep existing tests green; do not weaken quality targets (80% line floor checked by Build and Test on full suite).
- Minor findings addressed in plan/implementation: (1) `ExportResult.investigationOutcome`/status mapping explicitly aligned to C6/A3 semantics (`ok/SUCCESS` => `USED` as applicable, fallback after official attempt => `PARTIALLY_USED`/`FALLBACK_USED` context, not feasible => `NOT_FEASIBLE_CONFIRMED`, unresolved/unknown => `UNKNOWN`) without fabricating success; (2) functional-design enum/status naming mapped to existing contract/U3 DTO types in implementation notes/tests (no orphan enum semantics).

## Brownfield approach

- Modify existing files in-place; create new files only for missing U3 components/packages (no duplicate class files).
- Preserve compatibility with existing U1/U2/U4 consumers and tests that use `ShoppingListItem`, `ShoppingListGenerationResult`, `StorePresenceResult`/`BatchPresenceResult`, `ApplyBatchPresenceToListRequest/Result`, `SharePayload/ShareResult`, and current enum values. Evolve types additively (new fields with defaults, new enum values only where compatible, adapter/wrapper types/functions) so existing tests remain green.
- Keep application code under `app/src/main/**`; tests under `app/src/test/**`.

## Implementation steps (execute sequentially)

- [ ] Step 1 — Project structure and production configuration skeleton (brownfield-safe). Confirmed existing Android/Kotlin/Gradle structure runnable for unit tests (`:app` debug unit tests execute). Missing U3 `domain/availability` main/test package created in Step 5 as needed; export test package already present. No new backend/public API, cloud infra, or secrets. (FR9, FR10, NFR1, NFR3, BR3.9)
- [ ] Step 2 — Test runner readiness and exact unit-scoped command. Ran exact command from project root before first test-first cycle: `./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.shopping.*" --tests "com.mealplanner.domain.availability.*" --tests "com.mealplanner.domain.export.*" --console=plain` — **BUILD SUCCESSFUL in 3s** (27 actionable tasks: 1 executed, 26 up-to-date). Prerequisites: project Gradle wrapper/JDK/Android SDK available in environment; no external services. Coverage-threshold behavior not weakened (project 80% floor remains Build and Test/full-suite concern). (NFR4, NFR5, contract runner step)
- [ ] Step 3 — C4 domain model evolution (key behavior, test-first where practical). Added `ShoppingListC4CompletionTest` first (6 tests), then evolved `ShoppingModels.kt`/`CoreTypes.kt`/`ShoppingListRules.kt` in-place: C4 aggregate/patch/merge/outcome DTOs, `LockSnapshot`/stale warning exact text, `evaluateRegenerationRequest`, `applyItemPatch`, BR3.5-compatible `applyPresenceCheckBatch` with list+store scope/remap-to-`UNKNOWN`/`NOT_CHECKED` reason mapping, and legacy `applyBatchPresenceToList` behavior preserved. (FR9, US3.1–US3.2, ADR-006/R-01–R-03, BR3.1–BR3.5, BR3.10, NFR2, NFR4)
- [ ] Step 4 — C4 shopping list use-case/service completion (key behavior, test-first where practical). Added `ShoppingListServiceTest` (6 tests) and `ShoppingListService` for locked-snapshot generate/regenerate (re-resolve via injected C2 port), deterministic merge/no fabrication, confirmation-required on edited rebuild, stale reflection, and `prepareCurrentExportState` from current edited aggregate. (FR9, FR10, US3.1–US3.2, ADR-006, BR3.1–BR3.4, BR3.8, NFR2, NFR5)
- [ ] Step 5 — C5/A2 presence and store contracts (key behavior, test-first where practical). Added availability package/tests (`StoreAvailabilityContractsTest`, 5 tests), evolved `PresenceModels.kt`/`CoreTypes.kt`/adapter compatibility with C5 query/result/batch/store-selection DTOs and batch mapped outcomes; implemented `StorePresenceUseCase` (PRISMA name/presence only, typed outcomes, no item mutation) and `StoreSelectionUseCase` (manual always available, nearest suggestion best-effort port, no raw GPS). (FR7, FR8, US3.3, US5.2–US5.3, BR3.5–BR3.7, BR3.10, NFR2, NFR3, NFR5)
- [ ] Step 6 — C6/A3 export completion and BR3.9 seam (key behavior, test-first where practical). Added `ExportShoppingListUseCaseTest` (5 tests), then implemented inward export seam in `ExportSeam.kt` (`ExportShoppingListUseCase`, `ShareExportPort`, `SostoslistaOfficialExportPort`, `OfficialExportAttempt`, `DefaultExportShoppingListUseCase`) using existing U3 DTOs in `ExportModels.kt`; maps official `SUCCESS=>USED`, `NOT_SUPPORTED=>NOT_FEASIBLE_CONFIRMED` fallback, `UNAVAILABLE|FAILED_MAPPED=>PARTIALLY_USED` fallback without fabricating success; mandatory share/copy fallback instructions preserved; no Android intent types in domain. Legacy `ExportFallbackUseCase`/`ExportFallbackTest` remain compatible. (FR10, US4.1–US4.2, BR3.3, BR3.8, BR3.9, BR3.10, NFR2, NFR3, OQ4)
- [ ] Step 7 — Compatibility, adapter cleanup, and supporting tests (practical cadence for lower-risk/supporting behavior). Preserved existing shopping/export behavior via additive DTO/API evolution and targeted compatibility fixes (BR3.5 ambiguous remap reason + unresolved merge not upgraded to fabricated present). Ran U3-scoped tests after Steps 5 and 6; existing filtered tests remained green. No thresholds weakened, no tests removed. (NFR4, NFR3, NFR2)
- [ ] Step 8 — Environment/build configuration. No new build plugins or coverage-threshold changes. Commands remained layout-explicit and CI-safe (fake-backed, no live PRISMA/S-ostoslista). U3-scoped unit tests executed successfully (`BUILD SUCCESSFUL`). (NFR1, NFR4)
- [ ] Step 9 — Documentation and traceability artifacts. Ran `aidlc engine sensor-traceability --stage code-generation --unit u3-grocery-store-export-completion --output-path aidlc/spaces/default/intents/261107-weekly-meal-planner/construction/u3-grocery-store-export-completion/code-generation/traceability.json --write-upstream` (single command) — output written successfully. Produced `code-summary.md` and `source-manifest.json`. U3-scoped tests executed; full-suite/coverage not executed (Build and Test responsibility). (NFR4, traceability sensor, BR3.* coverage)

## Story/BR traceability map (plan-level)

| Plan steps | Stories / FR / BR / NFR |
|---|---|
| 1–2, 8 | Project/test readiness, NFR1/NFR4/NFR5, CI-safe fake-backed scope |
| 3 | US3.1–US3.2, FR9, ADR-006/R-01–R-03, BR3.1–BR3.5, BR3.10, NFR2 |
| 4 | US3.1–US3.2, FR9/FR10, BR3.1–BR3.4, BR3.8, NFR2/NFR5 |
| 5 | US3.3, US5.2–US5.3, FR7/FR8/FR12, BR3.5–BR3.7, BR3.10, NFR2/NFR3, OQ2–OQ3 |
| 6 | US4.1–US4.2, FR10/FR11, BR3.8–BR3.10, NFR2/NFR3, OQ4, security/privacy-safe export seam |
| 7, 9 | NFR4 (existing tests green, no weakened targets), traceability, compatibility |

## Risks / deferred

- OQ2–OQ4 remain investigational: live PRISMA/S-ostoslista adapters not implemented/assumed; ports/fakes and mandatory fallbacks are implemented.
- Persistence schema/UI (U4) deferred; U3 provides domain contracts and current-state DTOs only.
- 80% line-coverage floor is project-level; Build and Test verifies on full suite (unit-scoped run may hit configured threshold despite passing selected tests — do not weaken).
- Exact Kotlin package/module naming may follow existing `com.mealplanner.*` layout; keep inward dependencies and BR3.9 seam semantics.
