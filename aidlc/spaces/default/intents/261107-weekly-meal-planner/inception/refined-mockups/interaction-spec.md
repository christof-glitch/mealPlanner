# Interaction Specification — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner`  
**Stage:** Refined Mockups (inception)  
**Date:** 2026-11-08  
**Baseline:** Material 3 (Q1=A), WCAG 2.2 AA (Q2=B), full-screen replacement/store selection (Q3=B), portrait primary + landscape supported (Q4=A).

## Format
Component-level specs follow the project template in `.aidlc/knowledge/aidlc-design-agent/component-spec-template.md` (adapted for Android/Material 3 semantics).

## Global Interaction Rules
- **Navigation:** Bottom navigation preserves top-level destinations (Today/Plan/Grocery/User). Pushed screens provide Back. Full-screen flows (replacement, store selection) provide Back/Close and preserve user context.
- **Plan/Today synchronization (R-06):** Today (RM-02) and Plan (RM-03) read/write the same meal-slot selections (single source of truth). Unfilled slots remain empty until the user selects a recipe; no screen auto-fills or hides empty slots.
- **Slot context (R-06):** Any action that views/selects/replaces a recipe for a meal slot carries explicit slot context (day + meal type + slot id). Recipe detail (RM-04) and Alternatives (RM-05) opened from a slot can apply changes only to that slot and return to the originating screen (Today or Plan) focused on the updated slot/confirmation.
- **State transparency:** Missing/unknown/inconclusive data uses explicit labels (`Nutrition information unavailable`, `Season fit unknown`, `Unknown` + reason, time-unverifiable explanation). No fabricated data.
- **Fallbacks never block core flow:** Sydänmerkki → curated/import fallback; PRISMA presence → `Unknown`/manual checklist; S-ostoslista official → share/copy fallback; location denied → manual store selection; integration failures show status and next action.
- **Shopping list generation (R-03):** Shopping list is generated only after recipes are locked/finalized for shopping and only by explicit user action (**Generate shopping list** / **Regenerate**). No silent regeneration when plan changes. Regeneration with existing edits requires explicit confirmation and does not silently lose edits without notice.
- **Edited list integrity:** Export/share uses the current edited shopping-list state (FR10, US3.2/AC3.2.2).
- **Non-color-only status:** Every status badge/chip has text; icons supplement text.
- **Focus & announcements:** Screen transitions move focus to title/main heading; important status changes (permission result, fallback active, lock state, generation/regeneration, export result, item added/removed) are announced via accessibility-friendly patterns (e.g., snackbar/live region semantics as implemented).

---

## Bottom Navigation Bar

| Field | Value |
|---|---|
| Component | Bottom Navigation Bar |
| Description | Primary navigation across Today, Plan, Grocery, User |
| Category | navigation |

### States
| State | Description | Trigger |
|---|---|---|
| default | Four destinations visible; active destination indicated | app shell visible |
| selected | Current destination highlighted with label and indicator | user tap / current route |
| disabled/hidden | N/A for MVP top-level nav | — |

### Props / Inputs
| Prop | Type | Required | Default | Description |
|---|---|---|---|---|
| destinations | list | yes | Today,Plan,Grocery,User | Top-level routes |
| selectedId | string | yes | — | Active destination |
| onSelect | action | yes | — | Navigates to destination |

### Responsive Behaviour
| Breakpoint | Behaviour |
|---|---|
| phone portrait | Standard bottom navigation |
| phone landscape | Bottom navigation remains if space/safe area allows; otherwise follow Material guidance while keeping all 4 destinations reachable |

### Accessibility
| Requirement | Implementation |
|---|---|
| ARIA/semantics role | Navigation bar / tab semantics |
| Keyboard/focus interaction | Focusable destinations; activation selects route |
| Label / aria-label | Visible labels: Today, Plan, Grocery, User |
| Contrast ratio | WCAG 2.2 AA text/UI contrast |
| Screen reader | Announces destination name and selected state |
| Focus management | Focus moves to destination screen title/main content after navigation |

---

## Nutrition Planning Aid (FR6, US2.4/AC2.4.3–AC2.4.4)

| Field | Value |
|---|---|
| Component | Nutrition Balance Cue |
| Description | Minimal, non-medical, view-only planning aid that helps users consider nutrition while selecting meals using only reliably available nutrition attributes |
| Category | display / feedback |

