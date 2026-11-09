## Problem Statement
- Planning weekly meals is a recurring decision that needs better variety, inspiration, and support for household/family planning around budget, nutrition, or dietary restrictions. [Q1]

## Target Customer
- Primary users are members of a household or family who want meal variety and inspiration for the week. [Q1]
- The app is intended to help plan meals with budget, nutrition, or dietary restrictions in mind. [Q1]

## Success Metrics
- A user can view a useful 7-day meal suggestion plan and select the recipes they would actually use. [Q3]
- The weekly plan provides 3 meals (Breakfast, Lunch, Dinner) on weekends and 2 meals (Breakfast, Dinner) on weekdays (Monday–Friday). [Q3]
- Weekday recipes take no more than 30 minutes to prepare. [Q3]

## Initiative Trigger
- The initiative is driven by both a personal need for a practical weekly meal planning tool and a learning goal to practice building a mobile application. [Q4]

## Initial Scope Signal
- **Workflow-selected scope (workflow-selected):** `mvp`. [scope]
- **User-confirmed product boundary for this first version:** No grocery delivery integration or store-specific pricing. [Q7]
- **User-confirmed first-version capabilities:** [Q2], [Q8], [Q9], [Q10], [Q11], [Q12], [Q13], [Q14], [Q15], [Q16], [Q17], [Q18]
  - Android mobile application for the first version. [Q8]
  - Weekly recipe suggestions with preparation notes; the app should be able to query recipes from `www.sydanmerkki.fi`. Feasibility/terms will be investigated; if live querying is not feasible or allowed for MVP, use a curated/imported Sydänmerkki recipe set as a fallback. [Q2], [Q11], [Q14]
  - Recipes should be seasonal, determined based on the current date/season in the user’s region, with a default from date/region and the ability for the user to override. [Q2], [Q9], [Q18]
  - Verify ingredient availability against the selected PRISMA store using available PRISMA data. Feasibility/access will be investigated; if automated availability is not accessible for MVP, use best-effort/unknown indicators or a checklist fallback. [Q2], [Q12], [Q15]
  - The closest PRISMA store is suggested by phone location (with permission), but the user can select a store manually. Phone location permission is acceptable; a reliable PRISMA store-location data source will be investigated/used. [Q2], [Q10], [Q16]
  - Include a combined shopping list and the ability to import/share that shopping list to S-ostoslista using an official S-ostoslista import/share method if one exists (format to be confirmed). The user is expected to have an S-ostoslista (`s-ostoslista.fi`) account; sharing a shopping list is possible from there. The shopping list format/method will be investigated; if a suitable official/share method or format cannot be confirmed for MVP, use a fallback (e.g., share-sheet/text export) that fits what S-ostoslista accepts. [Q2], [Q13], [Q17]

## Assumptions & Open Questions
- [assumption] Querying recipes from `www.sydanmerkki.fi` depends on the site’s terms, technical constraints, and available data/integration approach; feasibility/access will be investigated and a curated/imported fallback is acceptable if live querying is not feasible or allowed for MVP. [Q11], [Q14]
- [assumption] Verifying ingredient availability against a PRISMA store depends on accessible product/availability data from `www.prisma.fi`; feasibility/access will be investigated and best-effort/unknown indicators or a checklist fallback are acceptable if automated availability is not accessible for MVP. [Q12], [Q15]
- [assumption] Suggesting the nearest PRISMA store by phone location depends on location permission and a reliable PRISMA store-location data source; permission is acceptable and the data source will be investigated/used. Manual selection remains available. [Q10], [Q16]
- [assumption] S-ostoslista import/share depends on an official method/format (to be investigated) and the user having an S-ostoslista account; a share-sheet/text export fallback is acceptable if a suitable official/share method or format cannot be confirmed for MVP. [Q13], [Q17]
- [assumption] Seasonal selection defaults from current date/region and allows user override; the detailed regional/season mapping rule will be defined during implementation. [Q9], [Q18]
