# Code Generation Plan — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, test strategy `Standard`)  
**Unit:** U2 — Recipe + planning completion (`library`, C2/C3 + A1)  
**Stage:** Code Generation (construction, per-unit)  
**Date:** 2026-11-09

## Summary

- Builds: U2 C2/C3 completion + A1 recipe-source adapter boundary (normalization/eligibility/season/nutrition/alternatives, slot planning state, gated live + mandatory curated fallback) on existing Android/Kotlin codebase.
- Touches: `app/src/main/java/com/mealplanner/domain/model/*`, `domain/recipe/*` (new), `domain/planning/*`, `adapters/*`, `diagnostics/*` (hook as needed), and scoped tests under `app/src/test/java/com/mealplanner/...` (plus module files only if layout requires).
- Tests: ~40–60 unit/fake-boundary tests across A1 adapter/gate/fallback, time normalization, season fit, nutrition/cue, alternatives, and C3 slot/stale semantics (Standard, 5–8 per key area/component), unit-scoped command in `unit-test-instructions.md`.

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

## Inputs and constraints
- **Consumes:** `functional-design/{functional-spec,rules,entities}.md`, `contract-summary.md`, `nfr-design/security-design.md`, `units-generation/unit-of-work.md`, `requirements-analysis/requirements.md`.
- **Invariants:** FQ1=A live Sydänmerkki default-off with explicit approved OQ1 gate; mandatory curated/imported fallback; no scraping/terms assumptions; no fabrication (FR4/FR5/FR6/FR11); PRISMA name/presence only irrelevant to U2 but boundary discipline applies; ADR-006 exact stale warning `"Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."`, no auto-regeneration, explicit regenerate confirmation owned downstream, snapshot IDs+context only; R-06 single Today/Plan source of truth (`WeeklyPlanState`/C3); U2 never reads `WeeklyPlanState` for C2 nutrition cue aggregation except via `WeeklyNutritionBalanceCueRequest` inputs; NFR4.3 no weakened quality targets.
- **Brownfield in workspace:** U1 app/tests exist; modify in-place, no duplicate `*_modified` files. Leave unrelated project files as-is unless required.
- **Application code:** workspace root (`app/`), never under `aidlc/`. Tests in `app/src/test/...`.

## Story/requirement traceability (plan-level)
- US1.1/US1.2, FR1/FR2/FR9, BR2.7/BR2.8, R-06/ADR-006 → Steps 2, 11, 12.
- US2.1/US2.5, FR4/FR11/FR12, BR2.1/BR2.2/BR2.9, NFR2/NFR3 → Steps 2, 4, 5, 6.
- US2.2, FR3, BR2.3, OQ7 → Steps 2, 7.
- US2.3, FR5, BR2.4, OQ5 → Steps 2, 8.
- US2.4, FR6, BR2.5, OQ6 → Steps 2, 9.
- US1.2 alternatives, BR2.6 → Steps 2, 10.
- NFR1–NFR5.x, NFR4.1–4.3 → all relevant steps + `unit-test-instructions.md`.

## Steps

### Step 1 — Project structure and production configuration skeleton (U2 delta)
- [x] Confirm U1 Android/Kotlin/JUnit structure is runnable (no changes to unrelated files). Add only minimal U2 packages/classes as needed: `com.mealplanner.domain.recipe` (C2 evaluators/services), extend `domain.model`/`adapters`/`diagnostics` in-place. No secrets/endpoints/keys; A1 config injection points only.
- [x] Record any required build/config touchpoints (none expected beyond existing app module); do not relax coverage/quality thresholds.

### Step 2 — Bootstrap minimal test runner/config and exact unit-scoped command (runner ready before first executable test)
- [x] Verify existing test runner executes unit-scoped U2 command recorded in `unit-test-instructions.md`: `./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.recipe.*" --tests "com.mealplanner.domain.planning.SlotSelection*" --tests "com.mealplanner.domain.planning.C3*" --tests "com.mealplanner.adapters.A1*" --tests "com.mealplanner.adapters.RecipeSourceAdapter*" --console=plain` (and future module-scoped tasks if layout changes).
- [x] Ensure fakes/stub patterns are usable (no live network). Command must be runnable before first test-first step.

