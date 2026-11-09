# Contract Summary — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Stage:** Contract Design (inception)  
**Date:** 2026-11-08  
**Conversation language:** English

## Imports / Upstream Targets

This contract summary is derived from and must remain consistent with:
- [unit-of-work.md](../units-generation/unit-of-work.md) — unit definitions, responsibilities, kinds, deployment model (single Android app, modularized for future extraction).
- [unit-of-work-dependency.md](../units-generation/unit-of-work-dependency.md) — true build/test dependency DAG and integration points (topology only).
- [components.md](../domain-design/components.md) — domain component catalogue, entity ownership, behaviors, ADR-006/R-01–R-03 resolutions.
- [requirements.md](../requirements-analysis/requirements.md) — `FR1–FR12`, `NFR1–NFR5`, constraints, assumptions, open questions `OQ1–OQ8`.

Confirmed answers: [contract-design-questions.md](contract-design-questions.md) (Q1–Q7, summary confirmed `Looks correct`).

## Boundary Scope and Interpretation

- **Public/external API exposed by this MVP:** **None.** The app does not expose a provider API consumed by another team, partner, or the public internet in MVP (Q1=C with direction). No public API is committed.
- **Future extraction seam (Q1 direction):** design internal contracts so the grocery list/shopping-export domain (ShoppingList, StoreAvailability projection context, export/fallback) can be extracted later behind stable interfaces/use cases/data models. No backend/public contract is introduced now.
- **Inter-unit boundaries (in scope):** U1–U4 integrate inside one Android app. Each DAG edge and each UI-to-domain integration point is an in-app contract boundary (Kotlin interfaces/use cases/data models). These are treated as formal agreements enabling parallel Construction.
- **External system boundaries (outbound adapters, consumed by the app):** Sydänmerkki, PRISMA store-location/presence, S-ostoslista integration surface, Android platform services (permissions, share/clipboard, location/date, local persistence). The app is the consumer; adapters must be tolerant and map to explicit fallback statuses (FR4/FR8/FR10/FR11/NFR2/NFR3/NFR5).

## U1 Module Role (R-02)

`u1-mvp-foundation-skeleton` is recorded as kind `service` in the unit DAG for schema purposes, but in this MVP it is **not a deployable backend service**. It is the **Android app foundation/integration slice**: foundation modules plus shared domain/data contracts plus thin end-to-end wiring that proves the integrated path (preferences/season context → minimal recipe catalog fallback + weekday eligibility → 7-day plan/slot selection + lock snapshot → shopping list generation/merge/edit basics + share/copy fallback + stale warning). Contract/Functional Design must document module boundaries accordingly (ADR-001, Q5=B). U2/U3 extend U1 baseline contracts; U4 consumes contracts and owns no domain business-rule contracts (ADR-005).

## Contracts Table

