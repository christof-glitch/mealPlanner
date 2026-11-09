# Feasibility Questions — Weekly Meal Planner (MVP)

Sources: [intent-statement](../intent-capture/intent-statement.md)

## Q1. [Technical] Beyond the integrations already named (query recipes from sydanmerkki.fi, PRISMA store/location/availability data, and S-ostoslista shopping-list sharing), are there any other systems, services, or data sources this Android MVP must integrate with?

This helps identify integration constraints and technical risks early. The intent already captures Sydänmerkki, PRISMA, and S-ostoslista with investigate-and-fallback approaches. [intent-statement]

A. No other integrations for the first version
B. Yes — I can specify other integrations or services
C. Not sure yet; keep open and reassess later
X. Other (please specify)

[Answer]: A — No other integrations for the first version.

## Q2. [Technical] What Android technology stack and development skills will you use for this MVP (for example Kotlin vs Java, UI toolkit/framework, and whether you plan a backend/service)?

This affects feasibility, complexity, and how external data (recipes, PRISMA, S-ostoslista) is accessed. This is a learning project (Android MVP). [Q4], [Q8]

A. Native Android with Kotlin (Android Studio) — I’ll learn/build as I go; no backend planned unless needed
B. Native Android with Kotlin (Android Studio) — I expect to use a small backend/service if needed
C. Cross-platform framework (even though target is Android only for MVP) — I can specify
D. Not decided yet; I’d like a recommendation based on MVP goals
X. Other (please specify)

[Answer]: D — Not decided yet; I’d like a recommendation based on MVP goals. I expect the use of a backend/service.

## Q3. [Product/Technical] Where should the MVP store user choices and planning data (selected weekly meals, recipe selections, shopping list, preferred PRISMA store), and do you need any user account/sign-in in this app itself?

This clarifies data/privacy and architecture constraints. The intent notes S-ostoslista use requires an S-ostoslista account, but it does not require an account in MealPlanner itself. [Q2], [Q17]

A. On-device only for MVP (no MealPlanner account/cloud sync); S-ostoslista account is separate as already noted
B. On-device for MVP, but design so cloud sync/account could be added later
C. MealPlanner account/cloud sync is needed in the first version — I can specify
D. Not decided yet
X. Other (please specify)

[Answer]: B — On-device for MVP, but design so cloud sync/account could be added later.

## Q4. [Delivery] What timeline or learning constraints should shape the MVP’s technical complexity (for example a target date, limited weekly time, or “learn-first” pace)?

This helps assess feasibility and avoid over-scoping for a personal learning project. [Q4]

A. No fixed deadline — learning pace matters more than speed
B. I have a rough target timeframe — I can specify
C. I have a firm deadline — I can specify
D. Not defined
X. Other (please specify)

[Answer]: X (Other) — As early as possible (ASAP).

## Q5. [Technical] Are there any privacy, data-protection, or device-permission constraints to treat as requirements beyond what’s already decided (phone location permission is acceptable for nearest-store suggestion; manual store selection remains available)?

This checks for regulatory/privacy/permission risks (for example storing personal data, sharing data with third parties, or children’s data). [Q10], [Q16]

A. No additional constraints beyond location permission (with user consent) and normal Android app behavior; keep data handling minimal
B. Yes — I can specify constraints (privacy, data residency, permissions, etc.)
C. Not sure; treat as “keep minimal data, ask for consent where needed” for MVP
X. Other (please specify)

[Answer]: X (Other) — Yes, user personal privacy is very important. The MealPlanner app needs to comply with GDPR.

## Q6. [Delivery] Are there any current blockers or dependencies that could affect feasibility (for example Android test device/emulator access, PRISMA/S-ostoslista account access, or uncertainty you want resolved before building)?

This identifies organizational/personal blockers and dependencies early. [Q4], [Q17]

A. No known blockers; I can test on an Android device/emulator and use needed accounts
B. Yes — I can specify blockers or dependencies
C. Some uncertainty (accounts/access/testing setup) — I’ll confirm as we go
X. Other (please specify)

[Answer]: A — No known blockers; I can test on an Android device/emulator and use needed accounts.

## Consolidated Summary Confirmation

- Looks correct
- Request changes

[Answer]: Looks correct

