# Contract Design Questions — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Stage:** Contract Design (inception)  
**Date:** 2026-11-08  

## Sources

- [unit-of-work.md](../units-generation/unit-of-work.md)
- [unit-of-work-dependency.md](../units-generation/unit-of-work-dependency.md)
- [components.md](../domain-design/components.md)
- [requirements.md](../requirements-analysis/requirements.md)

Context: This MVP is a single Android app modularized for future extraction (Q5=B). Units U1–U4 integrate inside the app via Kotlin contracts (interfaces/use cases/data models). The app consumes external systems (Sydänmerkki, PRISMA, S-ostoslista, Android platform services) but does not expose a public API to outside consumers. Units Generation review direction requires: R-01 explicit U1 baseline contract surfaces/handoffs in Contract/Functional Design, R-02 clarify U1 `service` kind/module role in a single modular Android app, R-03 make US2.2 AC2.2.3 UI explanation trace explicit, R-04 add FR/NFR/ADR traceability in Contract/Functional Design/test planning.

## Q1. [Architect] Public/external API surface exposed by this MVP (a unit that another team, partner, or the public internet would call)

This MVP is described as an Android-only app with no MealPlanner backend/account in MVP. Does any unit expose a public or externally consumed API that this stage must pin down as a provider contract (as opposed to consuming external APIs/adapters)?

A. No public/external API exposed by the app in MVP — only in-app unit contracts and outbound adapter contracts to external systems (Sydänmerkki/PRISMA/S-ostoslista/Android platform)  
B. Yes — specify which unit exposes which external API and who consumes it (describe in Other)  
C. Not now, but design one internal contract shape as a future extraction seam (no public API committed in MVP)  
X. Other (please specify)

[Answer]: C (with direction) — No public API now, but design a future extraction seam, especially for the grocery list/shopping-export domain (no public API committed in MVP).

## Q2. [Architect] U1 `u1-mvp-foundation-skeleton` baseline contract surfaces and handoff points to U2/U3 (Units Generation R-01)

U1 is the first resolved DAG/smallest working integrated slice but currently spans baseline behavior across components. Which baseline contract surfaces must U1 guarantee (stable for U2/U3/U4 to build against), and which capabilities are explicitly owned by U2/U3 as extensions? This resolves overlap risk while keeping U1 runnable end-to-end.

A. Define explicit U1 baseline interfaces/use cases/data models for: Preferences/SeasonContext, minimal RecipeCatalog queries + weekday eligibility + fallback status, MealPlanning slots/selection/`LockedRecipeSnapshot`(IDs+plan context)/stale status, ShoppingList generate/regenerate from snapshot with re-resolve-from-catalog hook + basic merge/edit/check + stale warning + share/copy export, StoreAvailability stub/manual-store/Unknown result contract; U2 owns full Sydänmerkki adapter depth, full normalization/season/nutrition/alternatives/Today-Plan sync; U3 owns full PRISMA adapter, full merge/edit/regenerate-confirmation semantics, availability projection/remap rules, S-ostoslista official attempt  
B. U1 guarantees only data models and repository/storage boundaries; U2/U3 own most use-case contracts (higher risk for skeleton integration)  
C. U1 guarantees broad contracts for all components at baseline and U2/U3 only add implementations behind same interfaces (may over-constrain early)  
X. Other (please specify)

[Answer]: A — Explicit U1 baseline contracts (recommended) with the handoff split described in option A.

## Q3. [Architect] U1 kind/module role in a single modular Android app (Units Generation R-02)

U1 is recorded as kind `service` in the unit DAG, but the product is one Android app (no separate deployable backend). How should Contract/Functional Design interpret U1’s role/module shape so it is not mistaken for a separate runtime service?

A. Treat U1 as the **app foundation/integration slice** (not a backend service): e.g., `:app` or foundation modules plus shared domain/data contracts and thin end-to-end wiring; keep `service` as a schema label only where required, but document “module role: Android app foundation slice” in contracts/design  
B. Rename/reinterpret U1 as `library` foundation + `ui` thin shell split conceptually, but keep unit identity unchanged unless later approved  
C. Keep `service` literally and model U1 as an internal service layer module; no special clarification  
X. Other (please specify)