| # | Provider Unit | Consumer | Mechanism | Owner | Boundary |
|---|---|---|---|---|---|
| C1 | `u1-mvp-foundation-skeleton` (PreferencesPrivacy baseline) | `u2-recipe-planning-completion`, `u3-grocery-store-export-completion`, `u4-android-ui-experience` | In-app Kotlin interface/use case/data model (`SeasonContext`, preferences/permission state) | U1 | Inter-unit |
| C2 | `u1-mvp-foundation-skeleton` (RecipeCatalog minimal baseline) / extended by `u2-recipe-planning-completion` | `u1` thin UI shell, `u2`, `u3` (re-resolve at generation), `u4` | In-app Kotlin interface/use case/data model (queries, eligibility, details, fallback status) | U1 baseline; U2 completion | Inter-unit |
| C3 | `u1-mvp-foundation-skeleton` (MealPlanning baseline) / extended by `u2-recipe-planning-completion` | `u1` thin UI shell, `u2`, `u3`, `u4` | In-app Kotlin interface/use case/data model + observable state (`Flow`/equivalent where needed) | U1 baseline; U2 completion | Inter-unit |
| C4 | `u1-mvp-foundation-skeleton` (ShoppingList baseline) / extended by `u3-grocery-store-export-completion` | `u1` thin UI shell, `u3`, `u4` | In-app Kotlin interface/use case/data model + observable state | U1 baseline; U3 completion | Inter-unit |
| C5 | `u1-mvp-foundation-skeleton` (StoreAvailability stub baseline) / extended by `u3-grocery-store-export-completion` | `u1` thin UI shell, `u3`, `u4` | In-app Kotlin interface/use case/data model (results keyed by item/query context) | U1 baseline; U3 completion | Inter-unit |
| C6 | `u1-mvp-foundation-skeleton` / `u3-grocery-store-export-completion` (export fallback + official attempt boundary) | `u4-android-ui-experience`, Android share/clipboard/S-ostoslista surface | In-app export contract + outbound adapter/platform abstraction | U1 baseline fallback; U3 official attempt extension | Inter-unit + outbound adapter |
| A1 | External: Sydänmerkki source (`sydanmerkki.fi`) / curated-imported fallback catalog | `u2-recipe-planning-completion` (adapter), baseline usable by `u1` | Outbound adapter behind interface (HTTP/data-source/local catalog), tolerant parsing | U2 (adapter owner); U1 consumes baseline/fallback | Outbound adapter |
| A2 | External: PRISMA store-location/presence data sources | `u3-grocery-store-export-completion` (adapter) | Outbound adapter behind interface, best-effort name/presence only | U3 | Outbound adapter |
| A3 | External: S-ostoslista integration surface (investigational) | `u3-grocery-store-export-completion` (adapter) | Outbound adapter/intent/share integration attempt behind interface; mandatory fallback always available | U3 attempt; U1/U3 fallback contract | Outbound adapter |
| A4 | External: Android platform (permissions, location/date signals, share sheet, clipboard, local persistence) | All units via abstractions | Platform abstractions/adapters | Consuming unit owns abstraction usage; U1 defines shared baseline abstractions where cross-cutting | Outbound adapter |

## Per-Contract Specs

### C1 — Preferences & Season Context (shared-schema / in-app contract)

```yaml
shared-schema:
  contract: C1-PreferencesSeasonContext
  provider: u1-mvp-foundation-skeleton
  consumers: [u2-recipe-planning-completion, u3-grocery-store-export-completion, u4-android-ui-experience]
  mechanism: Kotlin interfaces, use cases, immutable data classes; suspend/Flow for observation if needed
  module_role_note: "U1 is Android app foundation/integration slice (R-02), not a backend service."
  operations:
    - name: getEffectiveSeasonContext
      output: SeasonContext
      errors: [unavailable]
      notes: "Default from device date/location signals when available; explicit override/reset supported (FR5, ADR-004)."
    - name: setSeasonOverride
      input: { seasonValue: string, areaLabel: string? }
      output: SeasonContext
    - name: clearSeasonOverride
      output: SeasonContext
    - name: getPermissionPrivacyState
      output: PermissionPrivacyState
      notes: "Location optional and purpose-limited (FR12/NFR3)."
  models:
    SeasonContext:
      required: [sourceMode, effectiveStatus]
      properties:
        sourceMode: { enum: [device_default, user_override] }
        seasonValue: { type: string, nullable: true }
        areaLabel: { type: string, nullable: true }
        deviceDate: { type: string, format: date, nullable: true }
        locationSignalStatus: { enum: [available, unavailable, denied, not_requested], nullable: true }
        effectiveStatus: { enum: [known, unknown] }
    PermissionPrivacyState:
      properties:
        locationPermission: { enum: [granted, denied, not_requested] }
        locationPurposeShown: { type: boolean }
        privacyAcknowledgementState: { type: string, nullable: true }
  fallback: "If season cannot be determined, effectiveStatus=unknown and UI/domain use label 'Season fit unknown' where applicable (FR5)."
  traceability: [FR5, FR12, NFR3, ADR-001, ADR-004, US2.3, US5.1, US5.2]
```

### C2 — Recipe Catalog Baseline and Completion (shared-schema / in-app contract)