### States
| State | Description | Trigger |
|---|---|---|
| default | Shows simple balance cues from reliably available attributes (e.g., veg-forward/fiber/protein tags or equivalent available cues) for visible selection context (Today or weekly Plan) | reliable nutrition attributes available |
| partial | Shows available cues and labels missing attributes | partial reliable data |
| unavailable | Shows `Nutrition information unavailable` | no reliable nutrition attributes for context |
| loading | Placeholder while plan/recipes load | data loading |

### Props / Inputs
| Prop | Type | Required | Default | Description |
|---|---|---|---|---|
| scope | enum | yes | — | `today` or `week` |
| cues | list | no | [] | Only reliably available attribute cues/tags; no fabricated values |
| missingDataLabel | string | no | `Some nutrition details unavailable` | Used when partial |
| unavailableLabel | string | no | `Nutrition information unavailable` | Used when no reliable data |
| interaction | enum | yes | view-only (MVP) | Optional simple filter only if backed by reliably available attributes and clearly labeled |

### Responsive Behaviour
| Breakpoint | Behaviour |
|---|---|
| phone portrait/landscape | Compact banner/card near top of Today/Plan content; wraps text; remains non-blocking |

### Accessibility
| Requirement | Implementation |
|---|---|
| ARIA role | Status/region with clear heading (e.g., “Nutrition balance cue”) |
| Keyboard interaction | Focusable if it contains a filter control; otherwise readable content |
| Label / aria-label | Announces scope, available cues, and missing/unavailable state |
| Contrast ratio | WCAG 2.2 AA |
| Screen reader | Does not imply medical advice; announces unavailable/partial labels |
| Focus management | Non-modal; does not steal focus on routine updates |

---

## Meal Slot Card

| Field | Value |
|---|---|
| Component | Meal Slot Card |
| Description | Displays a meal slot (day/meal type) with selected recipe summary and actions |
| Category | display / navigation |

### States
| State | Description | Trigger |
|---|---|---|
| default | Empty slot with CTA | no recipe selected |
| populated | Recipe summary, badges, actions | recipe selected |
| loading | Placeholder/skeleton | data loading |
| error | Clear error + retry/next action | load/fallback failure |
| unavailable | Recipe unavailable/fallback label | source issue |

### Props / Inputs
| Prop | Type | Required | Default | Description |
|---|---|---|---|---|
| dayLabel | string | yes | — | e.g., Mon, Sat |
| mealType | enum | yes | — | Breakfast/Lunch/Dinner (FR1 rules) |
| recipe | object \| null | no | null | Selected recipe summary |
| badges | list | no | [] | `≤30 min`, season fit/unknown (current season context), nutrition cue/unavailable, source |
| onViewRecipe | action | no | — | Opens RM-04 |
| onReplace | action | no | — | Opens RM-05 |

### Responsive Behaviour
| Breakpoint | Behaviour |
|---|---|
| phone portrait | Full-width stacked cards/list rows |
| phone landscape | Same content; increased horizontal padding/scroll as needed |

### Accessibility
| Requirement | Implementation |
|---|---|
| ARIA role | Group/list item with actionable controls |
| Keyboard interaction | Focusable View/Replace/Choose actions |
| Label / aria-label | Actions include slot context (e.g., “Replace Monday dinner”) |
| Contrast ratio | WCAG 2.2 AA |
| Screen reader | Announces slot, recipe, key badges, and available actions |
| Focus management | After replace, focus returns to updated slot or confirmation message |

---

## Replace Meal / Alternatives Flow (Full Screen)

| Field | Value |
|---|---|
| Component | Replace Meal Alternatives Screen |
| Description | Full-screen selection of eligible alternatives for one slot (including filling an empty slot); updates only that slot in the shared Plan/Today state |
| Category | navigation / input |

### States
| State | Description | Trigger |
|---|---|---|
| loading | Loading eligible alternatives | screen opened |
| populated | List of eligible alternatives | results available |
| empty | No eligible alternatives; keep current/leave empty + explanation | no results |
| error | Fetch/rule evaluation issue with retry/keep current | failure |
| selected | Recipe chosen; target slot updated | Select action |
| cancelled | No change; slot remains as before (filled or empty) | Cancel/Back |

### Props / Inputs
| Prop | Type | Required | Default | Description |
|---|---|---|---|---|
| slotContext | object | yes | — | Day, meal type, slot id, current recipe or `null` (empty slot), origin screen (Today/Plan) |
| eligibilityRules | list | yes | — | Slot fit, weekday total time ≤30 min, missing/ambiguous time excluded, seasonal/nutrition/source cues |
| alternatives | list | no | [] | Eligible recipes |
| onSelect | action | yes | — | Updates only target slot in shared state; returns to origin with focus/confirmation |
| onView | action | yes | — | Opens RM-04 with same `slotContext` |
| onCancel | action | yes | — | Returns to origin without change |

