# User Stories Planning Questions

**Intent:** `261107-weekly-meal-planner` (Android MVP, `mvp`)  
**Sources:** [requirements.md](../requirements-analysis/requirements.md), [team-practices.md](../practices-discovery/team-practices.md), [intent-statement](../../ideation/intent-capture/intent-statement.md)

Please answer by filling in each `[Answer]:` tag (or answer in chat and I’ll write them back).

---

## Q1. [Personas] Who should the MVP user stories focus on?

**Context:** The product is for household/family meal planning with variety/inspiration and consideration of budget, nutrition, or dietary restrictions. Stories should be written from clear personas while staying small enough for MVP.

A. One primary persona: **Household Meal Planner** (plans the week, selects recipes, manages shopping list/export). Treat “shopper” and “recipe chooser” as goals of the same persona.
B. Two personas: **Household Meal Planner** (plans/selects/exports) and **Household Shopper** (reviews checklist, checks items off, uses PRISMA/store context).
C. Three lightweight personas: **Household Meal Planner**, **Health-conscious Eater** (nutrition cues/aid), and **Household Shopper** (grocery/export). Keep stories minimal and avoid duplication.
X. Other (please specify)

[Answer]: A — One primary persona: **Household Meal Planner** (plans the week, selects recipes, manages shopping list/export). Treat “shopper” and “recipe chooser” as goals of the same persona.

---

## Q2. [Story format/granularity] How should integration fallbacks be represented in stories?

**Context:** Requirements include investigate-and-fallback behavior for Sydänmerkki, PRISMA availability, S-ostoslista official method, missing data (nutrition/time/season), and location permission. Stories must be INVEST/testable.

A. Include fallback behavior as **acceptance criteria within the main feature stories** (e.g., recipe source story includes Sydänmerkki unavailable -> curated fallback; grocery story includes PRISMA Unknown/manual checklist). Add separate stories only where the fallback is a distinct user-visible capability.
B. Create **separate fallback/reliability stories** for major integrations (Sydänmerkki fallback, PRISMA unknown/checklist, S-ostoslista export fallback, offline/local data) in addition to main feature stories.
C. Keep MVP stories focused on **happy-path core capabilities** and capture fallback details primarily in acceptance criteria notes; create only a small cross-cutting reliability story.
X. Other (please specify)

[Answer]: B — Create **separate fallback/reliability stories** for major integrations (Sydänmerkki fallback, PRISMA unknown/checklist, S-ostoslista export fallback, offline/local data) in addition to main feature stories.

---

## Q3. [Prioritization] Which capabilities are Must Have for the MVP story set?

**Context:** MoSCoW priorities will inform delivery planning. Scope already defines MVP in/out; this question confirms emphasis where implementation detail may vary.

A. **Must Have:** 7-day plan structure, recipe view/select/replace, weekday ≤30 min total-time rule with explanation, Sydänmerkki attempt with curated/import fallback, seasonal suggestions (device date/location, unknown allowed), nutrition cues + “unavailable” label + one simple planning aid, PRISMA manual store selection (nearest suggestion if feasible) + ingredient name/presence check with Unknown/checklist fallback, editable combined shopping list, S-ostoslista official attempt if feasible + mandatory share/copy fallback with instructions, location consent/purpose + local-first fallback behavior. **Should Have:** polish/convenience (better alternatives UI, richer nutrition aid, smoother store suggestion).
B. Same Must Have core as A, but mark **nearest-PRISMA suggestion** and **nutrition-aware planning aid** as **Should Have** (manual store selection, nutrition cues/unavailable label remain Must Have) to reduce MVP risk.
C. Narrow Must Have to the minimum end-to-end slice: plan structure, recipe select, basic shopping list, export/share fallback, and explicit fallback states; treat Sydänmerkki live attempt, PRISMA presence check automation, and nutrition aid as **Should Have** where feasible.
X. Other (please specify)

[Answer]: B — Same Must Have core as A, but mark **nearest-PRISMA suggestion** and **nutrition-aware planning aid** as **Should Have** (manual store selection, nutrition cues/unavailable label remain Must Have) to reduce MVP risk.