```yaml
shared-schema:
  contract: C2-RecipeCatalog
  provider_baseline: u1-mvp-foundation-skeleton
  provider_completion: u2-recipe-planning-completion
  consumers: [u1 thin UI shell, u2, u3, u4]
  mechanism: Kotlin interfaces/use cases/data models
  u1_baseline_guarantees:
    - "Minimal query for eligible recipes for a given slot/day context using effective season context"
    - "Weekday total-time eligibility for Mon-Fri Breakfast/Dinner: eligible only when total prep/cooking time is present, interpretable, and <= 30 minutes (FR3)"
    - "Missing/ambiguous weekday time excluded from weekday suggestions with explanation data (FR3, US2.2 AC2.2.3)"
    - "Basic recipe details needed for MVP flow, ingredient entries for locked re-resolution, fallback/source status"
  u2_completion_owns:
    - "Full Sydänmerkki adapter depth and curated/imported fallback status behavior (FR4, OQ1)"
    - "Full normalization of season tags/fit including 'Season fit unknown' (FR5)"
    - "Nutrition cues/labels/minimal planning-aid data; label 'Nutrition information unavailable' when missing (FR6)"
    - "Alternatives/replacement queries and richer metadata/preparation notes"
  operations:
    - name: getEligibleRecipes
      input: { slot: MealSlotRef, seasonContext: SeasonContext, filters: RecipeFilters? }
      output: RecipeQueryResult
    - name: getRecipeById
      input: { recipeId: string }
      output: RecipeDetailResult
    - name: getRecipesByIds
      input: { recipeIds: [string] }
      output: RecipeBatchResult
      notes: "Used by ShoppingList generation to re-resolve locked recipe ingredients (ADR-006/R-01). Missing/changed recipes returned with explicit status."
  models:
    RecipeSummary:
      properties:
        recipeId: { type: string }
        title: { type: string }
        totalTimeMinutes: { type: integer, nullable: true }
        timeVerifiability: { enum: [verified, missing, ambiguous] }
        weekdayEligible: { type: boolean, nullable: true }
        weekdayExclusionReason: { type: string, nullable: true }
        seasonFitLabel: { type: string, nullable: true }
        nutritionAvailability: { enum: [available, unavailable, partial] }
        sourceType: { enum: [sydanmerkki_live, sydanmerkki_curated_fallback, imported_fallback, unknown] }
        sourceStatus: { type: string }
    RecipeQueryResult:
      properties:
        items: { type: array, items: RecipeSummary }
        status: { enum: [ok, partial, fallback, empty, error_mapped] }
        message: { type: string, nullable: true }
    RecipeBatchResult:
      properties:
        items: { type: array, items: RecipeDetailOrMissing }
    RecipeDetailOrMissing:
      properties:
        recipeId: { type: string }
        status: { enum: [found, missing, changed] }
        recipe: { ref: RecipeDetail, nullable: true }
        message: { type: string, nullable: true }
  us2_2_ac2_2_3_trace:
    owner: "U1/U2 own weekday eligibility and explanation data/contracts (weekdayEligible, timeVerifiability, weekdayExclusionReason/source explanation fields). U4 presents the user-visible explanation in recipe/detail/fallback UI states."
  fallback: "No fabricated nutrition/season/time/ingredient data. Use explicit labels/statuses (FR3/FR5/FR6/NFR2)."
  traceability: [FR2, FR3, FR4, FR5, FR6, NFR2, NFR4, ADR-005, ADR-006, US2.1, US2.2, US2.3, US2.4, US2.5, US5.4]
```

### C3 — Meal Planning, Lock Snapshot, and Stale Status (shared-schema / in-app contract)

