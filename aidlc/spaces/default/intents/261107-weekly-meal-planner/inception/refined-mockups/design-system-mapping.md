# Design System Mapping — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner`  
**Stage:** Refined Mockups (inception)  
**Date:** 2026-11-08  
**Baseline:** Material 3 (Material You) — Q1=A. Neutral tokens/placeholders are defined for implementation to finalize (brand color, typography scale, shape, elevation refinements).

## Scope
This mapping covers MVP screens/components in [mockups.md](mockups.md) and [interaction-spec.md](interaction-spec.md). It is a **design-to-implementation guide**, not final visual branding.

## Token Mapping (Neutral / Placeholder)

| Token group | MVP guidance | Notes |
|---|---|---|
| Color | Material 3 dynamic/neutral color scheme; ensure WCAG 2.2 AA contrast for text, icons, and UI components. | Status uses text + iconography, not color alone. |
| Typography | Material 3 type scale; clear hierarchy: screen title, section heading, card title, body, label, supporting/error text. | Scannable labels and concise status copy. |
| Spacing | 4dp grid; consistent padding for cards/lists/touch targets. | Minimum touch target 48dp where practical (Android/WCAG-aligned). |
| Shape | Material 3 defaults (small/medium/large) for cards, sheets, dialogs, buttons. | Full-screen flows for replacement/store selection (Q3=B). |
| Elevation/outline | Use Material 3 surfaces; maintain separation for cards and status banners. | Keep fallback/error states visible. |
| Iconography | Material Symbols or equivalent consistent icon set; all icons paired with text labels for critical status/actions. | E.g., unknown/available/not found indicators. |
| Motion | Subtle transitions (screen push, list updates, snackbar feedback); respect reduced-motion preferences if implemented. | Avoid motion as sole feedback. |

## Component Mapping

| UI need | Material 3 component/pattern | Used in | Notes |
|---|---|---|---|
| Primary app navigation | Navigation bar (bottom) | All top-level screens | Today, Plan, Grocery, User. |
| Screen header/back | Top app bar (small/center-aligned as appropriate) | All screens; pushed flows | Back for Recipe detail, Replace, Store selection. |
| Meal/recipe summary | Card / list item | Today, Plan, Alternatives | Include title, time, badges, actions. |
| Status/badges | Assist/filter/suggestion chips or compact badges + text | Plan, Recipe, Grocery | `≤30 min`, source, `Season fit unknown`, `Nutrition information unavailable`, availability `Unknown`. Always text-readable. |
| Actions | Filled/tonal/outlined/text buttons; icon buttons with labels/tooltips as needed | All screens | Primary actions visible (Replace, View recipe, Select store, Share/export). |
| Recipe alternatives / store selection | Full-screen navigated surface (Q3=B) with lists/cards and selection controls | RM-05, RM-07 | Not bottom sheets for these flows. |
| Recipe details | Pushed detail screen with sections/cards/dividers | RM-04 | Ingredients, prep notes, metadata. |
| Checklist | Checkbox list items + editable text fields/rows | Grocery | Check off, edit quantity/text, add/remove. |
| Availability states | List item supporting text/status row | Grocery | Present / not found / `Unknown` + reason; best-effort name/presence only. |
| Location permission / notices | Card + dialog/system permission prompt + snackbar/status text | Onboarding, User, Grocery | Purpose text required (FR12). |
| Empty/loading/error | Progress indicator, empty state layout, error state with retry/next action | All primary screens | US5.5 includes first-launch/no-local-data state. |
| Export/share | Android share sheet (system) + in-app copy-text action and instruction text | Grocery/export | Official S-ostoslista method when feasible; fallback mandatory (FR10). |
| Settings/preferences | List/settings rows, switches/selectors as needed | User | Local/on-device MVP preferences. |
| Confirmation/status feedback | Snackbar / inline status message | Edits, export, fallback activation | Announce important outcomes accessibly. |

## Pattern Mapping

| Pattern | MVP rule | Requirement/story trace |
|---|---|---|
| Progressive disclosure | Show summary first (plan/slot/card), details on demand (recipe detail, alternatives). | US1.2, US2.1 |
| Inline editing | Shopping list edits happen in context before export; export uses current edited state. | US3.2, FR9, FR10 |
| Explicit fallback states | Every integration/data limitation has a visible label and next action. | FR4, FR8, FR10, FR11, US5.x |
| Permission with purpose + alternative | Location requested with purpose; manual PRISMA selection always available. | FR7, FR12, US5.1 |
| Safe uncertainty | Unknown/inconclusive results labeled clearly; no stock/availability guarantees. | FR5, FR6, FR8 |

## Implementation Notes
- Use idiomatic Android/Kotlin UI patterns and project tooling when established (team practices).
- Keep integration endpoints/secrets/config out of source UI specs; handle via adapters/config.
- Finalize tokens (color roles, typography, spacing scale, component variants) in design/dev implementation while preserving accessibility and fallback clarity.
- If Material 3 component availability differs by library/version, preserve equivalent semantics and accessibility behavior.