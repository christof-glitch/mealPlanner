# Unit of Work Dependencies — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Stage:** Units Generation (inception)  
**Date:** 2026-11-08

This artifact describes **topology only** (dependency DAG and integration points). It does **not** recommend a build order or critical path; Delivery Planning (Stage 2.9) chooses the economic path through this DAG.

## Machine-readable unit DAG (required)

```yaml
units:
  - name: u1-mvp-foundation-skeleton
    kind: service
    depends_on: []
  - name: u2-recipe-planning-completion
    kind: library
    depends_on: [u1-mvp-foundation-skeleton]
  - name: u3-grocery-store-export-completion
    kind: library
    depends_on: [u1-mvp-foundation-skeleton, u2-recipe-planning-completion]
  - name: u4-android-ui-experience
    kind: ui
    depends_on: [u1-mvp-foundation-skeleton, u2-recipe-planning-completion, u3-grocery-store-export-completion]
```

## Dependency rationale (true build/test dependencies)

| Edge | Rationale |
|---|---|
| `u2 -> u1` | U2 completes recipe/catalog and meal-planning behavior using U1 foundation contracts, persistence boundaries, preferences/season context, and baseline plan/lock models. |
| `u3 -> u1` | U3 uses U1 shopping-list/lock/export baseline contracts, stale-warning/generation semantics, and app foundation. |
| `u3 -> u2` | U3 re-resolves locked recipe ingredients and uses recipe/planning/catalog contracts and metadata behavior (ADR-006/R-01, FR3–FR6 inputs to grocery/list context) that are completed in U2; tests for full grocery behavior need those contracts/data behaviors. |
| `u4 -> u1,u2,u3` | U4 delivers full RM UI flows and integrates all domain capabilities via thin contracts; UI integration/tests require domain contracts and behaviors from U1–U3. |

Each unit’s code and tests must pass with its own code, the units it depends on, and the project as it stands. Units with no edge between them may be built in parallel in isolated copies (none in this DAG after edges are applied).

## Parallel development opportunities
- **After U1:** no independent units in the current true-dependency DAG (U3 depends on U2). If future contract stabilization proves U3 can build/test against U1 contracts only, the `u3 -> u2` edge can be revisited as a topology change with approval.
- **Independent from each other now:** none (DAG is effectively layered by true dependencies).

## Integration points and contracts

| From | To / Boundary | Contract / integration point | Notes |
|---|---|---|---|
| Presentation/UI (U4, thin shell in U1) | MealPlanning (U1/U2) | In-app use cases/interfaces: get week slots, select/replace recipe, lock/finalize snapshot, observe lock/stale status | Single source of truth for Today/Plan slot selections (R-06). |
| Presentation/UI | RecipeCatalog (U1/U2) | Query eligible recipes/alternatives/details, season fit labels, nutrition cues/availability, source/fallback status | FR2–FR6; labels `Season fit unknown`, `Nutrition information unavailable`. |
| Presentation/UI | ShoppingList (U1/U3) | Generate/regenerate from locked snapshot (explicit action), read/edit/check items, get stale warning, export current edited list | R-03: no auto-regeneration; stale warning exact intent: "Meal plan modified. Grocery list not up-to-date. Press the Regenerate button." |
| Presentation/UI | StoreAvailability (U3, minimal in U1) | Select/suggest store, request presence checks, render results/Unknown reasons | Manual store selection always available (FR7). |
| Presentation/UI | PreferencesPrivacy (U1) | Read/update season context override/reset, permission/privacy state | Location purpose-limited; optional (FR12/NFR3). |
| MealPlanning | RecipeCatalog | Eligibility/details queries for slots; locked recipe IDs/selections context | U2 deepens rules; U1 provides minimal path. |
| MealPlanning | PreferencesPrivacy | Effective season context for eligibility/display | Default device date/location or user override (FR5/ADR-004). |
| ShoppingList | MealPlanning | Obtain `LockedRecipeSnapshot` and lock/stale status | Snapshot has IDs/selections + plan context (ADR-006/R-01), not frozen ingredients. |
| ShoppingList | RecipeCatalog | Re-resolve ingredient entries for locked recipe IDs at generation time; handle missing/changed data explicitly | Never fabricate missing ingredients. |
| ShoppingList | StoreAvailability | Request checks keyed by item/query context; receive results; ShoppingList projects onto `ShoppingItem.availabilityStatus/availabilityReason` | ADR-006/R-02; `Unknown` after edit/merge/regenerate until rechecked where needed. |
| StoreAvailability | PreferencesPrivacy | Permission/privacy context for nearest-store suggestion | Nearest store best effort only when permitted/data available. |
| All units | External systems/adapters | Sydänmerkki (`sydanmerkki.fi`/fallback catalog), PRISMA store/location/presence data, S-ostoslista integration surface, Android share sheet/clipboard, device location/date signals, local persistence | External dependencies, not units; adapters must provide fallbacks/status labels. |

## Skeleton-first / integrated slice
- **First resolved DAG unit:** `u1-mvp-foundation-skeleton` (no dependencies). It is the smallest working integrated slice and must be runnable end-to-end before later units, subject to approved Construction checkpoint behavior.
- Later units (U2–U4) add capability/depth; they do not replace U1’s integrated path except by extending it through defined contracts.