```yaml
shared-schema:
  contract: C3-MealPlanning
  provider_baseline: u1-mvp-foundation-skeleton
  provider_completion: u2-recipe-planning-completion
  consumers: [u1 thin UI shell, u2, u3, u4]
  mechanism: Kotlin interfaces/use cases/data models; observable state via Flow/equivalent for Today/Plan/Grocery synchronization
  u1_baseline_guarantees:
    - "7-day plan structure (Mon-Fri Breakfast/Dinner; Sat-Sun Breakfast/Lunch/Dinner) (FR1)"
    - "Slot selection/replacement per slot without changing other slots"
    - "Shared slot selection source for Today/Plan synchronization baseline (R-06)"
    - "LockedRecipeSnapshot with locked recipe IDs/selections + plan context (ADR-006/R-01), not frozen ingredient data"
    - "Stale status when plan changes after snapshot used by current shopping list; exact warning semantics exposed for UI: 'Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.' (R-03/R-04)"
  u2_completion_owns:
    - "Full replacement/alternatives behavior and robust Today/Plan sync contracts"
    - "Deeper integration with RecipeCatalog completion and season/eligibility flows"
  operations:
    - name: observeWeeklyPlan
      output: Flow<WeeklyPlanState>
    - name: setSlotSelection
      input: { slotId: string, recipeId: string? }
      output: WeeklyPlanState
    - name: lockRecipesForShopping
      output: LockResult
      notes: "Explicit user action only. Returns LockedRecipeSnapshot or validation error."
    - name: getCurrentLockSnapshot
      output: LockedRecipeSnapshot?
    - name: observeLockAndStaleStatus
      output: Flow<LockStaleStatus>
  models:
    LockedRecipeSnapshot:
      required: [snapshotId, createdAt, lockedRecipeIds]
      properties:
        snapshotId: { type: string }
        createdAt: { type: string, format: date-time }
        lockedRecipeIds: { type: array, items: string }
        lockedRecipeSelections: { type: array, items: LockedSlotSelection }
        planContext: { type: object, nullable: true }
      invariant: "Snapshot stores IDs/selections+plan context; ingredients are re-resolved from RecipeCatalog at generation time (ADR-006/R-01)."
    LockStaleStatus:
      properties:
        snapshotId: { type: string, nullable: true }
        isStale: { type: boolean }
        staleWarning: { type: string, nullable: true }
        staleReason: { type: string, nullable: true }
  fallback: "No auto-regeneration. Unfilled slots remain empty."
  traceability: [FR1, FR2, FR3, FR9, NFR2, ADR-005, ADR-006, R-03, R-04, R-06, US1.1, US1.2, US3.1, US3.2]
```

### C4 — Shopping List Generation, Edits, and Export Baseline/Completion (shared-schema / in-app contract)

```yaml
shared-schema:
  contract: C4-ShoppingList
  provider_baseline: u1-mvp-foundation-skeleton
  provider_completion: u3-grocery-store-export-completion
  consumers: [u1 thin UI shell, u3, u4]
  mechanism: Kotlin interfaces/use cases/data models; observable state via Flow/equivalent
  u1_baseline_guarantees:
    - "Generate/regenerate only from current LockedRecipeSnapshot and only by explicit user action (R-03)"
    - "Re-resolve ingredient entries from RecipeCatalog for locked recipe IDs at generation time (ADR-006); mark missing/changed data explicitly, never fabricate"
    - "Basic deterministic merge/no-fabrication baseline and edit/check basics"
    - "Stale warning surfaced when plan modified after list generation: 'Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.'"
    - "Mandatory share sheet/copyable text fallback export using current edited list state (FR10)"
  u3_completion_owns:
    - "Full FR9 merge rules (normalized name + compatible units/amounts; else separate), no silent fabrication, incomplete ingredient data indication"
    - "Full edit/check/add/remove, regeneration confirmation when edits exist"
    - "Availability projection/remap rules (ADR-006/R-02): ShoppingList owns ShoppingItem.availabilityStatus/availabilityReason from StoreAvailability results keyed by item/query context; after edit/merge/regenerate remap where identity clear or set Unknown with reason until rechecked"
    - "S-ostoslista official-compatible attempt when feasible; fallback preserved"
  operations:
    - name: observeShoppingList
      output: Flow<ShoppingListState>
    - name: generateFromLock
      input: { snapshotId: string, confirmRegenerateIfEdited: boolean? }
      output: GenerationResult
    - name: regenerate
      input: { snapshotId: string, confirmRegenerateIfEdited: boolean }
      output: GenerationResult
    - name: updateItem
      input: { itemId: string, patch: ShoppingItemPatch }
      output: ShoppingListState
    - name: exportCurrentList
      input: { method: ExportMethod }
      output: ExportResult
      notes: "Always exports current edited list state, not pre-edit copy (FR10)."
  models:
    ShoppingItem:
      properties:
        shoppingItemId: { type: string }
        displayText: { type: string }
        normalizedName: { type: string, nullable: true }
        quantity: { type: string, nullable: true }
        unit: { type: string, nullable: true }
        checked: { type: boolean }
        editState: { type: string, nullable: true }
        ingredientDataStatus: { enum: [complete, incomplete, missing_source] }
        availabilityStatus: { enum: [present, not_found, unknown, not_checked] }
        availabilityReason: { type: string, nullable: true }
    ExportMethod:
      enum: [s_ostoslista_official_if_available, share_sheet, copy_text]
    ExportResult:
      properties:
        status: { enum: [success, fallback_used, failed_mapped] }
        methodUsed: { ref: ExportMethod }
        instructions: { type: string, nullable: true }
  fallback: "Core list generation/edit/export fallback must work with local/available data and without S-ostoslista official method."
  traceability: [FR9, FR10, NFR2, NFR5, ADR-005, ADR-006, R-03, R-04, US3.1, US3.2, US4.1, US4.2]
```

