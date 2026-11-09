# Refined Mockups — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner`  
**Stage:** Refined Mockups (inception)  
**Date:** 2026-11-08  
**Fidelity:** Mid-fidelity interaction-oriented mockups (structured screen specs; implementation uses Material 3 components per [design-system-mapping.md](design-system-mapping.md))

## Sources
- [Rough wireframes](../../ideation/rough-mockups/wireframes.md)
- [User flow](../../ideation/rough-mockups/user-flow.md)
- [User stories](../user-stories/stories.md) (US1.1–US5.6)
- [Requirements](../requirements-analysis/requirements.md) (FR1–FR12, NFR1–NFR5)
- [Refined mockups Q&A](refined-mockups-questions.md): Q1=A Material 3, Q2=B WCAG 2.2 AA, Q3=B full-screen replacement/store selection, Q4=A portrait primary + landscape supported.

## Global Layout & Navigation
- **Pattern:** Android bottom navigation with 4 destinations: **Today**, **Plan**, **Grocery**, **User**.
- **Top app bar:** Screen title/context, back navigation for pushed screens, optional contextual actions.
- **Primary content:** Scrollable lists/cards; key actions visible (replace, view recipe, select store, share/export).
- **Full-screen flows (Q3=B):** Recipe alternatives/replacement and PRISMA store selection are navigated full-screen flows (not modal bottom sheets). Recipe detail is a pushed screen.

## Screen Inventory

| ID | Screen | Primary stories | Entry points |
|---|---|---|---|
| RM-01 | Onboarding / Permissions | US5.1, US5.5, US5.6 | First launch |
| RM-02 | Today’s meals | US1.1 (context), US1.2, US2.1 | Bottom nav; post-onboarding |
| RM-03 | Plan (week view) | US1.1, US1.2, US2.2–US2.5 | Bottom nav |
| RM-04 | Recipe detail | US2.1–US2.5 | Meal slot/alternative “View recipe” |
| RM-05 | Replace meal / Alternatives (full screen) | US1.2, US2.2–US2.5 | “Replace” / “Choose different” |
| RM-06 | Grocery | US3.1–US3.3, US4.1, US5.2–US5.3 | Bottom nav |
| RM-07 | Select PRISMA store (full screen) | US3.3, US5.1, US5.2 | Grocery “Choose manually/Change” |
| RM-08 | Share / Export (system + fallback UI) | US4.1, US4.2 | Grocery share/export actions |
| RM-09 | User (preferences/permissions/privacy) | US2.3, US5.1, US5.6 | Bottom nav |

## RM-01 — Onboarding / Permissions
**Purpose:** Introduce value, explain minimal data/privacy, request location with purpose, allow manual-later path.

| Region | Content | Notes |
|---|---|---|
| Header | “Meal Planner”, optional Skip | Skip allowed after privacy/data notice is shown (user flow). |
| Body | Value summary: 7-day seasonal/nutrition-aware meal suggestions, shopping list, S-ostoslista export. | Concise, scannable. |
| Location card | Purpose: “Find nearby PRISMA stores.” Actions: **Allow location**, **Choose store manually later**. | Permission optional; manual path always available (FR7, FR12, US5.1). |
| Privacy card | “Data stored on device (MVP). Minimal data collection.” Link: **View privacy details**. | GDPR-aware clarity. |
| Primary action | **Continue** | Proceeds to Today/Plan. |

**States:** first run; permission granted; permission denied; privacy details viewed; first-launch no local data handled by clear loading/empty/error state and next action (US5.5/AC5.5.3) when remote data is unavailable.
**Key UI copy cues:** purpose text for location; no false guarantees.

## RM-02 — Today’s meals
**Purpose:** Quick daily view of today’s slots. **Always synchronized with Plan (RM-03):** same underlying meal-slot selections/single source of truth (R-06).

