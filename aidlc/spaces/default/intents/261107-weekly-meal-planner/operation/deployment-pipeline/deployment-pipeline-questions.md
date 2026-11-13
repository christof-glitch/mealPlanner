# Deployment Pipeline Questions

**Intent:** `261107-weekly-meal-planner` (feature scope, Play Store publishing readiness)  
**Stage:** Deployment Pipeline (4.1, operation)  
**Date:** 2026-11-13

Context: Android/Kotlin app (`:app`, Gradle, GitHub Actions CI enforcing `./gradlew :app:testDebugUnitTest :app:jacocoTestDebugUnitTestReport` and 80% JaCoCo line floor). MVP is on-device/local-first with no MealPlanner backend; external Sydänmerkki/PRISMA/S-ostoslista integrations remain investigational/gated with mandatory fallbacks (OQ1–OQ4). This stage defines deployment/release pipeline configuration, strategy, and rollback runbook for Play Store publishing based on existing evidence.

## Q1. [Release manager] What Play Store release/deployment strategy should we use?

Why this matters: the deployment strategy determines how new app versions reach testers and users (for example, staged percentage rollout vs direct full release) and what safety checks we rely on before wider exposure.

- A. Staged rollout: promote through testing tracks (internal/closed/open as needed) and use percentage rollout on production before 100% (recommended for safer MVP publishing)
- B. Direct production release: ship to production (100%) after required checks/approval, without percentage rollout
- C. Testing tracks only for now: internal/closed testing releases, defer production publishing strategy until later
- D. Other staged approach (specify track/rollout steps)
- X. Other (please specify)

[Answer]: A. Staged rollout: promote through testing tracks (internal/closed/open as needed) and use percentage rollout on production before 100% (recommended for safer MVP publishing)

## Q2. [Release manager] What promotion gates are required between release stages?

Why this matters: promotion gates define the evidence required before moving a build from one stage to the next (for example, internal testing -> closed/open testing -> production), reducing release risk.

- A. CI green + recorded verification/coverage floor pass + basic manual smoke test on a device/emulator before each promotion (recommended baseline)
- B. CI green only for testing tracks; add manual QA/sign-off before production
- C. Manual decision only (no formal automated gate beyond building the artifact)
- D. Custom gates (specify checks per stage)
- X. Other (please specify)

[Answer]: A. CI green + recorded verification/coverage floor pass + basic manual smoke test on a device/emulator before each promotion (recommended baseline)

## Q3. [Release manager] What approval workflow is required for production release?

Why this matters: Play Store production releases are user-facing; explicit approval and required evidence prevent accidental or unready releases.

- A. Single maintainer/owner approval before production, with evidence: CI/tests+coverage pass, release notes, and smoke-test confirmation (recommended for solo MVP)
- B. Two-step approval (technical/build verification + release/publishing approval) before production
- C. No explicit approval gate for production (not recommended)
- D. Custom approval workflow (specify approver(s) and required evidence)
- X. Other (please specify)

[Answer]: A. Single maintainer/owner approval before production, with evidence: CI/tests+coverage pass, release notes, and smoke-test confirmation (recommended for solo MVP) 

## Q4. [Developer] How should release artifacts, signing, and Play Console delivery be handled?

Why this matters: Play Store publishing requires a signed Android App Bundle (AAB) and a repeatable delivery path. Signing keys/secrets must stay out of source control.

- A. Manual/local-first for now: build signed AAB locally or from CI artifacts, upload/manage releases manually in Play Console; keep signing material outside repo and document the process (recommended until credentials/store access are ready)
- B. CI-assisted release build: GitHub Actions builds/tests and produces signed release artifact when secrets/keystore are configured securely; upload to Play Console remains manual
- C. Automated CD to Play Console: GitHub Actions builds, signs, and uploads/promotes via Play Developer API/service account (requires secure secrets and store setup)
- D. Custom release/signing/delivery approach (specify)
- X. Other (please specify)

[Answer]: B. CI-assisted release build: GitHub Actions builds/tests and produces signed release artifact when secrets/keystore are configured securely; upload to Play Console remains manual

## Q5. [Operations] What rollback procedure should we follow if a bad Play Store release is found?

Why this matters: rollback steps must be clear and fast (halt rollout, ship fix, communicate) because Android releases cannot be "rolled back" in place like server traffic; recovery usually means a new corrected release.

- A. Halt staged rollout (if active), disable affected track release where possible, publish a fixed build with incremented `versionCode`, and communicate status to testers/users (recommended)
- B. Publish a hotfix release with incremented `versionCode` as the primary recovery; halt rollout only if staged rollout is in use
- C. Revert code on `main` and rebuild/re-release only (no explicit Play Console rollout/communication steps)
- D. Custom rollback/runbook steps (specify)
- X. Other (please specify)

[Answer]: A. Halt staged rollout (if active), disable affected track release where possible, publish a fixed build with incremented `versionCode`, and communicate status to testers/users (recommended)

## Q6. [Developer] What feature-flag strategy should the release pipeline assume?

Why this matters: feature flags can reduce release risk, but cloud flag services (e.g. CloudWatch Evidently, AppConfig) do not fit this on-device MVP with no backend. Investigational integrations (Sydänmerkki/PRISMA/S-ostoslista, OQ1–OQ4) must remain gated with mandatory fallbacks.

- A. No cloud feature-flag service; keep current default-off/gated behavior and hardcoded/build-time guards for investigational features, with fallbacks always available (recommended for MVP)
- B. Add simple in-app/local or build-config feature flags for risky/investigational features (still default-off, no backend/cloud service)
- C. Plan external/cloud feature-flag service for future backend-connected releases (document as future, not used now)
- D. Custom feature-flag approach (specify)
- X. Other (please specify)

[Answer]: B. Add simple in-app/local or build-config feature flags for risky/investigational features (still default-off, no backend/cloud service)



## Consolidated Summary Confirmation

Summary of answers to confirm before generating deployment-pipeline artifacts:

- **Q1. [Release manager] Play Store release/deployment strategy:** A. Staged rollout: promote through testing tracks (internal/closed/open as needed) and use percentage rollout on production before 100% (recommended for safer MVP publishing).
- **Q2. [Release manager] Promotion gates between release stages:** A. CI green + recorded verification/coverage floor pass + basic manual smoke test on a device/emulator before each promotion (recommended baseline).
- **Q3. [Release manager] Production approval workflow:** A. Single maintainer/owner approval before production, with evidence: CI/tests+coverage pass, release notes, and smoke-test confirmation (recommended for solo MVP).
- **Q4. [Developer] Release artifacts, signing, and Play Console delivery:** B. CI-assisted release build: GitHub Actions builds/tests and produces signed release artifact when secrets/keystore are configured securely; upload to Play Console remains manual.
- **Q5. [Operations] Rollback procedure for a bad Play Store release:** A. Halt staged rollout (if active), disable affected track release where possible, publish a fixed build with incremented `versionCode`, and communicate status to testers/users (recommended).
- **Q6. [Developer] Feature-flag strategy:** B. Add simple in-app/local or build-config feature flags for risky/investigational features (still default-off, no backend/cloud service).

Does this all look correct before I generate the artifact?

- Looks correct
- Request changes

[Answer]:  
