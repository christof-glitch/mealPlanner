# Rough Mockups Questions — Weekly Meal Planner (Android MVP)

Sources: [intent-statement](../intent-capture/intent-statement.md), [scope-document](../scope-definition/scope-document.md), [intent-backlog](../scope-definition/intent-backlog.md)

## Q1. [UX] What are the primary user entry points and key screens/views for the MVP?
Context: Confirmed scope includes 7-day plan (weekend 3 meals, weekday Mon–Fri 2 meals), recipe suggestions/selection with prep notes (weekday ≤30 min), seasonal default+override, nutrition-aware planning, PRISMA store selection/nearest suggestion + availability support, combined shopping list, and S-ostoslista share/import support. [scope-document IN-01–IN-10]

A. Recommended MVP screens: **Plan (week view)**, **Recipe suggestions/detail + select for slot**, **Shopping list**, **Store/PRISMA settings** (select store, availability view), **Share/export** (S-ostoslista), plus lightweight onboarding/permissions (location consent) as needed
B. Fewer screens for ASAP: combine store/settings into shopping list flow; keep Plan, Recipe selection, Shopping list + share
C. I can specify a different screen set
D. Not sure — use the recommended MVP screen set
X. Other (please specify)

[Answer]: X (Other) — Views should be: **Onboarding** and **Today’s meals**; **Plan** (week view populated with meal suggestions; each meal has a link to its recipe); **Grocery** view (grocery stores nearby, grocery list + share/export); **User information, preferences, and permissions**. (Recipe detail is reached via meal/recipe links from Plan/Today.)

## Q2. [UX] What is the core happy-path user flow?
Context: User needs a useful 7-day plan and selects recipes they would actually use; shopping list is combined and shareable/importable to S-ostoslista. [intent-statement Q2, Q3, Q13]

A. **Open app → view/generate 7-day plan (seasonal + nutrition cues) → review/replace recipes per slot (weekday ≤30 min enforced/indicated) → confirm selected plan → view combined shopping list → choose/confirm PRISMA store (optional nearest-by-location) → review availability indicators/checklist → share/export to S-ostoslista**
B. Start from shopping list/store first, then build plan
C. I can specify a different flow
D. Not sure — use flow A
X. Other (please specify)

[Answer]: A — Adapted to chosen views: Open app → onboarding/permissions (location consent as needed) → Today/Plan with seasonal + nutrition cues → review/replace recipes per slot (weekday ≤30 min indicated/enforced) → Grocery: combined list → choose/confirm PRISMA store (optional nearest-by-location) → review availability indicators/checklist → share/export to S-ostoslista.

## Q3. [UX] What information hierarchy should the plan and recipe views prioritize?
Context: Success means a useful 7-day suggestion plan and selecting recipes the user would actually use; MVP must actively reflect nutrition planning. [intent-statement Q3], [scope Q5]

A. **Plan view:** week/day structure + meal slots first, selected recipe title/image placeholder + prep time + nutrition cue visible per slot; quick replace action. **Recipe view:** prep time (≤30 min badge for weekdays), ingredients, prep notes/steps, season fit, nutrition cue/info, source (Sydänmerkki/fallback) clearly labeled
B. Minimal hierarchy: title + prep time only on plan; details on recipe screen
C. I can specify priorities
D. Not sure — use A
X. Other (please specify)

[Answer]: A — Plan/Today: week/day + meal slots first; recipe title/image placeholder, prep time, nutrition cue, quick replace. Recipe (via meal link): prep time (≤30 min badge for weekdays), ingredients, prep notes/steps, season fit, nutrition cue/info, source (Sydänmerkki/fallback) clearly labeled.

## Q4. [Design] Are there existing brand guidelines, design systems, or UI patterns to follow?
Context: Personal learning project, Android-only MVP, stack undecided; no brand guidelines mentioned in intent/feasibility.

A. No existing brand/design system — use clean, accessible Android-native patterns (Material-style conventions acceptable as guidance) with simple neutral visuals
B. I can provide brand colors/name/logo/guidelines
C. Follow platform defaults only; keep visuals minimal
D. Not sure — use A
X. Other (please specify)

[Answer]: A — No existing brand/design system; clean, accessible Android-native patterns (Material-style guidance acceptable), simple neutral visuals.

## Q5. [Platform/Accessibility] What device/form factors and accessibility requirements must be supported for MVP wireframes?
Context: Android-only first version. GDPR/privacy important; location permission used with consent. No explicit WCAG/accessibility requirements stated.

A. **Phone-first Android** (portrait primary), responsive to common phone sizes; baseline accessibility: clear labels, sufficient touch targets, readable text, focus/order notes, non-color-only indicators, and simple permission/copy for location consent. Treat WCAG 2.1 AA-aligned practices as guidance where practical for MVP wireframes
B. Phone + tablet layouts required in MVP
C. I can specify device/accessibility requirements
D. Not sure — use A
X. Other (please specify)

[Answer]: A — Phone-first Android (portrait primary), common phone sizes; baseline accessibility (labels, touch targets, readable text, focus/order notes, non-color-only indicators, clear location-consent copy), WCAG 2.1 AA-aligned guidance where practical.

## Consolidated Summary Confirmation

- Looks correct
- Request changes

[Answer]: Looks correct
