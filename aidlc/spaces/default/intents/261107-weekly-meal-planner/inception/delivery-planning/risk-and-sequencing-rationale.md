# Risk and Sequencing Rationale — Weekly Meal Planner (Android MVP)

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Stage:** Delivery Planning (inception)  
**Date:** 2026-11-08  

## Sources

- [delivery-planning-questions.md](delivery-planning-questions.md) (Q1=C value-first, Q2=B lightweight WSJF-style, Q4=B mostly serial with investigation spikes, Q5=B confirm access/approvals before affected Bolts, Q6=B contract/DTO ambiguity carry-forward)
- [bolt-plan.md](bolt-plan.md), [team-allocation.md](team-allocation.md), [external-dependency-map.md](external-dependency-map.md)
- [unit-of-work.md](../units-generation/unit-of-work.md), [unit-of-work-dependency.md](../units-generation/unit-of-work-dependency.md), [unit-of-work-story-map.md](../units-generation/unit-of-work-story-map.md)
- [contract-summary.md](../contract-design/contract-summary.md), [requirements.md](../requirements-analysis/requirements.md)
- [project-description.json](../../project-description.json), `aidlc/spaces/default/memory/project.md` carry-forwards (R-01–R-03)

## Sequencing rationale (Q1=C, Q4=B)

**Build-first approach:** Value-first within the strict Unit DAG. The goal is to prioritize the most user-visible meal-planning flow as soon as each unit allows, while preserving mandatory fallbacks and explicit state handling.

**Why this fits the DAG:** The dependency graph is linear in effect: `u1-mvp-foundation-skeleton → u2-recipe-planning-completion → u3-grocery-store-export-completion → u4-android-ui-experience` (U3 depends on U1 and U2). U1 establishes the integrated foundation (slots, lock snapshot/ADR-006 semantics, re-resolve hook, basic list/export fallback, thin UI path). U2 delivers the primary planning value (eligible recipes, weekday ≤30 min explanations, seasonal/Sydänmerkki/fallback visibility, nutrition cues). U3 extends to grocery/store/export value with safe presence/export bridges. U4 completes the full Android UX.

**Execution mode:** Mostly serial (Q4=B). Parallel investigation spikes are allowed for OQ1–OQ4 only when they do not change DAG contracts or unit ownership boundaries. All Bolts are owned by `aidlc-developer-agent` (AI); team formation was skipped for MVP.

## Lightweight WSJF-style scoring (Q2=B)

Scores are relative (1–5, higher = more) to support rationale, not a formal weighted model.

| Bolt | Unit | User/business value | Urgency / time sensitivity | Risk reduction / opportunity enablement | Relative size/effort | Rationale (WSJF-style) |
|---|---|---:|---:|---:|---:|---|
| B1 | U1 | 4 | 4 | 5 | 3 | Foundational enabler: proves integrated loop and contract/fallback boundaries; unlocks all downstream units. |
| B2 | U2 | 5 | 4 | 4 | 4 | Highest user-visible planning value (7-day plan, eligible recipes, explanations, seasonal/Sydänmerkki cues) once foundation exists. |
| B3 | U3 | 4 | 3 | 4 | 4 | Converts plan into actionable grocery/export flow; carries integration/fallback risk (PRISMA/S-ostoslista) with safe degradation. |
| B4 | U4 | 5 | 3 | 3 | 4 | Completes end-to-end UX, accessibility/fallback clarity, and presentation of owned explanations/states; depends on stable domain contracts. |

**Interpretation:** Sequence follows DAG, not raw score alone. B1 precedes B2 despite B2’s high value because B2 depends on U1. B3 follows B2 because U3 depends on U2 (locked/eligible planning contracts and grocery generation inputs). B4 is last because it consumes all upstream contracts.

## Risks and mitigations

| Risk | Impact | Likelihood | Mitigation | Owner | When |
|---|---|---|---|---|---|
| Contract/DTO ambiguity causes rework (Q6=B, R-01–R-03) | High | Medium | Carry R-01–R-03 into Functional Design: define minimum required shapes for C2–C6/A1–A3 DTO/result types (or defer each with named owner + minimum fields/status values) before dependent Construction relies on them; add explicit C4/C5 presence projection/remap/Unknown-after-edit-merge-regenerate operation or U3-owned orchestrating use case; identify U3 grocery/export extraction seam (export use-case interface, export payload DTO, platform/share adapter port, dependencies inward). Preserve ADR-006 semantics, PRISMA name/presence only, S-ostoslista fallback. | `aidlc-developer-agent` (Functional Design/Construction), human review at gates | Before B2/B3 depend on affected contracts; U3 Functional Design for seam |
| Sydänmerkki access/terms/data uncertainty (OQ1) | Medium | Medium/High | Confirm access/approvals before affected Bolt behavior (Q5=B). Keep curated/imported Sydänmerkki-compatible fallback mandatory; show clear source/fallback status (FR4/NFR2). Investigation spike allowed if DAG contracts unchanged. | Human for access/approvals; AI executor for adapter/fallback | Before/during B1–B2 affected behavior |
| PRISMA store-location/presence uncertainty (OQ2/OQ3) | Medium | Medium/High | Confirm access/approvals before affected Bolt behavior (Q5=B). Manual store selection always available; presence checks best-effort name/presence only (not stock/guarantee); Unknown/manual checklist fallback required (FR7/FR8). | Human for access/approvals; AI executor | Before/during B3 affected behavior |
| S-ostoslista official method/format unavailable (OQ4) | Medium | Medium | Investigate official-compatible path; MVP must always provide share sheet/copyable text fallback with clear instructions (FR10). | AI executor; human for approvals if needed | B3 (and can spike earlier without contract change) |
| Contract drift between units during serial Construction | Medium | Medium | Follow unit ownership and additive-first extension rules; U4 consumes only. Use explicit typed results/states; no public API in MVP. | AI executor | All Bolts |
| Privacy/minimal data or permission handling mistakes | High | Low/Medium | Apply NFR3/FR12 constraints: location optional, manual store selection, GDPR/minimal data, no account, on-device/sync-ready later, no unnecessary personal data. | AI executor | All Bolts; verify in Build and Test |
| Greenfield Android setup/tooling gaps | Medium | Medium | First checkpoint/verification command selection may be deferred until runnable project check exists (stage Step 6). Establish Android project structure in B1; keep tests runnable in CI posture per team practices. | AI executor; human for verification command decision | B1 / checkpoints |

## Walking skeleton note

Team practices mark walking skeleton as scope-dependent/decide-later. U1 is the smallest integrated foundation and is the candidate slice if the Construction walking-skeleton ceremony applies. B1’s Definition of Done requires a thin end-to-end path and explicit fallback behavior; the actual verified skeleton checkpoint/human approval follows Construction checkpoint rules and the recorded/selected verification command.