[Answer]: A — Android app foundation/integration slice (not a backend service); document module role consistent with ADR-001/Q5=B; treat `service` as schema label only where required.

## Q4. [Developer] Integration mechanism per boundary (in-app unit boundaries and external adapters)

Given in-app Kotlin modules/units and external systems, what mechanism should each boundary use for MVP contracts?

A. **In-app unit boundaries (U1↔U2↔U3↔U4):** Kotlin interfaces/use cases/data classes (suspend/Flow where async observation needed, e.g., plan/list state) owned per contract; no network/gRPC/events inside app. **External adapters:** outbound HTTP/SDK/data-source adapters behind interfaces for Sydänmerkki/PRISMA/S-ostoslista where applicable; Android platform intents/share/clipboard/location/persistence via platform abstractions; all adapters return explicit status/fallback-capable results (no throwing across UI boundary without mapping)  
B. Use shared database/schema as the primary contract between in-app units (tighter coupling)  
C. Use an internal event bus as the primary contract between all units (more indirection than needed for MVP)  
X. Other (please specify)

[Answer]: A — Kotlin contracts in-app; outbound adapters behind interfaces for external/platform boundaries with explicit status/fallback-capable results.

## Q5. [Developer] Contract ownership, versioning, and breaking-change policy (module boundaries and adapters)

Which unit owns which contract, and how are breaking/additive changes handled so parallel Construction work stays safe?

A. **Ownership:** U1 owns foundation contracts (Preferences/SeasonContext, lock snapshot model, generation trigger/status contracts, export fallback contract, shared result/status types). U2 owns RecipeCatalog/MealPlanning completion contracts extending U1 baseline. U3 owns ShoppingList completion/StoreAvailability/official-export attempt contracts extending U1 baseline. U4 owns only UI-facing consumption contracts (no domain business-rule contracts). **Policy:** additive changes preferred (new optional fields/params with defaults, consumers ignore unknown data); breaking changes require coordinated update of provider+consumers in same change set before merge; adapters versioned/tolerant to missing external fields and always map to explicit fallback statuses  
B. Each unit owns all contracts it touches; breaking changes allowed freely during MVP  
C. Centralize all contracts in a single shared module owned by U1 with strict semver-like discipline (good, but specify exceptions for MVP speed)  
X. Other (please specify)

[Answer]: A — Clear ownership (U1 foundation; U2/U3 completion extending baseline; U4 consumes) + additive-first policy; breaking changes coordinated; adapters tolerant and mapped to fallback statuses.

## Q6. [Developer] Error, timeout, retry, and fallback behavior at boundaries (especially external adapters)

What failure behavior must contracts specify for in-app and external boundaries, aligned with FR4/FR8/FR10/FR11/NFR2/NFR5?

A. **In-app:** domain operations return typed success/failure/status results where failure is expected; no fabricated data; stale/missing states explicit. **External adapters (Sydänmerkki/PRISMA/S-ostoslista/network/location/data):** bounded timeouts, limited safe retries only for idempotent reads/lookups, clear reason codes/messages (`Unknown`, `unavailable`, `restricted`, `not found`, `inconclusive`, etc.), never block core local plan/list/edit/export-fallback flows; PRISMA is best-effort name/presence only (not stock/guarantee); S-ostoslista official attempt falls back to share/copy; location denial falls back to manual store selection  
B. Fail fast on all adapter errors and surface raw errors to UI  
C. Retry aggressively on all external failures without explicit timeout/fallback rules  
X. Other (please specify)

[Answer]: A — Typed results + bounded adapter fallbacks (timeouts, limited safe retries for idempotent reads, clear reason codes, never block core local flows; PRISMA name/presence only; S-ostoslista fallback to share/copy; location denial -> manual store).

## Q7. [Developer] US2.2 AC2.2.3 explanation trace and FR/NFR/ADR traceability in contracts (Units Generation R-03, R-04)

