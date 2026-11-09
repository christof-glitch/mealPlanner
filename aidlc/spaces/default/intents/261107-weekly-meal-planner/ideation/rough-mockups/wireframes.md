# Rough Wireframes — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner`  
**Sources:** [intent-statement](../intent-capture/intent-statement.md), [scope-document](../scope-definition/scope-document.md), [intent-backlog](../scope-definition/intent-backlog.md), [rough-mockups-questions](rough-mockups-questions.md) [Q1–Q5]

**Fidelity:** Low-fidelity concept wireframes (ASCII/structured). Phone-first Android, portrait primary, clean accessible Android-native patterns. [Q4, Q5]

## Information Architecture (outline)
```text
App
├── Onboarding / Permissions
│   ├── Welcome + value summary
│   ├── Location consent (nearest PRISMA; manual selection always available)
│   └── Basic preferences setup (optional: season region/override, nutrition focus)
├── Today’s meals (home/default after onboarding)
│   └── Today’s meal slots + recipe links + quick replace
├── Plan (week view)
│   ├── 7-day grid/list: weekends 3 meals, weekdays Mon–Fri 2 meals
│   ├── Meal slot card → Recipe detail (link)
│   └── Replace/regenerate suggestion actions
├── Recipe detail (from meal link)
│   ├── Prep time (≤30 min badge on weekdays), ingredients, prep notes
│   ├── Season fit, nutrition cue/info, source label
│   └── Select/use for slot / replace
├── Grocery
│   ├── Nearby PRISMA stores / selected store
│   ├── Ingredient availability indicators/checklist
│   ├── Combined grocery list (from selected plan)
│   └── Share/export to S-ostoslista (official method if available; fallback share-sheet/text)
└── User (info, preferences, permissions)
    ├── Profile basics (on-device/MVP)
    ├── Preferences (season region/override, nutrition focus cues)
    ├── Permissions (location)
    └── Privacy/GDPR info (minimal data, consent)
```
[scope-document IN-01–IN-12], [rough-mockups Q1–Q3]

## Global Navigation (MVP concept)
```text
+--------------------------------------------------+
| Header: screen title / context            [User]  |
+--------------------------------------------------+
|                                                  |
|                    MAIN CONTENT                  |
|                                                  |
+--------------------------------------------------+
|  [Today]   [Plan]   [Grocery]   [User]           |
+--------------------------------------------------+
Bottom nav: 4 primary destinations (phone-first). [Q1, Q5]
```

## Screen Wireframes

### 1) Onboarding / Permissions
**Purpose:** Introduce app, request location consent with clear purpose, set minimal preferences. [IN-07, IN-11, Q2]

```text
+--------------------------------------------------+
| Meal Planner                                   Skip |
+--------------------------------------------------+
|  Plan your week with seasonal, nutrition-aware   |
|  meal suggestions.                               |
|                                                  |
|  [Illustration/placeholder]                      |
|                                                  |
|  Location (optional)                             |
|  "Find nearby PRISMA stores."                    |
|  [Allow location]  [Choose store manually later] |
|                                                  |
|  Privacy: minimal data, stored on device.        |
|  [View privacy details]                          |
|                                                  |
|               [Continue]                         |
+--------------------------------------------------+
```
**States:** first run; permission granted/denied; manual-only path. **Skip conditions:** user may skip after privacy/data notice is shown; location permission remains optional and manual PRISMA store selection is always available. [R-03 addressed]  
**Accessibility note:** h1 “Meal Planner”; landmarks header/main/footer action; keyboard/focus entry at “Allow location” or primary action; permission copy clear and non-color-only; touch targets >= 44dp.

### 2) Today’s meals
**Purpose:** Default quick view of today’s meal slots and selected/suggested recipes with links. [IN-01, IN-02, IN-06, Q1]

