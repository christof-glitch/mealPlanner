## Sources

- [desc] Initial description: "Build a mobile application that gives me meal suggestions for a week."
- [scope] Workflow-selected scope: `mvp`.

## Q1. [Product] What problem are weekly meal suggestions solving, and who will use this app?

This helps define the core need so the app solves a real problem (for example, decision fatigue, variety, time savings) for the right person.

A. I want help deciding what to eat for the week to save time and reduce decision fatigue (primary user: me)
B. I want meal variety and inspiration for a household or family (primary users: me and others I cook for)
C. I want to plan meals around goals like budget, nutrition, or dietary restrictions (primary user: me)
D. I am building this mainly as a learning or portfolio project; the user need is still being defined
E. Not yet defined
X. Other (please specify)

[Answer]: B, C — variety and inspiration for a household/family, and planning around budget, nutrition, or dietary restrictions.

## Q2. [Product] What should a good first version include when it suggests meals for a week?

"Meal suggestions for a week" can mean different things; your answer shapes what we build first (for example, just a list of ideas vs. recipes and a shopping list).

A. A simple weekly plan (7 days) with meal ideas/names for each day
B. Weekly plan plus basic recipe details or preparation notes for each suggestion
C. Weekly plan plus ingredients and a combined shopping list
D. Weekly plan that considers preferences (cuisine, time to cook) or dietary needs (vegetarian, allergies, etc.)
E. Not yet defined
X. Other (please specify)

[Answer]: X (Other) — Weekly suggestions of recipes with preparation notes for each suggestion. Recipes should come from www.sydanmerkki.fi. Recipes should be seasonal. Verify that ingredients of recipes are available from the closest PRISMA store (www.prisma.fi). Include a combined shopping list and the ability to import this shopping list to another mobile app named S-ostoslista.

## Q3. [Product] How will you know this first version is successful?

Clear success criteria help us focus on what matters most for the first release.

A. I can generate or view a useful 7-day meal suggestion plan that I would actually use
B. The app is easy to use on my phone and feels reliable for weekly planning
C. It saves me noticeable time compared to planning meals manually
D. Success is completing a working mobile app I can demo or learn from (project/learning goal)
E. Not yet defined
X. Other (please specify)

[Answer]: X (Other) — I can view a useful 7-day meal suggestion from which I can select the recipes I would actually use. There should be 3 meals (Breakfast, Lunch, Dinner) on weekends, and only 2 (Breakfast, Dinner) on weekdays (Monday to Friday). On weekdays, recipes should not take more than 30 minutes to prepare.

## Q4. [Product] Why build this now, and what triggered this idea?

Understanding the trigger helps prioritize what to include first.

A. Personal need - I want a practical tool for my own weekly meal planning
B. Learning goal - I want to practice building a mobile application
C. Both personal need and learning
D. Another reason (market opportunity, assignment, etc.) - I will specify
E. Not applicable
X. Other (please specify)

[Answer]: C — Both personal need and learning.

## Q5. [Stakeholders] Who are the key people involved or affected by this app, and what does each care about?

Even personal projects can involve others (testers, family members, instructors). This helps us avoid missing important perspectives.

A. Just me - I am the only user and decision-maker
B. Me as the main user, plus a small group who may give feedback (friends/family/testers)
C. Me plus an instructor, client, or team with specific expectations
D. Not yet defined
E. Not identified
X. Other (please specify)

[Answer]: B initially, with potential to evolve to C — starts as me plus a small group for feedback (friends/family/testers), and may later involve an instructor, client, or team with specific expectations.

## Q6. [Stakeholders] Who decides what goes into this first version and how changes are prioritized?

Knowing the decision-maker keeps scope and priorities clear as we plan.

A. I decide everything for this first version
B. I decide, but I will incorporate feedback from specific people I name
C. Someone else decides (client/instructor/team lead)
D. Not yet defined
X. Other (please specify)

[Answer]: A — I decide everything for this first version.

## Q7. [Product] What should this first version *not* include?

The mvp plan is already chosen. Name only what is out of this first version so we keep the first release focused.

A. No user accounts, cloud sync, or social/sharing features
B. No advanced nutrition tracking, calorie counting, or medical/dietary compliance guarantees
C. No grocery delivery integration or store-specific pricing
D. Keep it minimal overall - only the core weekly meal suggestion experience
E. Not yet defined
X. Other (please specify)

[Answer]: C — No grocery delivery integration or store-specific pricing.

## Consolidated Summary Confirmation

