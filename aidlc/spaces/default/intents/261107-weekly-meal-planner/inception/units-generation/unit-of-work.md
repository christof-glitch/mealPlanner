# Unit of Work — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Stage:** Units Generation (inception)  
**Date:** 2026-11-08  
**Plan approval:** `Looks correct` (recorded in [units-generation-questions.md](units-generation-questions.md))

## Sources
- [components.md](../domain-design/components.md), [decisions.md](../domain-design/decisions.md) (including ADR-006/R-01–R-03 resolutions)
- [requirements.md](../requirements-analysis/requirements.md) (FR1–FR12, NFR1–NFR5)
- [stories.md](../user-stories/stories.md) (US1.1–US5.6)
- Decomposition answers: Q1=A, Q2=A, Q3=A, Q4=A, Q5=B

## Decomposition approach
- **Boundary strategy:** Domain capability units inside one Android app; preserve Domain Design component responsibilities.
- **Granularity:** Coarse (4 units), each delivering meaningful integrated capability.
- **Dependencies:** Strict true build/test dependency DAG (topology only; no economic build order). Units with no edge may be built in parallel later.
- **Contracts:** In-app Kotlin contracts (interfaces/use cases/data models) + adapter boundaries for Sydänmerkki/PRISMA/S-ostoslista/share.
- **Deployment model:** Single Android app, modularized for future extraction (backend/integrations remain external dependencies).

## Unit IDs and directories

| Unit ID | Directory | Name (for DAG) |
|---|---|---|
| U1 | `u1-mvp-foundation-skeleton` | `u1-mvp-foundation-skeleton` |
| U2 | `u2-recipe-planning-completion` | `u2-recipe-planning-completion` |
| U3 | `u3-grocery-store-export-completion` | `u3-grocery-store-export-completion` |
| U4 | `u4-android-ui-experience` | `u4-android-ui-experience` |

Construction artifacts for each unit live under `construction/<unit-name>/` (engine-resolved). Names use lowercase path-segment identifiers.

## Unit definitions

### U1 — `u1-mvp-foundation-skeleton` (`service`, M)
**Description:** First resolved DAG unit and smallest working integrated slice. Establishes the Android app foundation, shared contracts, and an end-to-end path: preferences/season context → minimal recipe catalog fallback + weekday eligibility → 7-day plan/slot selection + lock snapshot → shopping list generation/merge/edit basics + share/copy fallback + stale warning. Proves integration before later capability units.

**Covers Domain Design components/responsibilities (partial where extended later):**
- `PreferencesPrivacyComponent`: local preferences, `SeasonContext` (default/override baseline), privacy/permission support state.
- `RecipeCatalogComponent` (minimal): curated/imported fallback catalog, recipe metadata needed for MVP flow, weekday total-time eligibility (FR3), basic source/fallback status.
- `MealPlanningComponent`: 7-day slot structure (FR1), slot selection/replacement, shared Today/Plan state source, `LockedRecipeSnapshot` with locked IDs/selections + plan context (ADR-006/R-01), stale status when plan changes after list generation.
- `ShoppingListComponent` (minimal): explicit generate/regenerate from locked snapshot (R-03), re-resolve ingredients from catalog (ADR-006), basic deterministic merge/no-fabrication, edit/check basics, stale warning `"Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."`, mandatory share sheet/copyable text fallback export.
- `PresentationUiComponent` (thin): minimal end-to-end UI/navigation shell to exercise the slice; full RM flows delivered in U4.
- `StoreAvailabilityComponent`: minimal/manual-store and `Unknown` fallback stubs/contracts as needed for U1 path (full PRISMA behavior in U3).

**Responsibilities / delivers:**
- Android project/module foundation modularized for future extraction (Q5=B), local persistence boundaries, and core domain contracts/use cases.
- Runnable integrated MVP skeleton with fallback-first behavior (no hard dependency on live external integrations).
- Testable domain rules for slot structure, weekday eligibility, lock/generate semantics, merge safety, and stale warning.

**Deployment model:** Contributes to the single Android app deployable (logical/test boundary; modular structure preferred).

**Implementation notes / constraints:**
- Respect ADR-001 (Android boundary; integrations external). ADR-005: UI orchestrates only; business rules in domain components. ADR-006: snapshot stores IDs/context; ShoppingList re-resolves ingredients and reports missing/changed data explicitly; no auto-regeneration.
- Sydänmerkki/PRISMA/S-ostoslista live access is investigational; U1 must work with curated/fallback/manual/share-copy paths.
- Keep secrets, endpoints, and unnecessary personal data out of source control; location optional/manual store fallback.

