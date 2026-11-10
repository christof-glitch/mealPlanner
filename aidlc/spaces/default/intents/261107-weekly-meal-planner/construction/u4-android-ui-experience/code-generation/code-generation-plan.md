# Code Generation Plan — U4 `u4-android-ui-experience`

## Summary

- Builds: Android MVP Compose/Material 3 UI for RM-01–RM-09 (Today/Plan/Grocery/User, recipe detail, alternatives, store selection, export/share) as thin orchestration over U1–U3 domain contracts.
- Touches: `app/src/main/java/com/mealplanner/ui/**` (navigation, state/facades/mappers, screens, components, platform boundaries), `MainActivity.kt`/`MealPlannerApp.kt` wiring as needed, `app/src/test/java/com/mealplanner/ui/**`.
- Tests: ~35–50 unit/UI-state tests (standard, fake-backed) covering key flows: slot sync/context, R-05 fallbacks, ADR-006 regenerate confirmation/stale warning/export current edited list, FR8 presence labels, FR10 share/copy fallback, permission/manual-store paths, accessibility labels.

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

## Sources and traceability anchors
- Stories/ACs: US1.1–US5.6 (`unit-of-work-story-map.md`, `stories.md`)
- Requirements: FR1–FR12, NFR1–NFR5 (`requirements.md`)
- UI spec: RM-01–RM-09, R-05, R-06 (`mockups.md`, `interaction-spec.md`, `functional-spec.md`, `frontend-components.md`)
- Contracts/invariants: C2–C6, A1–A3, ADR-005 thin UI, ADR-006/R-01–R-03 exact stale warning/no auto-regeneration/regenerate confirmation/export current edited list, R-02/BR3.5 C4 ownership, FR8 name/presence only, FR10/OQ4 mandatory fallback, BR3.9 inward export seam
- NFR/infrastructure design: `performance-design.md`, `security-design.md`, `logical-components.md`, `infrastructure-specification.md`, `cicd-pipeline.md`
- Brownfield entry points: `MainActivity.kt`, `MealPlannerApp.kt`, `ui/MealPlannerRoot.kt`, `ui/MealPlannerUiState.kt`

## Implementation steps

- [ ] **Step 1 — Project structure and UI skeleton (contract profile).** Create/organize U4 packages under `com.mealplanner.ui`: `navigation`, `state`, `facades`, `mappers`, `screens/{onboarding,today,plan,grocery,user,recipe,store}`, `components`, `platform`. Keep domain rule ownership in U1–U3 (ADR-005); UI packages contain only presentation/state orchestration and platform boundaries.
  - Story/AC anchors: cross-cutting RM-01–RM-09, ADR-005, NFR4 modularity.

- [ ] **Step 2 — Bootstrap/verify test runner and record exact unit-scoped command.** Brownfield app already uses JUnit4/kotlin.test/coroutines-test/MockK/Turbine. Verify/create `app/src/test/java/com/mealplanner/ui/` and record exact runnable command in `unit-test-instructions.md` before first key-behavior test-first cycle: `./gradlew :app:testDebugUnitTest --tests "com.mealplanner.ui.*" --console=plain`. Do not weaken coverage thresholds; if any whole-project coverage threshold fails with all selected U4 tests passing, record that outcome per test instructions (Build and Test checks project floor on full suite).
  - Anchors: testing contract runner step, MVP/Standard floor. Verified 2026-11-09: `BUILD SUCCESSFUL` (UiRunnerSmokeTest discovered). Command recorded in `unit-test-instructions.md`.

- [ ] **Step 3 — Key behavior test-first: shared plan slot state and R-06 navigation context (TDD/BDD style practical).** Write failing fake-backed tests for `PlanExperienceFacade`/state holder behavior: Today/Plan observe same slot state; unfilled slots remain empty; slot actions carry `slotId/day/mealType/origin`; selection/replace updates shared state and no screen auto-fills/hides empty slots. Then implement minimal state contracts/facade and make tests pass; refactor while green.
  - Story/AC anchors: US1.1 AC1.1.1–AC1.1.4, US1.2 AC1.2.1–AC1.2.3, R-06 RM-02/RM-03/RM-05. Green 2026-11-09 (5/5 PlanExperienceFacadeTest).

