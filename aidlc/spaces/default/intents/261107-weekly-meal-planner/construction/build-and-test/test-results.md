# Test Results — Meal Planner MVP

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, test strategy `Standard`, Guard Policy `off`, Construction autonomy `autonomous`, unit-major/serial)  
**Stage:** Build and Test (Construction 3.6, `aidlc-quality-agent`)  
**Date:** 2026-11-09  
**Verification command (recorded):** `./gradlew :app:testDebugUnitTest --console=plain`

## Commands executed (evidence)
| Command | Result | Notes |
|---|---|---|
| `./gradlew :app:testDebugUnitTest --console=plain` | `BUILD SUCCESSFUL` | 27 tasks: 1 executed, 26 up-to-date (2026-11-09). Full assembled `:app` debug JVM unit test suite. |
| `./gradlew :app:testDebugUnitTest :app:jacocoTestDebugUnitTestReport --console=plain` | `BUILD SUCCESSFUL` | 28 tasks: 2 executed, 26 up-to-date. JaCoCo measurement task added in-stage (measurement only; no weakened thresholds). |
| `aidlc engine sensor-traceability --stage build-and-test` | `pass:false` | Output saved `/tmp/mealplanner-trace3.json`. Uncovered `NFR1,NFR2,NFR3,NFR4,NFR5`; reason includes U2 code-generation NFR target-resolution messages for existing `app/src/...` files (manual verification performed). |

## Build status
- **Debug app build/test compilation:** successful under executed unit-test tasks (`BUILD SUCCESSFUL`).
- **Full `assembleDebug` not separately re-executed in this run** after all Build and Test documentation edits; unit-test compile/run completed successfully for the current codebase.

## Test totals / scope evidence
- Full assembled suite executed via `:app:testDebugUnitTest` (project-level, deduplicated). Gradle did not emit a consolidated test-count summary in captured tail output; test tasks completed successfully with no failures reported.
- Unit-scoped coverage spans U1–U4 as defined in per-unit `unit-test-instructions.md`/code summaries: planning/lock/stale (ADR-006/R-01–R-03/R-06), recipe/time/season/nutrition/alternatives + A1 gate/fallback (FQ1=A), shopping merge/regenerate/edits/presence projection (R-02, BR3.1–BR3.5), store presence name/presence only (FR8), export seam/share-copyright fallback (FR10/OQ4, BR3.8/BR3.9), UI facades/mappers/navigation-state behavior with fakes.
- **Not executed:** live Sydänmerkki/PRISMA/S-ostoslista integrations (OQ1–OQ4 investigational/gated); instrumented/device Compose end-to-end tests; real performance/security penetration/load testing.

## Coverage (measurement)
- **Report:** `app/build/reports/jacoco/jacocoTestDebugUnitTestReport/jacocoTestDebugUnitTestReport.xml` and `.../html/index.html`.
- **LINE:** `1896 / 3238 = 58.55%` covered/missed+covered.
- **INSTRUCTION:** `15573 / 42754 = 36.42%`.
- **Scope:** full `:app` debug unit-test coverage (includes generated/UI/domain/adapter code in module; standard filters applied in report config). MVP additive floor is 80% line coverage on the full assembled suite; **Not Met** (58.55%). Quality targets were not weakened.

## Browser-behavior evidence
**N/A (Android JVM unit tests).** UI behavior evidence is provided by fake-backed JVM tests under `com.mealplanner.ui.*` (plan/grocery/supporting facades, navigation state, user state, diagnostics mapping). No browser/app automation run in this stage.

## Target Verification Matrix