### Step 3 — Custom ordering (test-first for key behavior, practical for supporting/lower-risk)
- [x] Apply per key behavior area below: write tests first (TDD/BDD-style where practical), implement minimal code to satisfy, refactor while green. Supporting/lower-risk glue uses practical cadence but remains runnable in CI. Preserve exact custom ordering; do not convert to blanket layer-local TDD for all supporting code.

### Step 4 — Key behavior (test-first): U2 domain contracts/types and input validation (BR2.2, BR2.9, NFR3.3, NFR3.6)
- [x] **Tests first:** add/extend model tests for U2 entities (`RecipeSourceAdapterRequest/Result`, `RecipeDetail`, `RecipeTimeNormalization`, `SeasonFitEvaluation`, `NutritionProfile`, `WeeklyNutritionBalanceCueRequest`/`WeeklyNutritionBalanceCue`, `AlternativeRecipesRequest/Result`, `SlotSelectionChangeRequest/Result`, `RecipeFilters`) and boundary validation (invalid `PlanSlotRef`/slot compatibility, malformed IDs/filters/requests → typed non-fabricated failures).
- [x] **Implement:** add U2 data types/enums in `domain.model` (additive to existing types; use canonical `PlanSlotRef`; include A1 statuses `ok|fallback_used|unavailable|restricted|timeout|parse_error_mapped|not_supported`, investigation outcomes, typed reason/status fields). Add input validator/mapper helpers.
- [x] **Refactor/green:** keep existing U1 tests passing.

### Step 5 — Key behavior (test-first): A1 adapter port, default-off gate, resilience chain, mandatory curated fallback (BR2.1, BR2.2, NFR2.1, NFR2.4, NFR3.1–NFR3.3, NFR5.2, FQ1=A)
- [x] **Tests first (fake adapters, no live network):** default-off gate (`allowLiveSource=false` and OQ1 not approved) never invokes live client; curated fallback returned with typed reason; live enabled+approved path maps success; timeout/restriction/parse_error_mapped/unavailable/not_supported map to typed statuses and fallback when available; partial safe parse may contribute with explicit status, no fabrication; retries limited/idempotent only when allowed; no raw exceptions across boundary.
- [x] **Implement:** complete `RecipeSourceBoundary`/A1 adapter abstraction (port) and add `A1LiveAccessGate`/config injection, resilience chain (gate → bounded timeout → 0–2 idempotent retries allowed only → defensive parse/map → curated fallback resolver), curated/imported fallback source implementation, and diagnostics hook interface/no-op (NFR5.5). Keep `FallbackRecipeSourceBoundary` behavior compatible or migrate in-place.
- [x] **Refactor/green:** ensure U1 adapter stub tests remain valid or are updated to new compatible behavior.

### Step 6 — Supporting behavior (practical cadence): C2 catalog/query facade and safe normalization wiring (BR2.2, BR2.9)
- [x] **Tests where practical / implement:** C2 service/use-case methods for catalog/summary/detail/ingredients queries using A1 boundary results; expose `RecipeSourceStatus`/fallback/provenance; normalize missing fields to explicit unavailable/partial states; never fabricate.
- [x] **Green:** verify no regression in U1 recipe model usage.

### Step 7 — Key behavior (test-first): weekday time normalization and eligibility (BR2.3, FR3, OQ7)
- [x] **Tests first:** `KNOWN` total <=30 eligible for Mon–Fri Breakfast/Dinner; >30 => `WEEKDAY_TIME_OVER_30`; missing/ambiguous/range/conflicting/unit-confusing/unparseable => `MISSING|AMBIGUOUS`, not guessed, `WEEKDAY_TIME_AMBIGUOUS` exclusion; weekend unaffected by weekday 30-min rule.
- [x] **Implement:** deterministic `RecipeTimeNormalization` evaluator/service (pure domain logic) producing typed `ExclusionReason` data compatible with U1/U2 contracts.
- [x] **Refactor/green.**

### Step 8 — Key behavior (test-first): season fit evaluation and override precedence contracts (BR2.4, FR5, OQ5)
- [x] **Tests first:** tag/curated mapping => `IN_SEASON|OUT_OF_SEASON|SHOULDER` when evidence sufficient; insufficient evidence => `SEASON_FIT_UNKNOWN` with explanation key (no false match); override precedence `DEFAULT|OVERRIDE|RESET` reflected in effective resolution inputs.
- [x] **Implement:** `SeasonFitEvaluation` service using `SeasonContext`/`EffectiveSeasonResolution` inputs; bounded mapping lookup, no persistent cache.
- [x] **Refactor/green.**

