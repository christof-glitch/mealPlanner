# External Dependency Map — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Stage:** Delivery Planning (inception)  
**Date:** 2026-11-08  

## Policy (Q5=B)

Confirm access/approvals **before starting affected Bolts** (may delay). Owners and lead times are **TBD** unless confirmed. **Mandatory fallbacks remain required** so core MVP flow can degrade safely: Sydänmerkki curated/imported fallback, manual store/Unknown/manual checklist, and share sheet/copyable text export. No external blocker should permanently stop the MVP core flow.

Parallel investigation spikes for OQ1–OQ4 are allowed only when they do not change DAG contracts (Q4=B).

## Dependencies

| ID | Dependency | Type | Owner | Expected lead time | Affects Bolt(s) | Needed before | Fallback if slips / unavailable | Status |
|---|---|---|---|---|---|---|---|---|
| OQ1 | Sydänmerkki recipe access method, terms of use, and feasible data/query strategy (`sydanmerkki.fi`) | External data/API/terms | TBD (human/stakeholder to confirm) | TBD | B1 (baseline adapter boundary), B2 (completion) | Starting affected Sydänmerkki live-query behavior in B1/B2 | Use curated/imported Sydänmerkki-compatible fallback dataset; show clear source/fallback status; do not fabricate Sydänmerkki compliance (FR4, NFR2) | Open |
| OQ2 | PRISMA nearest-store/location data method (and permissions UX constraints) | External data/API/platform | TBD | TBD | B3 | Starting affected PRISMA store-location integration behavior in B3 | Location permission optional; manual store selection always available; nearest-store suggestion is best-effort when location allowed (FR7) | Open |
| OQ3 | PRISMA ingredient/product presence or name-match data method | External data/API/terms | TBD | TBD | B3 | Starting affected PRISMA presence-check integration behavior in B3 | Best-effort **name/presence** checks only (not stock quantity/guaranteed availability); show present/not found/Unknown with reason and manual checklist fallback (FR8, C6 constraints) | Open |
| OQ4 | S-ostoslista official-compatible integration/import method or supported format | External integration/format/approval | TBD | TBD | B3 (and optional non-contract-changing spike earlier) | Starting official-compatible S-ostoslista integration behavior in B3 | MVP must always provide Android share sheet and/or copyable text export with clear instructions (FR10, C4 export fallback) | Open |
| OQ5 | Nutrition data source/fields for recipe-level cues and Nutrition Balance Cue | Data/feasibility (product/data) | TBD | TBD | B1 baseline, B2 completion | Defining/using nutrition fields beyond unavailable labeling | Show nutrition cues when available; label `Nutrition information unavailable` when missing; Nutrition Balance Cue is view-only, non-medical, and labels partial data (FR6) | Open (fallback defined) |
| OQ6 | Canonical Finnish market seasonal ingredient/calendar or tag mapping | Data/curation | TBD | TBD | B1 baseline, B2 completion | Relying on authoritative seasonal source beyond metadata/tags/curated mapping | Use recipe metadata/tags/curated mapping; allow `Season fit unknown`; provide explicit season override/default/reset (FR5) | Open (fallback defined) |
| OQ7 | Curated/imported Sydänmerkki-compatible fallback dataset scope, licensing/attribution, and update process | Data/licensing/curation | TBD | TBD | B1, B2 | Shipping curated fallback as production data | Keep fallback mandatory for MVP resilience; do not include data without confirmed rights/attribution; use minimal safe seed data only if rights/process confirmed, otherwise mark as blocker for that dataset and preserve explicit unavailable/fallback behavior | Open |
| OQ8 | Stakeholder review cadence for major integration and fallback decisions | Process/approval | TBD | TBD | B1–B4 (gates) | Finalizing decisions that need stakeholder sign-off | Use AI-DLC stage approval gates; record assumptions and fallbacks in artifacts; escalate specific approvals before affected Bolt behavior (Q5=B) | Open |

## Notes

- **Access/approvals before affected Bolts (Q5=B):** Starting an affected Bolt’s integration-specific behavior may be delayed until relevant access/approvals are confirmed. This does not remove fallback requirements.
- **No-fabrication constraints:** Sydänmerkki compliance, PRISMA stock/guaranteed availability, and nutrition facts must not be fabricated. Use explicit statuses (`Unknown`, `Nutrition information unavailable`, source/fallback labels).
- **Contract stability for spikes:** OQ1–OQ4 investigation spikes must not change Unit DAG contracts/ownership boundaries; contract changes follow R-01–R-03 carry-forward and normal approval flow.
