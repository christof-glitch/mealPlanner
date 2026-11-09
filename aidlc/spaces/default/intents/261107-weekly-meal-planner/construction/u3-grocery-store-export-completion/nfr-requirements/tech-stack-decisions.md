# Tech Stack Decisions — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [requirements.md](../../../inception/requirements-analysis/requirements.md) — inception **NFR1**, **NFR4** (testability/quality gates).
- [U1 tech-stack-decisions.md](../../u1-mvp-foundation-skeleton/nfr-requirements/tech-stack-decisions.md), [U2 tech-stack-decisions.md](../../u2-recipe-planning-completion/nfr-requirements/tech-stack-decisions.md).
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md) (BR3.9 extraction seam).
- [nfr-requirements-questions.md](nfr-requirements-questions.md) (Q1=A, Q5=A).

## Decisions

| Area | Decision | Status | Rationale | Confirm/defer |
|---|---|---|---|---|
| Platform/module | Kotlin library code within established Android Gradle project | Decided (baseline) | U3 is a domain/library unit (C4/C5/C6) consumed by U4/app; no backend/public API. | Confirm module boundaries in NFR Design/Code Generation. |
| Language/build | Kotlin + Gradle Kotlin DSL, AndroidX/toolchain as established by U1/U2 | Decided (baseline) | Consistency with existing units and typed contracts. | Confirm versions/plugins only if build changes required. |
| Contracts/seam | Inward-pointing ports/DTOs: export use-case interface, `ShoppingListExportDto`, platform/share adapter port; A2/A3 as typed adapters | Decided (baseline) | BR3.9 stable extraction seam; supports fakes/tests and investigational PRISMA/S-ostoslista adapters (OQ2–OQ4). | Refine package/API names in Code Generation. |
| Concurrency model | Keep domain operations deterministic/synchronous where practical; represent adapter async/bounded outcomes via typed results (coroutines/Flow integration deferred to adapter/U4 boundary) | Accepted baseline | NFR5.2/NFR5.4 require responsive local behavior and safe external boundaries; exact async mechanism is implementation detail. | Confirm in NFR Design/Code Generation. |
| Persistence | No new persistence mechanism in U3 core; consumes/provides state needed by app persistence layer | Deferred | Storage schema is U4/NFR Design concern; U3 must not require raw coords/secrets. | NFR Design/U4. |
| PRISMA/S-ostoslista | Investigational adapters behind ports with mandatory fallbacks | Decided (MVP policy) | OQ2–OQ4 unresolved; never stock/guaranteed availability; share/copy fallback always available. | Adapter implementation only when feasible/confirmed. |
| Testability/quality gates (NFR4) | Automated tests for key U3 behaviors where practical; CI checks run; existing tests green; quality targets not weakened | Decided (policy baseline) | Team/inception NFR4. | Concrete test suite/CI wiring in build-and-test/Code Generation. |

## NFR4-derived targets (testability/quality gates)

| ID | Inherits | Target | Notes |
|---|---|---|---|
| NFR4.1 | NFR4 | Automated tests cover BR3.1–BR3.8 critical paths: locked-snapshot re-resolve behavior, deterministic merge/no fabrication, edit/regeneration confirmation, exact stale warning semantics, `applyPresenceCheckBatch` projection/remap, PRISMA `PRESENT|NOT_FOUND|UNKNOWN` mapping, and export current edited list + mandatory fallback. | Use fakes for C2/A2/A3 boundaries. |
| NFR4.2 | NFR4 | Adapter failures/unavailable/denied/not-supported paths are tested as typed non-fabricated outcomes. | Includes OQ2–OQ4 fallback scenarios. |
| NFR4.3 | NFR4 | Existing tests remain green; quality targets are not weakened to pass. | Build-and-test verification required. |