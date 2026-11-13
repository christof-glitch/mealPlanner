# Deployment Strategy

**Intent:** `261107-weekly-meal-planner` (feature scope, Play Store publishing readiness)  
**Stage:** Deployment Pipeline (4.1, operation)  
**Date:** 2026-11-14

## Summary
This strategy defines how the Android Meal Planner MVP is released to Play Store using **staged rollout** with explicit promotion gates and maintainer approval. It is grounded in existing CI/build evidence (GitHub Actions, unit tests, 80% JaCoCo line floor) and the app's on-device, no-backend architecture.

## Selected strategy (Q1): Staged rollout
- **Approach:** Promote builds through testing tracks (internal, then closed/open as needed) and release to production with **percentage rollout** before reaching 100%.
- **Rationale:** Limits user impact of regressions, provides time to observe Play Console/vitals and tester feedback, and fits solo-maintainer MVP publishing.
- **Non-goal:** Blue/green, canary-at-infrastructure, or server traffic shifting patterns (not applicable to standalone Android client with no MealPlanner backend).

## Promotion gates (Q2)
A release may advance to the next stage only when all required gates pass:
1. **CI green** on the release commit/ref (GitHub Actions workflow success).
2. **Recorded verification and coverage floor pass:** `./gradlew :app:testDebugUnitTest :app:jacocoTestDebugUnitTestReport --console=plain` successful and JaCoCo LINE coverage **>= 80%** (not weakened).
3. **Basic manual smoke test** on a device/emulator for the candidate build (critical flows: plan generation/selection, shopping list generation/edit/check-off, export/share fallback, fallback states, no crashes).

Additional production gate: release notes prepared and production evidence reviewed (Q3).

## Approval workflow (Q3)
- **Production approval:** **Single maintainer/owner approval** required before production release or production rollout increase.
- **Required evidence:** CI/tests+coverage pass, release notes, and smoke-test confirmation (commit/run/build identifiers and track/rollout details recorded).
- **Recordkeeping:** capture approval decision and evidence in release checklist/notes (and in repo release documentation or PR/release issue as used by the maintainer).

## Release flow
1. **Commit & CI:** merge to `main` (trunk-based) only with CI quality gates passing.
2. **Release candidate:** select verified commit, increment `versionCode`, update `versionName`/release notes.
3. **Build artifact (Q4):** CI-assisted release build produces signed AAB when secure signing secrets/keystore are configured in GitHub Actions; upload to Play Console is **manual** by maintainer.
4. **Internal testing:** upload to internal track; run smoke test; verify install/update behavior.
5. **Closed/open testing (as needed):** promote when gates pass and feedback is acceptable.
6. **Production staged rollout:** obtain maintainer approval with evidence; release to small percentage first, monitor, then increase incrementally to 100% when healthy.
7. **Post-release:** monitor Android Vitals/crashes/ANRs and user/tester feedback; trigger rollback runbook if criteria met.

## Delivery and signing strategy (Q4)
- **Selected approach:** CI-assisted release build (GitHub Actions builds/tests and produces signed release artifact when secrets/keystore are configured securely); **manual Play Console upload/promotion**.
- **Signing:** use secure keystore and credentials outside source control (CI encrypted secrets/protected environment or local secure store for manual builds). Align with Play App Signing as configured in Play Console.
- **Fallback/manual path:** if CI signing is not yet configured, maintainer may build signed AAB locally from a CI-verified commit following documented secure steps; gates and approval still apply.

## Feature-flag and risk-containment strategy (Q6)
- **Selected approach:** simple in-app/local or build-config feature flags for risky/investigational features, **default-off**, no backend/cloud flag service.
- **Applicability:** CloudWatch Evidently/AppConfig are **not applicable** to this on-device MVP.
- **Mandatory constraints:** Sydänmerkki live path default-off and gated (FQ1=A, OQ1 unresolved); PRISMA name/presence only with fallbacks (FR8, OQ2–OQ3); S-ostoslista investigational with mandatory Android share sheet/copyable text fallback (FR10, OQ4). No fabricated availability/stock claims; no secrets or raw GPS/PII in flags/config committed to repo.

## Database/schema migration considerations
- Not applicable as a server migration strategy (no MealPlanner backend). Any on-device persistence/schema evolution must preserve local data safety and be validated by tests/builds before release; include migration impact in smoke tests when relevant.

## Success criteria and halt conditions
- **Proceed/increase rollout when:** smoke tests pass, no critical crash/ANR spike, no blocking functional regression in core flows, and no privacy/safety issue observed.
- **Halt rollout / initiate rollback when:** critical crash or ANR increase, data-loss/privacy issue, core planning/shopping/export flow broken, or release violates MVP safety constraints (e.g. integration fallback/labeling regressions). See `rollback-runbook.md`.

## Dependencies and prerequisites
- Google Play Console app configured (package `com.mealplanner`), Play App Signing/access, and testing track setup.
- Secure release keystore/signing credentials available (outside repo) for CI-assisted or local signed builds.
- Maintainer capacity to perform smoke tests, manual upload/promotion, and staged rollout monitoring.
