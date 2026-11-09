# Team Practices

**Status:** Affirmed draft (greenfield, pending promotion).  
**Basis:** Human-confirmed answers in `practices-discovery-questions.md` for the Android-only MVP weekly meal planner (intent `261107-weekly-meal-planner`). Org defaults from `aidlc/spaces/default/memory/org.md` were used as suggestions, not established facts.

## Way of Working

We use a simple main line for day-to-day work. We make short-lived branches when helpful for focused changes and merge after checks pass. Commits made directly by a person stay on the branch they are already using unless we choose to isolate a change. [Q1]

## Walking Skeleton

We will decide later whether to build a thin end-to-end slice first (a minimal version that runs the whole way through, built first to prove the pieces connect before the real features go in). We will revisit this after requirements/user stories clarify the smallest useful slice, especially given uncertain third-party integrations (Sydänmerkki, PRISMA, S-ostoslista) and their fallbacks. [Q2]

## Testing Posture

We treat tests as part of delivering MVP behavior, especially around meal-plan generation/selection, shopping-list aggregation, integration adapters/fallbacks, and privacy-sensitive data handling. [intent-statement, scope-document]

- **Methodology**: custom
- **Ordering**: For key behavior, write tests before implementation (TDD/BDD style where practical), then implement to satisfy those tests; for supporting or lower-risk behavior, use the most practical test cadence and keep tests runnable in CI.
- **Methodology evidence**: Human affirmed Q3 for this learning/ASAP Android MVP. “Custom” reflects a practical mix (test-first for key behavior, flexible where practical) rather than a single pure cadence.
- **Quality targets:** Keep critical tests and CI checks in place; keep existing tests green. Do not weaken quality targets to make a step pass. Coverage expectations and Android test tooling/CI details will be confirmed as the codebase and pipeline are established.

## Deployment

This project is an Android application, so deployment primarily means verified builds and release distribution. We produce verified builds from the main line, run checks before sharing, and use internal testing before any wider release. [Q4]

If a small backend/service is introduced for integrations, we define separate build/release checks for that service, keep secrets out of source control, and require explicit approval before any production-facing release.

## Code Style

We follow idiomatic Kotlin/Android conventions and defer to project tooling where configured. We use consistent formatting and lint/static analysis in CI, and we handle integration/privacy concerns explicitly. [Q5]

- Organize code around features/domains as the codebase grows (planning, recipes, store/availability, shopping list, export/share, preferences/permissions).
- Handle errors explicitly at integration boundaries (network/data access, permissions, share/export) and make fallback behavior visible.
- Avoid hardcoded secrets, endpoints, or unnecessary personal data; follow GDPR/minimal-data and consent principles.
- Keep `sydanmerkki.fi`, PRISMA, and S-ostoslista integration behavior and fallbacks clearly documented/tested as access methods are confirmed.
