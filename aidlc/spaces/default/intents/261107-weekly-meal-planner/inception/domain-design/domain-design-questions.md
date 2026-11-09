# Domain Design Questions

**Intent:** `261107-weekly-meal-planner` (Android MVP, scope `mvp`)  
**Stage:** Domain Design (inception)  
**Sources:** [requirements.md](../requirements-analysis/requirements.md), [stories.md](../user-stories/stories.md), [refined mockups](../refined-mockups/mockups.md), [team-practices.md](../practices-discovery/team-practices.md)

Please answer by filling in each `[Answer]:` tag (or answer in chat and I’ll write them back).

---

## Q1. [Component boundary / deployment context] What is the MVP system boundary for domain components?

**Context:** Requirements state Android-only MVP, on-device by default/sync-ready later, and note stack undecided with a backend/service expected (investigation/fallback for Sydänmerkki, PRISMA, S-ostoslista). Domain Design identifies logical building blocks (code we write), not deployment topology. External systems are dependencies of components.

A. **Android app as the primary MVP boundary** — model domain components as modules/layers inside the Android app (planning, recipes/catalog, grocery/shopping, integrations/adapters, preferences/privacy). Any backend/service is treated as an external dependency/integration boundary to be detailed later if introduced. *(Recommended for MVP speed/on-device core.)*
B. **Android app + lightweight backend/service component(s) in scope** — include at least one backend/service logical component (e.g., integration proxy/normalization) alongside Android app components, even if implementation details come later.
C. **Defer backend boundary** — keep only Android app domain components now and record backend/service as an explicit open architectural decision in ADRs.
X. Other (please specify)

[Answer]: A — Android app as the primary MVP boundary; backend/service treated as external dependency/integration boundary for now.

---

## Q2. [Recipe/catalog ownership] How should recipe and suggestion logic be separated?

**Context:** FR2–FR6 cover plan structure, weekday ≤30 min rule, Sydänmerkki attempt/fallback, seasonal context, and nutrition cues/planning aid. Recipe data may come from live Sydänmerkki (if feasible) or curated/imported fallback.

A. **Separate Recipe Catalog/Source component from Meal Planning component** — Recipe Catalog owns recipe entities, source/fallback status, metadata normalization (time/season/nutrition cues), and eligibility filters; Meal Planning owns weekly slots, selections, locking, and slot replacement using catalog queries. *(Recommended: clear ownership and testability.)*
B. **Single Planning+Recipes component** — one component owns both weekly planning and recipe/catalog behavior for MVP simplicity.
C. **Three-way split** — Meal Planning, Recipe Catalog, and Suggestion/Eligibility Rules as distinct components.
X. Other (please specify)

[Answer]: A — Separate Recipe Catalog/Source from Meal Planning.

---

## Q3. [Grocery/shopping ownership] How should shopping-list generation, locking snapshot, and store/availability checks be modeled?

**Context:** FR7–FR9, R-03/R-04 fixes: list generated only after recipes are locked/finalized and by explicit user action; merge rules are deterministic; PRISMA store selection and ingredient name/presence checks are best-effort with `Unknown`/manual fallback.

A. **One Grocery component owning shopping list + PRISMA adapter boundary** — owns list generation from locked recipe snapshot, merge rules, edits/check-offs, store selection context, and best-effort presence-check orchestration; PRISMA is an external dependency/adapter.
B. **Split into Shopping List component and Store/Availability component** — Shopping List owns generation/edits/export-ready list state; Store/Availability owns PRISMA store selection and presence checks. *(Better separation if PRISMA complexity grows.)*
C. **Keep minimal: Shopping List component only** — treat PRISMA store/availability as a thin adapter detail inside Shopping List for MVP; record split as a future refactor if needed.
X. Other (please specify)

[Answer]: B — Split Shopping List and Store/Availability components.

---

## Q4. [Export/integration boundary] Where should S-ostoslista export/fallback logic live?

**Context:** FR10 requires investigating official S-ostoslista-compatible method if feasible, but MVP must always provide working share sheet/copyable text fallback using the current edited list.

A. **Inside Grocery/Shopping List component** — export is a responsibility of the shopping-list workflow (uses current edited list), with S-ostoslista/share as external dependencies/adapters. *(Recommended for MVP cohesion.)*
B. **Separate Export/Sharing component** — owns export formats, official-method capability detection, and fallback instructions; consumes shopping-list data.
C. **Platform/UI integration only** — model export as UI/platform behavior with no dedicated domain component; document in ADR.
X. Other (please specify)

[Answer]: A — Inside Grocery/Shopping List component (uses current edited list); S-ostoslista/share as external dependencies/adapters.

---

## Q5. [Preferences/privacy and cross-cutting state] How should preferences, permissions, and privacy-sensitive state be owned?

**Context:** FR5/FR12, NFR3: season context (device default or user override), location permission purpose/manual fallback, on-device MVP, no MealPlanner account, minimal data.

A. **Preferences & Privacy component** — owns local settings (season override, permission status metadata as needed), consent/purpose-visible state support, and privacy-relevant local persistence boundaries; other components depend on it for context. *(Recommended.)*
B. **Distribute ownership** — Meal Planning owns season context, Grocery owns location/store preference, UI owns permissions display; avoid a dedicated component.
C. **Local Settings as infrastructure-style external dependency** — treat Android DataStore/SharedPreferences/DB as the main boundary and keep preference logic inside feature components; document risks in ADR.
X. Other (please specify)

[Answer]: A — Preferences & Privacy component.

---

## Q6. [UI component structure] How should UI be represented in domain design artifacts?

**Context:** Refined mockups define Today/Plan/Grocery/User with Material 3 patterns and full-screen flows. Domain Design focuses on logical building blocks (code we write), not infrastructure; UI can be represented as presentation/application components if useful.

A. **Include a Presentation/UI Application component** — represents screen flows, state orchestration, and navigation boundaries (Today/Plan/Grocery/User, RM flows) that call domain components; keep domain logic out of UI. *(Recommended for traceability to mockups.)*
B. **Exclude UI from component catalogue** — focus only on domain/business components and capture UI implications in ADRs/rationale.
C. **Feature-based UI components** — separate UI/application components per feature (Plan UI, Recipes UI, Grocery UI, User UI) mapped to domain components.
X. Other (please specify)

[Answer]: A — Include a Presentation/UI Application component.

---

## Consolidated Summary Confirmation

- **Q1 [System boundary]:** A — Android app as the primary MVP boundary; backend/service treated as external dependency/integration boundary for now.
- **Q2 [Recipe/catalog ownership]:** A — Separate Recipe Catalog/Source from Meal Planning.
- **Q3 [Grocery ownership]:** B — Split Shopping List and Store/Availability components.
- **Q4 [Export boundary]:** A — Export/fallback inside Grocery/Shopping List component; S-ostoslista/share as external dependencies/adapters.
- **Q5 [Preferences/privacy]:** A — Preferences & Privacy component.
- **Q6 [UI structure]:** A — Include a Presentation/UI Application component.

**Ambiguity check (Step 3):** Answers are clear and consistent with MVP requirements/mockups. Q3=B + Q4=A means Shopping List owns generation/edits/export and depends on Store/Availability for presence context where needed; export remains in Shopping List. No blocking contradictions found.

Does this all look correct before I generate Domain Design artifacts (`components.md`, `decisions.md`, `traceability.json`)?
- Looks correct
- Request changes

[Answer]: Looks correct 