- **[Product] Who it’s for / problem (Q1):** Variety and inspiration for a household/family, and planning around budget, nutrition, or dietary restrictions.
- **[Product] What the first version includes (Q2):** Weekly recipe suggestions with preparation notes; recipes from `www.sydanmerkki.fi`; seasonal; verify ingredients available from the closest PRISMA store (`www.prisma.fi`); combined shopping list importable/shareable to S-ostoslista.
- **[Product] Success for the first version (Q3):** View a useful 7-day meal suggestion and select recipes to actually use; weekends 3 meals (Breakfast, Lunch, Dinner), weekdays (Monday–Friday) 2 meals (Breakfast, Dinner); weekday recipes take no more than 30 minutes to prepare.
- **[Product] Why now (Q4):** Both personal need and learning.
- **[Stakeholders] Who’s involved (Q5):** Starts as you plus a small group for feedback (friends/family/testers); may later involve an instructor, client, or team with specific expectations.
- **[Stakeholders] Who decides (Q6):** You decide everything for this first version.
- **[Product] Out of the first version (Q7):** No grocery delivery integration or store-specific pricing.
- **[Product] Mobile platform (Q8):** Android only.
- **[Product] Seasonal rule (Q9, Q18):** Based on the current date/season in the user’s region (default from date/region), with user override allowed.
- **[Product] Closest PRISMA store (Q10, Q16):** Suggest nearest by phone location (with permission, acceptable) but allow manual selection; investigate/use a reliable PRISMA store-location data source.
- **[Product] Sydänmerkki recipes (Q11, Q14):** The app should be able to query recipes from `sydanmerkki.fi`; investigate feasibility/terms, with a curated/imported Sydänmerkki fallback if live querying isn’t feasible/allowed for MVP.
- **[Product] PRISMA availability (Q12, Q15):** Verify ingredient availability against the selected PRISMA store using available PRISMA data; investigate feasibility/access, with best-effort/unknown indicators or checklist fallback if automated availability isn’t accessible for MVP.
- **[Product] S-ostoslista import (Q13, Q17):** Use an official S-ostoslista import/share method if one exists (format to be confirmed). User should have an S-ostoslista (`s-ostoslista.fi`) account; sharing is possible from there. Investigate list format/method; if not confirmed for MVP, use a fallback (e.g., share-sheet/text export) that fits what S-ostoslista accepts.
- **[Stakeholders/Process] (Q19):** Keep remaining stakeholder/communication assumptions open for the first version.

- Looks correct
- Request changes

[Answer]: Looks correct


## Assumption Confirmation

The intent documents include the following remaining assumptions/open questions (after Q14–Q19):

- **Sydänmerkki recipe access (from Q2, Q11, Q14):** Feasibility/terms for querying `www.sydanmerkki.fi` will be investigated; if live querying is not feasible/allowed for MVP, a curated/imported Sydänmerkki recipe set is an accepted fallback.
- **PRISMA availability data (from Q2, Q12, Q15):** Feasibility/access to PRISMA availability data will be investigated; if automated availability is not accessible for MVP, best-effort/unknown indicators or a checklist fallback are accepted.
- **Nearest PRISMA store/location data (from Q10, Q16):** Phone location permission is acceptable; a reliable PRISMA store-location data source will be investigated/used. Manual selection remains available.
- **S-ostoslista import/share (from Q2, Q13, Q17):** Official/share method and list format will be investigated; the user is expected to have an S-ostoslista (`s-ostoslista.fi`) account. If a suitable method/format cannot be confirmed for MVP, a share-sheet/text export fallback that fits S-ostoslista is accepted.
- **Seasonal region mapping detail (from Q9, Q18):** Default season comes from current date/region with user override; the detailed regional/season mapping rule will be defined during implementation.
- **Stakeholder/communication (from Q1, Q5, Q6, Q19):** Household/family influence, future stakeholder authority/cadence, and formal communication cadence remain open assumptions for the first version (no formal process needed now).

A. Accept assumptions
B. Convert to follow-up questions

[Answer]: A. Accept assumptions



## Q8. [Product] Which mobile platform(s) should the first version target?

This defines where the “mobile application” will run for the first release.

A. Android only
B. iOS only
C. Both Android and iOS
D. Cross-platform (one codebase targeting Android and iOS)
E. Not yet defined
X. Other (please specify)

[Answer]: A — Android only.

## Q9. [Product] How should “seasonal” recipes be determined?

You asked for seasonal recipes from Sydänmerkki; the rule for what counts as seasonal needs to be clear.

A. Based on the current date/season in the user’s region
B. Based on season tags or metadata provided by the recipe source (if available)
C. A simple manual season setting chosen by the user (spring/summer/autumn/winter)
D. Not yet defined
X. Other (please specify)

[Answer]: A — Based on the current date/season in the user’s region.

## Q10. [Product] How should the “closest PRISMA store” be determined?

You asked to verify ingredients are available from the closest PRISMA store; the app needs a clear way to pick that store.

A. Use the phone’s location (with permission) to find the nearest store
B. Let the user choose their preferred PRISMA store manually
C. Offer both: suggest nearest by location, but allow manual selection
D. Not yet defined
X. Other (please specify)

[Answer]: C — Offer both: suggest nearest by location, but allow manual selection.

## Q11. [Product] For the first version, how should recipes from sydanmerkki.fi be provided in the app?

This clarifies expectations for the MVP given that live access to an external recipe source may have technical or permission constraints.

A. Live access to recipes from sydanmerkki.fi (integrated in the app)
B. A curated/imported set of sydanmerkki.fi recipes prepared for the app (updated manually as needed)
C. Start with a small curated set for MVP, with live integration as a later improvement
D. Not yet defined
X. Other (please specify)

