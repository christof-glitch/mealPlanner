# Infrastructure Specification — U4 `u4-android-ui-experience`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`, space `default`)  
**Unit:** U4 — Android UI experience (`ui`)  
**Stage:** Infrastructure Design (construction, per-unit)  
**Date:** 2026-11-09

## Scope and upstream
- [logical-components.md](../nfr-design/logical-components.md)
- [performance-design.md](../nfr-design/performance-design.md), [security-design.md](../nfr-design/security-design.md)
- [functional-spec.md](../functional-design/functional-spec.md), [contract-summary.md](../../../inception/contract-design/contract-summary.md)
- [components.md](../../../inception/domain-design/components.md)
- [infrastructure-design-questions.md](infrastructure-design-questions.md) (Q1–Q4 resolved A, autonomous MVP assumptions)

U4 is an on-device Android presentation unit. **No AWS/cloud resources are provisioned by U4.** MVP has no MealPlanner backend/account; external Sydänmerkki/PRISMA/S-ostoslista integrations remain investigational behind domain/adapter boundaries (OQ1–OQ4).

## Deployment

| Facet | Choice | Rationale |
|---|---|---|
| Compute model | Android client runtime on user device (Compose/Material 3 app); no containers/serverless/VMs/cloud compute for U4 | MVP scope is local-first Android app; NFR3/FR12 prohibit backend/account coupling. |
| Networking topology | No VPC/subnet/ingress/egress infrastructure owned by U4. Any future external API calls go through domain/adapters with typed fallback states; investigational integrations are not enabled/provisioned here. | Prevents fabricated cloud topology; preserves fallback/manual flows (NFR2, FR7/FR8/FR10). |
| Storage strategy | On-device/local app storage managed by existing domain/persistence layers (Room/DataStore as established in brownfield/U1–U3 patterns); no cloud databases/caches/queues provisioned. | Minimal data, on-device MVP (NFR3). |
| Environments | Developer local build, CI verification build, debug/internal test distribution, and release candidate build as applicable to Android release process; no dev/staging/prod cloud environments for U4. | Aligns with team deployment rule for Android apps (verified builds/internal testing before wider release). |
| IaC approach | N/A for cloud resources. Build configuration is Gradle/Kotlin DSL (`app/build.gradle.kts`, version catalogs/plugins) in source control. | No CDK/Terraform/CloudFormation resources needed for client-only UI unit. |
| Resource sizing | Android app resource sizing follows existing compile/min/target SDK and Compose runtime; no cloud instance/database sizing. | MVP practicality; profile only if performance issues observed (NFR1). |

## Infrastructure Services

| Service | Role | Configuration | Notes |
|---|---|---|---|
| Android build toolchain (Gradle/AGP/Kotlin/Compose) | Build/package service for app artifacts | Layout-explicit module tasks (`:app:testDebugUnitTest`, assemble/lint as configured). Java 17, compile/target 35, minSdk 26. | Existing brownfield config; no new cloud service. |
| Android platform services (permissions, share sheet, clipboard/settings intents) | Device/platform capabilities used via boundaries | Runtime permission flow for location (purpose-limited), Android share/copy intents for export fallback; no secrets/raw GPS stored. | Accessed through `PermissionStatusProvider`/`ShareExportPort` boundaries (BR3.9, NFR3.2–NFR3.4). |
| Local persistence (existing) | On-device state continuity | Room/DataStore/domain repositories owned by U1–U3; U4 consumes via facades. | No cloud DB/cache/queue/search/CDN/DNS/load balancer. |
| External integration endpoints (investigational) | Not provisioned by U4 | Sydänmerkki/PRISMA/S-ostoslista remain behind adapters with fallback/manual paths (OQ1–OQ4). | UI must render typed fallback/investigational states; no live credentials/endpoints added in U4 infra. |

## Shared Infrastructure
None provisioned. U4 shares codebase/build config and domain/persistence contracts with U1–U3; no cross-unit cloud resources or owner/consumer cloud boundaries. Shared UI resources (theme/accessibility helpers) are code-level concerns handled in code generation/review.
