# NFR Requirements Questions — U1 `u1-mvp-foundation-skeleton`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Stage:** NFR Requirements (construction, per-unit)  
**Unit:** `u1-mvp-foundation-skeleton` — MVP foundation + integration slice (Android app)  
**Date:** 2026-11-08  
**Answer mode:** Guide me

Answer by filling each `[Answer]:` tag. U1 is an Android app foundation slice (no backend service in MVP). Where targets are uncertain for greenfield MVP, choose a reasonable default and we’ll document assumptions.

## Batch 1 — NFR scope and targets

### Q1 Performance targets (NFR3) for U1 foundation
What performance targets should U1 baseline use?

A. MVP reasonable defaults: local plan/shopping-list interactions feel responsive (target < 500 ms for common local state updates on typical mid-range Android device); external adapter calls (Sydänmerkki/PRISMA when implemented by downstream units) use bounded timeouts with explicit fallback (exact timeout values deferred to NFR Design/adapter config, but must not block core local flows).  
B. Stricter targets (specify p95 latency, device class, startup time).  
C. No numeric targets now; document qualitative “responsive for MVP” and defer measurable targets to NFR Design/testing.  
X. Other (please specify)

[Answer]: A

### Q2 Security/privacy requirements (NFR4, NFR5 scope)
What security/privacy baseline should U1 implement?

A. MVP baseline: no MealPlanner account/auth; store only minimal data needed for plan/list continuity (per Functional Design logical persistence); location permission optional and used only for nearest-store suggestion when granted; no hardcoded secrets/endpoints/keys; avoid logging personal data (location, raw identifiers) and keep diagnostics minimal; nutrition/health-related data treated as view-only non-medical context (no diagnoses/treatment).  
B. Add stronger controls now (encryption at rest for all local state, explicit data retention deletion UX, stricter logging policy details).  
C. Minimal: rely on Android platform defaults and document privacy requirements for later hardening.  
X. Other (please specify)

[Answer]: A

### Q3 Reliability/local continuity (NFR1, NFR2)
How strong should U1’s local continuity and fallback behavior be?

A. Core planning and shopping-list baseline usable with previously available/local data where possible; all external/data-source outcomes use typed explicit states (available/cached/stale/unknown/fallback) with next action where applicable; missing integrations degrade gracefully (curated/imported/local fallback or manual path); no fabricated data.  
B. Require full offline-first behavior for all U1 flows including first-run without any seed data.  
C. Best-effort local caching only; document known gaps for first-run/offline seed data.  
X. Other (please specify)

[Answer]: A

### Q4 Scalability/observability applicability for U1 (Android MVP slice)
How should we treat scalability and observability for this unit?

A. Mark service-style scalability as `N/A`/limited for single-user on-device Android MVP; capture only relevant app concerns (local data growth reasonable for weekly plan + lists, no multi-tenant/load targets). Observability: lightweight MVP baseline (structured local diagnostics/error states, optional crash/error reporting hook deferred/decided later, user-visible fallback states required; no distributed tracing/SLO backend).  
B. Define full observability stack and scalability targets now (specify).  
C. Minimal documentation: `N/A` for most scalability/observability with brief rationale.  
X. Other (please specify)

[Answer]: A

### Q5 Tech stack decisions for U1 baseline
What tech stack baseline should NFR Requirements record for this greenfield Android MVP?

A. Kotlin + modern Android app baseline (Gradle Kotlin DSL, AndroidX, Jetpack Compose UI or Views to be confirmed in NFR Design/Code Generation; local persistence mechanism deferred but must satisfy NFR1/NFR4; coroutines/Flow acceptable for async boundaries), with clean modular boundaries aligned to C1–C6. Record undecided UI toolkit/persistence library as explicit decisions-to-confirm in NFR Design.  
B. Specify exact stack now (UI toolkit, DI, persistence, networking, testing frameworks).  
C. Defer tech stack decisions entirely to NFR Design/Code Generation except “Android/Kotlin MVP”.  
X. Other (please specify)

[Answer]: A


## Analysis
Recorded answers: `Q1=A, Q2=A, Q3=A, Q4=A, Q5=A`. No ambiguity or contradiction found. Targets appropriate for greenfield Android MVP foundation; exact adapter timeouts, UI toolkit/persistence library, and optional crash-reporting hook remain explicit decisions/defers for NFR Design/Code Generation.

## Consolidated Summary Confirmation

Decision: Confirm consolidated NFR Requirements answers for `u1-mvp-foundation-skeleton` before generating `performance-requirements.md`, `security-requirements.md`, `scalability-requirements.md`, `reliability-requirements.md`, `observability-requirements.md`, `tech-stack-decisions.md`, and `traceability.json`.

Options:
- Looks correct
- Request changes

[Answer]: Looks correct 