### C5 — Store Selection and Ingredient Presence Results (shared-schema / in-app contract)

```yaml
shared-schema:
  contract: C5-StoreAvailability
  provider_baseline: u1-mvp-foundation-skeleton (stub/manual/Unknown)
  provider_completion: u3-grocery-store-export-completion
  consumers: [u1 thin UI shell, u3, u4]
  mechanism: Kotlin interfaces/use cases/data models
  u1_baseline_guarantees:
    - "Manual store selection/representation contract and Unknown/manual-checklist fallback shape"
    - "Result/status model suitable for ShoppingList projection"
  u3_completion_owns:
    - "Nearest PRISMA store suggestion best effort when location permitted and data exists (FR7)"
    - "Manual selection always available (FR7)"
    - "Best-effort ingredient name/presence checks only (not stock quantity/guaranteed availability) (FR8)"
    - "Results keyed by item/query context and selected store; clear reason codes for Unknown/not found/inconclusive failures"
  operations:
    - name: observeSelectedStore
      output: Flow<SelectedStoreState>
    - name: selectStoreManually
      input: { store: PrismaStoreRef }
      output: SelectedStoreState
    - name: suggestNearestStore
      output: StoreSuggestionResult
      notes: "Best effort; may return unavailable/denied/unknown."
    - name: checkIngredientPresence
      input: { storeId: string, items: [PresenceCheckQuery] }
      output: PresenceCheckBatchResult
  models:
    PresenceCheckQuery:
      properties:
        itemRef: { type: string }
        queryName: { type: string }
        normalizedQueryName: { type: string, nullable: true }
    PresenceCheckResult:
      properties:
        itemRef: { type: string }
        queryName: { type: string }
        resultStatus: { enum: [present, not_found, unknown] }
        reason: { type: string, nullable: true }
        checkedAt: { type: string, format: date-time, nullable: true }
        checkScope: { type: string, nullable: true }
  ownership_note: "StoreAvailability returns raw/batch check results. ShoppingListComponent owns projection onto ShoppingItem.availabilityStatus/availabilityReason and remap/Unknown behavior after edit/merge/regenerate (ADR-006/R-02)."
  fallback: "Never block shopping-list editing/export. Manual checklist fallback always available (FR8/FR11)."
  traceability: [FR7, FR8, FR11, FR12, NFR2, NFR3, ADR-006, R-02, US3.3, US5.2, US5.3]
```

### C6 — Export Fallback and S-ostoslista Attempt (shared-schema / in-app + outbound adapter contract)

