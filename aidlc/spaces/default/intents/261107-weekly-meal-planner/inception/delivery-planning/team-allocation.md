# Team Allocation — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Stage:** Delivery Planning (inception)  
**Date:** 2026-11-08  

## Definitions

A **Bolt** is one planned build pass over a slice of the work (one or more Units of Work) with a Definition of Done, a confidence hypothesis, and ownership.

A **mob** is the team (or AI agent team composition) that owns and executes a Bolt.

## Team formation context

Team Formation (stage 1.5) was **skipped** for MVP scope. Therefore, all Bolts are executed by the AI delivery executor.

## Bolt-to-mob allocation

| Bolt | Unit(s) | Mob / executor | Notes |
|---|---|---|---|
| B1 | `u1-mvp-foundation-skeleton` | `aidlc-developer-agent` (AI) | Foundation/integrated slice; establishes baseline contracts and fallback patterns. |
| B2 | `u2-recipe-planning-completion` | `aidlc-developer-agent` (AI) | Depends on B1/U1; may run OQ1 investigation spike in parallel only if DAG contracts unchanged (Q4=B). |
| B3 | `u3-grocery-store-export-completion` | `aidlc-developer-agent` (AI) | Depends on B1/U1 and B2/U2; may run OQ2–OQ4 investigation spikes in parallel only if DAG contracts unchanged (Q4=B). |
| B4 | `u4-android-ui-experience` | `aidlc-developer-agent` (AI) | Depends on B1–B3; consumes domain contracts (U4 owns no domain business-rule contracts). |

## Program Board analog

Not applicable — team count is 1 (AI executor), execution is mostly serial (Q4=B), and the Unit DAG constrains ordering.

## Skills / capacity notes

- Required skills: Android/Kotlin, modular app architecture, adapter/fallback design, testing (custom posture per team practices), privacy-aware implementation.
- External access/approvals (OQ1–OQ4, OQ8) require human confirmation before affected Bolt behavior starts (Q5=B); owners/lead times TBD in `external-dependency-map.md`. Mandatory fallbacks prevent core-flow blockage.