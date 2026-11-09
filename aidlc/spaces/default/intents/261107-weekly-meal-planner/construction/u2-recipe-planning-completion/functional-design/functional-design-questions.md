# Functional Design Questions — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Unit:** U2 — Recipe + planning completion (`library`)  
**Stage:** Functional Design (construction, per-unit)  
**Conversation language:** English

## Sources
- [unit-of-work.md](../../../inception/units-generation/unit-of-work.md) (U2 scope, constraints)
- [unit-of-work-story-map.md](../../../inception/units-generation/unit-of-work-story-map.md)
- [requirements.md](../../../inception/requirements-analysis/requirements.md) (FR2–FR6, FR11, FR12, NFR1–NFR5, OQ1, OQ5–OQ7)
- [stories.md](../../../inception/user-stories/stories.md) (US1.1, US1.2, US2.1–US2.5, US5.4, US5.5, US5.6; AC2.2.3, AC2.3.4)
- [contract-summary.md](../../../inception/contract-design/contract-summary.md) (C2/C3 baseline/completion, A1, US2.2 AC2.2.3 trace)
- [U1 functional design](../../u1-mvp-foundation-skeleton/functional-design/functional-spec.md), [entities](../../u1-mvp-foundation-skeleton/functional-design/entities.md), [rules](../../u1-mvp-foundation-skeleton/functional-design/rules.md)

## Instructions
Answer each question by filling `[Answer]:` (letter, or text after `X`). If all recommendations look right, reply **`Looks correct`** and I’ll treat unedited recommended answers as accepted. After answers, I’ll do ambiguity analysis and ask follow-ups only if needed.

---

## Q1 — Sydänmerkki adapter boundary and MVP data source (OQ1, FR4)
U2 owns the Sydänmerkki adapter boundary/investigation hooks and clear source/fallback status behavior. Live access/terms/fields are investigational.

**Question:** What should U2’s functional design require for the Sydänmerkki data path?

A) **Contract + curated baseline first (Recommended):** Define `RecipeSourceAdapter`/A1-facing port with typed outcomes (`ok/fallback_used/unavailable/restricted/timeout/parse_error_mapped`) and deterministic curated/imported Sydänmerkki-aligned catalog as the runnable MVP source; live `sydanmerkki.fi` query remains an adapter implementation behind the same port, attempted only when feasibility/terms/fields are confirmed. No scraping assumptions.  
B) Live-first: require live query implementation attempt in U2 and use curated data only as emergency fallback (risk: terms/access unknown).  
C) Curated-only for MVP: document Sydänmerkki as deferred and use only curated/imported catalog (simpler, weaker FR4 investigation trace).  
X) Other (please specify): ___

[Answer]: B

---

## Q2 — Season fit mapping and unknown handling (OQ5, FR5, AC2.3.3–AC2.3.4)
Device date/location is default; season fit uses recipe metadata/tags or curated mapping; `Season fit unknown` must be safe. Season override/default/reset is required by AC2.3.4.

**Question:** How should U2 model season fit and override behavior?

A) **Explicit season model + tag/curated mapping (Recommended):** C3 exposes effective season resolution (`RESOLVED|UNKNOWN|OVERRIDDEN`) using device date + optional coarse location hint/area label context; C2 computes `seasonFitLabel` from recipe `seasonTags`/curated mapping table when present, else `Season fit unknown`. Support explicit override (`DEFAULT|OVERRIDE|RESET`) persisted via preferences contract; override takes precedence until reset. No fabricated season match.  
B) Use only month/date ranges in code contracts, no curated mapping abstraction (less flexible for Finnish produce tags).  
C) Defer override to U4/UI only and keep C2/C3 contracts minimal (risks AC2.3.4 trace gap).  
X) Other (please specify): ___

[Answer]: A

---

## Q3 — Nutrition cues and minimal planning aid (OQ6, FR6, BR1.14)
Nutrition cues/info shown when available; missing labeled `Nutrition information unavailable`; at least one minimal nutrition-aware planning aid during selection; non-medical/view-only.

**Question:** Which nutrition contract should U2 define for MVP?

A) **Reliably-available attribute set + simple weekly cue (Recommended):** Recipe nutrition model carries optional fields only when reliably available in source/fallback data (e.g., `energyKcal`, `proteinG`, `carbG`, `fatG`, `fiberG`, `sydanmerkkiNutritionFlags/tags`, `sourceConfidence`); any missing/partial data yields `nutritionAvailability=UNAVAILABLE|PARTIAL` and label `Nutrition information unavailable` where appropriate. Minimal planning aid: a **simple weekly balance cue** computed from available attributes only (e.g., counts/averages of selected recipes with available nutrition tags/values), never medical targets/dosing; unavailable data excluded from calculations and clearly indicated.  
B) Only labels/tags, no numeric cues (simpler but weaker planning aid).  
C) Full macro targets/ranges (too medical/precise for MVP).  
X) Other (please specify): ___

