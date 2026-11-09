# Build Instructions — Meal Planner MVP

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, test strategy `Standard`)  
**Stage:** Build and Test (Construction 3.6)  
**Date:** 2026-11-09

## Prerequisites
- **JDK:** 17 (project uses Java/Kotlin `jvmTarget = 17`; JBR 21 observed in environment also works for Gradle).
- **Android SDK:** installed and configured via `local.properties` (`sdk.dir=...`). Required for `:app` Android Gradle Plugin tasks.
- **Gradle:** wrapper included (`gradlew`, `gradle/wrapper/gradle-wrapper.properties`, `gradle/wrapper/gradle-wrapper.jar`, Gradle 8.14.6).
- **Network:** required only for initial dependency resolution (Maven/Google repositories). Tests executed in this stage used local/unit-test paths and no live Sydänmerkki/PRISMA/S-ostoslista integrations.
- **Secrets:** none required. Do not add MealPlanner accounts, hardcoded secrets, endpoints, or raw GPS/PII.

## Environment setup
1. Ensure `local.properties` points to a valid Android SDK (already present in this workspace; machine-local file).
2. From project root: `/Users/christfm/opencode/meal-planner`.
3. Optional: `export JAVA_HOME` to a JDK 17+ installation if Gradle cannot find one.

## Dependency installation
Gradle resolves dependencies on first run:
```bash
./gradlew --version
./gradlew :app:tasks --console=plain
```

## Build commands
```bash
# Assemble debug app (compiles Kotlin/Compose/Room KSP as configured)
./gradlew :app:assembleDebug --console=plain

# Compile and run all debug JVM unit tests (full assembled app suite)
./gradlew :app:testDebugUnitTest --console=plain

# Generate JaCoCo line/branch coverage report for debug unit tests (measurement only; does not weaken MVP 80% floor target)
./gradlew :app:testDebugUnitTest :app:jacocoTestDebugUnitTestReport --console=plain
```

## Build verification steps
- `:app:assembleDebug` exits 0 → debug APK/AAB artifacts buildable.
- `:app:testDebugUnitTest` exits 0 → all selected/debug unit tests pass.
- Coverage report exists: `app/build/reports/jacoco/jacocoTestDebugUnitTestReport/jacocoTestDebugUnitTestReport.xml` and HTML under `.../html/index.html`.

## Troubleshooting common build issues
| Symptom | Likely cause | Action |
|---|---|---|
| `SDK location not found` | missing/invalid `local.properties` | set `sdk.dir` to Android SDK path (machine-local). |
| Gradle/JDK mismatch | wrong `JAVA_HOME` or unsupported JDK | use JDK 17+ compatible with AGP 8.13.x; rerun with `--stacktrace` if needed. |
| KSP/Room/Compose compile errors | dependency or generated code issue | run `./gradlew :app:compileDebugKotlin --console=plain --stacktrace`; fix reported source; keep tests green. |
| `testDebugUnitTest` UP-TO-DATE but coverage missing/stale | tasks up-to-date from prior run | run with coverage task after `enableUnitTestCoverage` changes; if needed `./gradlew :app:cleanTestDebugUnitTest :app:testDebugUnitTest :app:jacocoTestDebugUnitTestReport`. |
| Coverage threshold/floor questions | MVP scope floor is 80% line coverage on full assembled suite (Build and Test/CI obligation) | do not weaken thresholds; see `test-results.md` Target Verification Matrix. |
| External adapter failures in manual smoke | OQ1–OQ4 integrations investigational/gated | use curated/imported fallback, manual store/checklist, and share/copy export fallback per contracts. |
