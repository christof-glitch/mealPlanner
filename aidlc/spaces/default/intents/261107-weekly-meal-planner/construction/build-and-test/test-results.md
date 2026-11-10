# Test Results — Meal Planner MVP

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, test strategy `Standard`, Guard Policy `off`, Construction autonomy `autonomous`, unit-major/serial)  
**Stage:** Build and Test (Construction 3.6, `aidlc-quality-agent`)  
**Date:** 2026-11-10 (replay after U1–U4 Code Generation loop-back replay + post-UI Plan calendar/generate-week-plan changes at `31511f5`)  
**Verification command (recorded):** `./gradlew :app:testDebugUnitTest --console=plain`

## Commands executed (evidence)
| Command | Result | Notes |
|---|---|---|
| `./gradlew :app:compileDebugKotlin :app:compileDebugUnitTestKotlin --console=plain` | `BUILD SUCCESSFUL` | 2026-11-10. Post-UI-change compile verification (Plan coming-week calendar, `generate week plan`, curated recipe catalog wiring). |
| `./gradlew :app:testDebugUnitTest :app:jacocoTestDebugUnitTestReport --console=plain` | `BUILD SUCCESSFUL` | 2026-11-10. 28 tasks: 8 executed, 20 up-to-date. Full assembled `:app` debug JVM unit test suite + JaCoCo report. |
| `aidlc engine sensor-traceability --stage build-and-test` | `pass:false` | 2026-11-10, exit 0, output saved `/tmp/mealplanner-trace5.json`. Uncovered `NFR1,NFR2,NFR3,NFR4`; `NFR5` and `FR1–FR12`/ACs covered. |
| Prior replay evidence (2026-11-10, preserved) | `BUILD SUCCESSFUL` | Scoped replay/U2-boundary verification and earlier full-suite run recorded in Loop-Back Log context (`/tmp/mealplanner-trace4.json`). |

## Build status
- **Debug app/unit-test compilation and unit execution:** successful (`BUILD SUCCESSFUL`, 2026-11-10).
- **Full `assembleDebug` not separately re-executed in this replay**; compile, unit-test run, and JaCoCo report generation completed successfully for the current codebase including requested Plan UI/generation changes.

## Test totals / scope evidence
- Full assembled suite executed via `:app:testDebugUnitTest` with JaCoCo report (`:app:jacocoTestDebugUnitTestReport`). Gradle did not emit a consolidated test-count summary in captured output; tasks completed successfully with no failures reported.
- Unit-scoped coverage spans U1–U4: planning/lock/stale (ADR-006/R-01–R-03/R-06), recipe/time/season/nutrition/alternatives + A1 gate/fallback (FQ1=A), shopping merge/regenerate/edits/presence projection (R-02, BR3.1–BR3.5), store presence name/presence only (FR8), export seam/share/copy fallback (FR10/OQ4, BR3.8/BR3.9), UI facades/mappers/navigation-state behavior with fakes.
- **Post-UI Plan changes:** Plan screen/calendar rendering and `generate week plan` action path added/wired (coming-week slots from current day; Sat/Sun B/L/D, Mon–Fri B/D; breakfast by title heuristic; lunch/dinner random non-breakfast; unique recipe IDs across week; explicit generate action only). Compile and full JVM unit suite passed; no dedicated new JVM/UI-instrumentation tests added for this UI/generation path in this replay.
- **Not executed:** live Sydänmerkki/PRISMA/S-ostoslista integrations (OQ1–OQ4 investigational/gated); instrumented/device Compose end-to-end tests; real performance/security penetration/load testing.

## Coverage (measurement)
- **Report:** `app/build/reports/jacoco/jacocoTestDebugUnitTestReport/jacocoTestDebugUnitTestReport.xml` and `.../html/index.html` (generated 2026-11-10; build artifacts cleaned after evidence collection).
- **LINE:** `2019 / 3427 = 58.91%` covered.
- **INSTRUCTION:** `16377 / 44622 = 36.70%`.
- **BRANCH:** `801 / 3757 = 21.32%`.
- **Scope:** full `:app` debug unit-test coverage. MVP additive floor is 80% line coverage on the full assembled suite; **Not Met** (58.91%, decreased from prior 60.38% after added Plan UI/generation/curated-catalog code and 0% Compose/UI/navigation/platform concentration). Quality targets were not weakened.
- **Concentration:** large uncovered areas remain in 0%/low Compose UI packages (`ui/screens/*`, `ui/components`, `ui/navigation`, `ui/platform`, app/platform/UI roots), structurally difficult to cover with plain JVM `testDebugUnitTest`.

## Browser-behavior evidence
**N/A (Android JVM unit tests).** UI behavior evidence is provided by fake-backed JVM tests under `com.mealplanner.ui.*`. No browser/app automation run in this stage. Native browser/app rendering behavior not verified by real browser/device engine.

## Target Verification Matrix