```yaml
shared-schema:
  contract: C6-Export
  provider_baseline: u1-mvp-foundation-skeleton
  provider_completion: u3-grocery-store-export-completion
  consumers: [u4-android-ui-experience, External: Android share/copy targets; External: S-ostoslista when feasible]
  mechanism: In-app export contract + Android platform abstraction; outbound S-ostoslista adapter behind interface
  u1_baseline_guarantees:
    - "Mandatory share sheet and/or copyable text fallback with clear instructions (FR10)"
    - "Exports current edited list state"
  u3_completion_owns:
    - "Investigate/attempt official S-ostoslista-compatible method when feasible (OQ4)"
    - "Record/return investigation outcome status and fall back safely on partial/unavailable/failure"
  operations:
    - name: exportShoppingList
      input: { list: ShoppingListState, preferredMethod: ExportMethod }
      output: ExportResult
  constraints:
    - "Working fallback export is mandatory regardless of S-ostoslista outcome."
    - "Export must not use a stale pre-edit copy."
    - "Official method feasibility depends on OQ4; contract must degrade safely."
  future_extraction_seam: "Keep export DTOs/use case interfaces stable enough that grocery list/shopping-export could be extracted later (Q1 direction), without committing a public API in MVP."
  traceability: [FR10, FR11, NFR2, ADR-001, OQ4, US4.1, US4.2]
```

### A1 — Sydänmerkki / Recipe Source Adapter (outbound adapter contract)

```yaml
shared-schema:
  contract: A1-SydanmerkkiRecipeSourceAdapter
  provider: External:Sydänmerkki (sydanmerkki.fi) and/or curated/imported fallback catalog
  consumer: u2-recipe-planning-completion adapter layer (baseline consumed by u1 path)
  mechanism: Outbound HTTP/data-source adapter behind Kotlin interface; local curated/imported catalog fallback
  operations:
    - name: fetchRecipesOrQuery
      output: AdapterResult<RecipeSourcePayload>
  behavior:
    timeout: bounded
    retry: "limited, safe, idempotent reads only"
    fallback: "Use curated/imported Sydänmerkki-aligned recipe fallback when live source unavailable/restricted/fails (FR4)."
    status_mapping: [ok, fallback_used, unavailable, restricted, timeout, parse_error_mapped]
  constraints:
    - "No secrets/endpoints hardcoded in source control; configuration externalized."
    - "Tolerate missing fields; map to explicit recipe/source status labels."
  traceability: [FR4, FR11, NFR2, NFR3, NFR5, OQ1, US2.5, US5.4]
```

### A2 — PRISMA Store/Presence Adapter (outbound adapter contract)

```yaml
shared-schema:
  contract: A2-PrismaAvailabilityAdapter
  provider: External:PRISMA store-location and ingredient presence data sources
  consumer: u3-grocery-store-export-completion adapter layer
  mechanism: Outbound adapter behind Kotlin interface
  operations:
    - name: suggestNearestStore
      input: { locationHint: LocationHint? }
      output: AdapterResult<PrismaStoreRef?>
    - name: checkPresenceByName
      input: { storeId: string, queries: [string] }
      output: AdapterResult<[PresenceLookupRow]>
  behavior:
    timeout: bounded
    retry: "limited, safe, idempotent lookups only"
    fallback: "Manual store selection always available; presence failures map to Unknown with reason/manual checklist (FR7/FR8)."
    status_mapping: [ok, not_found, unknown, unavailable, denied, timeout, inconclusive]
  constraints:
    - "Best-effort ingredient name/presence check only; not stock quantity or guaranteed in-store availability (FR8)."
    - "Nearest-store suggestion best effort only when location available/permitted and data exists (FR7)."
  traceability: [FR7, FR8, FR11, FR12, NFR2, NFR3, NFR5, OQ2, OQ3, US3.3, US5.2, US5.3]
```

### A3 — S-ostoslista Integration Adapter (outbound adapter contract)

