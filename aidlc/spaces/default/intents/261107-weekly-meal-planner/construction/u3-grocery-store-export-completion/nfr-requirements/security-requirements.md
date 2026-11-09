# Security Requirements — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [requirements.md](../../../inception/requirements-analysis/requirements.md) — inception **NFR3 Privacy and GDPR-aware handling**, **FR7/FR8/FR12**. Semantic mapping in [traceability.json](traceability.json).
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md) (BR3.5–BR3.8, BR3.10).
- [nfr-requirements-questions.md](nfr-requirements-questions.md) (Q1=A, Q3=A, Q4=A).

## Requirements

| ID | Inherits | Category | Requirement | Control / target | Notes |
|---|---|---|---|---|---|
| NFR3.1 | NFR3 | Account scope | U3 introduces no MealPlanner account, authentication, or remote user identity. | Shopping list, store selection metadata, and export operate on local/app state only. | Future sync/account out of MVP scope. |
| NFR3.2 | NFR3 | Data minimization | Persist/process only data needed for grocery/store/export continuity. | List items, checked/edited state, merge/provenance metadata, selected store ref/label metadata, availability status/reason, and export payload derived from current edited list. No unnecessary personal profile data. | U4/local persistence design confirms exact storage schema later. |
| NFR3.3 | NFR3 | Location privacy boundary | Location data is optional/purpose-limited and not required by U3 core logic. | C5 contracts accept typed suggestion states (`DENIED|UNAVAILABLE|UNKNOWN|NOT_FOUND`/equivalent) and always allow manual store selection; U3 must not store raw GPS coordinates or request permissions. | Permission prompt/purpose text is Android/U4 responsibility. |
| NFR3.4 | NFR3 | Secrets/configuration | No hardcoded secrets, API keys, tokens, or private endpoints in U3 source/contracts. | PRISMA/S-ostoslista integration config, if any, is injected by adapters outside committed source; MVP fallback paths require no secrets. | Adapter implementation reviewed before enabling live calls (OQ2–OQ4). |
| NFR3.5 | NFR3 | Diagnostics/privacy-safe logging | Diagnostics and status reasons must avoid sensitive personal payloads. | No raw location coordinates, contact data, or unnecessary identifiers in logs/errors; use typed reason codes/messages safe for U4 display/tests. | External telemetry not required in MVP. |
| NFR3.6 | NFR3 | External boundary safety | Treat PRISMA/S-ostoslista/share targets as untrusted integration boundaries. | Validate/map adapter outputs to typed statuses; never claim stock quantity or guaranteed availability (FR8); export fallback must not leak secrets and must use current edited list only. | No fabrication of presence/export success. |