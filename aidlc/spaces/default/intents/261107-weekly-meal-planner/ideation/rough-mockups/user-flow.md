# User Flow — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner`  
**Sources:** [intent-statement](../intent-capture/intent-statement.md), [scope-document](../scope-definition/scope-document.md), [intent-backlog](../scope-definition/intent-backlog.md), [rough-mockups-questions](rough-mockups-questions.md) [Q1–Q5], [wireframes](wireframes.md)

## Primary Views
- **Onboarding / Permissions** (first run / permission setup)
- **Today’s meals** (default quick view)
- **Plan** (week view populated with meal suggestions; each meal links to its recipe)
- **Recipe detail** (reached via meal/recipe links)
- **Grocery** (nearby stores, grocery list + share/export)
- **User** (information, preferences, permissions)
[rough-mockups Q1]

## Core Happy Path
Adapted from [rough-mockups Q2]:

```text
[Open app]
   |
   v
[First run?] --Yes--> [Onboarding / Permissions]
   |                        |
   No                  [Location consent: Allow / Manual later]
   |                        |
   v                        v
[Today’s meals / Plan home] <---------------------------+
   |  (seasonal default by date/region + nutrition cues) |
   v                                                    |
[Review 7-day plan]                                     |
   |  weekends: Breakfast,Lunch,Dinner                  |
   |  weekdays Mon-Fri: Breakfast,Dinner                |
   |  weekday recipes: <=30 min indicated/enforced      |
   v                                                    |
[Tap meal slot / recipe link]                            |
   |                                                    |
   v                                                    |
[Recipe detail: prep time, ingredients, prep notes,      |
 season fit, nutrition cue/info, source label]          |
   |                                                    |
   +--> [Choose different / Replace slot]               |
   |        |                                           |
   |        v                                           |
   |   [Alternatives (sheet/list): 1 slot only;         |
   |    rules: slot fit, weekday <=30 min,              |
   |    seasonal default/override, nutrition cues,      |
   |    Sydänmerkki/fallback source label]             |
   |        |                                           |
   |        +--> [Select alternative] --> update slot --+
   |
   v
[Plan has selected recipes user would use] (individual slot replacements preserve other confirmed selections; full-week regeneration deferred unless specified) [R-01, R-02 addressed]
   |
   v
[Open Grocery view]
   |
   v
[Combined grocery list generated from selected plan]
   |
   v
[PRISMA store: nearest suggestion (optional, with permission)
 or manual selection]
   |
   v
[Availability support using available PRISMA data]
   |  success: indicators
   |  fallback: unknown/checklist
   v
[Share/export to S-ostoslista]
   |  preferred: official method if available (format confirmed)
   |  fallback: share-sheet/text export
   v
[Done / return to Today or Plan]
```

## Key Decision Points & Fallbacks

| Flow Point | Primary Path | Fallback / Alternate | Source |
|---|---|---|---|
| Sydänmerkki recipes | Query recipes from `sydanmerkki.fi` | Curated/imported Sydänmerkki recipe set | [intent-statement Q14], [scope IN-04] |
| Seasonal selection | Default from current date/region | User override in preferences/flow | [intent-statement Q18], [scope IN-05] |
| Nearest PRISMA store | Use phone location with consent | Manual store selection always available | [intent-statement Q16], [scope IN-07] |
| Ingredient availability | Verify via available PRISMA data | Best-effort/unknown indicators or checklist | [intent-statement Q15], [scope IN-08] |
| S-ostoslista export | Official import/share method if exists | Share-sheet/text export | [intent-statement Q17], [scope IN-10] |
| Nutrition data missing | Show nutrition cue/info from available data | Explicit “unknown/unavailable” cue; still actively reflect nutrition planning | [scope Q5, IN-06] |

## Secondary Flows
```text
[Today’s meals] --> tap meal --> [Recipe detail] --> back to Today
[Plan or Recipe detail] --> Replace/Choose different --> [Alternatives UI (bottom sheet or list screen): shows options for that slot only] --> select --> update slot only
  Replacement rules: meal-slot fit, weekday <=30 min (Mon-Fri Breakfast/Dinner), seasonal default/override, nutrition cues shown when available, Sydänmerkki/fallback source labeled.
[Onboarding] --> Skip allowed after privacy/data notice shown; location optional; manual PRISMA store selection always available [R-03 addressed]
[Grocery] --> change store --> [manual search/select or nearby results] --> update availability context
[User] --> change permissions/preferences --> [system settings or in-app preference] --> updated app behavior
[Share/export fails or method unavailable] --> [fallback export/share sheet] --> user completes in S-ostoslista context
```

## Error / Edge States (to handle in implementation)
- Location permission denied: nearest-store suggestion disabled; manual PRISMA selection remains. [scope IN-07]
- No recipe suggestions / integration issue: show fallback catalog and clear status. [scope IN-04]
- Missing prep time/nutrition data: show “unknown” and do not present false guarantees. [scope IN-03, IN-06]
- No selected PRISMA store: allow grocery list use; availability shown as checklist/unknown until store selected. [scope IN-08]
- S-ostoslista method unavailable: use share-sheet/text export fallback. [scope IN-10]
- Empty plan/list: provide clear CTA to generate plan or add recipes. [wireframes]

## Accessibility & UX Flow Notes
- Primary actions are visible and labeled (replace, view recipe, select store, share/export). [Q5]
- Non-color-only indicators for availability, badges, and status. [Q5]
- Focus/order supports moving through meal lists, recipe details, grocery checklist, and settings. [Q5]
- Location permission copy explains purpose (nearby PRISMA stores) and offers manual alternative. [IN-11]
