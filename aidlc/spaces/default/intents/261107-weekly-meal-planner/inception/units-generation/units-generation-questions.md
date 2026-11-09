# Units Generation Questions

**Intent:** `261107-weekly-meal-planner` (Android MVP, scope `mvp`)  
**Stage:** Units Generation (inception)  
**Sources:** [components.md](../domain-design/components.md), [decisions.md](../domain-design/decisions.md), [requirements.md](../requirements-analysis/requirements.md), [stories.md](../user-stories/stories.md), [team.md](../../../../memory/team.md)

Domain Design defines 6 logical Android-app components: `PresentationUiComponent`, `MealPlanningComponent`, `RecipeCatalogComponent`, `ShoppingListComponent`, `StoreAvailabilityComponent`, `PreferencesPrivacyComponent` (ADR-001: Android app primary boundary; backend/integrations are external dependencies). MVP is on-device, with uncertain external integrations (Sydänmerkki, PRISMA, S-ostoslista) and mandatory fallbacks.

---

## Q1. [Unit boundary strategy] How should implementation units be bounded for this Android MVP?

A. **By domain capability inside the Android app (recommended for MVP)** — group related components into a few vertical/domain units (e.g., app foundation + preferences/privacy, recipe/catalog + meal planning, grocery list + store/availability + export) while preserving component responsibilities; units can share the same Android module/deployable initially but have clear ownership/test boundaries.
B. **One unit per Domain Design component** — 6 units aligned 1:1 with components; fine-grained ownership, more integration coordination.
C. **Single Android MVP unit** — one unit covering the whole MVP; simplest coordination, weakest decomposition for Construction planning.
D. **By feature/flow** — units like “plan & recipes”, “grocery & store”, “export/share & settings”; may cut across component boundaries.
X. Other (please describe)

[Answer]: A — Domain capability units inside Android app; preserve component responsibilities; clear ownership/test boundaries.

---

## Q2. [Unit granularity] What unit granularity do you prefer?

A. **Coarse-grained (recommended for MVP)** — 2–4 units, each delivering a meaningful integrated capability; fewer handoffs.
B. **Medium** — 4–6 units, closer to component boundaries.
C. **Fine-grained** — many small units; only if you want strict isolation/parallel work.
X. Other (please describe)

[Answer]: A — Coarse-grained (2–4 units), meaningful integrated capabilities.

---

## Q3. [Dependency ordering / parallelism] How should dependencies be treated in the unit DAG?

A. **Strict dependency edges, allow parallelism where independent (recommended)** — record only true build/test dependencies; units with no edge may be built in parallel later (Construction/Delivery Planning decides execution). Each unit’s code/tests must pass with its own code, its dependencies, and the project as it stands.
B. **Strict topological bias** — keep DAG simple and mostly linear for MVP clarity, even if some units could be independent.
X. Other (please describe)

[Answer]: A — Strict true build/test dependency edges; allow parallelism where independent (topology only).

---

## Q4. [Integration points and contracts] What integration contracts should be emphasized between units?

A. **In-app Kotlin contracts (interfaces/use cases/data models) + adapter boundaries for externals (recommended)** — document unit interfaces, shared domain models/DTOs, and adapter contracts for Sydänmerkki/PRISMA/S-ostoslista/share; no network contracts between units (single Android app).
B. **Minimal contracts** — rely on shared codebase conventions and document only major integration points.
C. **Stronger internal module contracts** — treat units as Gradle modules with explicit API surfaces where practical.
X. Other (please describe)

[Answer]: A — In-app Kotlin contracts (interfaces/use cases/data models) + adapter boundaries for Sydänmerkki/PRISMA/S-ostoslista/share.

---

## Q5. [Deployment model] What deployment model should units assume for MVP?

A. **Single Android app deployable (recommended)** — all units contribute to one Android application artifact; internal boundaries are logical/test boundaries. External services remain external dependencies.
B. **Single app now, modularized for future extraction** — same deployable, but prefer module boundaries that could later support backend/proxy extraction.
C. **Include a backend/service unit** — add a deployable backend/proxy unit for integrations (not aligned with current ADR-001/Q1=A unless you want to revisit).
X. Other (please describe)

[Answer]: B — Single Android app now, modularized for future extraction (external integrations remain external dependencies).

---

## Proposed decomposition plan (for approval)

