# Unit Test Instructions — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`, C4/C5/C6)  
**Stage:** Code Generation (construction, per-unit)  
**Date:** 2026-11-09

## Test framework and configuration

- Existing Android/Kotlin project uses Gradle with `:app` module and JUnit/Kotlin test dependencies (brownfield; no new test framework/bootstrap changes planned unless required by runner readiness).
- Test style: unit tests with fakes/stubs for C2 recipe resolution, A2 PRISMA presence/store adapters, A3/S-ostoslista official-attempt port, and platform share/copy ports. No live network, no live PRISMA/S-ostoslista credentials, no raw GPS/PII in fixtures.
- Deterministic fixtures for locked snapshots, ingredient candidates, merge cases, edited lists, presence batches, and export payloads.

## Exact unit-scoped run command (U3 only)

Run from `/Users/christfm/opencode/meal-planner`:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mealplanner.domain.shopping.*" --tests "com.mealplanner.domain.availability.*" --tests "com.mealplanner.domain.export.*" --console=plain
```

This command is scoped to U3 domain packages (`shopping`, `availability`, `export`) and is runnable before the first test-first cycle once the test runner/configuration is verified (Step 2). If a new U3 test class is placed outside these packages, add an exact `--tests` filter for that class/package and keep the command unit-scoped (never a bare `./gradlew test`/`npm test` equivalent).

**Prerequisites:** JDK/Android SDK/Gradle wrapper as required by the project; no external services. If toolchain components are unavailable, record the fact and the exact command; do not claim tests executed.

## Expected scope/coverage targets

- **Strategy:** Standard — 5–8 tests per component area where practical, plus integration-style unit tests at key boundaries (fake C2/A2/A3/share ports). Cover BR3.1–BR3.8 critical paths, FR7/FR8/FR10 invariants, adapter fallback/denied/unavailable/not-supported/failed-mapped outcomes (OQ2–OQ4), and privacy-safe/no-fabrication behavior.
- **Key behaviors (test-first for key behavior):**
  - BR3.1/ADR-006: generate/regenerate only from locked snapshot IDs+context; re-resolve ingredients via port; invalid snapshot => typed result; missing/changed source => partial/notes, no fabrication.
  - BR3.2: deterministic merge by normalized name + compatible unit/amount; incompatible/uncertain => separate with reason.
  - BR3.3: edits/check/add/remove set `hasUserEdits`; regenerate with edits and no confirmation => `CONFIRMATION_REQUIRED` and list preserved; export uses current edited state.
  - BR3.4: exact stale warning `Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.`; no auto-regeneration.
  - BR3.5/R-02: explicit ShoppingList-owned `applyPresenceCheckBatch`; match scoped by list+store + stable item/query key precedence; ambiguous edit/merge/split/regenerate/manual-add => `UNKNOWN`/`NOT_CHECKED` with reason; C5 never mutates items.
  - BR3.6/BR3.7/FR8: manual store always available; presence `PRESENT|NOT_FOUND|UNKNOWN` only; never stock/guaranteed availability; typed unknown/partial/failure reasons.
  - BR3.8/BR3.9/FR10/OQ4: `ShoppingListExportDto` from current edited aggregate; mandatory share/copy fallback with instructions; S-ostoslista official attempt behind port with typed `NOT_SUPPORTED|UNAVAILABLE|FAILED_MAPPED`/success mapping and explicit `investigationOutcome` alignment; inward seam (no Android intent types in domain).
- **Project 80% line-coverage floor (MVP scope floor):** Build and Test checks this on the full test suite of the assembled project. A Unit-scoped run does not judge the project floor; if a coverage-threshold exit occurs with every selected test passing, record that fact (not a failed test) and do not weaken thresholds. Put any run whose threshold can fail in its own command so it cannot stop a later step.

## Mocking/stubbing guidance

- **C2 re-resolution:** fake recipe/ingredient resolver returning found/missing/changed results by locked recipe ID; assert no fabrication.
- **A2 PRISMA/store:** fake nearest-suggestion and batch presence adapters returning `PRESENT|NOT_FOUND|UNKNOWN`, `OK|PARTIAL|UNAVAILABLE|DENIED|FAILED_MAPPED` outcomes with reasons; assert manual fallback remains available and no stock/guarantee fields.
- **A3/S-ostoslista + platform share:** fake official-attempt port and `ShareExportPort`/clipboard/share boundaries; assert fallback always available and current edited DTO used.
- **Time/random IDs:** inject or fix clock/ID generator where needed for deterministic assertions.

## Test data management

- Use small in-memory fixtures: 7-day locked snapshot refs, a few recipe ingredient lines (compatible/incompatible units, missing/changed data), edited list states, store refs, presence batches, and export requests.
- Keep fixtures local to tests; no external files/services; no secrets/PII/GPS coordinates.
- Reuse/extend existing tests in `com.mealplanner.domain.shopping.*` where compatible; add new U3 tests alongside them.
