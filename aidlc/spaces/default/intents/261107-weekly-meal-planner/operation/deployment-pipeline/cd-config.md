# CD Configuration

**Intent:** `261107-weekly-meal-planner` (feature scope, Play Store publishing readiness)  
**Stage:** Deployment Pipeline (4.1, operation)  
**Date:** 2026-11-14

## Sources and scope
- CI baseline: `construction/ci-pipeline/ci-config.md`, `construction/ci-pipeline/quality-gates.md`, `.github/workflows/ci.yml` (GitHub Actions CI on `main`/PRs; `./gradlew :app:testDebugUnitTest :app:jacocoTestDebugUnitTestReport --console=plain`; 80% JaCoCo line floor enforced).
- Build/test evidence: `construction/build-and-test/test-results.md`, `construction/build-and-test/build-and-test-summary.md`, `verification/phase-check-construction.md`.
- Infrastructure/design evidence (on-device Android MVP, no MealPlanner backend/cloud resources): `construction/u2-recipe-planning-completion/infrastructure-design/cicd-pipeline.md`, `construction/u3-grocery-store-export-completion/infrastructure-design/cicd-pipeline.md`, `construction/u4-android-ui-experience/infrastructure-design/cicd-pipeline.md`, `construction/u4-android-ui-experience/infrastructure-design/infrastructure-specification.md`.
- Release decisions: `operation/deployment-pipeline/deployment-pipeline-questions.md` (Q1–Q6, confirmed).

This CD configuration covers **Android app release delivery for Play Store publishing** (client-only MVP). It does not introduce AWS/server deployment, cloud feature-flag services, or live Sydänmerkki/PRISMA/S-ostoslista integrations (OQ1–OQ4 remain gated/investigational with mandatory fallbacks).

## Release pipeline overview

| Stage | Purpose | Trigger / owner | Evidence / gate | Output |
|---|---|---|---|---|
| 1. Continuous Integration (existing) | Verify build, unit tests, and coverage floor | Push to `main` / PR to `main` (GitHub Actions CI); maintainer monitors | CI green; recorded verification command passes; JaCoCo LINE >= 80%; artifacts uploaded | Verified commit/build evidence and test/coverage reports |
| 2. Release candidate preparation | Prepare a releasable version | Manual trigger from maintainer on approved `main` commit | `versionCode` incremented; `versionName`/release notes prepared; no secrets in repo; investigational flags remain default-off | Release candidate metadata and changelog/release notes |
| 3. CI-assisted signed release build | Produce Play Store-ready artifact | Manual workflow dispatch or protected release workflow in GitHub Actions (when configured) with maintainer approval | CI quality gates pass; secure keystore/signing secrets injected from CI secret store (never committed); build produces signed **AAB** (`:app:bundleRelease` or equivalent project task) | Signed AAB artifact (and mapping/symbols if enabled) stored as CI artifact |
| 4. Manual smoke verification | Validate critical user paths before promotion | Maintainer (or designated tester) on device/emulator/internal track build | Promotion gate: CI green + verification/coverage floor + basic manual smoke test pass (Q2) | Smoke-test confirmation recorded in release checklist |
| 5. Play Console upload (manual) | Deliver artifact to Play Store | Maintainer with Play Console access | Signed AAB verified; release notes present; correct track selected | Draft/internal/track release in Play Console |
| 6. Staged promotion & production approval | Progress release safely to users | Maintainer approval per promotion (Q1, Q3) | Promotion gates satisfied for each stage; single maintainer/owner approval before production with evidence (CI/tests+coverage, release notes, smoke-test confirmation) | Track promotion / staged production rollout / 100% rollout |

## CD workflow configuration (GitHub Actions, CI-assisted release build)

**Current state:** CI workflow exists (`.github/workflows/ci.yml`). Release build/signing/upload automation is **not yet fully configured** in-repo (Q4 selected CI-assisted build with manual upload). The following configuration is the target CD extension to add when secure signing/store prerequisites are ready.

