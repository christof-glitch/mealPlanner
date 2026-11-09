# U3 Functional Design — Rules (`u3-grocery-store-export-completion`)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Stage:** Functional Design (construction, per-unit)  
**Date:** 2026-11-09

## Sources
- [entities.md](entities.md)
- [U1 rules](../../u1-mvp-foundation-skeleton/functional-design/rules.md) (BR1.6–BR1.12 authoritative for lock/stale/merge baseline/share fallback invariants)
- [U2 rules](../../u2-recipe-planning-completion/functional-design/rules.md) (C3 slot changes mark stale, R-06)
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (FR7–FR11, FR12, NFR2, NFR3, NFR5)
- [stories.md](../../../inception/user-stories/stories.md) (US3.1–US3.3, US4.1–US4.2, US5.2–US5.3)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (C4–C6, A2–A3, ADR-006/R-01–R-03)
- [functional-design-questions.md](functional-design-questions.md) (Q1–Q6=A)

## Rules — source of truth

```yaml
rules:
  - id: BR3.1
    title: Locked-snapshot generation and ingredient re-resolution
    scope: C4
    type: state
    requirement_refs: [FR9, NFR2, NFR5]
    story_refs: [US3.1]
    statement: "Shopping list generation/regeneration occurs only by explicit user action from the current `LockedRecipeSnapshot` (ADR-006/R-01/R-03). C4 re-resolves ingredient entries from C2/RecipeCatalog for locked recipe IDs at generation time; snapshot stores IDs/selections + plan context, not frozen ingredient data. Missing/changed recipes or incomplete ingredient data are marked explicitly (`MISSING_SOURCE`/`INCOMPLETE`, generation notes, partial status as applicable) and never fabricated."
    error_or_fallback: "Invalid/missing snapshot => typed `INVALID_SNAPSHOT`. Missing/changed source data => `PARTIAL`/notes with no silent fabrication."
    source: "FR9 AC3.1.1-AC3.1.4; ADR-006; BR1.6; BR1.7; Q1=A"

  - id: BR3.2
    title: Deterministic merge without unsafe guessing
    scope: C4
    type: business
    requirement_refs: [FR9, NFR2]
    story_refs: [US3.1]
    statement: "C4 combines ingredients from all selected/locked recipes into one shopping list. Obvious duplicates are merged by normalized ingredient name + compatible units/amounts: merge when normalized names match and unit tokens are compatible (same normalized unit, or both unitless) and amounts are numeric/addable. If units differ, amounts/text cannot be safely combined, compatibility is uncertain, or data is incomplete, keep items as separate lines with explicit `MergeDecision`/reason (`UNIT_INCOMPATIBLE|AMOUNT_INCOMPATIBLE|MISSING_DATA|UNSAFE_TO_MERGE`). No silent fabrication."
    error_or_fallback: "Unmergeable/uncertain items remain separate with reason/status; incomplete data indicated."
    source: "FR9 AC3.1.2-AC3.1.4; BR1.7; Q1=A"

  - id: BR3.3
    title: Editable list and regeneration confirmation
    scope: C4
    type: business
    requirement_refs: [FR9, FR10, NFR2]
    story_refs: [US3.2, US4.1]
    statement: "Users can check off, edit text/quantity, add, and remove shopping items before export. When regeneration is requested and the list has user edits/checks/manual adds/removes (`hasUserEdits`), C4 requires explicit confirmation (`confirmRegenerateIfEdited=true` or equivalent). Without confirmation C4 returns `CONFIRMATION_REQUIRED` and preserves the current edited list unchanged. On confirmed regeneration, C4 rebuilds from the locked snapshot per BR3.1/BR3.2 and reapplies availability projection rules (BR3.5). Export always uses the current edited list state."
    error_or_fallback: "Missing confirmation => `CONFIRMATION_REQUIRED`, no state loss. Edits never exported from stale pre-edit copy."
    source: "FR9 AC3.2.1-AC3.2.4; FR10 AC4.1.3; BR1.6; BR1.8; Q2=A"

  - id: BR3.4
    title: Stale list status, exact warning, and no auto-regeneration
    scope: C4/C3 boundary
    type: state
    requirement_refs: [FR9, NFR2]
    story_refs: [US3.2, US1.2]
    statement: "When the meal plan or slot selections change after a shopping list has been generated, the list becomes stale. C4 exposes `ShoppingListStaleStatus` with exact warning text `Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.` (ADR-006/R-03). The system must not auto-regenerate; regeneration occurs only via explicit user action with required confirmations."
    error_or_fallback: "If stale signal update is delayed, preserve stale/unknown safe state on next observable update; never silently auto-regenerate."
    source: "FR9 AC3.2.2-AC3.2.3; ADR-006; BR1.9; BR2.8; Q2=A"

  - id: BR3.5
    title: ShoppingList owns availability projection and remap after edit/merge/regenerate
    scope: C4/C5 boundary
    type: state
    requirement_refs: [FR8, FR9, NFR2]
    story_refs: [US3.2, US3.3]
    statement: "StoreAvailability (C5/A2) returns raw/batch presence check results keyed by item/query context and selected store; it never mutates shopping items directly. ShoppingList (C4) owns `ShoppingItem.availabilityStatus/availabilityReason` projection. C4 provides an explicit operation (e.g., `applyPresenceCheckBatch`) that applies matching `PresenceCheckResult` items to the current edited list, preserves unrelated user edits/checks, and is safe for the same batch+scope when possible. After edit, merge, split, regeneration, or manual add changes item identity/query context, remap prior results only when identity is clearly unchanged; otherwise set `availabilityStatus` to `UNKNOWN` (or `NOT_CHECKED` before any check) with explicit `availabilityReason` until rechecked."
    error_or_fallback: "Ambiguous remap => `UNKNOWN`/`NOT_CHECKED` with reason; never keep stale present/not_found for changed identity."
    source: "FR8; ADR-006/R-02; contract-summary C4/C5; project rule C4 apply-batch/remap; Q3=A; Q4=A"

  - id: BR3.6
    title: PRISMA store selection best effort with manual fallback
    scope: C5/A2
    type: integration
    requirement_refs: [FR7, FR11, FR12, NFR2, NFR3]
    story_refs: [US3.3, US5.2]
    statement: "C5 suggests a nearest PRISMA store only as best effort when location is permitted/available and store-location data exists. Manual store selection is always available and is the fallback when permission is denied/unavailable or lookup fails. Store selection state is visible to grocery/availability flows. Location data is purpose-limited and optional."
    error_or_fallback: "Location denied/unavailable/lookup failure => no dead-end; expose manual selection and explicit suggestion status/message."
    source: "FR7 AC3.3.1-AC3.3.3; FR12; NFR3; Q6=A"

  - id: BR3.7
    title: PRISMA presence is name/presence only, never stock or guaranteed availability
    scope: C5/A2
    type: integration
    requirement_refs: [FR8, FR11, NFR2, NFR3, NFR5]
    story_refs: [US3.3, US5.3]
    statement: "C5/A2 performs best-effort ingredient name/presence checks for the selected PRISMA store when data is available. Result statuses are `PRESENT`, `NOT_FOUND`, or `UNKNOWN` only as supported by implemented matching/data confidence; inconclusive/unavailable/failed checks return `UNKNOWN` with reason. The system must not claim stock quantity, shelf location certainty, or guaranteed in-store availability. Manual checklist remains usable regardless of automated outcome."
    error_or_fallback: "Adapter/data failure => `UNKNOWN`/`PARTIAL`/`FAILED_MAPPED` batch with reasons and manual checklist path; no blocking of edit/export."
    source: "FR8 AC3.3.1-AC3.3.3; contract-summary A2; OQ2/OQ3; Q6=A"

  - id: BR3.8
    title: Export uses current edited list with mandatory share/copy fallback
    scope: C6/A3
    type: integration
    requirement_refs: [FR10, FR11, NFR2]
    story_refs: [US4.1, US4.2]
    statement: "Export operations consume `ShoppingListExportDto` built from the current `ShoppingListAggregate` edited state (checked/text/quantity/add/remove reflected). Mandatory fallback export is always available via Android share sheet and/or copyable text with clear S-ostoslista instructions. Official S-ostoslista-compatible method is attempted only behind an adapter boundary when feasible/confirmed (OQ4); any not-supported/unavailable/failed result degrades safely to fallback (`FALLBACK_USED`) without blocking core shopping-list use."
    error_or_fallback: "Official method unavailable/fails => mapped fallback result with instructions and method used; export never uses stale pre-edit copy."
    source: "FR10 AC4.1.1-AC4.1.3, AC4.2.1-AC4.2.3; BR1.12; OQ4; Q5=A; Q6=A"

  - id: BR3.9
    title: Grocery/export extraction seam is stable and inward-pointing
    scope: C4/C6 architecture boundary
    type: architecture
    requirement_refs: [FR9, FR10, NFR2, NFR5]
    story_refs: [US3.1, US4.1]
    statement: "U3 defines a minimum stable seam for future grocery/export extraction: export use-case interface, `ShoppingListExportDto`/`ExportLineItem` payload, and platform/share adapter port (share sheet/copy/S-ostoslista attempt). Domain/application contracts depend inward; platform and external integrations are adapters. No public API or backend is introduced in MVP."
    error_or_fallback: "Adapter-specific details must not leak into domain DTO contracts except explicit mapped status/instruction fields."
    source: "project rule `Functional Design 2026-11-08`; Q1 direction; Q5=A"

  - id: BR3.10
    title: Reliability, privacy, and no fabricated availability
    scope: C4/C5/C6/A2/A3
    type: reliability
    requirement_refs: [FR11, FR12, NFR2, NFR3, NFR5]
    story_refs: [US5.2, US5.3, US5.4]
    statement: "All U3 adapter/domain results are typed and UI-safe (`OK/PARTIAL/UNKNOWN/UNAVAILABLE/DENIED/FAILED_MAPPED`/equivalent). Core shopping-list edit/export fallback remains usable with local data and without PRISMA/S-ostoslista official methods. No secrets in code, no unnecessary personal data, location optional/purpose-limited. No fabricated ingredients, merges, presence, stock, or export success."
    error_or_fallback: "External failures => explicit reason/status + next action; preserve current list state."
    source: "FR11; FR12; NFR2; NFR3; NFR5; BR1.13"
```

## Rule notes
- BR3.* completes U3 scope; BR1.6–BR1.9/BR1.12 and ADR-006 exact stale warning text remain authoritative.
- BR3.5 implements the project correction requiring explicit C4 apply-batch/remap/Unknown-after-edit-merge-regenerate behavior.
- BR3.9 implements the project correction requiring a stable grocery/export extraction seam.