### Responsive Behaviour
| Breakpoint | Behaviour |
|---|---|
| phone portrait | Vertical list/cards; sticky/visible cancel/back |
| phone landscape | Scrollable list; rules summary remains visible near top where practical |

### Accessibility
| Requirement | Implementation |
|---|---|
| ARIA role | Screen with heading and selectable list |
| Keyboard interaction | Back/Cancel, focus through alternatives, View/Select actions |
| Label / aria-label | “Choose recipe for [meal] ([day])” or “Replace [meal] ([day])”; options labeled by recipe and key constraints |
| Contrast ratio | WCAG 2.2 AA |
| Screen reader | Announces rules summary, result count, and selection/cancel outcome |
| Focus management | Initial focus on screen title/main region; after selection/cancel, return focus to updated/target slot in Plan/Today |

### Navigation/Back-stack (R-06)
- RM-05 → **View** → RM-04: Back from RM-04 returns to RM-05 preserving slot context.
- RM-05 → **Select**: updates shared slot state and returns to RM-02 or RM-03 (origin) with focus on the updated slot/confirmation.
- RM-05 → **Cancel/Back**: returns to origin with no state change.

---

## PRISMA Store Selector (Full Screen)

| Field | Value |
|---|---|
| Component | PRISMA Store Selector Screen |
| Description | Manual store selection always; nearest suggestion best effort when permission/data available |
| Category | input / navigation |

### States
| State | Description | Trigger |
|---|---|---|
| default | Manual selection available | screen opened |
| permission-needed | Location not granted; purpose/status shown | no permission |
| suggesting | Looking up nearest store | permission granted + lookup |
| suggested | Nearest store shown with Use action | lookup success |
| lookup-failed | Clear status; manual selection remains | lookup/data failure |
| selected | Store confirmed | user confirms selection |

### Props / Inputs
| Prop | Type | Required | Default | Description |
|---|---|---|---|---|
| currentStore | object \| null | no | null | Selected PRISMA store |
| locationPermission | enum | yes | — | granted/denied/not asked |
| suggestion | object \| null | no | null | Nearest store (best effort) |
| onUseSuggestion | action | no | — | Selects suggested store |
| onSelectManual | action | yes | — | Confirms manually chosen store |
| onManagePermission | action | no | — | Opens system settings/help text |

### Responsive Behaviour
| Breakpoint | Behaviour |
|---|---|
| phone portrait | Search/list and suggestion cards stacked |
| phone landscape | Scrollable; selection actions remain visible |

### Accessibility
| Requirement | Implementation |
|---|---|
| ARIA role | Form/list selection screen |
| Keyboard interaction | Focusable search/results/actions |
| Label / aria-label | Purpose text for location; store options clearly named |
| Contrast ratio | WCAG 2.2 AA |
| Screen reader | Announces suggestion availability/failure and selected store |
| Focus management | Focus to confirmation/status after selection |

---

## Shopping List Editor

| Field | Value |
|---|---|
| Component | Shopping List Editor |
| Description | Combined checklist generated from locked recipes by explicit user action; supports edit/add/remove/check-off and measurable merge-safe generation (R-03, FR9) |
| Category | input / display |

### States
| State | Description | Trigger |
|---|---|---|
| recipes-not-locked | No generation available; prompt to lock recipes in Plan | recipes not locked/finalized |
| locked-not-generated | Ready to generate from locked snapshot | recipes locked, no list |
| loading | Generating/regenerating list | explicit generate/regenerate |
| default | Editable checklist | list available |
| edited | User has modified/check-off changes vs generated baseline | edit/toggle/add/remove |
| stale | Locked recipes changed after generation; list kept until explicit regenerate | recipe selection/lock changed |
| regenerate-confirm | Confirmation required before replacing edited list | regenerate with edits |
| empty | No items; CTA/reason | no selected recipes or empty result |
| partial | Some ingredient data incomplete; indicator shown | missing recipe ingredient data |
| availability-updated | Presence statuses shown/updated | PRISMA check/fallback |

