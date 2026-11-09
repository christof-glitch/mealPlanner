# Unit of Work Story Map — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Stage:** Units Generation (inception)  
**Date:** 2026-11-08  
**Upstream:** [stories.md](../user-stories/stories.md) (`US1.1`–`US5.6`)

Rows are keyed by `USx.y`. Each implementing Unit traces all acceptance criteria for that story it helps implement. Units are named by `U{n}` and construction directory.

## Story-to-unit map

| Story ID | Unit ID(s) | Directory name(s) | Implementation note |
|---|---|---|---|
| US1.1 | U1; U2; U4 | `u1-mvp-foundation-skeleton`; `u2-recipe-planning-completion`; `u4-android-ui-experience` | U1: 7-day slot structure/shared state baseline. U2: complete plan model/synchronization contracts. U4: full plan view UI. |
| US1.2 | U1; U2; U4 | `u1-mvp-foundation-skeleton`; `u2-recipe-planning-completion`; `u4-android-ui-experience` | U1: slot selection/replacement baseline and lock-aware state. U2: full replacement/alternative contracts. U4: change-recipe UX. |
| US2.1 | U1; U2; U4 | `u1-mvp-foundation-skeleton`; `u2-recipe-planning-completion`; `u4-android-ui-experience` | U1: minimal recipe details/fallback labels. U2: full metadata normalization/labels. U4: recipe detail presentation. |
| US2.2 | U1; U2 | `u1-mvp-foundation-skeleton`; `u2-recipe-planning-completion` | U1: weekday total-time eligibility baseline (≤30 min, missing/ambiguous excluded/explained). U2: robust normalization/explanations and full rule behavior. |
| US2.3 | U1; U2; U4 | `u1-mvp-foundation-skeleton`; `u2-recipe-planning-completion`; `u4-android-ui-experience` | U1: `SeasonContext` default/override baseline, `Season fit unknown` handling hooks. U2: full season fit computation/mapping integration. U4: RM-09 season controls/status UI. |
| US2.4 | U1; U2; U4 | `u1-mvp-foundation-skeleton`; `u2-recipe-planning-completion`; `u4-android-ui-experience` | U1: safe unavailable-label behavior hooks. U2: nutrition cues/minimal planning-aid data from reliably available attributes (FR6). U4: Nutrition Balance Cue and recipe nutrition presentation. |
| US2.5 | U1; U2; U4 | `u1-mvp-foundation-skeleton`; `u2-recipe-planning-completion`; `u4-android-ui-experience` | U1: curated/imported fallback catalog path and basic source status. U2: Sydänmerkki adapter boundary/investigation/fallback status behavior. U4: source/fallback status UI. |
| US3.1 | U1; U3; U4 | `u1-mvp-foundation-skeleton`; `u3-grocery-store-export-completion`; `u4-android-ui-experience` | U1: generate from locked snapshot (ADR-006), re-resolve ingredients, basic merge/no-fabrication. U3: full deterministic merge/edit-aware list behavior and incomplete data indication. U4: grocery list UI. |
| US3.2 | U1; U3; U4 | `u1-mvp-foundation-skeleton`; `u3-grocery-store-export-completion`; `u4-android-ui-experience` | U1: check/edit basics and explicit regenerate/stale warning baseline. U3: full edit/check/add/remove, regeneration confirmation when edits exist, export current edited list. U4: inline checklist/edit UX. |
| US3.3 | U1; U3; U4 | `u1-mvp-foundation-skeleton`; `u3-grocery-store-export-completion`; `u4-android-ui-experience` | U1: manual store/Unknown fallback contracts where needed. U3: PRISMA adapters, nearest suggestion best effort, name/presence checks, availability projection (ADR-006/R-02), manual checklist fallback. U4: store selection and availability UI. |
| US4.1 | U1; U3; U4 | `u1-mvp-foundation-skeleton`; `u3-grocery-store-export-completion`; `u4-android-ui-experience` | U1: mandatory share sheet/copyable text fallback baseline. U3: complete export/fallback instructions and current edited list export behavior. U4: export flow UI. |
| US4.2 | U1; U3; U4 | `u1-mvp-foundation-skeleton`; `u3-grocery-store-export-completion`; `u4-android-ui-experience` | U1: fallback-safe export contract. U3: official S-ostoslista-compatible attempt when feasible; fallback on partial/unavailable/failure; preserve list state. U4: export method/capability UX. |
| US5.1 | U1; U4 | `u1-mvp-foundation-skeleton`; `u4-android-ui-experience` | U1: permission/privacy state, purpose-limited location handling, no MealPlanner account. U4: permission purpose text and controls UX. |
| US5.2 | U1; U3; U4 | `u1-mvp-foundation-skeleton`; `u3-grocery-store-export-completion`; `u4-android-ui-experience` | U1: selected-store visibility/context baseline. U3: nearest PRISMA suggestion best effort + manual fallback. U4: store selection/status UI. |
| US5.3 | U1; U3; U4 | `u1-mvp-foundation-skeleton`; `u3-grocery-store-export-completion`; `u4-android-ui-experience` | U1: `Unknown`/manual fallback path. U3: presence-check failure reasons, projection to `Unknown`, checklist fallback without blocking edits/export. U4: fallback status UI. |
| US5.4 | U1; U2; U4 | `u1-mvp-foundation-skeleton`; `u2-recipe-planning-completion`; `u4-android-ui-experience` | U1: curated/imported fallback catalog usable. U2: Sydänmerkki unavailable/restricted/failure fallback status and field labeling. U4: fallback status UI. |
| US5.5 | U1; U2; U3; U4 | `u1-mvp-foundation-skeleton`; `u2-recipe-planning-completion`; `u3-grocery-store-export-completion`; `u4-android-ui-experience` | U1: local/on-device baseline and minimal clear states. U2/U3: integration failure statuses for recipe/store/export paths. U4: RM-02/RM-03 first-launch/no-local-data and retry/bundled fallback/manual planning actions (R-05). |
| US5.6 | U1; U2; U3; U4 | `u1-mvp-foundation-skeleton`; `u2-recipe-planning-completion`; `u3-grocery-store-export-completion`; `u4-android-ui-experience` | U1: on-device/minimal-data foundation, no account. U2/U3: adapter data minimization and purpose-limited handling. U4: privacy-aware UX/consent presentation. |

