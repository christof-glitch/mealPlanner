# CI Configuration

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Stage:** CI Pipeline (3.7)  
**Date:** 2026-11-10  
**CI tool:** GitHub Actions  
**Config file:** `.github/workflows/ci.yml`

## Overview
Implements trunk-based CI for the Android/Kotlin Meal Planner MVP (`:app`, Gradle wrapper). The pipeline enforces Build and Test recorded verification and the MVP 80% JaCoCo line-coverage floor on every push to `main` and pull request targeting `main`.

## Workflow: `.github/workflows/ci.yml`

```yaml
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

permissions:
  contents: read

concurrency:
  group: ci-${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  build-test-coverage:
    name: Build, Test, and Coverage Gate
    runs-on: ubuntu-latest
    timeout-minutes: 30

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Set up Android SDK
        uses: android-actions/setup-android@v3

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Run unit tests and JaCoCo report (recorded verification + coverage)
        run: ./gradlew :app:testDebugUnitTest :app:jacocoTestDebugUnitTestReport --console=plain

      - name: Enforce MVP 80% line-coverage floor (JaCoCo)
        run: |
          python3 - <<'PY'
          import sys
          import xml.etree.ElementTree as ET

          path = "app/build/reports/jacoco/jacocoTestDebugUnitTestReport/jacocoTestDebugUnitTestReport.xml"
          root = ET.parse(path).getroot()
          covered = missed = None
          for c in root.findall("counter"):
              if c.get("type") == "LINE":
                  covered = int(c.get("covered", "0"))
                  missed = int(c.get("missed", "0"))
                  break
          if covered is None or missed is None:
              print("ERROR: LINE counter not found in JaCoCo report", file=sys.stderr)
              sys.exit(2)
          total = covered + missed
          ratio = (covered / total) if total else 0.0
          pct = ratio * 100.0
          print(f"JaCoCo LINE coverage: {covered}/{total} = {pct:.2f}%")
          if ratio + 1e-12 < 0.80:
              print(f"ERROR: MVP 80% line-coverage floor not met ({pct:.2f}% < 80.00%)", file=sys.stderr)
              sys.exit(1)
          print("Coverage floor met (>= 80%).")
          PY

      - name: Upload test and coverage reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-and-coverage-reports
          if-no-files-found: warn
          path: |
            app/build/reports/jacoco/jacocoTestDebugUnitTestReport/**
            app/build/test-results/testDebugUnitTest/**
            app/build/reports/tests/testDebugUnitTest/**
```

## Notes
- **Recorded command enforced:** `./gradlew :app:testDebugUnitTest --console=plain` (included in combined Gradle invocation) and JaCoCo report generation used by Build and Test evidence.
- **Coverage floor:** MVP additive 80% line coverage on full assembled suite; enforced in CI without weakening thresholds.
- **No live integrations:** Sydänmerkki/PRISMA/S-ostoslista remain investigational/gated (OQ1–OQ4); tests/fakes cover boundaries.
- **Security/privacy:** no secrets, credentials, MealPlanner account, or raw GPS/PII in workflow.
- **Artifacts:** test results and JaCoCo reports uploaded for every run (`if: always()`).

## Related artifacts
- `construction/ci-pipeline/quality-gates.md`
- `construction/ci-pipeline/ci-pipeline-questions.md`
- `construction/build-and-test/test-results.md`
- `construction/build-and-test/build-and-test-summary.md`
- `verification/phase-check-construction.md`