### Props / Inputs
| Prop | Type | Required | Default | Description |
|---|---|---|---|---|
| recipesLocked | boolean | yes | — | Whether plan recipes are locked/finalized for shopping |
| lockedSnapshotId | string \| null | no | null | Identifies locked recipe snapshot used for generation |
| hasEdits | boolean | yes | false | Whether current list differs from generated baseline |
| items | list | yes | [] | Item text, quantity, checked, availability status/reason |
| mergeRule | object/text | yes | see merge rules below | Deterministic merge compatibility rules (FR9, US3.1/AC3.1.2–AC3.1.3) |
| onGenerate | action | yes | — | Explicit generate from locked snapshot |
| onRegenerate | action | yes | — | Explicit regenerate; confirmation flow if `hasEdits` |
| onToggle | action | yes | — | Check off |
| onEdit | action | yes | — | Edit text/quantity |
| onAdd | action | yes | — | Add item |
| onRemove | action | yes | — | Remove item |
| onExport | action | yes | — | Exports current edited state |

### Shopping List Merge Rules (measurable, FR9)

**Normalization (for candidate comparison):**
- Trim leading/trailing whitespace and collapse internal whitespace to one space.
- Compare ingredient names case-insensitively.
- Preserve original display text on the kept/separate line(s); normalization is only for matching.
- Do **not** auto-merge when normalization would be ambiguous (e.g., “tomato” vs “tomato sauce”, “milk” vs “oat milk”) — keep separate.

**Compatible units/amounts (safe to combine):**
- Merge only when normalized names match the same ingredient identity **and** units/amount representations are compatible:
  - same normalized unit token (e.g., `g`+`g`, `ml`+`ml`, `pcs`+`pcs`), and both amounts are parseable numeric values → combine amounts and keep the unit.
  - countable items with same count unit or clearly equivalent count representation (e.g., `2 pcs` + `1 pcs`) → combine counts.
- If either amount is missing/ambiguous, units differ, or conversion is not explicitly supported by implementation, **do not combine quantities**; keep separate lines (or keep a single line only if items are exact duplicates including amount/unit text).

**Concrete examples:**
| Case | Items | Result | Reason |
|---|---|---|---|
| Mergeable same unit | `Spinach 100 g` + `spinach 50 g` | `Spinach 150 g` (or equivalent combined line) | Same normalized name + compatible unit + parseable numeric amounts |
| Mergeable count | `Egg 2 pcs` + `Egg 1 pcs` | `Egg 3 pcs` | Same item/count unit |
| Keep separate: different/incompatible units | `Milk 2 dl` + `Milk 1 l` | Separate lines (unless explicit supported conversion exists) | No implicit conversion in MVP rule |
| Keep separate: ambiguous ingredient | `Tomato 2 pcs` + `Tomato sauce 200 g` | Separate lines | Different ingredient identity |
| Keep separate: ambiguous/missing amount | `Onion 1 pcs` + `Onion` | Separate lines (or retain both as entered) | Cannot safely combine quantity without fabrication |

**No silent fabrication:** never invent units, conversions, or quantities. Incomplete ingredient data is indicated (US3.1/AC3.1.3).

### Generation & Regeneration (R-03, AC3.2.3)

| Trigger | Behavior |
|---|---|
| Recipes not locked | **Generate shopping list** disabled/unavailable with status “Lock recipes in Plan before generating your shopping list.” |
| Recipes locked, no list | User taps **Generate shopping list** → app generates combined list from locked recipe snapshot (FR9 merge rules). |
| List exists, no user edits | User may tap **Regenerate shopping list** → regenerates from current locked snapshot (confirmation optional). |
| List exists, user has edits/check-offs | User taps **Regenerate shopping list** → show confirmation dialog: “Regenerate from locked recipes? Your current list edits/check-offs may be replaced.” Actions: **Cancel** (keep current edited list) and **Regenerate and replace** (replaces with newly generated list). No silent loss. |
| Locked recipes changed after generation | Show status “Locked recipes changed — regenerate to update the shopping list.” Do **not** auto-regenerate. Keep current list and all edits/check-offs until user explicitly regenerates (with confirmation if edited). |
| Export | Always exports the **current edited list** displayed. |

### Responsive Behaviour
| Breakpoint | Behaviour |
|---|---|
| phone portrait | Vertical checklist rows with inline controls |
| phone landscape | Same; ensure edit controls/touch targets remain accessible |

### Accessibility
| Requirement | Implementation |
|---|---|
| ARIA role | Checklist/list with editable controls; confirmation dialog for destructive regenerate |
| Keyboard interaction | Focusable checkbox/edit/delete/add/generate/regenerate controls; dialog focus trap as applicable |
| Label / aria-label | Each control names the item (e.g., “Remove lentils”); regenerate warning clearly read |
| Contrast ratio | WCAG 2.2 AA |
| Screen reader | Announces lock/generation status, edits, add/remove, availability status including `Unknown` reason, and regenerate consequences |
| Focus management | After generate/regenerate, focus moves to list heading/status; after add/remove, move focus to logical next item or add control |