- [ ] **Step 4 — Key behavior test-first: grocery ADR-006/R-03 invariants and export current edited list.** Write failing tests for `GroceryExperienceFacade`/state logic using fake C4/C6: generate only when domain reports ready/explicit; regenerate with `hasUserEdits` requires explicit confirmation result path (no silent replacement); when recipes changed after generation keep current list and expose exact stale warning `Adr006LockRules.STALE_WARNING_TEXT` / `"Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."`; export uses current edited list payload; official S-ostoslista attempt maps to fallback states with instructions. Implement minimal facade/state/dialog contracts and make tests pass; refactor.
  - Story/AC anchors: US3.1 AC3.1.1–AC3.1.4, US3.2 AC3.2.1–AC3.2.3, US4.1 AC4.1.1–AC4.1.3, US4.2 AC4.2.1–AC4.2.3, ADR-006/R-01–R-03, FR10/OQ4, BR3.8/BR3.9.
  - NFR design carry-forward: make checklist edit/persistence boundary explicit in implementation (C4/domain save vs local UI edit state path) and treat `functional-spec.md` RM-06/C3/C4 as exact stale-warning/regenerate confirmation source. Green 2026-11-09 (5/5 GroceryExperienceFacadeTest). Local editable aggregate is current edited list for display/export.

- [ ] **Step 5 — Key behavior test-first: store presence/privacy labels and fallback mapping.** Write failing tests for `StoreAvailabilityFacade`/`DiagnosticsUiMapper`/presence rows: render only `PRESENT|NOT_FOUND|UNKNOWN` + reason, never stock/guaranteed availability; manual store selection always enabled; permission denied/not-asked maps to manual flow; diagnostics/messages contain no raw GPS/secrets/PII. Implement minimal mapping and pass.
  - Story/AC anchors: US3.3 AC3.3.1–AC3.3.4, US5.1 AC5.1.1–AC5.1.3, US5.2 AC5.2.1–AC5.2.3, US5.3 AC5.3.1–AC5.3.3, FR8, NFR3.2–NFR3.5. Green 2026-11-09 (5/5 StoreAvailabilityAndDiagnosticsTest).

- [ ] **Step 6 — Implement navigation shell and shared UI primitives (practical cadence).** Add Compose Navigation routes for bottom nav (`Today`,`Plan`,`Grocery`,`User`), pushed `RecipeDetail`, full-screen `ReplaceAlternatives` and `SelectPrismaStore`, and `RegenerateEditedListConfirmationDialog`. Implement `MealPlannerScaffold`, `StatusBanner`, `ErrorRetryPanel`, `MealSlotCard`, `NutritionBalanceCueCard`, `SeasonContextIndicator`, `RecipeMetaBadges`, `StorePresenceStatusRow` with accessibility labels/content descriptions and non-color-only status. Add stable list keys/focus helpers where practical.
  - Story/AC anchors: RM-01–RM-09 navigation, R-06 focus/back-stack, WCAG 2.2 AA baseline, NFR1.2. Done 2026-11-09: `MealPlannerScaffold` NavHost + bottom nav, `SlotContextNav` stable R-06 slot-context encoding, route arg wiring for RecipeDetail/Replace/SelectPrismaStore; primitives completed earlier; `:app:testDebugUnitTest --tests "com.mealplanner.ui.*"` BUILD SUCCESSFUL.

- [ ] **Step 7 — Implement screens RM-01/RM-02/RM-03/RM-09 (orchestrating domain facades).** Build onboarding/permissions, Today, Plan, User screens. Render R-05 first-launch/no-local-data+remote-unavailable next actions (`Retry`, `Use bundled fallback catalog` when available, `Continue with manual planning` where feasible). Keep Today/Plan synchronized and unfilled slots empty. Show lock control/status and links to Grocery without auto-generation. Season override/default/reset reflected in context indicator.
  - Story/AC anchors: US1.1, US1.2, US2.2–US2.5, US3.1, US5.1, US5.4, US5.5, US5.6, RM-01/RM-02/RM-03/RM-09, R-05/R-06. Done 2026-11-09: Onboarding/Today/Plan/User screens render R-05/fallback/loading/error/status, shared slot cards (stable keys), lock control with no auto-generation + Grocery link, season context/override UI; UI tests green.

- [ ] **Step 8 — Implement RM-04 recipe detail and RM-05 alternatives full-screen flow.** Use slot context; preserve R-06 back-stack/focus behavior (RM-05→RM-04 back returns to RM-05 with slot context). Render domain-provided eligibility/explanations only for FR3 weekday/time/missing-ambiguous rules; no UI re-evaluation. Show nutrition/season/source/missing-data labels without fabrication.
  - Story/AC anchors: US1.2, US2.1–US2.4, US2.5, RM-04/RM-05, ADR-005. Done 2026-11-09: RecipeDetail/ReplaceAlternatives full-screen UIs with slot context labels/actions, domain-provided label/explanation rendering (no rule re-evaluation), R-06 nav preserved by scaffold; UI tests green.