### Step 9 — Key behavior (test-first): nutrition availability labels and weekly non-medical cue (BR2.5, FR6, OQ6)
- [x] **Tests first:** missing profile => `Nutrition information unavailable`/`UNAVAILABLE`; partial/available handled explicitly; `WeeklyNutritionBalanceCue` computed only from `WeeklyNutritionBalanceCueRequest` (selected IDs + profiles map), excludes missing data, reports exclusion count, never reads `WeeklyPlanState`, no medical targets/dosing/diagnoses.
- [x] **Implement:** nutrition profile/cue calculators in `domain.recipe` or dedicated nutrition package; pure aggregation with typed statuses.
- [x] **Refactor/green.**

### Step 10 — Key behavior (test-first): alternatives respecting eligibility/season/fallback (BR2.6)
- [x] **Tests first:** alternatives filtered by slot/weekday eligibility, season context, and source/fallback status; empty result returns typed `EMPTY/FALLBACK/UNAVAILABLE` with reason and excluded candidates where useful; no fabricated alternatives.
- [x] **Implement:** alternatives selector/service using C2 catalog, time normalization, season fit, and A1 source status.
- [x] **Refactor/green.**

### Step 11 — Key behavior (test-first): C3 slot selection changes, R-06, ADR-006 stale signaling (BR2.7, BR2.8, FR1/FR2/FR9)
- [x] **Tests first:** `SET_SELECTION|REPLACE_WITH_ALTERNATIVE|CLEAR_SELECTION` affect only target `PlanSlotRef`; invalid refs/ops rejected with typed result; `WeeklyPlanState` remains single source model; when active lock/list context exists, change marks stale per BR1.9 with exact warning `"Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."`; **no auto-regeneration**; no ingredient freeze in U2.
- [x] **Implement:** C3 planning state manager/use-case in `domain.planning` (additive to `PlanLockService`/`Adr006LockRules`/`WeekdayEligibilityEvaluator`), preserving existing lock/stale behavior.
- [x] **Refactor/green:** keep all existing planning tests passing.

### Step 12 — Supporting behavior (practical cadence): observability/diagnostics hook and safe logging fields (NFR5.4–NFR5.6, NFR3.1)
- [x] **Tests where practical:** hook receives safe structured fields only (op/status/reason/count/timing bucket); default no-op; fake hook captures events; no PII/secrets/payload assertions required beyond absence from API.
- [x] **Implement:** `U2Diagnostics`/equivalent interface + no-op and wiring points in A1/C2 boundaries. (Uses existing `DiagnosticsSink`/`NoOpDiagnosticsSink`/`PrivacySafeDiagnostics`; A1 boundary wired.)

### Step 13 — Environment/build configuration (minimal, only if needed)
- [x] Add/update configuration only if required for test tasks/module wiring; no new secrets; no relaxed quality thresholds. Record exact commands in `unit-test-instructions.md` if changed. (No changes required; existing app module/JUnit config sufficient.)

### Step 14 — Documentation and traceability (Step 5 files after generation/review prep)
- [x] After implementation and tests pass (unit-scoped), create `code-summary.md`, `traceability.json` (via `aidlc engine sensor-traceability --stage code-generation --output-path ... --write-upstream` then map AC/NFR/BR IDs to existing workspace-relative implementation/test files), and `source-manifest.json` listing every application-source path U2 created/modified/deleted (including generator/shell-written files). Prepare for adversarial review of finished work.
- [x] Run unit-scoped verification: `./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.recipe.*" --tests "com.mealplanner.domain.planning.SlotSelection*" --tests "com.mealplanner.domain.planning.C3*" --tests "com.mealplanner.adapters.A1*" --tests "com.mealplanner.adapters.RecipeSourceAdapter*" --console=plain` (record result; whole-project coverage-floor failures on scoped run are handled per `unit-test-instructions.md`).

## Notes
- `MealSlotRef` not introduced; use canonical `PlanSlotRef`.
- A1 live execution remains disabled by default pending OQ1 approved, auditable path (FQ1=A).
- U4 owns UI/presentation, permission prompts, and orchestration wiring; U3 owns C4 apply-batch/stale regeneration flows. U2 provides typed contracts and domain behavior only.