| Target | Applicable | Verdict | Evidence | Notes / gaps |
|---|---:|---|---|---|
| `./gradlew :app:testDebugUnitTest --console=plain` (recorded verification command) | Yes | **Met** | `BUILD SUCCESSFUL` as part of full suite run, 2026-11-10 | Full assembled debug JVM unit suite executed successfully. |
| Post-UI-change compile verification (`:app:compileDebugKotlin :app:compileDebugUnitTestKotlin`) | Yes | **Met** | `BUILD SUCCESSFUL`, 2026-11-10 | Confirms requested Plan calendar/generate-week-plan integration compiles. |
| Build instructions / environment reproducibility | Yes | **Met (documented)** | `build-instructions.md` | Android SDK/JDK 17/Gradle wrapper; secrets not required. |
| Unit/component test execution (Standard, U1–U4) | Yes | **Met** | U1–U4 `unit-test-instructions.md`, `code-summary.md`, replay reviews/checkpoints; full suite + compile `BUILD SUCCESSFUL` | Scope covered by per-unit instructions and executed tests. |
| Integration/key-boundary tests (Standard) | Yes | **Met (fake-backed)** | `integration-test-instructions.md`; boundary tests across C1–C6/A1–A4 seams | No live external integrations; OQ1–OQ4 gated/fallbacks preserved. |
| MVP 80% line-coverage floor (full assembled suite) | Yes | **Not Met** | JaCoCo XML: LINE `58.91%` (`2019/3427`), 2026-11-10 | **Root cause:** insufficient coverage across generated/module code, increased denominator from added UI/generation/curated code, dominated by 0%/low Compose/UI packages not meaningfully coverable by plain JVM `testDebugUnitTest` without plan-affecting instrumentation/Robolectric setup. Thresholds must not be weakened. |
| Cross-unit FR/AC traceability coverage | Yes | **Met** | `/tmp/mealplanner-trace5.json` coverage for `FR1–FR12` and ACs; `cross-unit-traceability.md` | Consistent with unit artifacts, reviews, and executed tests. |
| Cross-unit NFR traceability / sensor pass | Yes | **Not Met** | `aidlc engine sensor-traceability --stage build-and-test` `pass:false` (`/tmp/mealplanner-trace5.json`); uncovered `NFR1–NFR4` | Residual `not_built`/`not_applicable` markers in per-unit traceability artifacts despite through-evidence; NFR5 covered. Evidence alignment may be feasible but does not resolve coverage blocker. |
| Android performance/security NFR operational validation (live/load/penetration) | No / limited | **Unverified (acceptable for MVP stage evidence)** | NFR/infrastructure artifacts per unit; Standard strategy; no live integrations | Browser/device-performance evidence N/A. Defer operational/load/penetration validation to CI/environment-specific hardening as applicable. |
| CI-before-merge obligation | Yes (downstream) | **Deferred to CI Pipeline stage** | `ci-pipeline` is `EXECUTE` and next in state | Build and Test does not claim CI green; handoff evidence prepared. |

## Loop-Back Log

### Loop-Back 1 — 2026-11-09T00:00:00Z
- **Diagnosis:** Build and Test found MVP 80% line-coverage floor **Not Met** (JaCoCo LINE `58.55%`, `1896/3238`) on the full assembled `:app` debug unit-test suite. Cross-unit traceability sensor also reported `pass:false` with uncovered `NFR1–NFR5` in initial diagnosis context; replay narrowed uncovered set to residual NFR markers.
- **Root-cause stage:** **Code Generation (3.5)** for coverage — insufficient unit/integration tests across generated code paths in U1–U4 (and untested production paths included in module coverage). Traceability/NFR sensor failures are partly evidence-format/documentation issues across code-generation traceability artifacts.
- **Planned fix:** Reopen Code Generation for **all units (U1–U4)**. Add targeted tests to increase meaningful line coverage toward/above the 80% MVP floor without weakening thresholds or writing vacuous assertions; align per-unit `traceability.json` NFR target formatting/evidence only where safe and clearly needed (no invention). Preserve all invariants (ADR-005/R-01–R-06, exact stale warning, FR8 name/presence only, FQ1=A/OQ1 gate, FR10 mandatory fallback, no secrets/PII/raw GPS).
- **Estimated impact:** **Effort:** high. **Financial cost:** low/none. **Risk:** medium — mitigated by preserved tests/fake boundaries, no live integrations, unit-major serial replay with reviews/Plan Approval on re-entry. **Policy:** do not weaken MVP 80% floor or quality targets.

## Outcome
Build and Test **does not pass its approval gate** after post-UI-change replay (2026-11-10 evidence). Blocking findings remain:
1. **MVP 80% line-coverage floor Not Met:** JaCoCo LINE `58.91%` (`2019/3427`) on full assembled `:app` suite (increased uncovered surface after Plan UI/generation/curated-catalog changes; large 0%/low Compose/UI packages remain structurally under-covered by plain JVM unit tests).
2. **Cross-unit traceability sensor Not Met:** `/tmp/mealplanner-trace5.json` `pass:false`, uncovered `NFR1–NFR4` (residual `not_built`/`not_applicable` markers), while FR1–FR12/ACs and NFR5 are covered.

**Failure predicate:** applicable targets include `Not Met` verdicts (coverage floor and NFR traceability sensor). `Pending` verdicts: none. Weakening/relaxing thresholds is not acceptable. Loop-backs used: **1/3** (Loop-Back 1 recorded above). No gate presented by this stage; no impact-estimated fix identified that resolves all blocking failures without weakening thresholds or plan-affecting changes — follow failure ladder/protocol and report for human decision/next directive.