| Region | Content | Notes |
|---|---|---|
| Header | “Today — [day, date]” | Current device date context; season context indicator (device default or user override per RM-09/AC2.3.4). |
| Optional cue | Nutrition/season cue banner (when available) | Labeled; missing data not hidden as false positive. |
| Nutrition planning aid (FR6, US2.4/AC2.4.3–AC2.4.4) | **“Nutrition balance cue”** (view-only MVP): simple cue from reliably available nutrition attributes across visible/today selections (e.g., balance tags such as veg-forward/fiber/protein cues when present). | Shows only attributes with reliable data. Partial data: show available cues + `Some nutrition details unavailable`. No data: `Nutrition information unavailable`; does not block viewing/selecting/replacing meals. Non-medical. |
| Meal slot cards | Slot label (Breakfast/Lunch/Dinner by day type). **Filled slot:** recipe title, time, nutrition cue, source label, **Replace**, **View recipe**. **Unfilled slot (R-06):** remains empty with “No recipe selected” + **Choose recipe** (never auto-filled by a different screen state). | Weekend 3 slots, weekday 2 slots (FR1). Selecting/replacing from Today updates the same slot shown in Plan. |
| Bottom nav | Today active | |

**States:** populated (all/some slots filled); partially filled with explicit empty slots; loading; recipe unavailable (fallback/source label); integration status visible when relevant.
**First launch / no local data + remote unavailable (US5.5/AC2.5.3, AC5.5.3):** explicit state with status copy (e.g., “Couldn’t load meal suggestions and no saved/local data is available yet”) and concrete next actions: **Retry**, **Use bundled fallback catalog** (if present in MVP build/data pack), and/or **Continue with manual planning** where feasible (browse/add available recipes; leave slots empty until filled). This state does not block unrelated local actions that are feasible in MVP. Unavailable remote data with existing local data shows local/cached content plus a non-blocking refresh/retry status.

## RM-03 — Plan (week view)
**Purpose:** 7-day plan with slot rules, recipe links, and replacement entry points. **Always synchronized with Today (RM-02):** same underlying meal-slot selections/single source of truth (R-06). Unfilled slots remain empty until the user selects a recipe.

| Region | Content | Notes |
|---|---|---|
| Header | “Week Plan” | Season/region context indicator: `Using device date/location` by default, or `Using override: [season], [region/area if set]` when set in RM-09 (AC2.3.4). |
| Nutrition planning aid (FR6, US2.4/AC2.4.3–AC2.4.4) | **“Weekly nutrition balance cue”** (view-only MVP): simple cue/filter context from reliably available nutrition attributes across selected week recipes (e.g., shows available balance cues/tags used for planning; no medical claims). | If a simple filter is supported by available attributes, label it clearly (e.g., “Show recipes with [available attribute]” in RM-05 context). Partial data: show available cues/filters and label missing attributes as `Some nutrition details unavailable`. No reliable data: `Nutrition information unavailable`; selection not blocked. |
| Day sections | Mon–Fri: Breakfast, Dinner; Sat–Sun: Breakfast, Lunch, Dinner. | FR1 structure enforced. |
| Slot row/card | **Filled slot:** recipe title, total time badge, `≤30 min` weekday badge where applicable, nutrition cue, season fit/unknown, source label, **Replace**, **View recipe**. **Unfilled slot:** `No recipe selected` + **Choose recipe** (remains empty; R-06). | FR2–FR6 signals. Season fit reflects current device/override context. |
| Actions | Per-slot replace/view/select; no full-week regeneration in MVP. **Lock recipes for shopping list** control (MVP): confirms current selected recipes as the source for Grocery generation (see RM-06). | Slot replacement preserves other selections. Locking does not auto-generate the list. |
| Bottom nav | Plan active | |

**States:** all/some/none slots filled (explicit empty slots); loading; no suggestions/fallback catalog; missing data labels (`Nutrition information unavailable`, `Season fit unknown`, time-unverifiable explanation); recipes locked for shopping / unlocked for editing (clear status).
**First launch / no local data + remote unavailable (US5.5/AC2.5.3, AC5.5.3):** explicit state with status copy and concrete next actions: **Retry**, **Use bundled fallback catalog** (if present), and/or **Continue with manual planning** where feasible. Does not block unrelated local actions feasible in MVP. Unavailable remote data with existing local data shows local/cached plan/content plus a non-blocking refresh/retry status.

## RM-04 — Recipe detail (pushed screen)
**Purpose:** Support selection decision with constraints, ingredients, prep notes, and source/fallback transparency.

