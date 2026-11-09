# Architecture Decision Records — Domain Design

**Intent:** `261107-weekly-meal-planner` (Android MVP)  
**Stage:** Domain Design (inception)  
**Date:** 2026-11-08  
**Sources:** [domain-design-questions.md](domain-design-questions.md) (summary confirmed), [components.md](components.md), [requirements.md](../requirements-analysis/requirements.md), [refined mockups](../refined-mockups/mockups.md)

---

## ADR-001: Android App Primary MVP Boundary; Backend as External Integration Boundary

- **Context:** The MVP is Android-only, on-device by default and sync-ready later (FR12, NFR1, NFR3). Integrations (Sydänmerkki, PRISMA, S-ostoslista) are investigational with mandatory fallbacks; stack is undecided and a backend/service may be useful later. Domain Design must identify logical building blocks (code we write), not deployment topology.
- **Decision:** Model the MVP logical components inside the Android app boundary (Presentation UI, Meal Planning, Recipe Catalog/Source, Shopping List, Store/Availability, Preferences & Privacy). Treat any backend/service as an external dependency/integration boundary to be detailed later if introduced.
- **Consequences:**
  - **Positive:** Keeps core planning/list behavior aligned with on-device MVP and fallback requirements; reduces initial coordination/deployment complexity; preserves clear adapter points for future backend/proxy work.
  - **Negative/Risk:** If live integrations require server-side secrets, normalization, or rate/terms management, component interfaces may need extension; integration constraints remain open (OQ1–OQ4).
- **Alternatives Rejected:**
  - **Android + backend/service component(s) in scope now (Q1=B):** rejected for MVP because backend responsibilities are not yet proven necessary and would expand design/ops scope before integration feasibility is known.
  - **Defer boundary only as unnamed TBD (Q1=C):** partially incorporated as ADR documentation, but rejected as the sole framing because the team needed an explicit MVP working boundary for component ownership.

## ADR-002: Separate Recipe Catalog/Source from Meal Planning

- **Context:** FR2–FR6 require recipe suggestion/selection, weekday ≤30 minute total-time eligibility, Sydänmerkki attempt/fallback, seasonal context, and nutrition cues/minimal planning aid. These concerns have different data sources, normalization rules, and change rates than weekly slot/selection/lock management.
- **Decision:** Split **RecipeCatalogComponent** from **MealPlanningComponent** (Q2=A). Recipe Catalog owns recipe entities, source/fallback status, metadata normalization, and eligibility/filter data; Meal Planning owns weekly slots, selections, Today/Plan shared state, replacement, and lock snapshot.
- **Consequences:**
  - **Positive:** Clear entity ownership (Recipe vs plan/slot/snapshot), better testability of eligibility/fallback rules, and easier evolution of data sources (Sydänmerkki live vs curated/import).
  - **Negative/Risk:** More inter-component calls and mapping; requires stable query contracts for slot eligibility and recipe details.
- **Alternatives Rejected:**
  - **Single Planning+Recipes component (Q2=B):** rejected because it mixes plan lifecycle with source normalization/fallback logic, increasing coupling and test complexity.
  - **Three-way split with separate Suggestion/Eligibility Rules component (Q2=C):** rejected for MVP as premature decomposition; eligibility remains closely tied to catalog metadata and can be refactored later if rule complexity grows.

## ADR-003: Split Shopping List and Store/Availability; Keep Export in Shopping List

- **Context:** FR7–FR9/R-03/R-04 require explicit locked-snapshot list generation, deterministic merge/edit behavior, best-effort PRISMA store suggestion/presence checks with `Unknown` fallback, and export of the current edited list (FR10). PRISMA checks have distinct external dependencies and uncertainty semantics.
- **Decision:** Split **ShoppingListComponent** and **StoreAvailabilityComponent** (Q3=B). Shopping List owns lock-snapshot generation, merge/edit/check-off state, and export/fallback behavior (Q4=A). Store/Availability owns PRISMA store selection and best-effort ingredient name/presence checks. PRISMA/S-ostoslista/share remain external dependencies.
- **Consequences:**
  - **Positive:** Separates deterministic list-edit workflow from uncertain external availability checks; preserves clear user-facing `Unknown`/manual fallback boundary; keeps export close to current edited list state.
  - **Negative/Risk:** Requires coordination for availability display in grocery context; ingredient matching quality depends on unresolved PRISMA data/source decisions (OQ2/OQ3).
- **Alternatives Rejected:**
  - **One Grocery component for list + PRISMA (Q3=A):** rejected because PRISMA uncertainty/fallback behavior would be tightly coupled to list editing/export logic.
  - **Shopping List only with thin PRISMA adapter inside (Q3=C):** rejected because it hides a distinct integration boundary likely to change.
  - **Separate Export/Sharing component (Q4=B):** rejected for MVP; export is tightly bound to current edited list state and fallback UX.
  - **Platform/UI export only (Q4=C):** rejected because domain-level responsibility for “export current edited list” and fallback capability selection would be unclear.

## ADR-004: Preferences & Privacy as a Dedicated Component

