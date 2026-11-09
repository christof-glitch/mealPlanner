package com.mealplanner.adapters

import com.mealplanner.diagnostics.DiagnosticCategory
import com.mealplanner.diagnostics.DiagnosticsSink
import com.mealplanner.diagnostics.NoOpDiagnosticsSink
import com.mealplanner.diagnostics.PrivacySafeDiagnostics
import com.mealplanner.domain.model.A1InvestigationOutcome
import com.mealplanner.domain.model.ExcludedRecipeCandidate
import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.Freshness
import com.mealplanner.domain.model.IngredientLine
import com.mealplanner.domain.model.RecipeAdapterStatus
import com.mealplanner.domain.model.RecipeCandidateResult
import com.mealplanner.domain.model.RecipeDataCompleteness
import com.mealplanner.domain.model.RecipeDetail
import com.mealplanner.domain.model.RecipeId
import com.mealplanner.domain.model.RecipeQueryStatus
import com.mealplanner.domain.model.RecipeSourceAdapterRequest
import com.mealplanner.domain.model.RecipeSourceAdapterResult
import com.mealplanner.domain.model.RecipeSourceQueryType
import com.mealplanner.domain.model.RecipeSourceStatus
import com.mealplanner.domain.model.RecipeSourceType
import com.mealplanner.domain.model.RecipeSummary
import com.mealplanner.domain.model.U2Validation

/**
 * Live Sydänmerkki execution gate (FQ1=A). Defaults must be false/false until OQ1 approved.
 * Injected/configured explicitly; never a silent toggle.
 */
interface A1LiveAccessGate {
    val allowLiveSource: Boolean
    val oq1Approved: Boolean
}

object DefaultA1LiveAccessGate : A1LiveAccessGate {
    override val allowLiveSource: Boolean = false
    override val oq1Approved: Boolean = false
}

data class A1LiveClientResult(
    val status: RecipeAdapterStatus,
    val recipes: List<RecipeDetail> = emptyList(),
    val summaries: List<RecipeSummary> = emptyList(),
    val fallbackReason: FallbackReason? = null,
    val partialDataUsable: Boolean? = null,
    val investigationOutcome: A1InvestigationOutcome? = null,
)

/** Outbound live client abstraction. No live network in unit tests; production wiring lives outside U2 domain scope. */
interface A1LiveRecipeClient {
    suspend fun execute(request: RecipeSourceAdapterRequest): A1LiveClientResult
}

/** Mandatory curated/imported Sydänmerkki-aligned fallback. */
interface CuratedRecipeFallbackSource {
    suspend fun find(request: RecipeSourceAdapterRequest): List<RecipeDetail>
    fun sourceStatus(): RecipeSourceStatus
}

/**
 * U2 A1 adapter boundary (BR2.1, NFR2.1, NFR2.4, NFR3.1-NFR3.3, NFR3.6, NFR5.2).
 * Gate -> bounded live attempt (optional retries, idempotent only) -> defensive mapping -> curated fallback.
 * Default-off live execution; no scraping/terms assumptions; no fabrication.
 */