| Region | Content | Notes |
|---|---|---|
| Top bar | Back, “Recipe” | Pushed navigation. |
| Summary | Title, source (Sydänmerkki/fallback), season fit or `Season fit unknown` (evaluated using current season context: device date/location default or RM-09 override), total prep/cooking time, weekday `≤30 min` eligibility indicator when relevant. | FR3–FR5, AC2.3.4. |
| Nutrition | Nutrition cues/info when available; otherwise `Nutrition information unavailable`. | FR6; no blocking. Supports FR6 planning context when shown from alternatives/plan. |
| Ingredients | List; missing data labeled. | No fabrication. |
| Preparation notes | Steps/text when available. | |
| Actions | **Use for this meal slot** (primary when opened with slot context from RM-02/RM-03/RM-05), **Choose different**. | Updates the same slot selection used by Today/Plan (R-06). “Choose different” opens RM-05 for that same slot. |

**Slot context & back-stack (R-06):** RM-04 is always opened with slot context when reached from a meal slot (Today/Plan) or from an alternative for a slot (RM-05). “Use/Select for this meal slot” updates that slot and returns to the originating screen (Today or Plan) with focus on the updated slot/confirmation. If opened without slot context (future/browse case not required in MVP), selection action is hidden and only Back is available. Back returns to the previous screen; from RM-05→RM-04, Back returns to RM-05 preserving slot context.

**States:** loaded; loading; source unavailable/fallback; missing nutrition/time/season data; season context default or overridden.

## RM-05 — Replace meal / Alternatives (full screen, Q3=B)
**Purpose:** Choose/fill or replace a recipe for one slot with an eligible alternative (also used for unfilled slots via **Choose recipe**).

| Region | Content | Notes |
|---|---|---|
| Top bar | Back/Close, “Choose recipe for [meal] ([day])” or “Replace [meal] ([day])” | Full-screen flow; slot context always present. |
| Rules summary | Fits selected slot; weekday Mon–Fri Breakfast/Dinner uses total time ≤30 min; excludes missing/ambiguous weekday time with explanation; seasonal/nutrition cues shown when available; source labeled. | FR2–FR6, US1.2/US2.2. |
| Alternatives list | Selectable recipe cards: title, time, badges/cues, source, **View**, **Select**. | One slot only; other selections preserved. **View** opens RM-04 with same slot context. |
| Footer/actions | **Cancel** / **Keep current** (when replacing) | |
| Empty/error | “No eligible alternatives” + explanation + **Keep current/Leave empty** + retry. | Clear next action. |

**Slot context & sync (R-06):** Opened from a specific Today/Plan slot (filled or empty). **Select** updates that slot in the shared Plan/Today state and returns to the originating screen with focus on the updated slot/confirmation. Cancel/Back returns without changes. Unfilled slots remain empty until selected.

**States:** loading alternatives; populated; no alternatives; selected updates only target slot; cancelled (no change).

## RM-06 — Grocery
**Purpose:** Combined shopping list, PRISMA store context, best-effort name/presence checks, export/share. **R-03 fix:** shopping list is created only from **locked/finalized recipes** and only when the user explicitly generates it (button).

| Region | Content | Notes |
|---|---|---|
| Header | “Grocery” | |
| Plan readiness card | Shows whether recipes are locked for shopping list (`Recipes locked: [n] slots selected` or `Recipes not locked`). **Lock recipes in Plan** link/action (navigates to RM-03) when not locked. | No list is auto-generated from in-progress plan changes. |
| Generate action | Primary button **Generate shopping list** (enabled when recipes are locked and at least one selected recipe with ingredient data context exists; otherwise disabled with reason). **Regenerate shopping list** shown after a list exists. | Explicit user action required (R-03). |
| PRISMA store card | Selected store, nearby suggestion status (if available), **Use suggested**, **Choose manually/Change**, location status/manage. | FR7, US5.1, US5.2. Full-screen store selection (RM-07). |
| Availability section | Per-ingredient status: present/not found/`Unknown` with reason; best-effort name/presence only (not stock quantity). **Manual checklist** fallback. | FR8, US3.3, US5.3. |
| Combined list | Checklist items with text/quantity; **add**, **edit**, **remove**, check off. Merge duplicates only when measurable compatibility rules pass; otherwise keep separate lines. | FR9, US3.1, US3.2. Edits apply to the current generated list. |
| Export actions | **Share/export to S-ostoslista** (official method if feasible) + **Copy text/share fallback** with instructions. | FR10, US4.1, US4.2. Uses current edited list. |
| Bottom nav | Grocery active | |

