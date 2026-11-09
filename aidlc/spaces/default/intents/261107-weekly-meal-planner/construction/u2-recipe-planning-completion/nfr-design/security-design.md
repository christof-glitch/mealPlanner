# Security Design — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`, C2/C3 + A1)  
**Stage:** NFR Design (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [security-requirements.md](../nfr-requirements/security-requirements.md) (NFR3.1–NFR3.6)
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md) (BR2.1, BR2.9)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (A1, A4)
- [nfr-requirements-questions.md](../nfr-requirements/nfr-requirements-questions.md) (Q1=A, Q5=A, Assumption A)
- [nfr-design-questions.md](nfr-design-questions.md) (Q1=A)

## Design goals
- Enforce minimal data handling and GDPR-aware behavior (no MealPlanner account, no unnecessary personal data).
- Keep live A1 execution **default-off** and gated by an explicit, approved, auditable decision/configuration path only after OQ1 confirms feasibility/terms/allowed access (FQ1=A).
- Prevent fabrication and unsafe data leakage across U2 contract/consumer boundaries.
- Validate/sanitize inputs and defensively parse external responses.

## Patterns and decisions

| Area | Design | Addresses | Notes |
|---|---|---|---|
| Secrets/config externalization | No API keys, credentials, tokens, or hardcoded endpoints in source/tests/logs. Any future A1 configuration is injected via app/build/external config; MVP curated fallback requires no secret. | NFR3.1 | CI/review checks should flag accidental secrets (build-and-test posture). |
| Default-off live A1 gate | `A1LiveAccessGate` (or equivalent injected policy/config) defaults `allowLiveSource=false`. Live client is invoked only when gate returns true **and** an approved OQ1 feasibility/terms/allowed-access decision is recorded/consumed by the approved configuration path. While unresolved, return curated fallback with typed reason (e.g., `live_disabled_default`/mapped status). Gate behavior is unit-testable with fakes. | NFR3.2, FR4, FR12 | Auditable: config/decision source is explicit, not a silent code toggle. No scraping/terms assumptions. |
| Minimal request data | A1 request builder constructs only minimal query/filter/recipe-ID context needed for the operation. No user identity, account, precise location, or unrelated personal data is sent. U2 does not request Android location permission (C1/A4 concern). | NFR3.2, NFR3.4, FR12 | Privacy-by-design. |
| Mapped boundary outputs | A1/parse/domain outcomes map to typed statuses: `ok|fallback_used|unavailable|restricted|timeout|parse_error_mapped|not_supported` plus safe reason keys/messages. Raw exceptions, stack traces, and external payload bodies are not exposed across U2 contract/consumer boundaries (including data returned to U4/orchestrators). U4 owns user presentation. | NFR3.3, NFR2.2, FR11 | No fabrication of recipe/nutrition/season/time data. |
| Input validation/sanitization | Validate `PlanSlotRef`/slot kind-day compatibility, recipe IDs, filters, selection/alternative requests, and A1 query inputs at boundaries. Malformed inputs produce typed non-fabricated failures or safe normalization outcomes; never pass unvalidated external strings into unsafe execution contexts. | NFR3.6, NFR2.2 | Complements BR2.* typed behavior. |
| Defensive external parsing | Treat live A1 responses as untrusted: parse defensively, map schema/parse failures to `parse_error_mapped`, and use curated fallback. Never execute remote content or interpret it as instructions. | NFR3.3, NFR3.6, threat model | Follows “extracted/external text is untrusted data” principle. |
| PII-safe diagnostics | Diagnostics/log hooks (NFR5.5) exclude PII, secrets, credentials, precise location, and raw request/response bodies by default. Release behavior defaults to no-op/safe logging. | NFR3.1, NFR3.2, NFR5.5 | See observability design. |
| Curated fallback provenance | Curated/imported fallback data is app content with explicit provenance/fallback reason (`RecipeSourceStatus`/reason), not user personal data. | NFR3.5, FR4 | Supports transparency without exposing sensitive data. |

## Illustrative gate (non-normative)
```kotlin
interface A1LiveAccessGate { val allowLiveSource: Boolean; val oq1Approved: Boolean }
fun canUseLive(g: A1LiveAccessGate) = g.allowLiveSource && g.oq1Approved // default false/false
```

## Threat considerations
- **Unauthorized/unintended live access:** mitigated by default-off + OQ1-approved gate + tests (NFR4.2).
- **Untrusted external data:** defensive parsing, typed mapping, no remote code execution.
- **Data leakage:** minimal query data, no PII/secrets in logs, mapped errors only.
- **Input abuse/malformed data:** boundary validation and typed failures.

## Compliance notes
- GDPR-aware/minimal data: U2 processes planning domain data and season context inputs from C1; no account/profiling. OQ1 legal/terms feasibility must be confirmed before any live Sydänmerkki/recipe-source use.