US2.2 AC2.2.3 requires a user-visible explanation when weekday prep/cook time is missing/ambiguous (recipe excluded from Mon–Fri Breakfast/Dinner). Also R-04 requires FR/NFR/ADR traceability (e.g., ADR-006/R-01–R-03, FR3/FR9/R-03, privacy/fallback NFRs) in Contract/Functional Design/test planning. How should Contract Design make these explicit?

A. Include an explicit traceability section in `contract-summary.md`: map key contracts to FR/NFR/ADR/US (ADR-001 boundary, ADR-005 UI thin orchestration, ADR-006 snapshot re-resolve + availability projection + stale semantics, FR3 weekday rule/explanation data, FR9/R-03 generate/regenerate/merge ownership, FR12/NFR3 privacy). State: U1/U2 own weekday eligibility and explanation data/contracts; U4 presents explanation in recipe/detail and fallback UI states (US2.2 AC2.2.3 trace). Carry same traces into Functional Design/test planning.  
B. Keep traceability only in story map/requirements docs; do not repeat in contracts  
C. Add traceability only as informal notes, no table/mapping  
X. Other (please specify)

[Answer]: A — Explicit traceability section in `contract-summary.md`; state US2.2 AC2.2.3 ownership (U1/U2 data/contracts, U4 presents); carry FR/NFR/ADR traces into Functional Design/test planning.

## Consolidated Summary Confirmation

- **Q1 [Public/external API surface]:** C (with direction) — No public/external API exposed in MVP; design a future extraction seam, especially for the grocery list/shopping-export domain (no public API committed).
- **Q2 [U1 baseline contract surfaces / R-01]:** A — U1 guarantees explicit baseline contracts: Preferences/SeasonContext; minimal RecipeCatalog queries + weekday eligibility (FR3) + fallback status; MealPlanning slots/selection + `LockedRecipeSnapshot` (IDs+plan context, ADR-006) + stale status; ShoppingList generate/regenerate (explicit) with re-resolve-from-catalog hook + basic merge/edit/check + stale warning + share/copy export; StoreAvailability stub/manual/Unknown. U2 owns full Sydänmerkki adapter depth, normalization/season/nutrition/alternatives/Today-Plan sync. U3 owns full PRISMA adapter, full merge/edit/regenerate-confirmation semantics, availability projection/remap, S-ostoslista official attempt.
- **Q3 [U1 module role / R-02]:** A — U1 is the Android app foundation/integration slice (not a backend service); document module role consistent with ADR-001/Q5=B; treat `service` as schema label only where required.
- **Q4 [Integration mechanism]:** A — In-app boundaries use Kotlin interfaces/use cases/data classes (suspend/Flow where needed), no in-app network/events. External boundaries use outbound adapters behind interfaces with explicit status/fallback-capable results; Android platform via abstractions.
- **Q5 [Ownership/versioning/breaking changes]:** A — U1 owns foundation contracts; U2 owns RecipeCatalog/MealPlanning completion contracts extending baseline; U3 owns ShoppingList/StoreAvailability/official-export completion contracts extending baseline; U4 consumes only. Additive-first; breaking changes coordinated provider+consumers before merge; adapters tolerant/mapped to fallback statuses.
- **Q6 [Error/timeout/retry/fallback]:** A — In-app typed success/failure/status results, explicit stale/missing states, no fabricated data. External adapters: bounded timeouts, limited safe retries for idempotent reads, clear reason codes (`Unknown`/`unavailable`/`restricted`/`not found`/`inconclusive`), never block core local flows. PRISMA name/presence only (not stock/guarantee). S-ostoslista falls back to share/copy. Location denial falls back to manual store selection.
- **Q7 [US2.2 AC2.2.3 + traceability / R-03,R-04]:** A — Explicit traceability section in `contract-summary.md`; U1/U2 own weekday eligibility/explanation data/contracts, U4 presents explanation in UI (US2.2 AC2.2.3); carry FR/NFR/ADR traces (ADR-001/005/006, FR3/FR9/R-03, FR12/NFR3, etc.) into Functional Design/test planning.

Does this all look correct before I generate the contract summary?
- Looks correct
- Request changes

[Answer]: Looks correct
