# Practices Discovery Evidence

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`, Greenfield)  
**Sources inspected:** `aidlc-state.md`, `project-description.json` (via state context), `ideation/intent-capture/intent-statement.md`, `ideation/feasibility/feasibility-assessment.md`, `aidlc/spaces/default/memory/org.md`, `aidlc/spaces/default/memory/team.md` (no affirmed content). No reverse-engineering artifacts (greenfield). No git repository detected (`no-git`).

## Discovery Mode
- **Project type:** Greenfield.
- **Collaborators:** Off for `mvp` scope (`support_agents` empty); lead integrated alone. No contribution files produced.
- **Org defaults:** Used as suggestions only; human interview confirmed/selected practices.

## Interview Decisions (confirmed)
| Area | Decision | Notes |
|---|---|---|
| Way of Working | A — simple main line; short-lived branches when helpful; merge after checks pass | [Q1] |
| Thin end-to-end slice first (walking skeleton) | C — decide later after requirements/user stories clarify smallest useful slice | Glossed in interview question; [Q2] |
| Testing Posture | C — tests before implementation for key behavior (TDD/BDD style where practical), then implement to satisfy them | Recorded as `Methodology: custom` with explicit ordering; [Q3] |
| Deployment/Release | A — verified builds from main line; checks before sharing; internal testing before wider release | Android MVP distribution focus; [Q4] |
| Code Style | A — idiomatic Kotlin/Android; formatter + lint/static analysis in CI; explicit integration/privacy handling | [Q5] |

## Uncertainties / Follow-ups
- Walking-skeleton stance deferred until requirements/user stories define smallest useful slice (relevant given integration unknowns).
- Specific Android test tools (JUnit, Compose UI tests, instrumentation), CI provider, and numeric coverage targets to be confirmed when project structure/pipeline is established.
- If a backend/service is added for integrations, define service-specific practices and GDPR/data-flow responsibilities.
- External integration access methods (Sydänmerkki, PRISMA, S-ostoslista) remain unconfirmed; fallback behavior must stay explicit. [feasibility RAID]