[Answer]: A

---

## Q4 — Weekday total-time normalization and eligibility (OQ7, FR3, AC2.2.3, BR1.2)
Weekday Mon–Fri `Breakfast`/`Dinner` suggestions use total prep/cooking time; eligible only when present, interpretable, ≤ 30 minutes; missing/ambiguous excluded with explanation.

**Question:** How should U2 normalize and report weekday time eligibility?

A) **Typed normalization + explicit verifiability (Recommended):** C2 normalizes source time into `totalTimeMinutes` when confidently parseable (integer minutes) and sets `timeVerifiability=KNOWN|MISSING|AMBIGUOUS` plus `weekdayEligible` for slot context. Ambiguous formats (ranges, conflicting prep+cook fields, unit confusion, unparseable text) are **not guessed**; they become `AMBIGUOUS` and excluded from weekday suggestions with `ExclusionReason` (`WEEKDAY_TIME_AMBIGUOUS` or `WEEKDAY_TIME_OVER_30`). Explanation data is contract-owned by U1/U2; U4 presents.  
B) Best-effort parse ranges to upper bound (risk: may incorrectly include/exclude).  
C) Treat any non-integer/simple format as missing (safe but may exclude usable data).  
X) Other (please specify): ___

[Answer]: A

---

## Q5 — Alternatives/replacement and Today/Plan synchronization (US1.2, R-06, C3 completion)
U2 completes replacement/alternative contracts and robust Today/Plan synchronized state contracts.

**Question:** What should U2’s C3 completion contract guarantee?

A) **Single slot-selection source of truth + alternative query contract (Recommended):** `WeeklyPlanState`/slot selections remain the single source for both Today and Plan views (R-06). C3 exposes per-slot replacement operations (`setSlotSelection`, `replaceWithAlternative`, `clearSlotSelection` as applicable) that affect only the target slot and emit updated observable state. C2 provides `getAlternatives` for a slot/recipe context respecting weekday eligibility, season fit, and fallback status; alternatives may be empty with explicit reason. Slot changes mark lock stale per ADR-006/BR1.9 (U1 baseline) — no auto-regenerate.  
B) Keep alternatives in UI only (conflicts with U2 domain completion responsibility).  
C) Return alternatives as part of plan state mutation responses only (couples concerns).  
X) Other (please specify): ___

[Answer]: A

---

## Q6 — U2 functional spec depth for UI-facing contracts
U2 is `library` kind; U4 owns UI. U2 must provide thin UI-consumable contracts.

**Question:** How detailed should U2 workflows/contracts be for UI consumption?

A) **Domain contract-focused (Recommended):** Specify entities/rules/workflows for recipe normalization, eligibility, season fit, nutrition cues/planning aid computation inputs, source/fallback status, alternatives/replacement, and Today/Plan sync semantics. Include clear request/response/state shapes and status enums; avoid screen layout/copy beyond contract labels required by rules (e.g., `Nutrition information unavailable`, `Season fit unknown`, AC2.2.3 explanation fields).  
B) Include detailed screen/state UI flows (duplicates U4).  
X) Other (please specify): ___

[Answer]: A

---

## Ambiguity Analysis (mandatory)

- **Q1=B ambiguity:** “Live-first / require live query implementation attempt in U2” can conflict with OQ1 (Sydänmerkki access method, terms, and fields unknown). FR4/scope require curated/imported fallback when live query is not feasible/allowed/fails, and we must not assume scraping/terms.
- **Proposed resolution (for artifact generation):** U2 defines/implements the **live Sydänmerkki adapter path** behind A1/C2 port and records investigation outcome (`feasible+used`, `feasible+limited`, `not feasible/allowed for MVP`). Live network/query execution happens **only when feasibility/terms/allowed access are confirmed**; otherwise the adapter returns typed `restricted`/`unavailable`/`not_supported` status and uses the **mandatory curated/imported Sydänmerkki-aligned fallback** as the runnable source. Curated fallback is not “emergency only” — it is the safe default until live access is confirmed. No terms violation/scraping assumptions.
- **Q2–Q6:** no blocking ambiguity detected.

## Follow-up (resolve before artifact generation)

**FQ1:** Do you accept the proposed Q1=B resolution above?  
A) **Yes** — live adapter path required in U2 design/implementation boundary, live execution gated by confirmed feasibility/terms, mandatory curated fallback always available.  
B) No — use Q1=A instead (recommended).  
C) No — different interpretation (please specify).  

[Answer]: A (confirmed 2026-11-09)

## Summary (optional)

[Notes]: 