| Target | Applicable | Verdict | Evidence | Notes / gaps |
|---|---:|---|---|---|
| `./gradlew :app:testDebugUnitTest --console=plain` (recorded verification command) | Yes | **Met** | `BUILD SUCCESSFUL`, 27 tasks (1 executed, 26 up-to-date), 2026-11-09 | Full assembled debug JVM unit suite. |
| Build instructions / environment reproducibility | Yes | **Met (documented)** | `build-instructions.md` | Android SDK/JDK 17/Gradle wrapper; secrets not required. |
| Unit/component test execution (Standard, U1–U4) | Yes | **Met** | U1–U4 `unit-test-instructions.md`, `code-summary.md`; `:app:testDebugUnitTest` success | Scope covered by per-unit instructions and executed full suite. |
| Integration/key-boundary tests (Standard) | Yes | **Met (fake-backed)** | `integration-test-instructions.md`; boundary tests across C1–C6/A1–A4 seams | No live external integrations; OQ1–OQ4 gated/fallbacks preserved. |
| MVP 80% line-coverage floor (full assembled suite, CI/Build-and-Test obligation) | Yes | **Not Met** | JaCoCo XML/HTML: LINE `58.55%` (`1896/3238`) | **Root cause:** insufficient tests across generated code paths (U1–U4), not a scaffolding defect. Must not weaken thresholds. Requires loop-back to code-generation to add targeted tests (impact-estimated). |
| Cross-unit FR/AC traceability coverage | Yes | **Met (sensor coverage) / Partially verified** | `/tmp/mealplanner-trace3.json` coverage includes FR1–FR12 and AC entries; `cross-unit-traceability.md` | Manual verification confirms FR1–FR12/ADR invariants represented in unit artifacts; sensor `pass:false` due to NFR/unresolved target-resolution reporting. |
| Cross-unit NFR traceability / sensor pass | Yes | **Not Met** | `aidlc engine sensor-traceability --stage build-and-test` `pass:false`; uncovered `NFR1–NFR5`; U2 NFR target-resolution messages for existing files | U4 `nfr-summary.md` added in-stage. U2 `traceability.json` normalized in-stage but sensor still reports target-file resolution issues (investigated; manual file existence verified). NFR evidence is split across U1–U4 NFR/infrastructure/code artifacts; unresolved at gate. |
| Android performance/security NFR operational validation (live/load/penetration) | No / limited | **Unverified (acceptable for MVP stage evidence)** | NFR/infrastructure artifacts per unit; Standard strategy; no live integrations | Browser-performance evidence N/A. Defer operational/load/penetration validation to CI/environment-specific hardening as applicable. |
| CI-before-merge obligation | Yes (downstream) | **Deferred to CI Pipeline stage** | `ci-pipeline` is `EXECUTE` and next in state | Build and Test does not claim CI green; handoff evidence prepared. |

## Loop-Back Log

### Loop-Back 1 — 2026-11-09T00:00:00Z
- **Diagnosis:** Build and Test found MVP 80% line-coverage floor **Not Met** (JaCoCo LINE `58.55%`, `1896/3238`) on the full assembled `:app` debug unit-test suite. Cross-unit traceability sensor also reports `pass:false` with uncovered `NFR1–NFR5` and U2 NFR target-resolution failures despite normalized paths and manually verified existing target files; NFR evidence exists in split per-unit artifacts but is not fully sensor-satisfied.
- **Root-cause stage:** **Code Generation (3.5)** for coverage — insufficient unit/integration tests across generated code paths in U1–U4 (and any untested production paths included in module coverage). Traceability/NFR sensor failures are partly evidence-format/documentation issues across code-generation traceability artifacts; Build and Test produced `cross-unit-traceability.md` and U4 NFR summary but cannot fully resolve sensor path-resolution behavior in-stage without risking unrelated traceability rewrites.
- **Planned fix:** Reopen Code Generation for **all units (U1–U4)** (coverage spans every unit; NFR traceability may need targeted per-unit evidence alignment). Add targeted tests to increase meaningful line coverage toward/above the 80% MVP floor without weakening thresholds or writing vacuous assertions; focus on uncovered domain rules, adapter fallbacks/mapping branches, persistence boundaries, and UI state/facade branches already represented by fake-backed seams. Align per-unit `traceability.json` NFR target formatting/evidence only where safe and clearly needed (no invention). Preserve all invariants (ADR-005/R-01–R-06, exact stale warning, FR8 name/presence only, FQ1=A/OQ1 gate, FR10 mandatory fallback, no secrets/PII/raw GPS).
- **Estimated impact:** **Effort:** high (test additions across 4 units + targeted traceability fixes; multiple review cycles). **Financial cost:** low/none (local Gradle/JVM tests). **Risk:** medium — new tests may expose latent behavior mismatches; mitigated by preserving existing tests, fake boundaries, no live integrations, and unit-major serial replay with fresh reviews/Plan Approval on re-entry. **Policy:** do not weaken MVP 80% floor or quality targets.

## Outcome
Build and Test **did not pass its approval gate** due to coverage floor Not Met and unresolved sensor-traceability findings. Autonomous failure loop-back initiated (Construction autonomy `autonomous`, 0 prior loop-back entries, impact-estimated fix identified) per Build and Test Step 9 / loop-back protocol. **Gate not presented**; learnings ritual deferred to eventual passing run; jump to code-generation executed through engine directives.
