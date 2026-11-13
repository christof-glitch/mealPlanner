# Rollback Runbook

**Intent:** `261107-weekly-meal-planner` (feature scope, Play Store publishing readiness)  
**Stage:** Deployment Pipeline (4.1, operation)  
**Date:** 2026-11-14

## Purpose
Define clear, fast recovery steps when a Play Store release is faulty. Android app releases cannot be rolled back in place like server traffic; recovery is achieved by **halting rollout**, stabilizing distribution, and shipping a **corrected build with an incremented `versionCode`**.

## Selected rollback approach (Q5)
- **Primary:** Halt staged rollout (if active), disable/stop the affected track release where possible, publish a fixed build with incremented `versionCode`, and communicate status to testers/users.
- **Always:** Keep evidence (what failed, affected versions/tracks, timeline, decision, fix commit/build).

## Triggers (initiate rollback response)
Initiate this runbook when any of the following is observed after upload/promotion:
- Critical crash, ANR spike, or startup/install failure.
- Core flow regression: plan generation/selection (FR1–FR3), shopping list generation/edit/check-off/regeneration semantics (FR9/ADR-006/R-03 exact stale warning), export/share or copyable fallback (FR10), store/availability fallback behavior (FR7/FR8 labels `PRESENT|NOT_FOUND|UNKNOWN`, no stock/guaranteed availability), or privacy/permission regressions (FR12/NFR3).
- Release artifact/config issue: wrong signing, broken permissions/manifest behavior, incorrect versioning, or missing required fallback states.
- Security/privacy concern: secret exposure, unintended data collection/transmission, or unsafe integration behavior.

## Roles
- **Release owner/maintainer:** decides halt/rollback, executes Play Console actions, approves hotfix release with required evidence (Q3).
- **Developer/maintainer:** diagnoses issue, implements fix on trunk, verifies CI/tests+coverage and smoke test.

## Immediate response (containment)
1. **Acknowledge and classify severity** (critical/high/medium) and identify affected track(s), rollout %, `versionCode`/`versionName`, and commit/build.
2. **Halt staged rollout** in Play Console if production rollout is in progress (stop increasing %, reduce/pause as available).
3. **Disable/pause affected release** on the affected track where Play Console allows (or stop promotion) to prevent further exposure.
4. **Preserve evidence:** screenshots/logs/Play Console vitals, user/tester reports, workflow run links, and artifact identifiers.
5. **Communicate status:** notify testers (internal/closed/open) and, if user-facing impact exists, prepare user-facing communication/release note as appropriate.

## Recovery (fix and re-release)
1. **Diagnose root cause** from verified commit/build and reports; avoid speculative fixes.
2. **Fix on `main`** (trunk-based) following normal development/testing practices; add/adjust targeted tests if needed without weakening quality gates.
3. **Verify fix:**
   - CI green on fix commit.
   - `./gradlew :app:testDebugUnitTest :app:jacocoTestDebugUnitTestReport --console=plain` passes; JaCoCo LINE >= 80%.
   - Basic manual smoke test on device/emulator confirms issue resolved and core flows intact.
4. **Prepare corrected release:** increment `versionCode` (mandatory), update `versionName`/release notes with fix summary and impact.
5. **Build signed AAB** via CI-assisted release workflow (preferred, Q4) or secure local/manual build from verified commit.
6. **Upload and promote:** manual Play Console upload; apply promotion gates and single maintainer/owner production approval with evidence (Q3).
7. **Resume rollout carefully:** start with limited staged rollout, monitor vitals/crashes/feedback before increasing to 100%.

## If staged rollout is not in use
- If the bad build was fully released, prioritize a **hotfix release** with incremented `versionCode` and expedited verification (same quality gates/evidence). Use Play Console controls available for the track to limit exposure where possible and communicate clearly.

## Communication checklist
- What happened (user impact), affected versions/tracks, and current status.
- Mitigation taken (rollout halted/track paused).
- Fix version (`versionCode`/`versionName`) and ETA or release status.
- User guidance if needed (e.g. update when available); avoid exposing secrets/internal endpoints.

## Post-incident follow-up
- Record timeline, root cause, fix, and verification evidence.
- Update release checklist/smoke-test scope if a gap was found.
- Confirm MVP constraints preserved: no hardcoded secrets, no MealPlanner account, minimal data/no raw GPS persistence, mandatory integration fallbacks and correct labels.
- Consider whether a simple in-app/build-config feature flag (default-off) should guard similar risky behavior in future (Q6).

## References
- `cd-config.md` (release pipeline, track mapping, artifact/signing configuration)
- `deployment-strategy.md` (staged rollout, promotion gates, approval evidence)
- `construction/ci-pipeline/quality-gates.md`, `.github/workflows/ci.yml`
- `construction/u4-android-ui-experience/infrastructure-design/cicd-pipeline.md` (Android release/rollback context)
