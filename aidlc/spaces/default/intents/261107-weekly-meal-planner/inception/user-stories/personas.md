# Personas

**Intent:** `261107-weekly-meal-planner` (Android MVP)  
**Source:** [user-stories-questions.md](user-stories-questions.md) Q1=A, [requirements.md](../requirements-analysis/requirements.md)

## Persona 1: Household Meal Planner (Primary)

| Field | Description |
|---|---|
| **Name** | Household Meal Planner |
| **Role** | Person in a household/family responsible for planning meals and preparing the shopping list |
| **Goals** | Plan a useful 7-day week of meals; get variety/inspiration; choose recipes they would actually use; keep weekday meals quick (≤30 min total time); consider nutrition and season; find ingredients at a selected PRISMA store; create and share/import a shopping list to S-ostoslista |
| **Pain points** | Weekly meal decisions are repetitive; recipe prep time is unclear; nutrition/season info is inconsistent; store availability is uncertain; shopping lists are tedious to assemble; external services may be unavailable or hard to integrate |
| **Context** | Uses an Android phone, may be at home or on the go; may grant/deny location; expects clear fallback behavior when data/integrations fail; values privacy/minimal data collection |
| **Devices/constraints** | Android-only MVP; on-device/local-first where possible; optional location with manual store selection; no MealPlanner account in MVP |
| **Success signal** | Can view a 7-day plan, select suitable recipes, review/edit a combined shopping list, and export/share it successfully (including fallback export) |

## Persona Relationships and Priority

- **Priority 1 (MVP focus):** Household Meal Planner — all MVP stories are written from this persona.
- **Embedded goals (not separate MVP personas):** “Shopper” (checklist/check-off/store context) and “recipe chooser” (browse/compare/select) are treated as goals/contexts of the Household Meal Planner per Q1=A. Health-conscious behavior is represented through nutrition-aware planning stories rather than a separate persona.

## Notes
- Stories remain household/family oriented but avoid assuming multiple accounts, shared devices, or medical/dietary prescription workflows in MVP.
- Fallback/reliability stories are user-visible outcomes for the same primary persona (clear status, next action, no blocked core flow).