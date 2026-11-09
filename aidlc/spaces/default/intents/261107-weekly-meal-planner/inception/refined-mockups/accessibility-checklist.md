# Accessibility Checklist — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner`  
**Stage:** Refined Mockups (inception)  
**Date:** 2026-11-08  
**Target:** WCAG 2.2 AA baseline (practical Android/mobile application), referenced where applicable — Q2=B.

## How to Use
- Apply this checklist during detailed design and implementation.
- Mark items `Pass`, `Fail`, `N/A`, or `Needs review` during QA/accessibility testing.
- Android implementation should also validate with TalkBack, font scaling, display size changes, and landscape orientation.

## Checklist

| ID | WCAG 2.2 AA area (applicable) | Requirement | MVP application | Status |
|---|---|---|---|---|
| A11Y-01 | 1.1.1 Non-text Content | Meaningful icons/images have text alternatives; decorative images are ignored. | Status icons (availability, source, season/nutrition cues) paired with text labels (`Unknown`, `Season fit unknown`, etc.). | Needs review |
| A11Y-02 | 1.3.1 Info and Relationships | Structure/headings/lists/controls convey relationships programmatically. | Screen titles, day/section headings, meal slot groups, checklist items, form controls labeled. | Needs review |
| A11Y-03 | 1.3.2 Meaningful Sequence | Reading/focus order matches visual/logical order. | Today/Plan lists, recipe details, grocery checklist, settings flow in logical order. | Needs review |
| A11Y-04 | 1.4.1 Use of Color | Color is not the only means of conveying information. | Availability, eligibility, badges, errors, and fallback states include text labels/icons. | Needs review |
| A11Y-05 | 1.4.3 Contrast (Minimum) | Text and essential UI components meet contrast guidance. | Verify Material 3 theme tokens in light/dark/system variants as implemented. | Needs review |
| A11Y-06 | 1.4.4 Resize Text | Content remains usable with larger text/font scaling. | Cards/lists/buttons wrap or expand; no clipped critical actions/labels at common font scales. | Needs review |
| A11Y-07 | 1.4.10 Reflow / 1.4.12 Text Spacing (practical) | Layout adapts without loss of content/function. | Portrait primary and landscape supported (Q4=A); scrollable content; no two-dimensional scrolling for core reading. | Needs review |
| A11Y-08 | 2.1.1 Keyboard / 2.1.2 No Keyboard Trap (practical) | Interactive elements are operable with assistive navigation; no traps. | External keyboard/switch/TalkBack navigation can reach all actions; dialogs/full-screen flows dismissible. | Needs review |
| A11Y-09 | 2.4.3 Focus Order | Focus moves in a predictable order. | Bottom nav, lists, cards, replace/store selection, checklist edit controls, export actions. | Needs review |
| A11Y-10 | 2.4.4 Link Purpose / 2.4.6 Headings and Labels | Labels/headings describe purpose. | “View recipe”, “Replace Monday dinner”, “Choose manually”, “Copy list text” are specific where needed. | Needs review |
| A11Y-11 | 2.4.7 Focus Visible (practical) | Focus indicator is visible. | Material/focus states visible for controls in all themes. | Needs review |
| A11Y-12 | 2.4.11 Focus Not Obscured (Minimum) | Focused element is not fully hidden by sticky UI. | Bottom nav/bars/snackbars do not obscure focused controls. | Needs review |
| A11Y-13 | 2.5.8 Target Size (Minimum) (practical mobile) | Touch targets are large enough/easy to activate. | Buttons, checkboxes, icon actions, nav items sized/Spaced for touch (target ~48dp guidance). | Needs review |
| A11Y-14 | 3.1.1 Language of Page (as applicable) | App/content language is identifiable. | Android locale/language metadata set; user-facing labels consistent. | Needs review |
| A11Y-15 | 3.2.3 Consistent Navigation / 3.2.4 Consistent Identification | Navigation/components behave consistently. | Bottom nav destinations stable; status labels reused consistently (`Unknown`, unavailable/fallback). | Needs review |
| A11Y-16 | 3.3.1 Error Identification / 3.3.3 Error Suggestion | Errors are identified clearly with recovery guidance. | Integration failures, permission denial, lookup failures, export failures show status + next action. | Needs review |
| A11Y-17 | 3.3.2 Labels or Instructions | Inputs/choices have clear labels and instructions. | Location permission purpose text; store selection; checklist edit fields; export instructions for S-ostoslista. | Needs review |
| A11Y-18 | 4.1.2 Name, Role, Value (practical) | Controls expose accessible name/role/state. | Checkboxes checked/unchecked, selected store, active nav item, expanded/collapsed states as used. | Needs review |
| A11Y-19 | 4.1.3 Status Messages (practical) | Important status changes can be perceived without moving focus unnecessarily. | Fallback activated, item added/removed, copy/export result, permission change, lookup failure announced appropriately. | Needs review |

## Screen-Specific Checks

| Screen | Key checks |
|---|---|
| Onboarding/Permissions | Location purpose is clear before/with request; manual-later path is accessible; privacy notice readable. |
| Today / Plan | Slot cards announce meal type/day/recipe/status; Replace/View actions have specific labels; weekday/weekend structure understandable. |
| Recipe detail | Back navigation, section headings, unavailable data labels, selection actions reachable. |
| Replace alternatives (full screen) | Rules summary readable; alternatives selectable; no-result state gives keep-current/retry path; focus returns to updated slot. |
| Grocery / Store selector | Manual store selection works without location; `Unknown` availability includes reason text; checklist edits announce changes; export actions distinguish official vs fallback. |
| User | Permission status and system-settings action clear; privacy/data statements readable. |

## Known MVP Notes
- Final color/type/token values are implementation-dependent; contrast and scaling must be verified after token finalization.
- Official S-ostoslista method UI depends on feasibility (OQ4); fallback copy/share/instructions remain mandatory and accessible.
- WCAG 2.2 is used as a practical baseline; Android-native accessibility behavior (TalkBack semantics) is the implementation acceptance lens.