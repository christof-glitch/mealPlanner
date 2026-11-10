# Quality Gates

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Stage:** CI Pipeline (3.7)  
**Date:** 2026-11-10

## Enforced gates (CI-before-merge)

| Gate | Command / check | Threshold / pass criteria | Evidence source |
|---|---|---|---|
| Build + unit tests (recorded verification) | `./gradlew :app:testDebugUnitTest --console=plain` (executed as part of combined command below) | Exit code 0; all assembled `:app` debug JVM unit tests pass | Build and Test `test-results.md` (2026-11-10: BUILD SUCCESSFUL, 33 tasks: 2 executed, 31 up-to-date) |
| Coverage report generation | `./gradlew :app:testDebugUnitTest :app:jacocoTestDebugUnitTestReport --console=plain` | Exit code 0; JaCoCo XML produced at `app/build/reports/jacoco/jacocoTestDebugUnitTestReport/jacocoTestDebugUnitTestReport.xml` | Build and Test `build-and-test-summary.md` / `test-results.md` |
| MVP line-coverage floor | CI parses JaCoCo XML `report/counter[@type='LINE']` (`covered`, `missed`) | `covered / (covered + missed) >= 0.80` (80% line coverage, full assembled suite). **Not weakened.** | Latest measured: 84.83% (2729/3427) on 2026-11-10 (`test-results.md`) |
| Traceability (stage evidence, not runtime CI gate) | `aidlc engine sensor-traceability --stage build-and-test` | `pass:true`, uncovered none (FR1–FR12, ACs, NFR1–NFR5) | `/tmp/mealplanner-trace8.json`, `cross-unit-traceability.md` |

## Branch / trigger policy
- **Strategy:** Trunk-based (`main`).
- **Triggers:** `push` to `main`; `pull_request` targeting `main`.
- **Merge expectation:** CI quality gates must pass before merge/promotion (MVP additive obligation).

## Environment assumptions
- JDK 17 (Temurin/compatible) with Gradle wrapper 8.14.6.
- Android SDK available in CI runner (GitHub Actions `ubuntu-latest` with Android SDK setup/action as configured in workflow).
- No secrets, API keys, MealPlanner accounts, or raw GPS/PII required. No live Sydänmerkki/PRISMA/S-ostoslista integrations executed.

## Artifacts retained
- JaCoCo XML and HTML reports (`app/build/reports/jacoco/jacocoTestDebugUnitTestReport/**`).
- Unit test results (`app/build/test-results/testDebugUnitTest/**`, `app/build/reports/tests/testDebugUnitTest/**` when present).

## Non-goals / deferred
- Instrumented/device UI tests, live external integrations (OQ1–OQ4), release signing/distribution, and dedicated SAST/DAST/performance gates are out of this MVP CI baseline unless explicitly added later without weakening existing gates.
