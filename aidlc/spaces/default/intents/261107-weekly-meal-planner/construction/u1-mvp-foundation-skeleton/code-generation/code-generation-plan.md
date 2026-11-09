# Code Generation Plan — U1 `u1-mvp-foundation-skeleton`

## Summary

- Builds: Android MVP foundation/skeleton (single app, modularized packages) with C1–C6/A1–A4 baseline contracts, local continuity, typed fallback states, and ADR-006 lock/generate/regenerate semantics.
- Touches: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradle/wrapper/*`, `app/build.gradle.kts`, `app/src/main/**` (Compose shell, domain/use-case models, persistence, adapter boundaries, platform abstractions, diagnostics), `app/src/test/**`, `app/src/androidTest/**` (stubs only if needed), and `code-summary.md`/`traceability.json` in this stage record.
- Tests: ~38 unit tests (5–8 per key domain component) + 3 integration stubs for Room/persistence boundaries (standard strategy, mvp scope floor); key behavior test-first where practical; coverage floor 80% checked by Build and Test on full suite.

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

## Scope and constraints
- **Unit:** U1 `u1-mvp-foundation-skeleton` (service/foundation, single Android app, modularized packages). Greenfield workspace: create Android project skeleton at workspace root; do not create duplicates of existing AI-DLC files.
- **Stack (from NFR):** Kotlin + modern Android baseline (Gradle Kotlin DSL, AndroidX). **Jetpack Compose** UI target (Q1=A). **Room + DataStore** split for minimal continuity set (Q2=A). DI/testing libraries selected practically in Code Generation (kept minimal).
- **Invariants to preserve:** ADR-006/R-01–R-03 (lock snapshot stores recipe IDs/selections + plan context, never ingredient freeze; C4 re-resolves; exact stale warning `"Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."`; no auto-regeneration; regeneration with edits requires explicit confirmation). R-02 split: C5 `MapIngredientsToStorePresenceBatch` only presence mapping; C4 `ApplyBatchPresenceToList` only list projection/merge/remap/unknown-after-edit semantics; C4 never PRISMA lookup, C5 never merge/stale; U3 orchestrates. PRISMA name/presence only (`PRESENT|NOT_FOUND|UNKNOWN`), never stock/guarantee. S-ostoslista official method investigational; mandatory share sheet/copy-text fallback with instructions. No fabrication. Explicit fallback/unknown states and next actions.
- **Not in U1 runtime completion:** full Sydänmerkki live query (OQ1) — adapter boundary/fallback contracts only; full PRISMA adapter and presence orchestration implementation (U3); U3 export extraction seam deferred to U3 FD (R-03/Q7=A); full RM UI flows (U4). API/endpoint server layer is N/A (on-device Android MVP).

## Story/requirement traceability summary
| Area | Stories | Requirements / rules |
|---|---|---|
| Plan/slots/weekday eligibility | US1.1, US2.2 | FR1–FR3, BR1.1–BR1.2 |
| Recipe/source/nutrition/season baseline | US2.1, US2.3–US2.5 | FR4–FR6, BR1.3–BR1.4, BR1.14 |
| Lock/generate/regenerate/stale | US3.1, US3.2 | FR9, ADR-006/R-01–R-03, BR1.6–BR1.10 |
| Store/presence baseline | US4.2, US5.1 | FR7, FR8, BR1.5, BR1.12 |
| Export fallback | US3.3, US4.1 | FR10, R-03, BR1.11 |
| Reliability/privacy/local continuity | US5.1–US5.6 | FR11, FR12, NFR1–NFR5, BR1.13 |

## Execution order (numbered, preserves dependency order and custom testing methodology)
- [ ] **Step 1 — Project structure and production configuration skeleton** (plan profile step 1). Create Android greenfield skeleton: Gradle Kotlin DSL root (`settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, version catalog), Gradle wrapper, `:app` module with AndroidX/Compose/Room/DataStore/Kotlin test dependencies, manifest, minimal Compose entrypoint shell, and package layout modularized for future extraction (`domain`, `data/persistence`, `adapters`, `platform`, `diagnostics`, `ui`). No secrets. *(Supports all stories; NFR4.4, team code style.)*

- [ ] **Step 2 — Bootstrap minimal test runner/configuration and record exact unit-scoped command** (plan profile step 2, required before first test-first cycle). Configure JUnit (Jupiter or Android-compatible JUnit4+Kotlin test as selected), MockK/Turbine/coroutines-test as needed for domain tests, and unit-test source sets. Record the exact unit-scoped command in `unit-test-instructions.md` (`./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.*"` and exact test classes as they are created; never bare `./gradlew test`). Ensure command is runnable before first executable test-first step. *(Team testing posture, plan profile runner readiness.)*

- [ ] **Step 3 — Key behavior tests first: planning/lock/merge/export/presence rules (custom ordering)** (plan profile step 3). Write failing/behavior-defining tests for highest-risk key behavior before implementation where practical:
  - [ ] Step 3a: `WeeklyPlanState`/slot structure and weekday eligibility/exclusion reason baseline (BR1.1–BR1.2, FR1–FR3, US1.1/US2.2). Tests: required 7-day slot structure, empty slot visibility, weekday >30/ineligible with `WEEKDAY_TIME_OVER_30`, missing/ambiguous time with `WEEKDAY_TIME_AMBIGUOUS` and explanation fields.
  - [ ] Step 3b: ADR-006 lock snapshot + stale semantics (BR1.6–BR1.9, FR9, US3.1/US3.2). Tests: snapshot stores IDs/selections+context only, no ingredient freeze; generate requires explicit action + locked snapshot; plan change marks `STALE_LOCK` and exact warning text; no auto-regenerate; regenerate with existing edits returns `NEEDS_CONFIRMATION`/requires explicit confirmation.
  - [ ] Step 3c: C4 re-resolution/merge/no-fabrication baseline (BR1.8, BR1.10, FR9). Tests: re-resolve from C2 boundary, missing/changed ingredient data becomes explicit unresolved/unknown (no fabricated qty/unit), compatible merge by normalized name+compatible units/amounts, incompatible kept separate.
  - [ ] Step 3d: R-02 presence split (BR1.12, FR8, US4.2). Tests: C5 maps batch presence projections name/presence only; C4 applies projections/remap and sets unknown after ambiguous mapping/edit/merge context as designed; no PRISMA lookup in C4 implementation path; no merge/stale ownership in C5.
  - [ ] Step 3e: Export uses current edited list + mandatory fallback (BR1.11, FR10, US3.3/US4.1). Tests: payload built from current edited list; share/copy fallback result states `SHARED|COPIED|CANCELLED|FAILED|FALLBACK_INSTRUCTIONS_SHOWN`; official S-ostoslista path absent uses fallback.
  Run/observe failing tests with the exact unit-scoped command (Red-equivalent where TDD-style is used); record failures in test evidence before Green. *(Custom test-first for key behavior.)*

- [ ] **Step 4 — Implement domain contracts, use cases, and supporting persistence/adapter boundaries to satisfy key tests** (plan profile step 4). Implement minimal baseline to make Step 3 tests pass, then refactor while green:
  - [ ] Step 4a: Domain models/contracts (ENT-U1 baseline): `WeeklyPlanState`, `PlanSlotState`, `LockSnapshot`/`LockedRecipeSelection`/`PlanSnapshotContext`, `ExclusionReason`, `DataSourceStatus`, recipe/ingredient refs, shopping list items/generation results, `StorePresenceResult`/batch request-result, `SharePayload`/`ShareResult`, typed A1–A3 status/reason shapes.
  - [ ] Step 4b: C1/C3 planning/lock use cases (initialize plan, lock/finalize, mark stale, generate/regenerate with confirmations).
  - [ ] Step 4c: C2/C5 boundary interfaces and fallback-safe stubs (no live external calls required in U1); weekday eligibility helper.
  - [ ] Step 4d: C4 shopping/merge/presence application/export-fallback use cases preserving ADR-006/R-02/R-03.
  - [ ] Step 4e: C6 platform share/copy boundary abstraction and fallback payload builder (extraction seam deferred to U3).
  - [ ] Step 4f: Minimal Compose UI shell state wiring for the integrated skeleton (supporting; U4 owns full UX). Keep thin orchestration (ADR-005).
  Add/adjust tests only as needed to preserve custom ordering and behavior coverage. *(FR1–FR12 baseline, BR1.1–BR1.14, ADR-005/006, R-01–R-03.)*

- [ ] **Step 5 — Repository/data access and local persistence (Room + DataStore) with practical test cadence** (plan profile step 4/layers; supporting behavior). Implement persistence for minimal continuity set (plan state, lock snapshot context IDs/selections+plan context, shopping list+edits, selected store, season override, source/fallback/status metadata). Add unit tests for mappers/serialization logic and 3 integration test stubs/boundary tests for Room/DataStore repositories (runnable in local JVM where feasible; instrumented tests documented if environment-limited). *(FR11, FR12, NFR1.1, NFR4.2, BR1.13.)*

- [ ] **Step 6 — Adapter resilience and platform abstractions (supporting, typed boundaries)**. Implement bounded timeout/cancellation hooks and typed fallback result mapping patterns for A1–A3 boundaries; A4 abstractions for permission, share sheet/clipboard, date/location signals (no continuous tracking). Privacy-safe diagnostic event model (no coordinates/raw personal identifiers). *(FR4, FR7, FR8, FR10, FR11, NFR1.2, NFR2.1, NFR3.2, NFR4.3–NFR4.6.)*

- [ ] **Step 7 — Environment/build configuration** (plan profile step 5). Finalize Gradle config, test tasks, Compose/Room/DataStore compiler settings, lint/test configuration hooks (no CI pipeline created here unless trivially safe; CI execution before merge remains a scope floor checked when pipeline exists). Document coverage-floor handling: 80% project line-coverage floor is verified by Build and Test on full suite; do not weaken thresholds to make unit-scoped runs pass. *(Team deployment/style; mvp floor.)*

- [ ] **Step 8 — Documentation and traceability** (plan profile step 6). Update `unit-test-instructions.md` with final exact unit-scoped commands and test inventory; create `code-summary.md` (files created/modified, key decisions, tests written/executed/not executed with reasons, deviations); create `source-manifest.json` listing every application-source path created/modified in this unit (including Gradle/wrapper/generated files as applicable); run `aidlc engine sensor-traceability --stage code-generation --output-path <code-generation-record>/traceability.json --unit u1-mvp-foundation-skeleton --write-upstream` and set `OK`/`Deferred` targets to existing workspace-relative files. *(Stage Step 5; R-04 follow-up.)*

## Test plan notes (active strategy: Standard; scope: mvp)
- **Components and target counts:** Planning/lock/schedule (6–8), Recipe/eligibility/source baseline (5–6), Shopping merge/regenerate/stale (6–8), Presence application split (5–6), Export fallback (5–6), Persistence mappers/repositories unit-level (5–6) = ~32–40 unit tests; plus 3 integration stubs/boundary tests for Room/DataStore persistence.
- **Test-first key behavior:** Steps 3a–3e before corresponding implementation in Step 4.
- **Regression/fallback coverage:** integration adapter fallback states, unknown presence, no fabrication, stale warning exact text, explicit confirmation before regeneration with edits.
- **Not executed in this stage (expected):** instrumented Android/UI device tests, performance benchmarks, and CI pipeline runs if pipeline/device not available; record in `code-summary.md`.

## Deviations to avoid
- No backend service/API endpoints; no hardcoded secrets/endpoints.
- No PRISMA stock/guarantee claims; no fabricated data.
- No automatic regeneration; no silent inclusion of ineligible weekday recipes.
- No weakening of quality thresholds/coverage targets.
- No application code under `aidlc/` record tree.