### Proposed workflow: `.github/workflows/release.yml` (to be added when ready)
- **Triggers:** `workflow_dispatch` (manual, required) with inputs such as `track` (internal/closed/open/production-draft), `versionName`/notes reference, and commit/ref; optionally tag-based trigger later (explicitly approved).
- **Permissions:** minimal `contents: read` (add only what is required for artifact/release steps; never broader than needed).
- **Concurrency:** `release-${{ github.ref }}` (no cancel-in-progress for release builds unless explicitly desired).
- **Runner:** `ubuntu-latest` (consistent with CI), JDK 17 (Temurin), Android SDK, Gradle setup/cache.
- **Steps (gated):**
  1. Checkout selected ref (must be a CI-verified commit on `main` or approved release commit).
  2. Run quality gates: `./gradlew :app:testDebugUnitTest :app:jacocoTestDebugUnitTestReport --console=plain` and enforce JaCoCo LINE >= 80% (same as CI) unless artifact is explicitly built from an already-verified CI run with traceable evidence.
  3. Configure signing **from CI secrets only** (keystore material, passwords/alias, key passwords) injected at runtime; never committed, never printed in logs.
  4. Build signed release artifact: `./gradlew :app:bundleRelease` (or project-confirmed release bundle task) producing AAB; optionally `:app:assembleRelease` if APK is needed for internal validation.
  5. Upload release artifacts (`AAB`, test/coverage reports if rebuilt, mapping/symbol files when available) via `actions/upload-artifact`.
- **Manual handoff:** Maintainer downloads signed AAB and uploads/promotes in Play Console (manual upload per Q4). Automated Play Developer API upload is out of scope for this configuration but can be added later with secure service-account secrets and explicit approval.

### Secrets and configuration management
- **Never in source control:** keystore files, keystore/key passwords, Play service-account JSON, API keys/tokens, private endpoints, raw GPS/PII.
- **Allowed secure locations:** GitHub Actions encrypted secrets / environment secrets, protected environments, local developer credential stores (outside repo) for manual signing.
- **Release signing readiness checklist (prerequisite):** upload/Play App Signing configured in Play Console; release keystore and credentials stored securely; CI environment secrets defined; access limited to maintainer/release owner.
- **Investigational integrations:** Sydänmerkki/PRISMA/S-ostoslista remain gated/default-off (OQ1–OQ4). No live credentials or network integrations added to CD.

## Environment and track mapping (Play Store)

| Environment / track | Purpose | Promotion gate (Q2) | Approval (Q3) |
|---|---|---|---|
| Local/CI verification | Build and automated verification | CI green + tests/coverage floor | N/A (automated) |
| Internal testing | Fast validation with trusted testers | CI green + verification/coverage floor + basic manual smoke test | Maintainer |
| Closed testing | Wider controlled validation (as needed) | CI green + verification/coverage floor + basic manual smoke test; prior track stable | Maintainer |
| Open testing (optional) | Pre-production feedback at scale (as needed) | CI green + verification/coverage floor + basic manual smoke test; prior track stable | Maintainer |
| Production (staged rollout) | User-facing release | All above + release notes + smoke-test confirmation; start at limited % per strategy | **Single maintainer/owner approval before production** with evidence (CI/tests+coverage pass, release notes, smoke-test confirmation) |
| Production (100%) | Full availability | Staged rollout healthy/no blocking issues per monitoring/feedback; rollback criteria not triggered | Maintainer approval to increase to 100% |

## Versioning and artifact rules
- **`versionCode` (integer):** must increase for every Play Console upload; increment on every release candidate/hotfix.
- **`versionName`:** human-readable release identifier (update per release).
- **Current app config:** `applicationId` `com.mealplanner`, `minSdk` 26, `targetSdk`/`compileSdk` 35, JDK 17 (from `app/build.gradle.kts`).
- **Artifact type:** Android App Bundle (AAB) preferred for Play Store; APK only for ad hoc/internal validation if needed.
- **Traceability:** record commit SHA, workflow run, `versionCode`/`versionName`, track, rollout %, and smoke-test evidence for each promoted release.

## Feature flags and runtime risk controls
- **Cloud feature flags:** Not applicable for this on-device MVP (no backend; CloudWatch Evidently/AppConfig not used).
- **Selected approach (Q6):** simple in-app/local or build-config feature flags for risky/investigational features, **default-off**, with mandatory fallbacks always available.
- **Constraints:** Sydänmerkki live path remains gated (FQ1=A, OQ1 unresolved); PRISMA name/presence only with fallbacks (FR8/OQ2–OQ3); S-ostoslista investigational with mandatory share/copy fallback (FR10/OQ4). No secrets/endpoints hardcoded.

## Observability and release monitoring (CD inputs)
- Use Play Console release/Android Vitals/crash and ANR reports, staged rollout metrics, and tester/user feedback as primary production signals (no custom backend telemetry required by this stage).
- Define go/no-go checks during staged rollout (crash rate, critical functional regressions in smoke-critical flows) before increasing rollout percentage.

## Out of scope / deferred
- Fully automated Play Console upload/promotion (Play Developer API) until credentials/store access and secure service-account handling are approved.
- Backend/cloud infrastructure, server deployments, and cloud feature-flag services (not part of current MVP architecture).
- Live external Sydänmerkki/PRISMA/S-ostoslista integrations (OQ1–OQ4).