[Answer]: X (Other) — The MealPlanner app should be able to query recipes from sydanmerkki.fi.

## Q12. [Product] For the first version, how should PRISMA ingredient availability be handled?

This clarifies expectations for the MVP given that store availability checks may depend on accessible data.

A. Verify ingredient availability against the selected PRISMA store using available PRISMA data
B. Best-effort: show ingredients and indicate availability only when data is available; otherwise mark as unknown
C. Manual/checklist support for MVP (shopping list provided; user verifies availability in PRISMA), with automated verification later
D. Not yet defined
X. Other (please specify)

[Answer]: A — Verify ingredient availability against the selected PRISMA store using available PRISMA data.

## Q13. [Product] How should the combined shopping list be imported into S-ostoslista?

This clarifies what “import to S-ostoslista” means for the first version.

A. Use an official S-ostoslista import/share method if one exists (format to be confirmed)
B. Export the shopping list in a common format (e.g., text or share sheet) that the user can import/open in S-ostoslista
C. Copy/share the list to S-ostoslista via the phone’s share functionality (best-effort MVP)
D. Not yet defined
X. Other (please specify)

[Answer]: A — Use an official S-ostoslista import/share method if one exists (format to be confirmed).

## Q14. [Product/Technical] Sydänmerkki recipe access (from Q2, Q11): how should the app query recipes from sydanmerkki.fi?

You said the app should be able to query recipes from `sydanmerkki.fi`. Access may depend on the site’s terms, technical constraints, and available data/integration approach.

A. I know an official/allowed way to query recipes (API/feed/export) — I can specify details
B. Investigate what’s feasible and allowed; if live querying isn’t feasible/allowed for MVP, use a curated/imported Sydänmerkki recipe set as a fallback
C. Keep as an open assumption for now (investigate in later stages)
X. Other (please specify)

[Answer]: B — Investigate what’s feasible and allowed; if live querying isn’t feasible/allowed for MVP, use a curated/imported Sydänmerkki recipe set as a fallback.

## Q15. [Product/Technical] PRISMA ingredient availability data (from Q2, Q12): how should “available PRISMA data” be obtained?

You want to verify ingredient availability against the selected PRISMA store using available PRISMA data. Data access, accuracy, permissions, and method aren’t confirmed.

A. I know an official/allowed PRISMA data source/API for availability — I can specify details
B. Investigate what’s feasible and allowed; if automated availability isn’t accessible for MVP, use best-effort/unknown indicators or checklist fallback
C. Keep as an open assumption for now (investigate in later stages)
X. Other (please specify)

[Answer]: B — Investigate what’s feasible and allowed; if automated availability isn’t accessible for MVP, use best-effort/unknown indicators or checklist fallback.

## Q16. [Product/Technical] Nearest PRISMA store by location (from Q10): what’s acceptable for location and store data?

You chose to suggest the nearest store by phone location (with permission) but allow manual selection.

A. Phone location permission is acceptable; investigate/use a reliable PRISMA store-location data source
B. Phone location permission is acceptable; I can specify the store-location data source
C. Prefer manual store selection for MVP (location suggestion later)
D. Keep as an open assumption for now
X. Other (please specify)

[Answer]: A — Phone location permission is acceptable; investigate/use a reliable PRISMA store-location data source.

## Q17. [Product/Technical] S-ostoslista official import method (from Q2, Q13): what if the official method/format isn’t confirmed?

You chose to use an official S-ostoslista import/share method if one exists (format to be confirmed).

A. I know the official import/share method and format — I can specify details
B. Investigate the official method; if none is available/confirmed for MVP, fall back to share-sheet/text export the user can open in S-ostoslista
C. Keep as an open assumption for now
X. Other (please specify)

[Answer]: X (Other) — The MealPlanner app user should have an S-ostoslista (`s-ostoslista.fi`) account; from there it’s possible to share a shopping list. Investigate the shopping list format. If a suitable official/share method or format cannot be confirmed for MVP, use a fallback (e.g., share-sheet/text export) that fits what S-ostoslista accepts.

## Q18. [Product] Seasonal region mapping (from Q9): how should “user’s region/season” be determined?

You chose seasonal recipes based on the current date/season in the user’s region.

A. Use Finland seasons by default based on current date (and/or device locale/region if available)
B. Let the user set/confirm their region or season (manual override)
C. Both: default from date/region, allow user override
D. Keep as an open assumption for now
X. Other (please specify)

[Answer]: C — Both: default from date/region, allow user override.

## Q19. [Stakeholders/Process] Remaining stakeholder/communication assumptions (from Q1, Q5, Q6): how should these be handled for the first version?

These are: household/family may influence preferences but no specific decision rights confirmed; future instructor/client/team authority/cadence not defined; no formal reporting cadence/channel beyond your decisions.

A. Keep these as open assumptions for the first version (no formal stakeholder/communication process needed now)
B. I want to define specific stakeholder roles or communication needs now
X. Other (please specify)

[Answer]: A — Keep these as open assumptions for the first version (no formal stakeholder/communication process needed now).

