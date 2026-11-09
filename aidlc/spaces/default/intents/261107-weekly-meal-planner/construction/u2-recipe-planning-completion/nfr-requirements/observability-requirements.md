# Observability Requirements — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-09

## Sources
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (NFR2, NFR5)
- [nfr-requirements-questions.md](nfr-requirements-questions.md) (Q3=A)

## Requirements

| ID | Requirement | Target / measure | Inherited from | Notes |
|---|---|---|---|---|
| NFR5.4 | Typed status/reason codes at domain boundaries | C2/C3/A1 contracts expose machine-readable typed statuses and reason codes/keys already defined in functional design (`RecipeSourceAdapterResult` status/investigationOutcome, `RecipeTimeNormalization`, `SeasonFitEvaluation`, `NutritionProfile` availability, alternatives/slot-change results, exclusion/fallback reasons). These serve as primary observability signals for tests and U4 presentation. | NFR5, NFR2 | Q3=A. |
| NFR5.5 | Structured log hooks without PII/secrets | Adapter/normalization/eligibility boundaries may emit structured debug/diagnostic log hooks (e.g., operation name, status, reason code, counts, timing bucket) when enabled by app/build diagnostics. Logs must not include PII, secrets, credentials, precise location, raw external payload bodies by default, or unnecessary personal data. | NFR5, NFR3 | Implementation pattern refined in NFR Design; default safe in release builds. |
| NFR5.6 | No external telemetry backend required for MVP U2 | U2 does not require analytics/telemetry SaaS, distributed tracing backend, or remote metrics pipeline for MVP. U4/build-and-test own user-visible surfacing and verification; any future telemetry is a separate decision. | NFR5 | Can be revisited post-MVP. |

## Non-goals (MVP)
- SLI/SLO dashboards, alerting thresholds, distributed tracing spans across services (no backend), or production APM integration owned by U2.