- **Context:** FR5/FR12/NFR3 require effective season context (device default or explicit override, R-01/AC2.3.4), location permission purpose/manual fallback, on-device MVP, no MealPlanner account, and minimal data handling. Multiple features consume season/permission context.
- **Decision:** Introduce **PreferencesPrivacyComponent** (Q5=A) to own local preferences, SeasonContext, and privacy/permission support state. Meal Planning and Store/Availability depend on it for context; UI reads/writes settings.
- **Consequences:**
  - **Positive:** Centralizes privacy-sensitive local settings and season override behavior; improves traceability for consent/purpose and minimal-data rules.
  - **Negative/Risk:** Adds a dependency for components needing context; must avoid becoming a generic dumping ground for unrelated state.
- **Alternatives Rejected:**
  - **Distribute ownership across features/UI (Q5=B):** rejected due to risk of inconsistent season/permission behavior and duplicated privacy handling.
  - **Treat local settings as infrastructure-only and keep logic in feature components (Q5=C):** rejected because season override and consent-aware context are domain-relevant behaviors, not just storage mechanics.

## ADR-005: Include Presentation/UI Application Component; Enforce Shared Plan State and Explicit Shopping Generation

- **Context:** Refined mockups define Android Material 3 flows (RM-01–RM-09) and Q3=B full-screen replacement/store selection. R-03 requires list generation only after locked recipes and only by explicit user action, with no silent loss of edits. R-06 requires unfilled slots to remain empty and Today/Plan to stay synchronized. Architecture review R-03 raised a guardrail concern for UI reimplementing business rules.
- **Decision:** Include **PresentationUiComponent** in the catalogue (Q6=A) for flow/state orchestration traceability, while keeping domain rules in feature components. Record as architectural/domain behavior: MealPlanningComponent is the single source of truth for Today/Plan slot selections and lock snapshots; ShoppingListComponent generates/regenerates only from lock snapshot by explicit action with confirmation when edits exist. UI invokes thin domain contracts/use cases and must not duplicate R-03 lock/generate rules, FR3 eligibility, or FR9 merge rules.
- **Consequences:**
  - **Positive:** Strong traceability from mockups/stories to implementation boundaries; predictable handling of empty slots, slot replacement, and post-edit list regeneration; clearer domain/UI boundary for downstream design.
  - **Negative/Risk:** UI orchestration can become complex; lock/unlock UX and stale-list messaging must remain clear and testable.
- **Alternatives Rejected:**
  - **Exclude UI from component catalogue (Q6=B):** rejected because mockup-driven flows and state synchronization are central to MVP behavior and reviewability.
  - **Feature-based UI components (Q6=C):** rejected for MVP to avoid premature UI decomposition before implementation structure is known.
  - **Implicit auto-generation/auto-refresh of shopping list:** rejected because it risks silent loss of user edits/check-offs and violates R-03/AC3.2.3 intent.

## ADR-006: Resolve Domain Design Review Findings R-01/R-02; Carry R-04 Forward

- **Context:** Advisory architecture review (`review-01.md`, READY with concerns) identified R-01 (LockedRecipeSnapshot contract ambiguity), R-02 (ShoppingItem availability projection/check lifecycle ambiguity), R-03 (UI business-rule guardrail), and R-04 (FR/NFR/ADR-level traceability gap). User approved Domain Design with explicit direction on 2026-11-08.
- **Decision:**
  - **R-01 (resolved):** `LockedRecipeSnapshot` captures locked recipe IDs/selections and plan context, **not** frozen ingredient data. `ShoppingListComponent` re-resolves ingredient entries from `RecipeCatalogComponent` for locked recipes at generation time. If catalog data is missing/changed after lock, generation marks affected items/recipes with explicit incomplete/missing ingredient status and never fabricates silently. If the meal plan changes after the snapshot used by the current list, UI/Shopping List shows stale warning: `"Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."` No auto-regeneration.
  - **R-02 (resolved):** `StoreAvailabilityComponent` executes best-effort presence checks and returns results keyed by shopping item/query context for the selected store. `ShoppingListComponent` owns projection onto `ShoppingItem.availabilityStatus`/`availabilityReason` (latest applicable result). After edit/merge/regenerate, ShoppingList remaps checks where identity is clear or sets status `Unknown` with reason until rechecked. `IngredientPresenceCheck` records are execution/result records owned by StoreAvailabilityComponent.
  - **R-03 (guardrail):** Keep R-03 lock/generate, FR3 eligibility, and FR9 merge rules in domain components; define thin UI-facing use cases/contracts in downstream Unit/Functional Design.
  - **R-04 (carried forward):** Keep story-level traceability; add/reference FR/NFR/ADR traceability in Contract/Functional Design and test planning artifacts.
- **Consequences:**
  - **Positive:** Removes ambiguity before Units/Contract/Functional Design for the two major review findings; preserves explicit R-03 stale/regeneration behavior.
  - **Negative/Risk:** Re-resolving ingredients at generation time means post-lock catalog changes can affect generated content; this is intentional per approval direction and must be surfaced as incomplete/missing data or stale warnings, never silently.
- **Alternatives Rejected:**
  - **Freeze ingredient data in LockedRecipeSnapshot (R-01 alternative):** not selected; user chose catalog re-resolution after lock with explicit stale/warning behavior.
  - **Store availability projection inside StoreAvailabilityComponent or UI (R-02 alternative):** rejected; ShoppingList owns item state and export/current-list context, so it owns projection onto ShoppingItem.