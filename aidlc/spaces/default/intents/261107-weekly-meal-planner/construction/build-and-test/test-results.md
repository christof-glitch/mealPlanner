# Test Results — Meal Planner MVP

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, test strategy `Standard`, Guard Policy `off`, Construction autonomy `autonomous`, unit-major/serial)  
**Stage:** Build and Test (Construction 3.6, `aidlc-quality-agent`)  
**Date:** 2026-11-10 (replay after U1–U4 Code Generation loop-back replay)  
**Verification command (recorded):** `./gradlew :app:testDebugUnitTest --console=plain`

## Commands executed (evidence)
| Command | Result | Notes |
|---|---|---|
| `./gradlew :app:testDebugUnitTest --tests "com.mealplanner.buildandtest.CoverageRepairU2BoundariesTest"` | `BUILD SUCCESSFUL` | 2026-11-10 (U2 replay verification). |
| U2/U3/U4 scoped unit commands (as executed in replay turns) | `BUILD SUCCESSFUL` | 2026-11-10 evidence captured in loop-back replay context. |
| `./gradlew :app:testDebugUnitTest :app:jacocoTestDebugUnitTestReport --console=plain` | `BUILD SUCCESSFUL` | 28 tasks: 1 executed, 27 up-to-date (2026-11-10). Full assembled `:app` debug JVM unit test suite + JaCoCo report. |
| `aidlc engine sensor-traceability --stage build-and-test` | `pass:false` | Exit 0, output saved `/tmp/mealplanner-trace4.json` (2026-11-10). Uncovered `NFR1,NFR2,NFR3,NFR4`; `NFR5` and `FR1–FR12`/ACs covered. |

## Build status
- **Debug app build/test compilation and unit execution:** successful (`BUILD SUCCESSFUL`, 2026-11-10).
- **Full `assembleDebug` not separately re-executed in this replay**; unit-test compile/run and JaCoCo report generation completed successfully for the current codebase.

## Test totals / scope evidence
- Full assembled suite executed via `:app:testDebugUnitTest` with JaCoCo report (`:app:jacocoTestDebugUnitTestReport`). Gradle did not emit a consolidated test-count summary in captured output; tasks completed successfully with no failures reported.
- Unit-scoped coverage spans U1–U4: planning/lock/stale (ADR-006/R-01–R-03/R-06), recipe/time/season/nutrition/alternatives + A1 gate/fallback (FQ1=A), shopping merge/regenerate/edits/presence projection (R-02, BR3.1–BR3.5), store presence name/presence only (FR8), export seam/share/copy fallback (FR10/OQ4, BR3.8/BR3.9), UI facades/mappers/navigation-state behavior with fakes.
- **Not executed:** live Sydänmerkki/PRISMA/S-ostoslista integrations (OQ1–OQ4 investigational/gated); instrumented/device Compose end-to-end tests; real performance/security penetration/load testing.

## Coverage (measurement)
- **Report:** `app/build/reports/jacoco/jacocoTestDebugUnitTestReport/jacocoTestDebugUnitTestReport.xml` and `.../html/index.html` (2026-11-10 run).
- **LINE:** `1955 / 3238 = 60.38%` covered.
- **INSTRUCTION:** `16011 / 42754 = 37.45%`.
- **BRANCH:** `802 / 3699 = 21.68%`.
- **Scope:** full `:app` debug unit-test coverage. MVP additive floor is 80% line coverage on the full assembled suite; **Not Met** (60.38%, improved from 58.55% but below floor). Quality targets were not weakened.
- **Concentration:** large uncovered areas remain in 0% Compose/UI packages (`ui/screens/*`, `ui/components`, `ui/navigation`, `ui/platform`, app/platform/UI roots), structurally difficult to cover with plain JVM `testDebugUnitTest`.

## Browser-behavior evidence
**N/A (Android JVM unit tests).** UI behavior evidence is provided by fake-backed JVM tests under `com.mealplanner.ui.*`. No browser/app automation run in this stage. Native browser/app rendering behavior not verified by real browser/device engine.

## Target Verification Matrix

