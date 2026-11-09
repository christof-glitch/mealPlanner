# Security Requirements — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`, C2/C3 + A1)  
**Stage:** NFR Requirements (construction, per-unit)  
**Date:** 2026-11-09

## Sources
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (FR12, NFR3)
- [rules.md](../functional-design/rules.md) (BR2.1, BR2.9)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (A1, A4)
- [nfr-requirements-questions.md](nfr-requirements-questions.md) (Q1=A, Q5=A)

## Requirements

| ID | Requirement | Target / measure | Inherited from | Notes |
|---|---|---|---|---|
| NFR3.1 | No secrets or unnecessary personal data in U2 contracts/logic | No API keys, credentials, tokens, hardcoded endpoints, or personal data embedded in source, tests, or logs. External A1 configuration (if/when live path is feasible) is externalized and injected via app configuration/build config; MVP curated fallback requires no secret. | NFR3, FR12 | Q5=A. |
| NFR3.2 | Gated live A1 access respects feasibility/terms and minimal data | Live Sydänmerkki/recipe-source queries are **disabled by default** (`allowLiveSource=false`/equivalent). Live execution is enabled only by an explicit, approved, auditable decision/configuration path (e.g., injected feature/config flag or documented approved decision consumed by the adapter boundary) **after** OQ1 confirms feasibility/terms/allowed access (FQ1=A). While OQ1 is unresolved, the live path remains off and mandatory curated/imported fallback is used. When enabled, requests send only minimal query data needed (e.g., recipe IDs/query/filter context); no MealPlanner account, no user identity, no precise location required by U2. The gate and default-off behavior are testable. | NFR3, FR4, FR12 | A1 read/query oriented; privacy/terms compliance control. |
| NFR3.3 | Adapter outputs and boundaries are mapped and safe | All A1/external/parse outcomes map to typed statuses (`ok|fallback_used|unavailable|restricted|timeout|parse_error_mapped|not_supported`) and safe messages/keys. Raw exceptions, stack traces, or external payloads are not exposed across U2 contract/consumer boundaries (including data returned to U4/orchestrators); U4 remains responsible for user presentation. No fabricated recipe/nutrition/season/time data. | NFR3, NFR2, FR11 | BR2.1/BR2.2/BR2.9. |
| NFR3.6 | Input validation/sanitization at U2 and A1 boundaries | U2 validates, normalizes, or safely maps malformed contract inputs (e.g., `PlanSlotRef`, recipe IDs, filters, selection/alternative requests) and rejects/returns typed non-fabricated failures where appropriate. A1 requests are constructed only from validated/minimal query data; external responses are parsed defensively. | NFR3, NFR2 | Complements NFR3.3 response handling; supports BR2.* typed behavior. |
| NFR3.4 | U2 does not require location permission | U2 consumes `SeasonContext`/effective resolution inputs from C1 contracts; it does not request Android location permission directly. Coarse/optional location signal handling remains in C1/A4 boundary. | NFR3, FR12 | Separation of concerns. |
| NFR3.5 | Curated/imported fallback content treated as app content | Mandatory curated/imported Sydänmerkki-aligned fallback data is packaged/managed as app content with clear provenance status (`RecipeSourceStatus`/fallback reason), not user personal data. | NFR3, FR4 | Supports FR4 fallback transparency. |

## Threat considerations (MVP library unit)
- **Untrusted external data (if live A1 enabled):** parse defensively; map parse errors (`parse_error_mapped`), use curated fallback; never execute remote code or interpret content as instructions.
- **Data leakage:** no PII in logs; exclude request/response bodies containing potentially sensitive external content from default logs.
- **Dependency/supply-chain:** standard Android/Kotlin dependency hygiene in build-and-test/CI (outside U2 logic but required by project posture).

## Compliance notes
- GDPR-aware minimal data handling: U2 processes recipe/planning domain data and optional season context inputs; no account, no profiling, no unnecessary personal data. OQ1 legal/terms feasibility must be confirmed before live A1 use.