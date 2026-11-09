# Delivery Planning Questions — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Stage:** Delivery Planning (inception)  
**Date:** 2026-11-08  

## Sources

- [requirements.md](../requirements-analysis/requirements.md)
- [stories.md](../user-stories/stories.md)
- [mockups.md](../refined-mockups/mockups.md)
- [components.md](../domain-design/components.md)
- [unit-of-work.md](../units-generation/unit-of-work.md)
- [unit-of-work-dependency.md](../units-generation/unit-of-work-dependency.md)
- [unit-of-work-story-map.md](../units-generation/unit-of-work-story-map.md)
- [contract-summary.md](../contract-design/contract-summary.md)
- [team-practices.md](../practices-discovery/team-practices.md)

Context: A **Bolt** is one build pass over a planned slice of the work (one or more Units of Work), ending with a clear Definition of Done and something that runs or can be verified. The Unit dependency DAG is strict: `u1-mvp-foundation-skeleton` → `u2-recipe-planning-completion` → `u3-grocery-store-export-completion` → `u4-android-ui-experience` (U3 depends on U2, so Construction is effectively serial for these units). Team formation was skipped for MVP scope, so all Bolts are executed by `aidlc-developer-agent` (AI). External integration uncertainty remains (OQ1–OQ4: Sydänmerkki access, PRISMA store/presence data, S-ostoslista official method), but mandatory fallbacks exist.

## Q1. [Delivery] What should we build first?

The DAG already constrains order (`u1 → u2 → u3 → u4`). Within that constraint, should we treat this as: risk-first (prove integrations/fallback boundaries early), value-first (get user-visible planning flow early), a thin end-to-end slice first (a **walking skeleton** — the smallest working integrated slice that proves the pieces connect, with a real check), or a mix?

A. Risk-first within DAG order — prioritize integration/fallback proof points and explicit unknown handling early (recommended given OQ1–OQ4)  
B. Walking-skeleton-first — make U1 the explicit walking skeleton (integrated slice + real end-to-end check/human checkpoint), then continue U2→U3→U4  
C. Value-first — prioritize the most user-visible meal-planning flow as soon as each unit allows  
D. Mix — describe where each approach applies  
X. Other (please specify)

[Answer]: C — Value-first: prioritize the most user-visible meal-planning flow as soon as each unit allows (while respecting DAG and mandatory fallbacks).

## Q2. [Delivery] Should we score/rank work with a formal model?

Should the plan include a lightweight scoring rationale (for example WSJF-style: value/urgency/risk reduction against size), or a clear qualitative rationale is enough for this 4-unit MVP?

A. Qualitative rationale is enough (recommended for 4-unit serial MVP)  
B. Lightweight WSJF-style scoring (value, urgency/risk reduction, size)  
C. Formal scoring with explicit weights  
X. Other (please specify)

[Answer]: B — Lightweight WSJF-style scoring (value, urgency/risk reduction, size).

## Q3. [Delivery] How big should one Bolt be?

Given 4 units and the strict DAG, how should Bolts be sized?

A. One Bolt per Unit (4 Bolts: B1=U1, B2=U2, B3=U3, B4=U4) — clear boundaries/verification (recommended)  
B. Bundle related units into fewer Bolts (describe which)  
C. Thin slices across units (higher coordination overhead for MVP)  
X. Other (please specify)

[Answer]: A — One Bolt per Unit (4 Bolts: B1=U1, B2=U2, B3=U3, B4=U4).

## Q4. [Delivery] Can Bolts be built at the same time, or one after another?

The Unit DAG requires U2 after U1, U3 after U1 and U2, U4 after U1–U3. Is there any safe parallel work you want planned (for example design spikes/investigations) outside the DAG, or should execution be one Bolt after another?

A. One after another, following DAG (recommended for MVP/single AI executor)  
B. Mostly serial, but allow parallel investigation spikes for OQ1–OQ4 where they don’t change DAG contracts  
C. Attempt parallel Bolts where possible (not supported by current DAG)  
X. Other (please specify)

[Answer]: B — Mostly serial, but allow parallel investigation spikes for OQ1–OQ4 where they don’t change DAG contracts.

## Q5. [Delivery] External blockers and dependencies

Anything outside this team likely to hold us up (APIs, data, approvals, hand-offs)? For each: owner, expected lead time, which Bolt it blocks, and fallback if it slips. Known candidates: Sydänmerkki access/terms/data (OQ1), PRISMA store-location (OQ2), PRISMA presence/name-match (OQ3), S-ostoslista official method/format (OQ4), stakeholder review cadence (OQ8).

A. Treat OQ1–OQ4 as external/feasibility dependencies with **mandatory fallbacks already required**; no blocker should stop MVP core flow — investigate during relevant Bolt and degrade safely (recommended)  
B. Confirm access/approvals before starting affected Bolts (may delay)  
C. List specific blockers with owners/lead times/fallbacks (describe in Other)  
X. Other (please specify)

[Answer]: B — Confirm access/approvals before starting affected Bolts (may delay). Owners/lead times TBD; mandatory fallbacks remain required (Sydänmerkki curated/imported fallback, manual store/Unknown/manual checklist, share sheet/copyable text export), so core MVP flow can degrade safely if access slips.

## Q6. [Delivery] Biggest worry to tackle early

What worries you most about this build?

A. Third-party integration uncertainty (Sydänmerkki/PRISMA/S-ostoslista) — handle with adapter boundaries + fallbacks + early investigation (recommended)  
B. Contract/DTO ambiguity causing rework (carry R-01/R-02/R-03 into Functional Design before dependent Construction)  
C. Android MVP complexity/UX polish across all flows  
D. Privacy/data-minimization compliance in implementation details  
X. Other (please specify)

[Answer]: B — Contract/DTO ambiguity causing rework (carry R-01/R-02/R-03 into Functional Design before dependent Construction).

## Consolidated Summary Confirmation

- **Q1 [Build first]:** C — Value-first: prioritize the most user-visible meal-planning flow as soon as each unit allows (respecting DAG and mandatory fallbacks).
- **Q2 [Scoring/ranking]:** B — Lightweight WSJF-style scoring (value, urgency/risk reduction, size).
- **Q3 [Bolt size]:** A — One Bolt per Unit (4 Bolts: B1=U1, B2=U2, B3=U3, B4=U4).
- **Q4 [Parallel vs serial]:** B — Mostly serial, but allow parallel investigation spikes for OQ1–OQ4 where they don’t change DAG contracts.
- **Q5 [External blockers]:** B — Confirm access/approvals before starting affected Bolts (may delay). Owners/lead times TBD; mandatory fallbacks remain required.
- **Q6 [Biggest worry]:** B — Contract/DTO ambiguity causing rework (carry R-01/R-02/R-03 into Functional Design before dependent Construction).

Does this all look correct before I generate the Delivery Planning artifacts?
- Looks correct
- Request changes

[Answer]: Looks correct