| Target | Applicable | Verdict | Evidence | Notes / gaps |
|---|---:|---|---|---|
| `./gradlew :app:testDebugUnitTest --console=plain` (recorded verification command) | Yes | **Met** | `BUILD SUCCESSFUL` as part of full suite run, 2026-11-10 | Full assembled debug JVM unit suite executed successfully. |
| Build instructions / environment reproducibility | Yes | **Met (documented)** | `build-instructions.md` | Android SDK/JDK 17/Gradle wrapper; secrets not required. |
| Unit/component test execution (Standard, U1–U4) | Yes | **Met** | U1–U4 `unit-test-instructions.md`, `code-summary.md`, replay reviews/checkpoints; full suite + scoped runs `BUILD SUCCESSFUL` | Scope covered by per-unit instructions and executed tests. |
| Integration/key-boundary tests (Standard) | Yes | **Met (fake-backed)** | `integration-test-instructions.md`; boundary tests across C1–C6/A1–A4 seams | No live external integrations; OQ1–OQ4 gated/fallbacks preserved. |
| MVP 80% line-coverage floor (full assembled suite) | Yes | **Not Met** | JaCoCo XML/HTML: LINE `60.38%` (`1955/3238`), 2026-11-10 | **Root cause:** insufficient coverage across generated/module code, dominated by 0% Compose/UI packages not meaningfully coverable by plain JVM `testDebugUnitTest` without plan-affecting instrumentation/Robolectric setup. Thresholds must not be weakened. |
| Cross-unit FR/AC traceability coverage | Yes | **Met** | `/tmp/mealplanner-trace4.json` coverage for `FR1–FR12` and ACs; `cross-unit-traceability.md` | Consistent with unit artifacts, reviews, and executed tests. |
| Cross-unit NFR traceability / sensor pass | Yes | **Not Met** | `aidlc engine sensor-traceability --stage build-and-test` `pass:false`; uncovered `NFR1–NFR4` | Residual `not_built`/`not_applicable` markers in per-unit traceability artifacts despite through-evidence; NFR5 covered. Evidence alignment may be feasible but does not resolve coverage blocker. |
| Android performance/security NFR operational validation (live/load/penetration) | No / limited | **Unverified (acceptable for MVP stage evidence)** | NFR/infrastructure artifacts per unit; Standard strategy; no live integrations | Browser/device-performance evidence N/A. Defer operational/load/penetration validation to CI/environment-specific hardening as applicable. |
| CI-before-merge obligation | Yes (downstream) | **Deferred to CI Pipeline stage** | `ci-pipeline` is `EXECUTE` and next in state | Build and Test does not claim CI green; handoff evidence prepared. |

## Loop-Back Log

### Loop-Back 1 — 2026-11-09T00:00:00Z
- **Diagnosis:** Build and Test found MVP 80% line-coverage floor **Not Met** (JaCoCo LINE `58.55%`, `1896/3238`) on the full assembled `:app` debug unit-test suite. Cross-unit traceability sensor also reports `pass:false` with uncovered `NFR1–NFR5` and U2 NFR target-resolution failures despite normalized paths and manually verified existing target files; NFR evidence exists in split per-unit artifacts but is not fully sensor-satisfied.
- **Root-cause stage:** **Code Generation (3.5)** for coverage — insufficient unit/integration tests across generated code paths in U1–U4 (and any untested production paths included in module coverage). Traceability/NFR sensor failures are partly evidence-format/documentation issues across code-generation traceability artifacts; Build and Test produced `cross-unit-traceability.md` and U4 NFR summary but cannot fully resolve sensor path-resolution behavior in-stage without risking unrelated traceability rewrites.
- **Planned fix:** Reopen Code Generation for **all units (U1–U4)** (coverage spans every unit; NFR traceability may need targeted per-unit evidence alignment). Add targeted tests to increase meaningful line coverage toward/above the 80% MVP floor without weakening thresholds or writing vacuous assertions; focus on uncovered domain rules, adapter fallbacks/mapping branches, persistence boundaries, and UI state/facade branches already represented by fake-backed seams. Align per-unit `traceability.json` NFR target formatting/evidence only where safe and clearly needed (no invention). Preserve all invariants (ADR-005/R-01–R-06, exact stale warning, FR8 name/presence only, FQ1=A/OQ1 gate, FR10 mandatory fallback, no secrets/PII/raw GPS).
- **Estimated impact:** **Effort:** high (test additions across 4 units + targeted traceability fixes; multiple review cycles). **Financial cost:** low/none (local Gradle/JVM tests). **Risk:** medium — new tests may expose latent behavior mismatches; mitigated by preserving existing tests, fake boundaries, no live integrations, and unit-major serial replay with fresh reviews/Plan Approval on re-entry. **Policy:** do not weaken MVP 80% floor or quality targets.

## Outcome
Build and Test **does not pass its approval gate** after Loop-Back 1 replay (2026-11-10 evidence). Blocking findings remain:
1. **MVP 80% line-coverage floor Not Met:** JaCoCo LINE `60.38%` (`1955/3238`, improved from 58.55%) on full assembled `:app` suite; large 0% Compose/UI packages remain structurally under-covered by plain JVM unit tests.
2. **Cross-unit traceability sensor Not Met:** `/tmp/mealplanner-trace4.json` `pass:false`, uncovered `NFR1–NFR4` (residual `not_built`/`not_applicable` markers), while FR1–FR12/ACs and NFR5 are covered.

**Failure predicate:** applicable targets include `Not Met` verdicts (coverage floor and NFR traceability sensor). `Pending` verdicts: none. Weakening/relaxing thresholds is not acceptable. Loop-backs used: **1/3** (Loop-Back 1 recorded above). No gate presented by this stage; follow failure ladder/protocol for next action (impact-estimated options / human decision as required).
