# NFR Requirements Questions — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`, C2/C3 + A1 boundary)  
**Stage:** NFR Requirements (construction, full mode)  
**Date:** 2026-11-09

## Sources
- [functional-spec.md](../functional-design/functional-spec.md), [rules.md](../functional-design/rules.md)
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (NFR1–NFR5, OQ1/OQ5–OQ7)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (A1 bounded/tolerant, C2/C3)
- [U1 tech-stack-decisions.md](../../u1-mvp-foundation-skeleton/nfr-requirements/tech-stack-decisions.md)

## Questions
Answer each with `[Answer]:` using A–E or `X. Other (please specify)`.

**Q1. A1 (Sydänmerkki/recipe-source) adapter budget (OQ1 still open): what NFR target should U2 commit to for MVP?**  
A) **Recommended:** bounded per-attempt timeout (e.g., 5–10s), limited safe/idempotent read retries (e.g., 0–2 with backoff) only when live path is confirmed/allowed; any failure/restriction/parse issue maps to typed status and mandatory curated fallback; no blocking UI thread.  
B) Stricter: shorter timeout (e.g., 3s), no retries, immediate fallback.  
C) More tolerant: longer timeout (e.g., 15s) with limited retries.  
X) Other (please specify).

[Answer]: A (accepted by user: "Accept assumptions", 2026-11-09)

**Q2. On-device recipe/planning performance (NFR5): what explicit quantifiable target should U2 artifacts state for MVP?**  
A) **Recommended (MVP baseline):** local catalog query/eligibility/alternatives/normalization for typical MVP curated dataset (e.g., up to ~500 recipes / ~3,500 ingredient lines) completes without ANR or user-visible freeze; operations that may touch A1 show status/progress and do not block unrelated local interactions; exact ms targets deferred to NFR Design/testing.  
B) Set explicit p95 targets now (please specify in `Other` if B): e.g., local eligibility query p95 < 200ms on mid-range device.  
X) Other (please specify).

[Answer]: A (accepted by user: "Accept assumptions", 2026-11-09)

**Q3. Observability for a library/unit inside one Android app (NFR5/quality): what is required for U2?**  
A) **Recommended:** typed result/status/error-reason codes and structured log hooks at adapter/normalization boundaries (no PII/secrets); no external telemetry backend required in MVP; U4/build-and-test own user-visible surfacing and test verification.  
B) Add explicit metrics/tracing contracts now (specify).  
X) Other (please specify).

[Answer]: A (accepted by user: "Accept assumptions", 2026-11-09)

**Q4. Scalability/growth assumptions for MVP U2 (NFR1 platform scope, Android-only, on-device)?**  
A) **Recommended:** no server scalability targets; on-device data growth handled by local storage/query design within MVP dataset assumptions; curated fallback/catalog size and season/nutrition mappings kept bounded; revisit if sync/backend added.  
X) Other (please specify).

[Answer]: A (accepted by user: "Accept assumptions", 2026-11-09)

**Q5. Security/compliance specifics beyond FR12/NFR3 for U2 (no account, minimal data, A1 external calls)?**  
A) **Recommended:** no secrets/endpoints/keys in source; external config only; A1 calls only when OQ1 confirms feasibility/terms/allowed access and only minimal query data; no location permission required by U2 itself (C1 provides context); curated/imported fallback data treated as app content; all adapter outputs mapped (no raw errors across boundaries).  
X) Other (please specify).

[Answer]: A (accepted by user: "Accept assumptions", 2026-11-09)

## Assumption Confirmation
- **A. Accept assumptions** — proceed with Recommended (A) defaults for unanswered/ambiguous items after my confirmation, carrying OQ1/OQ5–OQ7 forward.
- **B. Convert to follow-up questions** — ask follow-ups before generating artifacts.

[Answer]: A (user accepted assumptions; plan approved to proceed with Recommended defaults, 2026-11-09)

If any ambiguity remains after answers, follow-up questions will be added here and must be resolved before Step 5 artifacts are generated.