class RecipeSourceAdapterBoundary(
    private val liveClient: A1LiveRecipeClient? = null,
    private val fallbackSource: CuratedRecipeFallbackSource,
    private val gate: A1LiveAccessGate = DefaultA1LiveAccessGate,
    private val diagnostics: DiagnosticsSink = NoOpDiagnosticsSink(),
    private val idempotentRetryCount: Int = 0,
) : RecipeSourceBoundary {

    private val fallbackStatus get() = fallbackSource.sourceStatus()

    override suspend fun execute(request: RecipeSourceAdapterRequest): RecipeSourceAdapterResult {
        val validation = U2Validation.validateAdapterRequest(request)
        if (validation != null) {
            record("a1.request.invalid", request, RecipeAdapterStatus.unavailable)
            return unavailable(validation.code, validation.message, A1InvestigationOutcome.unknown)
        }
        if (!canUseLive(request)) {
            record("a1.live.gated_off", request, RecipeAdapterStatus.fallback_used)
            return fallbackResult(request, "LIVE_DISABLED_DEFAULT", "Live recipe source disabled by default until OQ1 is approved and allowed.", A1InvestigationOutcome.not_feasible_or_allowed)
        }
        val client = liveClient ?: return fallbackResult(request, "LIVE_CLIENT_UNAVAILABLE", "Live recipe client is not configured.", A1InvestigationOutcome.not_feasible_or_allowed)
        val live = executeLiveWithRetries(client, request)
        return mapLiveOutcome(request, live)
    }

    private fun canUseLive(request: RecipeSourceAdapterRequest): Boolean =
        request.allowLiveSource && gate.allowLiveSource && gate.oq1Approved

    private suspend fun executeLiveWithRetries(client: A1LiveRecipeClient, request: RecipeSourceAdapterRequest): A1LiveClientResult {
        val maxAttempts = (idempotentRetryCount.coerceIn(0, 2)) + 1
        var last = A1LiveClientResult(RecipeAdapterStatus.unavailable)
        repeat(maxAttempts) { attempt ->
            last = safeLiveCall(client, request)
            if (last.status == RecipeAdapterStatus.ok || last.status == RecipeAdapterStatus.restricted || last.status == RecipeAdapterStatus.not_supported) return last
            if (!isRetryable(last.status) || attempt == maxAttempts - 1) return last
        }
        return last
    }

    private fun isRetryable(status: RecipeAdapterStatus): Boolean =
        status == RecipeAdapterStatus.timeout || status == RecipeAdapterStatus.unavailable

    private suspend fun safeLiveCall(client: A1LiveRecipeClient, request: RecipeSourceAdapterRequest): A1LiveClientResult = try {
        client.execute(request)
    } catch (_: Throwable) {
        // NFR3.3: never expose raw exceptions across boundary.
        A1LiveClientResult(RecipeAdapterStatus.parse_error_mapped, fallbackReason = FallbackReason("LIVE_EXECUTION_MAPPED_ERROR", "Live source execution could not be safely completed."))
    }

    private suspend fun mapLiveOutcome(request: RecipeSourceAdapterRequest, live: A1LiveClientResult): RecipeSourceAdapterResult {
        val safeRecipes = live.recipes.filter { it.title.isNotBlank() && it.recipeId.isNotBlank() }
        return when (live.status) {
            RecipeAdapterStatus.ok -> {
                if (safeRecipes.isEmpty() && live.summaries.isEmpty()) fallbackResult(request, live.fallbackReason?.code ?: "LIVE_EMPTY", live.fallbackReason?.message ?: "Live source returned no usable data.", live.investigationOutcome ?: A1InvestigationOutcome.feasible_limited)
                else {
                    record("a1.live.ok", request, RecipeAdapterStatus.ok)
                    RecipeSourceAdapterResult(
                        status = RecipeAdapterStatus.ok,
                        investigationOutcome = live.investigationOutcome ?: A1InvestigationOutcome.feasible_used,
                        recipes = safeRecipes.ifEmpty { null },
                        summaries = live.summaries.ifEmpty { null },
                        sourceStatus = liveSourceStatus(fallbackUsed = false),
                        partialDataUsable = live.partialDataUsable,
                    )
                }
            }
            RecipeAdapterStatus.parse_error_mapped -> {
                if (live.partialDataUsable == true && safeRecipes.isNotEmpty()) {
                    val fallback = fallbackSource.find(request).filter { fb -> safeRecipes.none { it.recipeId == fb.recipeId } }
                    val merged = safeRecipes + fallback
                    if (fallback.isNotEmpty()) {
                        RecipeSourceAdapterResult(
                            status = RecipeAdapterStatus.fallback_used,
                            investigationOutcome = live.investigationOutcome ?: A1InvestigationOutcome.feasible_limited,
                            recipes = merged,
                            sourceStatus = fallbackStatus,
                            fallbackReason = live.fallbackReason ?: FallbackReason("PARSE_ERROR_MAPPED_FALLBACK_FILLED", "Partial live data used; curated fallback filled missing records."),
                            partialDataUsable = true,
                        )
                    } else {
                        RecipeSourceAdapterResult(
                            status = RecipeAdapterStatus.parse_error_mapped,
                            investigationOutcome = live.investigationOutcome ?: A1InvestigationOutcome.feasible_limited,
                            recipes = merged,
                            sourceStatus = liveSourceStatus(fallbackUsed = false),
                            fallbackReason = live.fallbackReason ?: FallbackReason("PARSE_ERROR_MAPPED_PARTIAL", "Only partial live data could be safely mapped."),
                            partialDataUsable = true,
                        )
                    }
                } else fallbackResult(request, live.fallbackReason?.code ?: "PARSE_ERROR_MAPPED", live.fallbackReason?.message ?: "Live data could not be safely mapped.", live.investigationOutcome ?: A1InvestigationOutcome.feasible_limited)
            }
            RecipeAdapterStatus.restricted, RecipeAdapterStatus.not_supported, RecipeAdapterStatus.timeout, RecipeAdapterStatus.unavailable -> {
                val fallback = fallbackSource.find(request)
                if (fallback.isNotEmpty()) fallbackResult(request, live.fallbackReason?.code ?: live.status.name.uppercase(), live.fallbackReason?.message ?: "Live source unavailable; curated fallback used.", live.investigationOutcome ?: A1InvestigationOutcome.feasible_limited, fallback)
                else unavailable(live.fallbackReason?.code ?: live.status.name.uppercase(), live.fallbackReason?.message ?: "Live source unavailable and no fallback data.", live.investigationOutcome ?: A1InvestigationOutcome.feasible_limited)
            }
            RecipeAdapterStatus.fallback_used -> fallbackResult(request, live.fallbackReason?.code ?: "FALLBACK_USED", live.fallbackReason?.message, live.investigationOutcome ?: A1InvestigationOutcome.unknown)
        }
    }

    private suspend fun fallbackResult(request: RecipeSourceAdapterRequest, code: String, message: String?, outcome: A1InvestigationOutcome, preloaded: List<RecipeDetail>? = null): RecipeSourceAdapterResult {
        val recipes = preloaded ?: fallbackSource.find(request)
        record("a1.fallback", request, if (recipes.isEmpty()) RecipeAdapterStatus.unavailable else RecipeAdapterStatus.fallback_used)
        return if (recipes.isEmpty()) unavailable(code, message ?: "No curated fallback data available.", outcome)
        else RecipeSourceAdapterResult(
            status = RecipeAdapterStatus.fallback_used,
            investigationOutcome = outcome,
            recipes = recipes,
            summaries = recipes.map { it.summary },
            sourceStatus = fallbackStatus,
            fallbackReason = FallbackReason(code, message),
        )
    }

    private fun unavailable(code: String, message: String?, outcome: A1InvestigationOutcome): RecipeSourceAdapterResult {
        val unavailableStatus = RecipeSourceStatus(RecipeSourceType.NONE, Freshness.UNKNOWN, fallbackUsed = false, fallbackReason = FallbackReason(code, message))
        return RecipeSourceAdapterResult(
            status = RecipeAdapterStatus.unavailable,
            investigationOutcome = outcome,
            sourceStatus = unavailableStatus,
            fallbackReason = FallbackReason(code, message),
            userSafeMessageKey = "RECIPE_SOURCE_UNAVAILABLE",
        )
    }

    private fun liveSourceStatus(fallbackUsed: Boolean) = RecipeSourceStatus(
        sourceType = RecipeSourceType.SYDANMERKKI,
        freshness = Freshness.CURRENT,
        fallbackUsed = fallbackUsed,
    )

    // --- U1-compatible baseline methods ---
    override suspend fun findCandidates(query: Map<String, String>): RecipeCandidateResult {
        val result = execute(RecipeSourceAdapterRequest(RecipeSourceQueryType.CATALOG, filters = null, allowLiveSource = false))
        val candidates = result.summaries ?: result.recipes?.map { it.summary }.orEmpty()
        val status = when (result.status) {
            RecipeAdapterStatus.ok -> RecipeQueryStatus.OK
            RecipeAdapterStatus.fallback_used, RecipeAdapterStatus.parse_error_mapped -> RecipeQueryStatus.FALLBACK
            else -> if (candidates.isEmpty()) RecipeQueryStatus.UNAVAILABLE else RecipeQueryStatus.PARTIAL
        }
        return RecipeCandidateResult(
            queryContext = query,
            candidates = candidates,
            excludedCandidates = emptyList<ExcludedRecipeCandidate>(),
            status = status,
            sourceStatus = result.sourceStatus,
            fallbackReason = result.fallbackReason,
        )
    }

    override suspend fun getRecipeSummary(recipeId: RecipeId): RecipeSummary? {
        val result = execute(RecipeSourceAdapterRequest(RecipeSourceQueryType.BY_ID, recipeIds = listOf(recipeId), allowLiveSource = false))
        return result.summaries?.firstOrNull { it.recipeId == recipeId } ?: result.recipes?.firstOrNull { it.recipeId == recipeId }?.summary
    }

    override suspend fun resolveIngredients(recipeIds: List<RecipeId>): Map<RecipeId, List<IngredientLine>> {
        val result = execute(RecipeSourceAdapterRequest(RecipeSourceQueryType.BY_IDS, recipeIds = recipeIds, allowLiveSource = false))
        val details = result.recipes.orEmpty().associateBy { it.recipeId }
        return recipeIds.associateWith { id -> details[id]?.ingredients.orEmpty() }
    }

    override fun sourceStatus(): RecipeSourceStatus = fallbackStatus

    private fun record(name: String, request: RecipeSourceAdapterRequest, status: RecipeAdapterStatus) {
        diagnostics.record(PrivacySafeDiagnostics.event(name, DiagnosticCategory.INTEGRATION_FALLBACK, mapOf(
            "queryType" to request.queryType.name,
            "status" to status.name,
            "recipeCount" to (request.recipeIds?.size ?: 0).toString(),
        )))
    }
}