---

## Share / Export Actions

| Field | Value |
|---|---|
| Component | Export Actions |
| Description | Official S-ostoslista-compatible method when feasible; mandatory share/copy fallback |
| Category | feedback / navigation |

### States
| State | Description | Trigger |
|---|---|---|
| default | Export options available | list exists |
| official | Official method offered/used | method feasible |
| fallback | Share sheet/copy text + instructions | official unavailable/failed/unknown |
| success | Export completed/handed off | success |
| cancelled | User cancelled; list preserved | cancel |
| error | Export failed with fallback/retry | failure |

### Props / Inputs
| Prop | Type | Required | Default | Description |
|---|---|---|---|---|
| listSnapshot | object/text | yes | — | Current edited shopping list |
| officialCapability | enum | yes | — | available/partial/unavailable/unknown |
| onOfficialExport | action | no | — | Official S-ostoslista-compatible path |
| onShareSheet | action | yes | — | Android share sheet |
| onCopyText | action | yes | — | Copyable text fallback |
| instructions | text | yes | — | Clear S-ostoslista usage instructions |

### Responsive Behaviour
| Breakpoint | Behaviour |
|---|---|
| phone portrait/landscape | Actions visible near list/export area; dialogs/sheets follow system behavior; copy confirmation visible |

### Accessibility
| Requirement | Implementation |
|---|---|
| ARIA role | Buttons/actions; status messages announced |
| Keyboard interaction | Focusable export actions; dialog dismissal accessible |
| Label / aria-label | Distinguish official method vs fallback copy/share |
| Contrast ratio | WCAG 2.2 AA |
| Screen reader | Announces fallback reason/instructions availability and result |
| Focus management | Return focus to export area after completion/cancel |

---

## Interaction Flow Notes (by key story)

| Story | Interaction summary |
|---|---|
| US1.1/US1.2 | Plan/Today cards show slot structure and per-slot Replace/Choose/View; **Today and Plan are synchronized** (shared slot state). Unfilled slots remain empty until selected (R-06). Selection/replacement updates only the target slot and is reflected in both views. |
| US2.2 | Weekday candidates filtered by total time ≤30 min; missing/ambiguous excluded with explanation visible in alternatives/details. |
| US2.3 / AC2.3.4 | Default season context uses device date and location. RM-09 provides explicit override control (`Override season context` off by default; when on set Season + optional Area/region label; Reset to device default). Current context is visible in Plan/Today/Recipe (`Using device date/location` or `Using override…`) and affects season fit display; `Season fit unknown` shown when undetermined. |
| US2.4 / AC2.4.3–AC2.4.4 | Nutrition cues shown when available; `Nutrition information unavailable` when no reliable data. **Nutrition Balance Cue** (view-only MVP) appears on Today/Plan from reliably available attributes; partial data shows available cues + `Some nutrition details unavailable`; never blocks selection and makes no medical claims. |
| US3.1/US3.2 / AC3.2.3 (R-03) | User locks/finalizes recipes in Plan (RM-03), then explicitly taps **Generate shopping list** in Grocery (RM-06). List is generated from the locked snapshot using measurable merge rules (no silent fabrication). Inline edits/check-offs apply to the current list. If recipes change after generation, show stale status and do not auto-regenerate. If user regenerates after edits, require explicit confirmation (“edits/check-offs may be replaced”) with **Cancel** or **Regenerate and replace**; no silent loss. Export always uses current edited list. |
| US3.3/US5.2/US5.3 | Full-screen store selection; nearest suggestion best effort; name/presence check shows present/not found/`Unknown` + reason; manual checklist fallback. |
| US4.1/US4.2 | Official S-ostoslista attempt when feasible; fallback share/copy always; instructions shown; cancel preserves list. |
| US5.1/US5.5/US5.6 | Location permission purpose + manual alternative; RM-02/RM-03 include explicit first-launch/no-local-data + unavailable-remote-data state with next actions (Retry, bundled fallback catalog if present, manual planning where feasible) without blocking feasible local actions; local/on-device MVP, minimal data. |
| R-06 navigation | RM-04/RM-05 always carry slot context when opened from a slot. RM-05 Select/Cancel returns to origin (Today/Plan) with correct focus; RM-05→RM-04 Back returns to RM-05; RM-04 “Use for this meal slot” updates shared slot and returns to origin. |
