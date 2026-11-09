# CI/CD Pipeline — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`, C2/C3 + A1)  
**Stage:** Infrastructure Design (construction, per-unit)  
**Date:** 2026-11-09

## Upstream references (consumed)
- `nfr-design/performance-design.md`, `security-design.md`, `scalability-design.md`, `reliability-design.md`, `observability-design.md`, `logical-components.md` (U2)
- `inception/domain-design/components.md`
- `functional-design/functional-spec.md`
- `inception/contract-design/contract-summary.md`

## Scope / applicability
U2 is an on-device Android `library`/domain+adapter unit. **No AWS/cloud infrastructure, no server deployment, and no packaging/deployment target is owned by U2.** `infrastructure-specification.md` and `monitoring-design.md` are N/A for this unit per stage `produces_kinds` (`library` produces only `cicd-pipeline` + `traceability`). Cloud deployment/monitoring decisions, if any, belong to app/service/packaging units and future intents. The CI/CD gates below implement verification/delivery controls consistent with U2 `nfr-design` (security default-off A1 gate, typed fallback boundaries, deterministic C2/C3 behavior, safe diagnostics) and `logical-components.md`.

## Pipeline goals
- Verify BR2.* key behaviors and U2 NFR designs (NFR1–NFR5.x, NFR4.1–4.3) through automated checks.
- Enforce safe adapter behavior: **A1 live path default-off** (FQ1=A, OQ1 unresolved), mandatory curated fallback, no live Sydänmerkki/network required in CI, no secrets.
- Preserve quality posture: existing tests green; do not weaken targets (NFR4.3).

## Stage → gate mapping

| Stage | Gate / action | U2-relevant checks | Failure handling |
|---|---|---|---|
| Source | Branch/merge checks as per team workflow (team.md Q1) | No secrets/endpoints/keys committed; A1 gate defaults off (`allowLiveSource=false`, OQ1 not approved). Any future live A1 enablement must use only an explicit, approved, auditable OQ1 feasibility/terms/allowed-access decision/configuration path (NFR3.2, security-design) and remains disabled while OQ1 is unresolved—never a silent code toggle. | Block merge; remediate before retry. |
| Build | Android/Gradle build (toolchain per U1 baseline) | Compile unit and dependent app modules; fail on compilation/config errors. | Fix build errors; no partial release. |
| Unit tests | Project verification command(s), layout-explicit | Run verification that **always includes U2 library/module tests regardless of module layout** (e.g., U2 module-scoped `test*UnitTest` tasks and/or an aggregated project task that covers U2), plus project/app verification as configured (current baseline includes `./gradlew :app:testDebugUnitTest --console=plain`; keep `--console=plain`/project conventions). Verifies BR2.1–BR2.9 behaviors: A1 typed mapping/gate/fallback (`ok/fallback_used/restricted/unavailable/timeout/parse_error_mapped/not_supported`), weekday normalization/eligibility no-guess, season fit/unknown, nutrition unavailable/cue boundaries, alternatives fallback, C3 slot/state and ADR-006 stale semantics (no auto-regeneration), input validation/mapped boundaries (NFR3.3/NFR3.6), NFR4.1/NFR4.2. | Block merge; add targeted regression for failures; keep existing tests green. |
| Static checks | Lint/static analysis as configured | Kotlin/Android lint/static checks configured by project; flag hardcoded secrets/config anti-patterns where tooling supports. | Block or require fix per project policy; do not weaken checks. |
| Artifact/publish | N/A for U2 standalone | U2 is integrated into the Android app build; no separate deployment artifact. App packaging/release is owned by later units/build-and-test. | N/A. |
| Deploy | N/A | No AWS/server deployment for U2. Android distribution (internal testing/wider release) handled by project release process (team.md Q4). | N/A. |
| Rollback | Standard VCS/app release rollback | Revert offending commit/build; curated fallback and default-off live gate reduce runtime risk. | Follow project release rollback procedure. |

## Secrets and environment
- No secrets required for MVP U2/CI.
- Live A1 configuration, if ever introduced, must be injected externally and only through an explicit, approved, auditable OQ1 feasibility/terms/allowed-access decision/configuration path (NFR3.2); while OQ1 is unresolved it remains disabled by default. CI never uses live A1 credentials or network.

## Test data / fakes
- A1 tests use fake/stub adapters and curated fallback fixtures; no live network.
- Deterministic normalization/season/nutrition/alternatives fixtures cover BR2.* typed outcomes and no-fabrication cases.

## Constraints carried forward
- FQ1=A: live Sydänmerkki disabled by default; mandatory curated fallback.
- ADR-006/R-06: exact stale warning semantics, no auto-regeneration, single Today/Plan source of truth.
- NFR4.3: do not weaken coverage/quality targets to pass.