```text
+--------------------------------------------------+
| Today - Sat, 7 Nov                       [User]  |
+--------------------------------------------------+
| Nutrition tip/cue: balanced choices for today     |
|--------------------------------------------------|
| Breakfast                              [Replace] |
| Oat bowl (Sydänmerkki)  15 min  [Nutrition cue]  |
| [View recipe >]                                  |
|--------------------------------------------------|
| Lunch                                  [Replace] |
| Salmon soup             25 min  [Nutrition cue]  |
| [View recipe >]                                  |
|--------------------------------------------------|
| Dinner                                 [Replace] |
| Veggie bake             40 min  [Nutrition cue]  |
| [View recipe >]                                  |
+--------------------------------------------------+
|  [Today]   [Plan]   [Grocery]   [User]           |
+--------------------------------------------------+
```
**States:** populated (3 meals weekend / 2 meals weekday Mon–Fri), loading, empty/error fallback, recipe unavailable (show fallback/source label).  
**Accessibility note:** h1 “Today”; landmarks header/main/nav; focus enters main list; each meal card is a focusable group with explicit “View recipe”/“Replace” labels.

### 3) Plan (week view, populated with meal suggestions)
**Purpose:** 7-day plan; each meal has a link to its recipe; review/replace selections. [IN-01–IN-03, IN-05, IN-06, Q1, Q3]

```text
+--------------------------------------------------+
| Week Plan                                         |
+--------------------------------------------------+
| Mon (2 meals)  Season: Autumn  Region: [default] |
|  Breakfast: Skyr bowl 10m [N] [Replace][Recipe>] |
|  Dinner:   Lentil stew 25m [N][<=30m][Replace][>]|
|--------------------------------------------------|
| Tue (2 meals)                                    |
|  Breakfast: ...                                  |
|  Dinner:   ...                                   |
|--------------------------------------------------|
| ... Wed Thu Fri ...                              |
|--------------------------------------------------|
| Sat (3 meals)                                    |
|  Breakfast / Lunch / Dinner ...                  |
| Sun (3 meals)                                    |
|  Breakfast / Lunch / Dinner ...                  |
+--------------------------------------------------+
|  [Today]   [Plan]   [Grocery]   [User]           |
+--------------------------------------------------+
```
**States:** generated suggestions, partially selected, loading, no suggestions/fallback catalog; weekday badge shows ≤30 min. **Regeneration (MVP clarification):** MVP supports replacing individual meal slots via alternatives; full-week regeneration is deferred unless defined in requirements. Replacing a slot preserves other confirmed selections and applies meal-slot fit, weekday ≤30 min, seasonal default/override, nutrition cues, and Sydänmerkki/fallback source labeling. [R-01 addressed]  
**Accessibility note:** h1 “Week Plan”; landmarks header/main/nav; day sections use headings (h2/h3 pattern as implemented); replace/recipe actions labeled per slot; non-color-only badges.

### 4) Recipe detail (reached via meal/recipe link)
**Purpose:** Help user decide to use recipe; show prep notes and required info. [IN-02–IN-06, Q3]

```text
+--------------------------------------------------+
| < Back                    Recipe                  |
+--------------------------------------------------+
| Lentil stew                                      |
| Source: Sydänmerkki / fallback        [Season fit]|
| Prep time: 25 min   [<=30 min weekday]           |
| Nutrition: [cue/info summary - actively reflected]|
|--------------------------------------------------|
| Ingredients                                      |
| - lentils ...                                    |
| - carrots ...                                    |
|--------------------------------------------------|
| Preparation notes                                |
| 1. ...                                           |
| 2. ...                                           |
|--------------------------------------------------|
| [Use for this meal slot]      [Choose different] |
+--------------------------------------------------+
```
**States:** loaded, loading, unavailable source/fallback, missing nutrition/prep data (show “unknown” with explanation). **Choose different:** opens the Replace/Alternatives interaction (Screen 4a) for the same meal slot. [R-02 addressed]  
**Accessibility note:** h1 recipe title; landmarks header/main; focus enters title/back; source/fallback and availability/nutrition limitations textually indicated.

### 4a) Replace meal / Alternatives (inline bottom sheet or list screen)
**Purpose:** Show alternative suggestions for one meal slot without replacing other confirmed selections. [IN-02, IN-03, IN-05, IN-06, Q2, R-02 addressed]

