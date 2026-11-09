# NFR Design Questions — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`)  
**Stage:** NFR Design (construction, full mode)  
**Date:** 2026-11-09

## Focus
Design patterns for C2/C3 domain logic and A1 adapter boundary: resilience/fallback (BR2.1, NFR2.x), performance (on-device, non-blocking A1, NFR5.x), security (default-off auditable A1 gate, mapped boundaries, input validation, NFR3.x), observability (typed status/log hooks, NFR5.4–5.6), scalability (on-device bounded, NFR1.x), logical components/testability (NFR4.x).

## Questions
Answer with `[Answer]:` A–E or `X. Other`.

**Q1. A1 resilience pattern (beyond typed fallback): which design is preferred?**  
A) **Recommended:** adapter port with decorator/chain for gate check (default-off/OQ1), bounded timeout, limited idempotent retries, parse/result mapping, and curated-fallback resolver; all outcomes return typed `RecipeSourceAdapterResult`; no circuit breaker service required for MVP library.  
X) Other (please specify).

[Answer]: A (plan approved, defaults accepted 2026-11-09)

**Q2. C2 local computation/caching: any caching beyond in-memory call scope?**  
A) **Recommended:** no persistent cache in U2 domain layer for MVP; pure/deterministic normalization and evaluation; optional short-lived in-memory memoization only if NFR Design/implementation proves needed for repeated catalog scans (bounded by MVP dataset). U1/U4 own persistence concerns.  
X) Other (please specify).

[Answer]: A (plan approved, defaults accepted 2026-11-09)

**Q3. Observability implementation shape (typed hooks)?**  
A) **Recommended:** define a small diagnostics/log hook interface (or adapter) injected into A1/C2 boundaries; default no-op/safe release behavior; structured fields limited to operation, status/reason code, counts, timing bucket (no PII/secrets/payloads).  
X) Other (please specify).

[Answer]: A (plan approved, defaults accepted 2026-11-09)

## Assumption Confirmation
- **A. Accept assumptions** — proceed with Recommended defaults for unanswered items.

[Answer]: A (plan approved, defaults accepted 2026-11-09)