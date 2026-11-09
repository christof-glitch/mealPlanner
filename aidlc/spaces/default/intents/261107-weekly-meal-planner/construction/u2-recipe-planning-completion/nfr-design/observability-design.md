# Observability Design — U2 `u2-recipe-planning-completion`

**Intent:** `261107-weekly-meal-planner` (scope `mvp`)  
**Unit:** U2 — Recipe + planning completion (`library`)  
**Stage:** NFR Design (construction, per-unit)  
**Date:** 2026-11-09

## Upstream
- [observability-requirements.md](../nfr-requirements/observability-requirements.md) (NFR5.4–NFR5.6)
- [tech-stack-decisions.md](../nfr-requirements/tech-stack-decisions.md) (NFR4.1–NFR4.3 testability hooks)
- [security-design.md](security-design.md) (PII/secrets safety)
- [nfr-design-questions.md](nfr-design-questions.md) (Q3=A)

## Design goals
- Make C2/C3/A1 behavior observable through typed status/reason codes and safe diagnostics (NFR5.4, NFR5.5).
- Avoid external telemetry backend dependency for MVP U2 (NFR5.6).
- Support unit/adapter testing and U4 status presentation.

## Patterns and decisions

| Area | Design | Addresses | Notes |
|---|---|---|---|
| Typed status as primary signal | C2/C3/A1 return machine-readable typed statuses/reason keys already defined by contracts: `RecipeSourceAdapterResult` status + investigation outcome/fallback reason, `RecipeTimeNormalization`/exclusion reasons, `SeasonFitEvaluation` status, `NutritionProfile` availability labels, alternatives/slot-change results. Tests and U4 consume these types directly. | NFR5.4, NFR4.1 | Stable contract-first observability; no log parsing required. |
| Diagnostics hook interface | Define a small injectable `U2Diagnostics`/hook interface (or equivalent) at A1 adapter and key C2 normalization/selection boundaries. Default implementation is no-op for release/safe builds. Injectable fake captures events in tests. | NFR5.5, NFR4.2 | Q3=A. Keeps library decoupled from Android logging framework. |
| Safe structured fields | Allowed diagnostic fields: operation name, status/reason code, counts, timing bucket/duration class, and boolean flags (e.g., `fallbackUsed`, `liveGateAllowed`). Prohibited by default: PII, secrets, credentials, precise location, raw request/response payload bodies, and unnecessary personal data. | NFR5.5, NFR3.1, NFR3.2 | Security design constrains all logging. |
| No external telemetry in MVP | No analytics SDK, remote metrics pipeline, distributed tracing backend, or SLI/SLO dashboard owned by U2. Local/debug diagnostics and typed results are sufficient for MVP. | NFR5.6 | Future telemetry is a separate decision. |
| Test observability | Adapter/domain tests assert typed outcomes and, where useful, captured diagnostic events from fake hook (reason/status/count/timing class) without asserting sensitive content. CI remains network-independent. | NFR4.1, NFR4.2, NFR4.3 | Supports regression coverage for BR2.* and fallback/gate behavior. |

## Illustrative hook (non-normative)
```kotlin
interface U2Diagnostics {
  fun event(op: String, status: String, reason: String? = null, count: Int? = null, timingBucket: String? = null)
}
object NoOpU2Diagnostics : U2Diagnostics { override fun event(...) {} }
```

## Risks and follow-ups
- **Debuggability vs privacy:** if production issues require more detail, add redacted/opt-in diagnostics via explicit approved change; never log raw payloads/PII.
- **Hook overhead:** keep events lightweight and disabled/no-op by default in performance-sensitive paths.