**Generation / post-edit behavior (R-03, AC3.2.3, no silent loss):**
- List generation uses the **locked recipe selection snapshot** only. Changing recipes in Plan/Today does not change an existing generated list silently.
- If the user edits/checks off list items and later taps **Regenerate**, show a confirmation: “Regenerate shopping list from locked recipes? Your current list edits/check-offs may be replaced or need review.” Choices: **Cancel** (keep current list), **Regenerate and replace list** (explicit). MVP default: replace with newly generated list after explicit confirmation; preserve is not implied.
- If locked recipes become stale/changed (user unlocks/edits selections), show status “Locked recipes changed — regenerate to update the shopping list.” Do not auto-regenerate; keep current list until explicit regenerate.
- Export always uses the **current edited list** shown to the user.

**States:** recipes not locked; locked, not generated; generated/editable; list edited; stale after recipe changes (status only, no auto change); regenerating with confirmation; no store selected; location denied (manual only); availability unknown/fallback; empty list/incomplete ingredient data; export success/fallback/failure with retry.

## RM-07 — Select PRISMA store (full screen, Q3=B)
**Purpose:** Manual selection always available; nearest suggestion best effort when permission/data available.

| Region | Content | Notes |
|---|---|---|
| Top bar | Back, “Select PRISMA store” | |
| Location/suggestion | Permission/purpose/status; suggested nearest store card when available with **Use store**. | Best effort; not required. |
| Search/browse | Manual store search/selection UI (implementation-defined; list/map not required in MVP mockup). | Must work without location. |
| Selected state | Current selection highlighted; **Confirm** | |
| Empty/error | “Could not suggest nearby stores” + manual selection remains. | Clear next action. |

**States:** permission granted with suggestion; permission denied/manual only; lookup failure; store selected.

## RM-08 — Share / Export
**Purpose:** Official S-ostoslista-compatible method when feasible; mandatory fallback always works.

| Region | Content | Notes |
|---|---|---|
| Trigger | Grocery export actions | |
| Official path (if feasible) | App-provided S-ostoslista-compatible action; success confirmation; return without losing list. | FR10, OQ4 implementation outcome. |
| Fallback | Android share sheet and/or **Copy list text**; instructions for using with S-ostoslista (user has account). | Always available. |
| State messaging | “Official S-ostoslista method unavailable — using share/copy fallback.” when applicable. | No blocking. |

**States:** official available; official unavailable/failed; fallback success; user cancels (list preserved).

## RM-09 — User (preferences, permissions, privacy)
**Purpose:** Manage season override context, nutrition cues visibility preference (if exposed), permissions, and privacy info.

| Region | Content | Notes |
|---|---|---|
| Header | “User” | |
| Preferences | **Season context (IN-05, US2.3/AC2.3.4, FR5):** default `Using device date and location` (read-only summary). **Override control:** toggle/selector `Override season context` (off by default). When on, user can set **Season** (Spring/Summer/Autumn/Winter or month/season period as supported by implementation) and optional **Area/region label** (free text or coarse region selector) used for seasonal matching. Save applies locally and updates season fit display in Plan/Today/Recipe detail/Alternatives context; `Season fit unknown` remains when matching cannot be determined. **Reset to device default** available. Nutrition planning cues preference where applicable. | Avoid medical claims. No precise location required for override; override is user-provided context. |
| Permissions | Location status (granted/denied/not asked), **Manage in system settings**, purpose text. | US5.1. |
| Privacy & data | On-device MVP, minimal collection, no MealPlanner account. **Privacy details**. | US5.6, NFR3. |
| Bottom nav | User active | |

**States:** permission states; season default active; season override on (values set) / invalid/incomplete override handled with clear message and fallback to default until valid; **Reset to device default** restores default; preference changes saved locally; settings opened.

## Responsive Behavior (Q4=A)
- **Portrait (primary):** Single-column content, bottom navigation visible, cards/lists full width with comfortable padding.
- **Landscape (supported):** Scrollable content remains primary; top/bottom bars adapt to safe areas; avoid hiding critical actions; long forms/lists use vertical scroll. No tablet-specific two-pane layouts in MVP.

## Visual/Interaction Notes
- Use Material 3 components and neutral tokens (final colors/type scale/tokens in implementation).
- Status is never color-only: badges/icons are paired with text (`Unknown`, `Season fit unknown`, `Nutrition information unavailable`, `≤30 min`, source/fallback labels).
- Loading/empty/error/partial states are required on primary screens (see [interaction-spec.md](interaction-spec.md)).
