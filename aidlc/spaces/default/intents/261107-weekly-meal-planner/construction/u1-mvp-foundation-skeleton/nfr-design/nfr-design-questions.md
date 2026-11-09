# NFR Design Questions — U1 `u1-mvp-foundation-skeleton`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Stage:** NFR Design (construction, per-unit)  
**Unit:** `u1-mvp-foundation-skeleton` — MVP foundation + integration slice (Android app)  
**Date:** 2026-11-08  
**Answer mode:** Guide me

Answer by filling each `[Answer]:` tag. U1 is a single-user on-device Android MVP foundation (no backend). Designs should be architectural patterns/strategies, not implementation code.

## Batch 1 — Core design decisions

### Q1. UI toolkit (deferred in NFR Requirements)
Which UI toolkit should U1/U4 baseline target in design?

A. **Jetpack Compose** (recommended for greenfield MVP): modern declarative UI, easier typed state rendering for explicit fallback/unknown/stale states; Compose-specific details refined in Code Generation.  
B. **Android Views/XML**: conventional approach; more boilerplate for dynamic state-heavy screens.  
C. Keep toolkit-agnostic in NFR Design artifacts and record final choice at Code Generation kickoff.  
X. Other (please specify)

[Answer]: A

### Q2. Local persistence mechanism (must satisfy NFR1.1/NFR4.2)
How should local persistence be designed for plan state, ADR-006 lock snapshot context, shopping list/edits, selected store, season override, and source/fallback/status metadata?

A. **Room + DataStore (or equivalent split)** (recommended): Room/structured storage for plan/list/snapshot entities and DataStore/preferences for small settings (selected store, season override) — exact split refined in Code Generation; schema supports explicit states and minimal data.  
B. **Single structured local store** (e.g., Room only, or document store abstraction) for all persisted MVP data.  
C. **File/JSON abstraction only** for MVP simplicity, with migration path noted (higher risk for query/consistency).  
X. Other (please specify)

[Answer]: A

### Q3. Adapter resilience for Sydänmerkki/PRISMA (implemented by U2/U3, contracts in U1)
What resilience pattern should NFR Design specify for external adapters?

A. **Bounded timeout + typed fallback result + no core-flow blocking** (recommended): each adapter call has a bounded timeout (value deferred to config), returns typed available/cached/unknown/fallback/error state; UI/core flows use local/manual fallback; optional limited retry only for safe idempotent reads, never fabricating data.  
B. Add circuit-breaker/bulkhead patterns now (may be heavier than MVP needs).  
C. Simple try/catch with fallback, no explicit timeout/retry policy in design.  
X. Other (please specify)

[Answer]: A

### Q4. Security/privacy controls in design
Which controls should be explicit in security design?

A. **MVP baseline controls** (recommended): no account/auth; data minimization per persisted set; optional location permission only for nearest-store suggestion with graceful denial; no hardcoded secrets/endpoints/keys; privacy-safe logging redaction policy (no coordinates/raw personal identifiers); Android platform storage defaults; optional crash reporting hook deferred with privacy review; nutrition view-only/non-medical.  
B. Add stronger controls now (encryption at rest for all local state, explicit local-data deletion UX, stricter audit logging).  
C. Minimal: platform defaults + high-level privacy notes only.  
X. Other (please specify)

[Answer]: A

### Q5. Observability / diagnosability (on-device MVP)
What observability design fits U1?

A. **Lightweight on-device observability** (recommended): typed user-visible status/reason/next-action contracts (A1–A3), structured local diagnostic events with privacy-safe fields, optional crash/error reporting integration point deferred with privacy review; no distributed tracing/backend SLOs. Performance measurement method deferred to build-and-test.  
B. Integrate a specific crash/analytics SDK now (specify).  
C. User-visible states only; no local diagnostic event design.  
X. Other (please specify)

[Answer]: A


## Analysis
Recorded answers: `Q1=A, Q2=A, Q3=A, Q4=A, Q5=A`. No ambiguity or contradiction found. Designs will target Jetpack Compose baseline, Room + DataStore split (refined in Code Generation), bounded timeout + typed fallback + no core-flow blocking for adapters, MVP security/privacy baseline controls, and lightweight on-device observability with privacy-safe diagnostics and deferred optional crash reporting.

## Consolidated Summary Confirmation

Decision: Confirm consolidated NFR Design answers for `u1-mvp-foundation-skeleton` before generating `performance-design.md`, `security-design.md`, `scalability-design.md`, `reliability-design.md`, `observability-design.md`, `logical-components.md`, and `traceability.json`.

Options:
- Looks correct
- Request changes

[Answer]: Looks correct 