```text
+--------------------------------------------------+
| Replace dinner (Mon)                       [Close]|
+--------------------------------------------------+
| Rules: fits dinner slot; weekday <=30 min;       |
| seasonal default/override; nutrition cues shown; |
| source labeled (Sydänmerkki/fallback).           |
|--------------------------------------------------|
| ( ) Chickpea salad      20m [N][<=30m] Sydänmerkki|
|     [View]                        [Select]       |
|--------------------------------------------------|
| ( ) Tofu stir-fry       25m [N][<=30m] fallback  |
|     [View]                        [Select]       |
|--------------------------------------------------|
| ( ) Fish soup           30m [N][<=30m] Sydänmerkki|
|     [View]                        [Select]       |
|--------------------------------------------------|
|                 [Cancel]                          |
+--------------------------------------------------+
```
**States:** loading alternatives, no alternatives (keep current + explain), selected replacement updates only that slot.  
**Accessibility note:** h2 “Replace dinner (Mon)” or dialog title; focus trapped in modal/sheet when open; options are selectable with clear labels; rules text readable (not icon-only).

### 5) Grocery (nearby stores, grocery list + share/export)
**Purpose:** Combined shopping list, PRISMA store context, availability support, share/export to S-ostoslista. [IN-07–IN-10, Q1, Q2]

```text
+--------------------------------------------------+
| Grocery                                           |
+--------------------------------------------------+
| PRISMA store                                     |
| Nearby: Prisma X (1.2 km) [Use]  [Choose manually]|
| Selected: Prisma Y                    [Change]  |
| Location: [permission status/manage]             |
|--------------------------------------------------|
| Availability (best-effort/checklist)             |
| [!] Milk - unknown   [ ] Tomatoes - available    |
| [ ] Lentils - check in store                     |
|--------------------------------------------------|
| Combined grocery list                            |
| [ ] Lentils                                      |
| [ ] Carrots                                      |
| [ ] Oats                                         |
| ...                                              |
|--------------------------------------------------|
| [Share/export to S-ostoslista] [Copy text/export]|
+--------------------------------------------------+
|  [Today]   [Plan]   [Grocery]   [User]           |
+--------------------------------------------------+
```
**States:** no store selected, location denied (manual only), availability unknown/fallback, empty list, share success/fallback.  
**Accessibility note:** h1 “Grocery”; landmarks header/main/nav; status text not by color alone; share actions clearly labeled (official S-ostoslista path vs fallback export).

### 6) User information, preferences, and permissions
**Purpose:** Manage preferences, permissions, and view privacy/data info. [IN-05, IN-07, IN-11, Q1]

```text
+--------------------------------------------------+
| User                                              |
+--------------------------------------------------+
| Preferences                                      |
| Season/region default: [Auto by date/region]     |
| Season override: [None v]                        |
| Nutrition planning cues: [On v]                  |
|--------------------------------------------------|
| Permissions                                      |
| Location: [Granted/Denied] [Manage in system]    |
| Purpose: suggest nearby PRISMA stores            |
|--------------------------------------------------|
| Privacy & data                                   |
| Data stored on device (MVP). Minimal collection. |
| [Privacy details] [GDPR-related info as needed]  |
+--------------------------------------------------+
|  [Today]   [Plan]   [Grocery]   [User]           |
+--------------------------------------------------+
```
**States:** permission granted/denied/never asked; preferences changed.  
**Accessibility note:** h1 “User”; landmarks header/main/nav; controls have visible labels; settings changes announced/confirmed via standard Android patterns.

## Cross-cutting UX Notes
- **Nutrition-active MVP:** show nutrition cues/info where recipe data supports it; when unavailable, show explicit limitation rather than hiding the concept. [scope Q5]
- **Fallback transparency:** label recipe source (Sydänmerkki/fallback) and availability/share method (official/fallback/checklist) clearly. [scope-document OUT-05, IN-04/08/10]
- **Errors/empty/loading:** every primary screen needs loading, empty, error, and partial states in implementation (wireframes indicate key cases). [wireframing guide]
- **Privacy/GDPR:** clear consent copy for location; minimal data; no unnecessary third-party sharing implied in UI. [IN-11]
