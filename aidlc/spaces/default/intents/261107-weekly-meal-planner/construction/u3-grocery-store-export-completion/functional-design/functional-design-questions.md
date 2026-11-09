# Functional Design Questions — U3 `u3-grocery-store-export-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U3 — Grocery + store + export completion (`library`)  
**Stage:** Functional Design (construction, per-unit)  
**Date:** 2026-11-09

## Context
U3 completes ShoppingList (C4), StoreAvailability (C5), and Export (C6) on top of U1 baselines and C2/C3 contracts. Contracts C4–C6/A2–A3, ADR-006/R-01–R-03, FR7–FR11, NFR2/NFR3/NFR5, and project rules (explicit C4 apply-batch/remap/Unknown-after-edit-merge-regenerate; stable grocery/export extraction seam) are authoritative.

## Questions

**Q1. Merge identity and unit compatibility (FR9)**
How should deterministic merge decide "compatible units/amounts" for MVP without fabrication?
- A) Merge by normalized ingredient name + same normalized unit token (or both unitless) when amounts are numeric/addable; if units differ, amounts are non-numeric, or compatibility is uncertain, keep separate lines and mark merge reason/status explicitly.
- B) Merge by normalized name only and concatenate quantity text.
- C) Require a full unit-conversion engine in U3.
- D) Never merge duplicates.

[Answer]: A

**Q2. Regeneration confirmation when list has edits (FR9, AC3.2.4)**
When regenerate is requested and the list has user edits/checks/manual adds/removes, what is the contract?
- A) Require explicit confirmation (`confirmRegenerateIfEdited=true`/equivalent). Without confirmation return a typed "confirmation required" result and preserve the current edited list unchanged.
- B) Regenerate silently and discard edits.
- C) Always block regeneration if edited.
- D) Create a new list and keep old list hidden.

[Answer]: A

**Q3. Availability remap after edit/merge/regenerate (ADR-006/R-02)**
After item identity changes (edit/merge/split/regenerate/new add), how is `availabilityStatus` handled?
- A) ShoppingList owns projection: remap prior check result only when identity/query context is clearly unchanged; otherwise set `availabilityStatus=unknown` (or `not_checked` before check) with explicit `availabilityReason` until rechecked. StoreAvailability never mutates list items directly.
- B) Preserve old present/not_found status for any edited item.
- C) Clear all availability on every keystroke.
- D) Let StoreAvailability write ShoppingItem fields directly.

[Answer]: A

**Q4. Presence-check batch application (U3 C4/C5 integration)**
How should batch presence results be applied to the current edited list?
- A) Define an explicit C4 operation/use case (e.g., `applyPresenceCheckBatch`/`projectAvailabilityResults`) that takes keyed `PresenceCheckResult` items + selected store/scope, applies only to matching current items (by stable item/query key), preserves unrelated edits/checks, and sets non-matching/ambiguous post-edit items to `unknown` with reason. Operation is idempotent for same batch+scope where possible.
- B) UI directly mutates item availability fields.
- C) Regenerate the whole list whenever presence is checked.
- D) Store results outside the shopping-list contract only.

[Answer]: A

**Q5. Grocery/export extraction seam (project learning)**
What minimum stable seam should U3 define?
- A) Inward-pointing seam: export use-case interface, export payload DTO (current edited list state), and platform/share adapter port (Android share sheet/copy, S-ostoslista attempt). Domain/application contracts depend inward; adapters implement ports. No public API/backend introduced.
- B) Put export logic directly in UI and Android intents.
- C) Add a REST backend for export.
- D) Defer seam definition.

[Answer]: A

**Q6. PRISMA/S-ostoslista feasibility (OQ2–OQ4)**
Given investigational sources, what is the MVP behavior?
- A) Implement adapter boundaries with typed outcomes and safe fallbacks: PRISMA nearest-store/presence best effort name/presence only (never stock/guaranteed availability), manual store/checklist always available; S-ostoslista official attempt behind interface only when feasible/confirmed, mandatory share sheet/copyable text fallback with instructions. Record investigation outcome statuses.
- B) Hard-fail when official methods unavailable.
- C) Assume PRISMA returns stock quantities.
- D) Require S-ostoslista official integration for MVP.

[Answer]: A