```yaml
shared-schema:
  contract: A3-SOstoslistaAdapter
  provider: External:S-ostoslista integration surface (official method investigational)
  consumer: u3-grocery-store-export-completion
  mechanism: Outbound adapter/intent/deep-link/share integration attempt behind interface
  operations:
    - name: attemptOfficialExport
      input: { exportPayload: ShoppingListExportDto }
      output: AdapterResult<OfficialExportOutcome>
  behavior:
    timeout: bounded
    retry: "only if safe/idempotent per confirmed method; default none/limited"
    fallback: "Always fall back to C6 share sheet/copyable text with instructions (FR10)."
    status_mapping: [ok, not_supported, unavailable, failed_mapped, fallback_used]
  constraints:
    - "Official method depends on OQ4 feasibility/terms/format; contract must not assume availability."
    - "Do not block core shopping-list use on failure."
  traceability: [FR10, FR11, NFR2, OQ4, US4.2]
```

### A4 — Android Platform Abstractions (outbound adapter contract)

```yaml
shared-schema:
  contract: A4-AndroidPlatformAbstractions
  provider: External:Android platform services
  consumers: [u1, u2, u3, u4 via abstractions]
  mechanism: Platform abstractions (permission/location/date, persistence, share sheet, clipboard, intents)
  capabilities:
    - local_persistence
    - share_sheet_copy
    - location_permission_and_signal
    - date_time_locale_signal
  behavior:
    fallback: "Location denial/unavailable -> manual store selection and device-default/unknown season handling as applicable. Share/copy fallback remains available."
    privacy: "Purpose-limited location; minimal data; no MealPlanner account (FR12/NFR3)."
  constraints:
    - "No secrets or unnecessary personal data stored."
    - "Core planning/shopping/export fallback usable with local data where possible (FR11)."
  traceability: [FR5, FR7, FR10, FR11, FR12, NFR1, NFR2, NFR3, NFR5]
```

## Contract Ownership Rules

| Rule | Policy |
|---|---|
| Provider ownership | The provider unit owns its contract spec and implementation. U1 owns foundation contracts (C1 baseline, C2/C3/C4/C5 baseline shapes, C6 fallback export contract, shared result/status types). U2 owns RecipeCatalog/MealPlanning completion extensions (C2/C3 completion). U3 owns ShoppingList/StoreAvailability/export completion and S-ostoslista/PRISMA adapter contracts (C4/C5/C6 completion, A2/A3). U4 owns no domain business-rule contracts and consumes only. |
| Additive changes | Preferred. Add new optional fields/operations with safe defaults; consumers ignore unknown fields/data where practical. Keep stable IDs and core semantics (`LockedRecipeSnapshot` IDs+context, stale warning semantics, no auto-regeneration). |
| Breaking changes | Coordinated in the same change set before merge: provider and all consumers updated together; tests/contracts updated; no silent breakage across U1–U4 boundaries. |
| Adapter tolerance | External adapters are tolerant to missing/changed external fields and map outcomes to explicit fallback statuses/reason codes. Never throw raw adapter/platform errors across UI boundaries without mapping. |
| Consumer safety | UI presents explicit labels/states and next actions (NFR2). Domain consumers never fabricate missing recipe/nutrition/season/availability/ingredient data. |
| Parallel work safety | U2/U3 build against U1 baseline contracts; U4 builds against U1–U3 contracts. Any baseline contract change that affects dependents is treated as breaking/coordinated. |

## Traceability (R-03, R-04)

