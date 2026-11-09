# CI/CD Pipeline — U4 `u4-android-ui-experience`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Unit:** U4 — Android UI experience (`ui`)  
**Stage:** Infrastructure Design (construction, per-unit)  
**Date:** 2026-11-09

## Scope and upstream
- [infrastructure-specification.md](infrastructure-specification.md)
- [monitoring-design.md](monitoring-design.md)
- [security-design.md](../nfr-design/security-design.md)
- [functional-spec.md](../functional-design/functional-spec.md)
- [infrastructure-design-questions.md](infrastructure-design-questions.md) (Q2/Q4 resolved A)

This pipeline covers **Android app build/test verification** for U4 changes. No new AWS/cloud promotion pipeline or infrastructure is provisioned.

## Stage → gate mapping

| Stage | Action | Gate / evidence | Notes |
|---|---|---|---|
| Source | Implement U4 UI/facade/state/navigation/components in brownfield `app/` module | Code review preserves ADR-005, R-02/R-03, ADR-006, FR8, FR10, NFR3 | No secrets/raw GPS/PII; UI thin over domain contracts. |
| Static checks | Run configured lint/static analysis (layout-explicit task names as present) | No new blocking issues in changed scope; privacy/copy checks for FR8/NFR3 labels | Exact tasks confirmed by project config during code generation. |
| Unit tests | `./gradlew :app:testDebugUnitTest --console=plain` (authorised Construction verification command) | `BUILD SUCCESSFUL`; existing tests remain green | Add/maintain fake-backed UI/facade/state tests for U4 flows (confirmation gate, stale warning, fallback states, share/copy fallback, presence labels). |
| Build artifact | Assemble debug/release artifact as needed (`:app:assembleDebug`/release process) | Build succeeds; signing/release secrets kept outside source control | Release signing/distribution is outside U4 infra provisioning. |
| Manual smoke (pre-merge/release candidate) | Verify RM-01–RM-09 critical paths on emulator/device profile | R-05 next actions, R-06 slot sync/focus, ADR-006 regenerate confirmation + exact stale warning, FR8 presence-only labels, FR10 share/copy fallback/manual paths | Accessibility spot checks (labels, focus, non-color-only status). |
| Internal test / release | Follow team Android deployment rule: verified builds, internal testing before wider release | Explicit approval before any wider release | No automatic production cloud deploy; no MealPlanner backend introduced. |

## Deployment strategy
- **Client app:** Standard Android build/distribute flow (no blue-green/canary cloud routing). Rollback is by shipping a previous verified app version through the normal release process.
- **Feature risk control:** Keep official S-ostoslista path investigational/guarded (OQ4); mandatory share/copy fallback always available. Keep PRISMA/Sydänmerkki fallback/manual paths intact.

## Secrets management in CI/CD
- No secrets, API keys, tokens, or private endpoints committed to source.
- Any future integration credentials (only if OQ1–OQ4 become feasible and approved) must use CI/Android secret stores or environment-specific secure config outside VCS; never in UI state/logs.
- Location permission is runtime/user-controlled; no raw GPS stored or emitted.

## Rollback / recovery
- Revert offending code changes and rebuild verified artifact.
- Preserve user-safe fallback/manual flows so core planning/shopping remains usable when integrations fail/unavailable.
- If a U4 change breaks shared state behavior, prioritize restoring R-06/ADR-006 invariants and re-run unit tests before release.
