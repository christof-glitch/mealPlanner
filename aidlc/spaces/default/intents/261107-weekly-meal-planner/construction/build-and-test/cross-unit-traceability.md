# Cross-Unit Traceability — Build and Test

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Stage:** Build and Test (Construction 3.6)  
**Date:** 2026-11-10 (replay after Build-and-Test Loop-Back 1 / U1–U4 Code Generation replay)  
**Source:** `aidlc engine sensor-traceability --stage build-and-test` (`/tmp/mealplanner-trace4.json`, exit 0, `pass:false`) + manual verification of unit code-generation artifacts and executed tests.

## Sensor result (verbatim summary)
- **pass:** `false`
- **uncovered:** `NFR1`, `NFR2`, `NFR3`, `NFR4`
- **covered:** `FR1`–`FR12`, `AC1.1.1`–`AC5.6.3`, `NFR5` (all marked covered with through/built_in evidence in sensor output)

## FR / AC coverage
- **FR1–FR12:** **Covered** by sensor and consistent with U1–U4 implementations/tests and unit traceability artifacts (planning/C3, recipe/time/season/nutrition/alternatives/A1, shopping/lock-snapshot/stale/merge/regenerate, store presence name-only, export share/copy fallback, privacy/fallback behaviors).
- **AC1.1.1–AC5.6.3:** **Covered** by sensor `coverage`/`built_in` entries across U1–U4.
- **Manual verification:** executed full `:app:testDebugUnitTest :app:jacocoTestDebugUnitTestReport` (`BUILD SUCCESSFUL`, 2026-11-10) and preserved U1–U4 review/checkpoint evidence; no threshold/invariant weakening.

## NFR coverage (not met by sensor)
| NFR | Sensor | Notes |
|---|---|---|
| NFR1 (Reliability/core availability) | **Not covered** (`covered:false`) | Has `through` evidence across U1–U4 but `not_built` flags remain for `u1-mvp-foundation-skeleton: NFR1.1, NFR1.2` and `u3-grocery-store-export-completion: NFR1.1, NFR1.2` in unit traceability targets. |
| NFR2 (Performance/offline behavior expectations) | **Not covered** | Has broad `through` evidence across U1–U4 but `not_built` flags remain for `u1-mvp-foundation-skeleton: NFR2.1–NFR2.3`. |
| NFR3 (Privacy/GDPR/minimal data/no secrets/raw GPS) | **Not covered** | Has broad `through` evidence across U1–U4 but `not_built` flags remain for `u1-mvp-foundation-skeleton: NFR3.1, NFR3.3, NFR3.4` and `not_applicable` for `u2-recipe-planning-completion: NFR3.4` (`code-summary.md`). |
| NFR4 (Usability/accessibility/fallback UX) | **Not covered** | Has `through` evidence for `u1-mvp-foundation-skeleton: NFR4.1–NFR4.5` but `not_built` flags remain for `u1-mvp-foundation-skeleton: NFR4.1, NFR4.3, NFR4.5`. |
| NFR5 (Maintainability/testability/quality targets) | **Covered** | Through evidence across U1–U4; MVP 80% line-coverage floor remains separately **Not Met** in Build and Test results. |

## Interpretation
- FR/AC traceability is satisfied by sensor and consistent with implementation/test evidence.
- NFR sensor failures are driven by residual `not_built`/`not_applicable` target markers in per-unit traceability artifacts (especially U1, and some U2/U3 entries), despite substantial through-evidence across units. Aligning these markers/evidence safely may be feasible in a targeted code-generation replay, but must not fabricate evidence.
- **Project-wide blocking context:** JaCoCo LINE `60.38%` (`1955/3238`) remains below MVP 80% floor; uncovered lines are concentrated in 0% Compose/UI packages (`ui/screens/*`, `ui/components`, `ui/navigation`, `ui/platform`, app/platform/UI roots), which is structurally difficult to raise to 80% with plain JVM `testDebugUnitTest` alone.

## Verdict
**Traceability: Not Met (sensor pass:false, uncovered NFR1–NFR4).** FR1–FR12 and ACs covered. Use this document together with `test-results.md` and `build-and-test-summary.md` for the Build and Test gate decision.
