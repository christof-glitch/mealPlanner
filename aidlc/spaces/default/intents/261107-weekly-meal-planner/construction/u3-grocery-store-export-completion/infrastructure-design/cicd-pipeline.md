# CI/CD Pipeline — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`, C4/C5/C6)  
**Stage:** Infrastructure Design (construction, per-unit)  
**Date:** 2026-11-09

## Upstream references (consumed)
- `nfr-design/performance-design.md`, `security-design.md`, `scalability-design.md`, `reliability-design.md`, `observability-design.md`, `logical-components.md` (U3)
- `inception/domain-design/components.md`
- `functional-design/functional-spec.md`
- `inception/contract-design/contract-summary.md`

## Scope / applicability
U3 is an on-device Android `library`/domain+adapter unit (ShoppingList C4, StoreAvailability contracts/A2 boundary, Export C6/A3 seam). **No AWS/cloud infrastructure, no server deployment, and no standalone packaging target is owned by U3.** `infrastructure-specification.md` and `monitoring-design.md` are N/A for this library unit per stage `produces_kinds` (`library` produces only `cicd-pipeline` + `traceability`). Cloud deployment/monitoring, if any, belong to packaging/service units and future intents. CI/CD gates below verify U3 NFR designs (NFR1–NFR5.x, NFR4.1–NFR4.3), ADR-006/R-01–R-03, R-02/BR3.5, FR8 name/presence only, and BR3.9 inward export seam.

## Pipeline goals
- Verify BR3.* critical behaviors and NFR designs through automated checks (NFR4.1).
- Enforce fallback-first investigational adapter boundaries (OQ2–OQ4): no live PRISMA/S-ostoslista network/credentials required in CI; mandatory share/copy fallback and manual store/checklist paths remain testable (NFR4.2, NFR2.5, NFR3.6).
- Preserve quality posture: existing tests green; do not weaken targets (NFR4.3).

## Stage → gate mapping

| Stage | Gate / action | U3-relevant checks | Failure handling |
|---|---|---|---|
| Source | Branch/merge checks per team workflow (team.md Q1) | No secrets/API keys/tokens/private endpoints committed. No raw GPS coords in U3 domain code/tests/logging. PRISMA/S-ostoslista official paths remain investigational; no silent enablement. | Block merge; remediate before retry. |
| Build | Android/Gradle build (toolchain per U1 baseline) | Compile U3 module and dependent modules; fail on compilation/config errors. | Fix build errors; no partial release. |
| Unit tests | Project verification command(s), layout-explicit | Run verification that **always includes U3 library/module tests regardless of module layout** (module-scoped tasks and/or aggregated command covering U3), plus project baseline (currently includes `./gradlew :app:testDebugUnitTest --console=plain`; keep `--console=plain`/project conventions). Cover BR3.1–BR3.8: locked-snapshot re-resolve/no fabrication, deterministic merge separate-on-uncertainty, edit/check/add/remove + regeneration confirmation (`CONFIRMATION_REQUIRED` preserves list), exact ADR-006 stale warning text and no auto-regeneration, explicit `applyPresenceCheckBatch` projection/remap to `UNKNOWN|NOT_CHECKED` on ambiguity (R-02), PRISMA `PRESENT|NOT_FOUND|UNKNOWN` name/presence only (never stock/guarantee), export DTO uses current edited list, mandatory share/copy fallback and typed S-ostoslista `NOT_SUPPORTED|UNAVAILABLE|FAILED_MAPPED` handling. | Block merge; add targeted regression; keep existing tests green. |
| Static checks | Lint/static analysis as configured | Kotlin/Android lint/static checks; flag hardcoded secrets and obvious PII logging patterns where tooling supports. | Block or require fix per project policy; do not weaken checks. |
| Artifact/publish | N/A for U3 standalone | U3 integrated into Android app build; no separate deployment artifact. | N/A. |
| Deploy | N/A | No AWS/server deployment for U3. Android distribution handled by project release process (team.md Q4). | N/A. |
| Rollback | Standard VCS/app release rollback | Revert offending commit/build; fallback-first adapters and local export/share-copy reduce runtime risk. | Follow project release rollback procedure. |

## Secrets and environment
- No secrets required for MVP U3/CI.
- PRISMA/S-ostoslista live configuration, if ever introduced, must be injected externally only after feasibility/terms/allowed access are confirmed (OQ2–OQ4); CI never uses live credentials or live PRISMA/S-ostoslista network calls.

## Test data / fakes
- C2, A2, A3, and platform share ports tested with fakes/stubs; deterministic fixtures for merge, presence batch mapping, edit/remap, stale warning, and export payload/current edited state.
- No live network required in CI.

## Constraints carried forward
- ADR-006/R-01–R-03: snapshot IDs+context, re-resolve, explicit regenerate confirmation, exact stale warning `Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.`, no auto-regeneration.
- R-02/BR3.5: ShoppingList owns availability projection; explicit `applyPresenceCheckBatch`; StoreAvailability never mutates items.
- FR8: PRISMA name/presence only (`PRESENT|NOT_FOUND|UNKNOWN`), never stock/guaranteed availability.
- FR10/OQ4/BR3.8/BR3.9: export current edited list, mandatory share/copy fallback, stable inward extraction seam.
- NFR4.3: do not weaken coverage/quality targets to pass.