### U2 — `u2-recipe-planning-completion` (`library`, M)
**Description:** Completes recipe/catalog and meal-planning domain capabilities on top of U1 contracts and skeleton.

**Covers Domain Design components/responsibilities:** `RecipeCatalogComponent`, `MealPlanningComponent` (completion depth), related `PreferencesPrivacyComponent` season-context usage, and thin UI contracts consumed by U4.

**Responsibilities / delivers:**
- Sydänmerkki adapter boundary/investigation hooks and clear source/fallback status behavior (FR4), with curated/imported fallback.
- Full recipe metadata normalization: total time verifiability, season tags/fit including `Season fit unknown` (FR5), nutrition cues/labels/minimal planning-aid data (FR6), ingredient entries/preparation notes.
- Full plan behaviors: alternatives/replacement, Today/Plan synchronized state contracts (R-06), slot-specific changes without affecting other slots.
- Robust weekday rule handling and explanations for missing/ambiguous time (FR3).

**Deployment model:** Contributes to the single Android app deployable (logical/test boundary).

**Implementation notes / constraints:**
- Must not duplicate UI business-rule ownership (ADR-005/R-03). Nutrition/season missing data must be labeled, not fabricated.
- OQ1/OQ5/OQ6/OQ7 remain relevant; implement adapter abstractions and fixture/curated data to keep tests deterministic.

### U3 — `u3-grocery-store-export-completion` (`library`, M)
**Description:** Completes grocery, store/availability, and export behaviors on top of U1 and recipe/planning contracts.

**Covers Domain Design components/responsibilities:** `ShoppingListComponent`, `StoreAvailabilityComponent`, export/fallback behavior (FR10), related `PreferencesPrivacyComponent` permission context.

**Responsibilities / delivers:**
- Full FR9 shopping-list behavior: deterministic merge rules (normalized name + compatible units/amounts), separate incompatible items, no silent fabrication, incomplete ingredient data indication, full edit/check/add/remove, regeneration confirmation when edits exist, export of current edited list.
- StoreAvailability/PRISMA adapter boundaries: nearest-store suggestion best effort when permitted (FR7), manual selection always, best-effort ingredient name/presence checks (not stock/guarantee), `Unknown` with reason, manual checklist fallback (FR8). Availability projection owned by ShoppingList (ADR-006/R-02).
- S-ostoslista official-compatible method investigation/attempt when feasible plus mandatory share sheet/copyable text fallback with instructions (FR10).

**Deployment model:** Contributes to the single Android app deployable (logical/test boundary).

**Implementation notes / constraints:**
- PRISMA/S-ostoslista sources are investigational (OQ2–OQ4); adapters must degrade safely. Never claim stock quantity or guaranteed availability.
- Remap or set availability `Unknown` after edit/merge/regenerate until rechecked.

### U4 — `u4-android-ui-experience` (`ui`, M)
**Description:** Completes the Android presentation experience and integrates domain units through thin contracts.

**Covers Domain Design components/responsibilities:** `PresentationUiComponent` (full), UI integration of all domain components.

**Responsibilities / delivers:**
- Full RM-01–RM-09 flows: Today, Plan, Grocery, User; full-screen replacement/alternatives and PRISMA store selection (Q3=B from refined mockups context), pushed Recipe detail, bottom navigation.
- Loading/empty/error/fallback states including RM-02/RM-03 first-launch/no-local-data handling (R-05), explicit labels/statuses, and next actions.
- Accessibility baseline (WCAG 2.2 AA practical Android/mobile), Material 3 patterns, phone portrait primary/landscape supported.
- Thin UI-facing use cases/contracts; no duplication of R-03/FR3/FR9 business rules.

**Deployment model:** Contributes to the single Android app deployable (UI layer).

**Implementation notes / constraints:**
- Depends on domain contracts from U1–U3. Keep export using current edited list state. Preserve synchronized Today/Plan behavior and explicit regeneration confirmations.

## Cross-unit implementation notes
- **External dependencies (all units):** Sydänmerkki, PRISMA, S-ostoslista are external/adapters with mandatory fallbacks; Android platform services (permissions, share/clipboard, location/date signals, local persistence) used with minimal data.
- **Testing posture (team):** tests part of MVP delivery for key behavior (plan generation/selection, shopping aggregation, integration adapters/fallbacks, privacy-sensitive handling). Prefer test-first for key behavior where practical; keep CI/critical tests green; do not weaken quality targets.
- **Traceability follow-up (R-04):** downstream Contract/Functional Design and test planning should add FR/NFR/ADR-level traceability.