| Contract(s) | FR/NFR | ADR / Refined Mockup / Rule | User Stories | Notes |
|---|---|---|---|---|
| C1 | FR5, FR12, NFR3 | ADR-001, ADR-004, R-01/RM-09 season context | US2.3, US5.1, US5.2, US5.6 | Season default/override/reset; location purpose-limited/optional. |
| C2 | FR2–FR6, NFR2, NFR4 | ADR-005, ADR-006; R-03 (US2.2 AC2.2.3 trace) | US2.1–US2.5, US5.4 | **US2.2 AC2.2.3:** U1/U2 own weekday eligibility and explanation data/contracts (`timeVerifiability`, `weekdayEligible`, `weekdayExclusionReason`/explanation fields). **U4 presents** the user-visible explanation in recipe/detail/fallback UI states. |
| C3 | FR1–FR3, FR9, NFR2 | ADR-005, ADR-006/R-01, R-03, R-04, R-06 | US1.1, US1.2, US3.1, US3.2 | Lock snapshot stores IDs/selections+plan context; stale warning exact intent: `"Meal plan modified. Grocery list not up-to-date. Press the Regenerate button."`; no auto-regeneration. |
| C4 | FR9, FR10, NFR2, NFR5 | ADR-005, ADR-006/R-01,R-02, R-03, R-04 | US3.1, US3.2, US4.1, US4.2 | Re-resolve ingredients from catalog at generation; no fabrication; current edited list exported; availability projection owned by ShoppingList. |
| C5, A2 | FR7, FR8, FR11, FR12, NFR2, NFR3 | ADR-006/R-02, R-02 | US3.3, US5.2, US5.3 | PRISMA best-effort name/presence only (not stock/guarantee); manual store/checklist always available; Unknown with reason. |
| C6, A3 | FR10, FR11, NFR2 | ADR-001; Q1 extraction seam (grocery/export) | US4.1, US4.2 | S-ostoslista official method investigational (OQ4); share/copy fallback mandatory. |
| A1 | FR4, FR11, NFR2, NFR3 | — | US2.5, US5.4 | Sydänmerkki attempt + curated/imported fallback; clear source/fallback status. |
| A4, all | FR11, FR12, NFR1–NFR5 | ADR-001 | US5.1, US5.5, US5.6 | Android-only MVP; on-device/local where possible; minimal data; no MealPlanner account. |

**Carry-forward requirement (R-04):** Functional Design and test planning must preserve and extend these FR/NFR/ADR/US traces, including ADR-006 snapshot re-resolution/availability projection/stale semantics, FR3/FR9/R-03 business-rule ownership, and privacy/fallback NFR constraints.

## Open Questions

| Contract | Question | Blocks |
|---|---|---|
| A1 | OQ1: What Sydänmerkki access method, terms, and data fields are feasible/allowed (live query vs curated/import)? | Final A1 adapter behavior/data completeness; U2 completion details. |
| A2 | OQ2: What reliable PRISMA store-location data/source and nearest-store method will be used? | A2 nearest-store suggestion implementation quality. |
| A2 | OQ3: What PRISMA ingredient/presence data source is available, and what name-matching quality is acceptable? | A2 presence-check confidence/reason mapping. |
| A3/C6 | OQ4: Is there an official, feasible S-ostoslista import/share/deep-link/API method and required format? | A3 official export path; fallback remains mandatory. |
| C1/C2 | OQ5: What curated mapping/tags define season fit in Finland (and how location granularity affects it)? | Season-fit accuracy/unknown handling details. |
| C2 | OQ6: Which nutrition attributes are reliably available across Sydänmerkki/fallback data for the required planning aid? | Nutrition cue/planning-aid field contracts. |
| C2/C3 | OQ7: How will inconsistent/missing recipe time formats be normalized before applying the ≤ 30 minute weekday rule? | FR3 normalization/eligibility implementation details. |
| All (process) | OQ8: Who are secondary stakeholders and what review/decision cadence applies? | Stakeholder review timing; not a contract schema blocker. |

## Assumptions

- Kotlin is the implementation language family for in-app contracts (team posture: idiomatic Kotlin/Android; stack undecided at architecture level but Android-only MVP). Functional Design may refine coroutine/Flow usage.
- No inter-unit network contracts exist in MVP; all C1–C6 are in-process/module contracts inside the Android app.
- Outbound adapters (A1–A3) may be partially stubbed/fallback-first in U1 and completed in U2/U3 according to feasibility.
- `shared-schema` fenced blocks are used as the contract spec format for in-app and adapter contracts because they are not REST/HTTP provider APIs or AsyncAPI event streams; OpenAPI/AsyncAPI can be introduced later if a public/external API or evented backend is added.
- “Unknown” and fallback states are first-class contract outcomes, never exceptional UI dead ends (NFR2/FR11).