**Strategy:** 4 coarse domain-capability units inside one Android app, modularized for future extraction (Q1=A,Q2=A,Q5=B). DAG records true build/test dependencies with parallelism allowed (Q3=A). Contracts are in-app Kotlin interfaces/use cases/data models plus external adapter boundaries (Q4=A). No economic build order is recommended here (topology only; Delivery Planning decides).

| Unit ID | Directory | Proposed kind | Purpose / components covered | Relative complexity |
|---|---|---|---|---|
| U1 | `u1-mvp-foundation-skeleton` | `service` | **First resolved DAG unit / smallest working integrated slice.** Android app foundation, PreferencesPrivacy context (season/permission/privacy state), minimal RecipeCatalog fallback + weekday eligibility, minimal MealPlanning (7-day slots, selection, lock snapshot per ADR-006), minimal ShoppingList generation/merge/export fallback (share/copy + stale warning), thin end-to-end UI path to prove the integration. Covers essential behavior from PresentationUi, PreferencesPrivacy, RecipeCatalog, MealPlanning, ShoppingList. | M |
| U2 | `u2-recipe-planning-completion` | `library` | Complete recipe/catalog and meal-planning domain capability: Sydänmerkki adapter investigation/fallback status, richer recipe metadata normalization (season/nutrition cues/labels, `Season fit unknown`), full FR2/FR3/FR4/FR5/FR6 behavior, slot replacement details, Today/Plan synchronization contracts. Builds on U1 contracts. | M |
| U3 | `u3-grocery-store-export-completion` | `library` | Complete grocery capability: full FR9 merge/edit/regenerate rules (ADR-006), StoreAvailability/PRISMA adapters (nearest suggestion best effort, manual selection, name/presence checks, `Unknown`), availability projection (ADR-006/R-02), S-ostoslista official-compatible attempt + mandatory fallback export. Builds on U1 lock/list contracts and may use U2 recipe resolution contracts. | M |
| U4 | `u4-android-ui-experience` | `ui` | Complete Presentation/UI flows per refined mockups RM-01–RM-09 (Today/Plan/Grocery/User), full-screen flows, accessibility, loading/empty/error/fallback states, thin use-case contracts only (ADR-005 guardrail). Integrates domain units. | M |

**Skeleton-on note:** U1 is shaped as the smallest working integrated slice and is the first unit in resolved DAG order (no dependencies). It must run end-to-end before later units. Later units add depth/capability; genuine dependencies are recorded in the DAG (no recommendation of economic order).

**Proposed DAG (topology):** `u1-mvp-foundation-skeleton` has no dependencies. `u2-recipe-planning-completion` depends on `u1`. `u3-grocery-store-export-completion` depends on `u1` and `u2` (uses recipe/lock/catalog contracts; if kept independent of U2 implementation details, edge can be reduced to `u1` — I’ll finalize based on true build/test dependency). `u4-android-ui-experience` depends on `u1`, `u2`, `u3` (full UI integration). Parallelism: after U1, U2 and U3 *may* be parallel only if U3 does not require U2 code/tests; default conservative DAG below assumes U3 depends on U2 for shared recipe/planning contracts used by grocery flows — I’ll confirm true edges in artifacts.

---

## Consolidated Summary Confirmation

- **Q1 [Boundary strategy]:** A — Domain capability units inside Android app; preserve component responsibilities.
- **Q2 [Granularity]:** A — Coarse-grained (2–4 units).
- **Q3 [Dependencies/parallelism]:** A — Strict true build/test dependency edges; allow parallelism where independent.
- **Q4 [Integration contracts]:** A — In-app Kotlin contracts/interfaces/use cases/data models + external adapter boundaries.
- **Q5 [Deployment model]:** B — Single Android app, modularized for future extraction.
- **Proposed unit set / first integrated slice:** 4 units (`U1`–`U4`) with `U1=u1-mvp-foundation-skeleton` as first resolved DAG/integrated slice; kinds `service`, `library`, `library`, `ui`.
- **Ambiguity check:** No blocking ambiguity. `u3` depends on `u1,u2` (true build/test deps for re-resolve/contracts); `u2,u3` not parallel in this DAG. No economic order recommended.

Does this all look correct before I treat this as the approved decomposition plan and generate `unit-of-work.md`, `unit-of-work-dependency.md`, `unit-of-work-story-map.md`, and `traceability.json`?
- Looks correct
- Request changes

[Answer]: Looks correct 
