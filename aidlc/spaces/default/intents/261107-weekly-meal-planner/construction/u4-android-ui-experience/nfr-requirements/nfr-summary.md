# NFR Requirements Summary — U4 `u4-android-ui-experience`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U4 Android UI experience  
**Purpose:** Build and Test cross-reference index for U4 NFR requirement artifacts (created during Build and Test to provide a stable target for traceability; content is defined in split requirement files).

## Source artifacts
- `performance-requirements.md` — NFR1.x UI responsiveness/performance targets and measurement notes.
- `security-requirements.md` — NFR3.x privacy/security/data-minimization and safe UI boundary requirements.
- `observability-requirements.md` — observability/diagnostics constraints where applicable.
- `reliability-requirements.md` / `scalability-requirements.md` — reliability/scalability posture (NFR2/NFR1 aspects as applicable to on-device MVP UI).
- `tech-stack-decisions.md` — stack decisions affecting NFR implementation.
- `traceability.json` — U4 NFR requirement traceability.

## Notes
- U4 is thin Compose UI orchestration (ADR-005) over U1–U3; NFR evidence is split across UI implementation, fake-backed UI tests, and U4 NFR design/infrastructure artifacts.
- No secrets, MealPlanner account, or raw GPS/PII in UI diagnostics (NFR3). Browser-only performance evidence does not apply (Android app). Full verification details are recorded in Build and Test.