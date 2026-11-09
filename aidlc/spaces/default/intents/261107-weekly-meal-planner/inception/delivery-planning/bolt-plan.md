# Bolt Plan — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Stage:** Delivery Planning (inception)  
**Date:** 2026-11-08  

## Definitions

A **Bolt** is one planned build pass over a slice of the work (one or more Units of Work) with a Definition of Done (what must be true for the Bolt to count as done), a confidence hypothesis (what shipping this Bolt will prove), and ownership. Bolts are planned here; the engine’s runtime iteration follows the Unit DAG in `unit-of-work-dependency.md`.

A **walking skeleton** is the smallest working integrated slice that proves the main parts of the system connect end to end, verified by a real project check and a human checkpoint when the walking-skeleton ceremony applies.

## Sources

- [delivery-planning-questions.md](delivery-planning-questions.md) (Q1=C, Q2=B, Q3=A, Q4=B, Q5=B, Q6=B)
- [unit-of-work.md](../units-generation/unit-of-work.md), [unit-of-work-dependency.md](../units-generation/unit-of-work-dependency.md), [unit-of-work-story-map.md](../units-generation/unit-of-work-story-map.md)
- [components.md](../domain-design/components.md)
- [contract-summary.md](../contract-design/contract-summary.md)
- [requirements.md](../requirements-analysis/requirements.md), [stories.md](../user-stories/stories.md)
- [team-practices.md](../practices-discovery/team-practices.md)

## Sequencing constraints (DAG)

```yaml
units:
  - name: u1-mvp-foundation-skeleton
    depends_on: []
  - name: u2-recipe-planning-completion
    depends_on: [u1-mvp-foundation-skeleton]
  - name: u3-grocery-store-export-completion
    depends_on: [u1-mvp-foundation-skeleton, u2-recipe-planning-completion]
  - name: u4-android-ui-experience
    depends_on: [u1-mvp-foundation-skeleton, u2-recipe-planning-completion, u3-grocery-store-export-completion]
```

Execution is **mostly serial** (Q4=B). Parallel investigation spikes for OQ1–OQ4 are allowed only when they do not change DAG contracts. Bolt order below respects the DAG.

## Ordered Bolts

| Bolt | Unit(s) | Walking-skeleton marker | Owner (mob) |
|---|---|---|---|
| B1 | `u1-mvp-foundation-skeleton` | **Candidate integrated slice.** U1 is the first resolved DAG unit and smallest end-to-end integrated foundation (preferences/season context, minimal recipe/eligibility, plan/slot + lock snapshot, shopping list generate/re-resolve/edit basics, share/copy fallback, stale warning, thin UI path). If Construction walking-skeleton ceremony applies, B1/U1 provides the slice to verify with a real end-to-end check and human checkpoint. | `aidlc-developer-agent` (AI) |
| B2 | `u2-recipe-planning-completion` | No | `aidlc-developer-agent` (AI) |
| B3 | `u3-grocery-store-export-completion` | No | `aidlc-developer-agent` (AI) |
| B4 | `u4-android-ui-experience` | No | `aidlc-developer-agent` (AI) |

### B1 — `u1-mvp-foundation-skeleton` (Foundation & Integrated Slice)

- **Definition of Done:** U1 baseline contracts and thin end-to-end path are implemented per `unit-of-work.md` and `contract-summary.md` (C1 baseline; C2/C3/C4/C5/C6 baselines as defined), with explicit fallback states and no fabricated data. Plan structure (FR1), slot selection/lock snapshot (ADR-006 IDs+plan context), shopping list generate/regenerate explicit trigger + stale warning semantics, basic merge/edit/check, and share/copy fallback are runnable. Relevant tests pass; existing tests remain green. Contract/DTO minimum shapes required by dependent units are defined or explicitly deferred with named owner + minimum fields/status values before B2/B3 rely on them (R-01 carry-forward).
- **Confidence hypothesis:** Shipping B1 will prove the core on-device loop and integration boundaries can work together (plan → lock snapshot → re-resolve ingredients → list → export fallback) with explicit state/fallback behavior, reducing uncertainty about foundational architecture and contract seams.
- **Expected demo:** A user can view a 7-day plan structure, select/lock recipes (minimal eligible set), generate a shopping list from the lock snapshot, edit/check items, see stale warning when plan changes (no auto-regeneration), and export via share/copy fallback. Missing/integration data appears as explicit fallback/Unknown states.