## Cross-cutting stories
- **US5.5, US5.6** span all units (reliability/privacy constraints apply across foundation, domain adapters, and UI states).
- **US2.3, US2.4, US2.5** span catalog/planning and UI presentation.
- **US3.3, US4.2, US5.2, US5.3** span grocery/store/export domain and UI, with adapter fallbacks.

## Implementation order within each unit
- **U1 (`u1-mvp-foundation-skeleton`):** app/module foundation + persistence boundaries → PreferencesPrivacy/SeasonContext → minimal RecipeCatalog fallback + weekday rule → MealPlanning slots/selection/lock (ADR-006) → ShoppingList generate/re-resolve/merge/edit/export fallback + stale warning → thin end-to-end UI path/tests.
- **U2 (`u2-recipe-planning-completion`):** recipe normalization contracts → Sydänmerkki adapter/fallback status → eligibility/season/nutrition rule completion → planning replacement/Today-Plan sync contracts → domain tests/fixtures.
- **U3 (`u3-grocery-store-export-completion`):** full merge/edit/regenerate behavior → StoreAvailability/PRISMA adapter contracts + name/presence checks → availability projection (ADR-006/R-02) → S-ostoslista official attempt + fallback export → adapter fallback tests.
- **U4 (`u4-android-ui-experience`):** navigation/shell + shared state wiring → Plan/Recipe flows → Grocery/store/export flows → User/settings/privacy flows → accessibility/loading/empty/error/fallback polish.

## Coverage verification
- **Stories covered:** `US1.1, US1.2, US2.1, US2.2, US2.3, US2.4, US2.5, US3.1, US3.2, US3.3, US4.1, US4.2, US5.1, US5.2, US5.3, US5.4, US5.5, US5.6` (18/18).
- **Units with rows:** `U1` (18), `U2` (10), `U3` (9), `U4` (18) — all units have story mappings.
- **AC tracing note:** Each named unit implements part of the mapped story and must trace the acceptance criteria relevant to that part in Functional Design; U4 traces UI/interaction acceptance and invokes domain contracts (ADR-005).