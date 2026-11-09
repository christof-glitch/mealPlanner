# Refined Mockups Questions

**Intent:** `261107-weekly-meal-planner` (Android MVP)  
**Sources:** [wireframes.md](../../ideation/rough-mockups/wireframes.md), [user-flow.md](../../ideation/rough-mockups/user-flow.md), [stories.md](../user-stories/stories.md), [requirements.md](../requirements-analysis/requirements.md)

Please answer by filling in each `[Answer]:` tag (or answer in chat and I’ll write them back).

---

## Q1. [Design system] What design baseline should the refined mockups target?

**Context:** The MVP is Android-only. Refined artifacts need a design-system mapping (components/tokens/patterns). The team follows idiomatic Android conventions; no existing design system was specified.

A. **Material 3 (Material You) baseline** — use Material 3 components/patterns as the default mapping (top/bottom navigation, cards, bottom sheets, chips/badges, checklists, dialogs, snackbars) with neutral tokens to be finalized later.
B. **Custom lightweight design system** — define a small app-specific component/token set inspired by Android patterns but not tied strictly to Material 3 component names.
C. **Defer detailed mapping** — keep mockups platform-neutral/Android-friendly and provide only a high-level mapping table for implementation to refine.
X. Other (please specify)

[Answer]: A — **Material 3 (Material You) baseline** with neutral tokens to be finalized later.

---

## Q2. [Accessibility] What accessibility target should the checklist use?

**Context:** Requirements and rough mockups call for clear labels, non-color-only indicators, focus order, and permission copy. Refined mockups must produce an accessibility compliance checklist.

A. **WCAG 2.1 AA baseline** (recommended for MVP) — apply relevant mobile/web-content principles to Android UI: perceivable text/contrast guidance, operable touch targets/focus, understandable labels/errors, robust semantics.
B. **WCAG 2.2 AA baseline** — same practical scope as A, using WCAG 2.2 AA as the checklist reference where applicable.
C. **Practical Android accessibility MVP checklist** — focus on TalkBack labels, touch target size, contrast, non-color indicators, focus order, error/permission clarity without formal WCAG clause mapping.
X. Other (please specify)

[Answer]: B — **WCAG 2.2 AA baseline** (practical Android/mobile application), referenced where applicable.

---

## Q3. [Interaction patterns] Which key interaction patterns should be specified?

**Context:** Rough mockups show meal-slot replacement, recipe detail navigation, store selection, checklist editing, and share/export. Refined interaction specs need consistent states and transitions.

A. **Use rough-mockup patterns:** bottom sheet/list for “Replace meal / Alternatives” (US1.2, R-02), full-screen/pushed Recipe detail, bottom navigation (Today/Plan/Grocery/User), inline checklist editing, Android share sheet + copy-text fallback for export.
B. Same as A, but prefer **full-screen flows** over bottom sheets for replacement and store selection (simpler navigation/state handling).
C. Same as A, but make **Recipe detail and alternatives** adaptable (bottom sheet on large content optional; primary path is navigated screen) and keep export as native share + copy fallback.
X. Other (please specify)

[Answer]: B — Prefer **full-screen flows** over bottom sheets for replacement/alternatives and store selection; pushed Recipe detail; bottom navigation (Today/Plan/Grocery/User); inline checklist editing; Android share sheet + copy-text fallback.

---

## Q4. [Responsive/scope] What device/layout scope should the refined mockups cover?

**Context:** Android-only MVP, phone-first. Refined mockups should define responsive behavior/breakpoints as needed.

A. **Phone portrait primary, phone landscape supported** — optimize for portrait; define key adaptations for landscape (scrolling lists, bottom nav/action placement). No tablet-specific layouts in MVP.
B. **Phone portrait only for MVP** — specify portrait behavior and note landscape/tablet as follow-up.
C. **Phone + basic tablet adaptation** — include simple breakpoint notes for larger widths (e.g., two-pane/list-detail where useful) while keeping MVP scope minimal.
X. Other (please specify)

[Answer]: A — **Phone portrait primary, phone landscape supported**; no tablet-specific layouts in MVP.

---

## Consolidated Summary Confirmation

- **Q1 [Design system]:** A — Material 3 (Material You) baseline with neutral tokens to be finalized later.
- **Q2 [Accessibility]:** B — WCAG 2.2 AA baseline (practical Android/mobile), referenced where applicable.
- **Q3 [Interaction patterns]:** B — Full-screen flows preferred over bottom sheets for replacement/alternatives and store selection; pushed Recipe detail; bottom navigation (Today/Plan/Grocery/User); inline checklist editing; Android share sheet + copy-text fallback.
- **Q4 [Responsive/scope]:** A — Phone portrait primary, phone landscape supported; no tablet-specific layouts in MVP.

Does this all look correct before I generate the refined mockup artifacts?
- Looks correct
- Request changes

[Answer]: Looks correct