### B2 — `u2-recipe-planning-completion` (Recipe & Planning Completion)

- **Definition of Done:** RecipeCatalog/MealPlanning completion contracts (C2/C3 extensions) are implemented: Sydänmerkki adapter depth with curated/imported fallback status (FR4/OQ1 handling), season normalization/labels including `Season fit unknown` (FR5), weekday eligibility/explanations (FR3, US2.2 AC2.2.3 data/contracts owned by U1/U2), nutrition cues/unavailable labeling (FR6), alternatives/replacement and Today/Plan synchronization contracts. Access/approvals for affected external sources confirmed before starting affected integration behavior (Q5=B), with fallbacks preserved. Tests pass; existing tests remain green.
- **Confidence hypothesis:** Shipping B2 will prove the meal-planning experience can deliver user-visible, rule-compliant recipe suggestions (weekday ≤30 min, seasonal/Sydänmerkki intent, nutrition cues) while remaining safe under missing/ambiguous/external-failure conditions.
- **Expected demo:** User sees eligible recipes for plan slots, understands weekday exclusion explanations (FR3/US2.2 AC2.2.3), can replace recipes, sees season fit/unknown and nutrition cues or unavailable labels, and sees clear source/fallback status when Sydänmerkki data is limited.

### B3 — `u3-grocery-store-export-completion` (Grocery, Store, Export Completion)

- **Definition of Done:** ShoppingList/StoreAvailability/export completion (C4/C5/C6 extensions, A2/A3 adapters) implemented: FR9 merge/edit/regenerate-confirmation semantics, availability projection/remap with explicit Unknown-after-edit/merge/regenerate behavior (ADR-006/R-02) via an explicit C4 operation or clearly named U3-owned orchestrating use case (R-02 carry-forward), PRISMA best-effort name/presence checks only (not stock/guarantee) with manual store/checklist fallback, S-ostoslista official-compatible attempt when feasible with mandatory share/copy fallback. Access/approvals for affected integrations confirmed before starting affected behavior (Q5=B). Grocery/export extraction seam minimum stable surface identified (export use-case interface, export payload DTO, platform/share adapter port; dependencies inward) (R-03 carry-forward). Tests pass; existing tests remain green.
- **Confidence hypothesis:** Shipping B3 will prove the grocery flow can produce a trustworthy, editable list from locked recipes and safely bridge to store presence checks and export without fabricating data or blocking on external integrations.
- **Expected demo:** User generates/regenerates from locked recipes with re-resolve semantics, edits/checks items, sees deterministic merge/no-fabrication behavior, selects a store or uses manual fallback, sees presence results/Unknown with reasons, and exports current edited list via S-ostoslista attempt (if feasible) or share/copy fallback.

### B4 — `u4-android-ui-experience` (Full Android UX)

- **Definition of Done:** Complete Android UI flows consume domain contracts (ADR-005, no domain business-rule contracts owned by U4): onboarding/permissions/privacy, Today/Plan, Recipe detail with required explanations/labels, Grocery/store/export flows, settings/season override, accessibility/loading/empty/error/fallback states (including RM-02/RM-03 and NFR4 verification in Build and Test). Explanations for US2.2 AC2.2.3 are presented in UI using U1/U2-owned data/contracts. Tests/checks pass; existing tests remain green.
- **Confidence hypothesis:** Shipping B4 will prove the MVP is usable end-to-end for the primary actor (busy home cook) with clear, trustworthy states and fallbacks across planning, grocery, store, and export journeys.
- **Expected demo:** End-to-end user journey: set preferences/season context, view/swap weekly meals, inspect recipe details and explanations, lock recipes, generate/edit/regenerate grocery list, check store presence or use manual checklist, and export the current list.

## Traceability summary

- B1–B4 map to U1–U4 respectively (Q3=A). Story coverage is maintained via `unit-of-work-story-map.md` (18/18 stories mapped across units).
- Cross-cutting constraints carried into all Bolts: FR11/NFR2 explicit fallbacks, FR12/NFR3 privacy/minimal data, ADR-006 lock snapshot re-resolve/no fabrication/stale semantics, PRISMA name/presence only, S-ostoslista fallback mandatory, R-01–R-03 Functional Design carry-forwards.