---

## Q4. [Breakdown approach] How should stories be organized?

**Context:** The stage can break stories down by feature, persona, workflow, domain area, or epic. Requirements group naturally into planning, recipes/integrations, grocery/store, and export/privacy.

A. **By workflow/epic:** Plan week -> Choose recipes -> Build grocery list -> Share/export, with cross-cutting stories for permissions/privacy and fallback states as needed.
B. **By domain/feature area:** Meal planning, Recipe catalog & constraints, Nutrition/season, PRISMA/store & availability, Shopping list & S-ostoslista, Settings/permissions/privacy.
C. **By persona goals:** Planner stories first, Shopper/export stories second, with system/fallback acceptance criteria embedded.
X. Other (please specify)

[Answer]: A — **By workflow/epic:** Plan week -> Choose recipes -> Build grocery list -> Share/export, with cross-cutting stories for permissions/privacy and fallback states as needed.

---

## Q5 (follow-up). [Contradiction] How should we resolve Q3:B vs confirmed requirements?

**Context:** Your Q3 answer marks **nutrition-aware planning aid** and **nearest-PRISMA suggestion** as **Should Have**. However, confirmed Requirements Analysis says: **FR6** requires showing nutrition cues/unavailable label *and* providing **at least one nutrition-aware planning aid** during selection (MVP). **FR7** requires manual store selection always and a **nearest PRISMA store suggestion** when location permission is granted (best effort). To keep stories testable and traceable, we need to resolve this before generation.

A. **Keep requirements as confirmed (recommended):** Treat “at least one nutrition-aware planning aid” and “nearest-PRISMA suggestion when permission granted (best effort, with manual fallback)” as **Must Have** in stories to satisfy FR6/FR7; keep richer nutrition aids/polish as Should Have.
B. **Adopt Q3:B prioritization and record requirement change:** Mark nearest suggestion and the planning aid as **Should Have** in stories, but I will explicitly note this as a requested change to FR6/FR7 assumptions for your confirmation (requirements may need revision in a later pass).
C. **Split capability:** Nearest-PRISMA suggestion = **Should Have** (manual selection remains Must Have, satisfying FR7 fallback intent); nutrition cues + unavailable label = **Must Have**, and **one minimal nutrition-aware planning aid** = **Must Have** (satisfying FR6), with richer aids = **Should Have**.
X. Other (please specify)

[Answer]: A — Keep requirements as confirmed: treat “at least one nutrition-aware planning aid” and “nearest-PRISMA suggestion when permission granted (best effort, with manual fallback)” as **Must Have** in stories to satisfy FR6/FR7; keep richer nutrition aids/polish as Should Have.

---

## Consolidated Summary Confirmation

- **Q1 [Personas]:** A — One primary persona: **Household Meal Planner** (plans week, selects recipes, manages shopping list/export). Shopper/recipe chooser are goals of the same persona.
- **Q2 [Fallback representation]:** B — Create **separate fallback/reliability stories** for major integrations (Sydänmerkki fallback, PRISMA unknown/checklist, S-ostoslista export fallback, offline/local data) in addition to main feature stories.
- **Q3 [Prioritization]:** B as clarified by Q5 — Must Have core MVP scope confirmed in requirements; **nearest-PRISMA suggestion** (permission granted, best effort, manual fallback) and **at least one minimal nutrition-aware planning aid** are **Must Have** to satisfy FR7/FR6; nutrition cues + unavailable label remain Must Have; richer nutrition aids/polish are Should Have.
- **Q4 [Breakdown]:** A — Organize **by workflow/epic**: Plan week -> Choose recipes -> Build grocery list -> Share/export, plus cross-cutting permissions/privacy/fallback stories as needed.
- **Q5 [Follow-up]:** A — Resolved contradiction in favor of confirmed requirements (FR6/FR7).

Does this all look correct before I generate the user stories artifacts?
- Looks correct
- Request changes

[Answer]: Looks correct