- [ ] **Step 9 — Implement RM-06 grocery, RM-07 store selection, RM-08 export/share UI.** Grocery: readiness/lock card, explicit generate, regenerate confirmation when edits/check-offs exist, stale status including exact ADR-006 warning, edit/check/add/remove, availability projection display, PRISMA card, export actions. Checklist edit/persistence boundary: local editable UI state represents current edited list for display/export; persistence/save path uses domain/facade contract when available and is explicit in code (no hidden rule duplication). Store selection: manual always available, suggestion best-effort. Export: official S-ostoslista attempt state/fallback plus Android share sheet/copy text with instructions always available; export current edited list.
  - Story/AC anchors: US3.1–US3.3, US4.1–US4.2, US5.1–US5.3, RM-06–RM-08, FR8, FR10, ADR-006, BR3.5/BR3.9. Done 2026-11-09: Grocery/Store/Export RM-08 UI implemented with ADR-006 exact stale warning path, regenerate confirmation, FR8 presence labels, manual-always store selection, current-edited-list export intents; UI tests green.
  - NFR design carry-forward: if diagnostics hooks are added, route through privacy-safe mapping and add/perform verification (no raw GPS/secrets/endpoints/PII).

- [ ] **Step 10 — Platform boundaries and brownfield wiring.** Add minimal `PermissionStatusProvider` and share/copy platform boundary implementations (Android runtime permission intents/settings, share sheet/clipboard) confined to platform/ui boundary; domain receives typed outcomes only. Update `MealPlannerRoot`/`MealPlannerUiState`/`MainActivity`/`MealPlannerApp` wiring to launch the new navigation shell while preserving existing app entry behavior. Do not hardcode secrets/endpoints.
  - Story/AC anchors: US5.1, US4.1, US5.6, BR3.9, NFR3.1–NFR3.4. Done 2026-11-09: `PermissionStatusProvider`/`DefaultPermissionStatusProvider`, `ShareCopyPlatformBoundary` (ShareExportPort, fallback payload, no domain Android types), `MealPlannerRoot` now launches `MealPlannerScaffold`; MainActivity/app entry preserved; no secrets/endpoints; UI tests green.

- [ ] **Step 11 — Supporting/practical tests and regression coverage.** Add remaining standard-scope tests for supporting UI behavior not covered by key test-first steps: screen/state rendering labels (`Nutrition information unavailable`, `Season fit unknown`, `Unknown` reasons), User settings validation/reset, export cancel preserves list, manual checklist fallback, accessibility/content-description smoke where practical in unit/fake tests. Keep existing app tests green.
  - Story/AC anchors: US2.3, US2.4, US3.3, US4.1, US5.3, US5.5, NFR2/NFR3.6. Done 2026-11-09: `SupportingUiBehaviorTest` added; `:app:testDebugUnitTest --tests "com.mealplanner.ui.*"` BUILD SUCCESSFUL.

- [ ] **Step 12 — Environment/build configuration checks.** Confirm Compose/Material3/navigation/lifecycle dependencies are sufficient (prefer existing versions; add only if required for implemented U4 code). Run unit-scoped U4 tests and fix regressions; record full-suite/coverage-floor handling per test instructions.
  - Anchors: brownfield `app/build.gradle.kts`, testing contract obligations. Done 2026-11-09: confirmed Compose/Material3/navigation/lifecycle deps sufficient; added `androidx.navigation.compose` and `material-icons-core` for U4 shell. `./gradlew :app:testDebugUnitTest --tests "com.mealplanner.ui.*" --console=plain` -> BUILD SUCCESSFUL (unit-scoped U4 tests). Full-project 80% coverage floor/CI is checked by Build and Test on the assembled full suite; no thresholds weakened.

- [ ] **Step 13 — Documentation and traceability artifacts.** Create `code-summary.md`, `traceability.json` (unit AC/FR/NFR/RM/R/ADR mappings with `OK` targets to existing workspace files), and `source-manifest.json` listing every application-source path created/modified/deleted by U4 (including test files and any shell/scaffolding outputs). Note tests written but not executed and reasons.
  - Anchors: Step 5 stage requirements, source-manifest schema.

## Guardrails (must preserve)
- ADR-005: UI orchestrates; no duplication of R-03/FR3/FR9 business rules.
- R-06: shared Today/Plan slot state; unfilled slots remain empty; slot context/back-stack/focus preserved.
- R-05: RM-01/RM-02/RM-03 first-launch/no-local-data+remote-unavailable with Retry/bundled fallback/manual planning where feasible; no dead-end core flows.
- ADR-006/R-01–R-03: explicit generate/regenerate, confirmation when `hasUserEdits`, exact stale warning text, no auto-regeneration, export current edited list, locked snapshot IDs+context flow respected.
- FR8: PRISMA name/presence only (`PRESENT|NOT_FOUND|UNKNOWN`), never stock/guaranteed availability.
- FR10/OQ4: official S-ostoslista investigational; mandatory share sheet/copy text fallback with instructions.
- NFR3: no MealPlanner account, no secrets/raw GPS/PII in UI/diagnostics; purpose-limited location with manual alternative.
- Do not weaken measurable quality targets/coverage thresholds to make a step pass.
