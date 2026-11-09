# Security Requirements — U1 `u1-mvp-foundation-skeleton`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U1 — MVP foundation + integration slice (Android app)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-08

## Upstream
- [requirements.md](../../../inception/requirements-analysis/requirements.md) — inception **NFR3 Privacy and GDPR-aware handling** (primary), NFR5 nutrition-scope context, FR11, FR12. Local derived IDs in this file (`NFR4.x`, `NFR5.1`) are U1 artifact-local namespaces; semantic mapping to inception NFR1–NFR5 is recorded in [traceability.json](traceability.json).
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md)
- [nfr-requirements-questions.md](nfr-requirements-questions.md) (Q2=A, summary confirmed)

## Requirements

| ID | Inherits | Category | Requirement | Control / Target | Notes |
|---|---|---|---|---|---|
| NFR4.1 | NFR4 | Authentication/account scope | MVP has no MealPlanner account or user authentication system. | No login/account data model in U1 baseline. | Future sync/account features are out of MVP scope. |
| NFR4.2 | NFR4 | Data minimization | Store only minimal data needed for plan/list continuity and explicit user choices. | Logical persisted data limited to: weekly plan state, lock snapshot context (ADR-006), shopping list + checked/edited items, selected store, season override, source/fallback/status metadata (Q4=A Functional Design). | No unnecessary personal data collection. |
| NFR4.3 | NFR4 | Location/privacy | Location permission is optional and used only for nearest-store suggestion when granted; manual store selection always available. | Permission requested only for nearest-store suggestion flow; graceful handling when denied/unavailable. | No continuous/background location tracking in MVP baseline. |
| NFR4.4 | NFR4 | Secrets/configuration | No hardcoded secrets, API keys, credentials, or private endpoints in source. | Use configuration/build-time injection only if needed by future adapters; MVP U1 baseline avoids embedded secrets. | Adapter-specific config reviewed in NFR Design/Code Generation. |
| NFR4.5 | NFR4 | Logging/diagnostics privacy | Avoid logging personal data (location coordinates, raw identifiers, personal profile data). Keep diagnostics minimal. | Diagnostic events/logs must not include sensitive personal payloads; user-visible fallback states preferred over verbose sensitive logs. | Optional crash/error reporting hook deferred/decided later with privacy review. |
| NFR5.1 | NFR5 | Medical scope boundary | Nutrition/health-related information is view-only and non-medical. | No diagnoses, treatment recommendations, or personalized medical dosing. Missing nutrition data labeled `Nutrition information unavailable`. | U1 contracts allow missing nutrition data without blocking core flows (BR1.14). |

## Threat/compliance considerations (MVP baseline)
- **GDPR/minimal data:** data minimization and optional location permission align with MVP privacy expectations; formal DPIA/legal review is outside this artifact.
- **Supply/integration risk:** external sources (Sydänmerkki/PRISMA/S-ostoslista) treated as untrusted/integration boundaries with explicit fallback states and no fabricated data.
