package com.mealplanner.adapters

import com.mealplanner.domain.model.A1InvestigationOutcome
import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.Freshness
import com.mealplanner.domain.model.RecipeAdapterStatus
import com.mealplanner.domain.model.RecipeDataCompleteness
import com.mealplanner.domain.model.RecipeDetail
import com.mealplanner.domain.model.RecipeSourceAdapterRequest
import com.mealplanner.domain.model.RecipeSourceQueryType
import com.mealplanner.domain.model.RecipeSourceStatus
import com.mealplanner.domain.model.RecipeSourceType
import com.mealplanner.domain.model.RecipeSummary
import com.mealplanner.domain.model.SydanmerkkiStatus
import com.mealplanner.domain.model.TimeConfidence
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class A1RecipeSourceAdapterGateTest {
    private val curatedSummary = RecipeSummary(
        recipeId = "curated-1", title = "Curated porridge", totalTimeMinutes = 10,
        timeConfidence = TimeConfidence.KNOWN, sydanmerkkiStatus = SydanmerkkiStatus.CURATED_FALLBACK,
        sourceStatus = RecipeSourceStatus(RecipeSourceType.CURATED_SYDANMERKKI_FALLBACK, Freshness.CACHED, true),
    )
    private val curatedDetail = RecipeDetail(
        recipeId = "curated-1", title = curatedSummary.title, summary = curatedSummary,
        dataCompleteness = RecipeDataCompleteness.PARTIAL, sourceStatus = curatedSummary.sourceStatus,
    )

    @Test
    fun `default-off gate never invokes live client and returns curated fallback with typed reason`() = runTest {
        val live = RecordingLiveClient(RecipeAdapterStatus.ok, recipes = listOf(curatedDetail))
        val fallback = FixedFallbackSource(listOf(curatedDetail))
        val adapter = RecipeSourceAdapterBoundary(
            liveClient = live,
            fallbackSource = fallback,
            gate = StaticA1LiveAccessGate(allowLiveSource = false, oq1Approved = false),
        )
        val result = adapter.execute(RecipeSourceAdapterRequest(RecipeSourceQueryType.CATALOG, allowLiveSource = true))
        assertEquals(0, live.calls)
        assertEquals(RecipeAdapterStatus.fallback_used, result.status)
        assertEquals(A1InvestigationOutcome.not_feasible_or_allowed, result.investigationOutcome)
        assertTrue(result.fallbackReason?.code?.contains("LIVE", ignoreCase = true) == true || result.userSafeMessageKey != null)
        assertNotNull(result.recipes?.firstOrNull { it.recipeId == "curated-1" })
    }

    @Test
    fun `live enabled but OQ1 not approved still uses curated fallback and never invokes live`() = runTest {
        val live = RecordingLiveClient(RecipeAdapterStatus.ok)
        val adapter = RecipeSourceAdapterBoundary(
            liveClient = live,
            fallbackSource = FixedFallbackSource(listOf(curatedDetail)),
            gate = StaticA1LiveAccessGate(allowLiveSource = true, oq1Approved = false),
        )
        val result = adapter.execute(RecipeSourceAdapterRequest(RecipeSourceQueryType.CATALOG, allowLiveSource = true))
        assertEquals(0, live.calls)
        assertEquals(RecipeAdapterStatus.fallback_used, result.status)
    }

    @Test
    fun `approved gate maps live success without fabrication`() = runTest {
        val liveDetail = curatedDetail.copy(
            recipeId = "live-1", summary = curatedSummary.copy(recipeId = "live-1", sydanmerkkiStatus = SydanmerkkiStatus.VERIFIED,
                sourceStatus = RecipeSourceStatus(RecipeSourceType.SYDANMERKKI, Freshness.CURRENT, false)),
            sourceStatus = RecipeSourceStatus(RecipeSourceType.SYDANMERKKI, Freshness.CURRENT, false),
            dataCompleteness = RecipeDataCompleteness.COMPLETE,
        )
        val live = RecordingLiveClient(RecipeAdapterStatus.ok, recipes = listOf(liveDetail))
        val adapter = RecipeSourceAdapterBoundary(
            liveClient = live,
            fallbackSource = FixedFallbackSource(emptyList()),
            gate = StaticA1LiveAccessGate(allowLiveSource = true, oq1Approved = true),
        )
        val result = adapter.execute(RecipeSourceAdapterRequest(RecipeSourceQueryType.CATALOG, allowLiveSource = true))
        assertEquals(1, live.calls)
        assertEquals(RecipeAdapterStatus.ok, result.status)
        assertEquals(A1InvestigationOutcome.feasible_used, result.investigationOutcome)
        assertEquals("live-1", result.recipes?.singleOrNull()?.recipeId)
    }

    @Test
    fun `timeout and restricted outcomes map to typed statuses and fallback when available`() = runTest {
        val timeout = RecipeSourceAdapterBoundary(
            liveClient = RecordingLiveClient(RecipeAdapterStatus.timeout),
            fallbackSource = FixedFallbackSource(listOf(curatedDetail)),
            gate = StaticA1LiveAccessGate(true, true),
        ).execute(RecipeSourceAdapterRequest(RecipeSourceQueryType.CATALOG, allowLiveSource = true))
        assertEquals(RecipeAdapterStatus.fallback_used, timeout.status)
        assertNotNull(timeout.fallbackReason)

        val restricted = RecipeSourceAdapterBoundary(
            liveClient = RecordingLiveClient(RecipeAdapterStatus.restricted),
            fallbackSource = FixedFallbackSource(emptyList()),
            gate = StaticA1LiveAccessGate(true, true),
        ).execute(RecipeSourceAdapterRequest(RecipeSourceQueryType.CATALOG, allowLiveSource = true))
        assertEquals(RecipeAdapterStatus.unavailable, restricted.status)
    }

    @Test
    fun `parse error mapped with safe partial data uses explicit status and may fill from fallback`() = runTest {
        val partial = curatedDetail.copy(recipeId = "partial-1", dataCompleteness = RecipeDataCompleteness.PARTIAL)
        val adapter = RecipeSourceAdapterBoundary(
            liveClient = RecordingLiveClient(RecipeAdapterStatus.parse_error_mapped, recipes = listOf(partial), partialDataUsable = true),
            fallbackSource = FixedFallbackSource(listOf(curatedDetail)),
            gate = StaticA1LiveAccessGate(true, true),
        )
        val result = adapter.execute(RecipeSourceAdapterRequest(RecipeSourceQueryType.CATALOG, allowLiveSource = true))
        assertTrue(result.status == RecipeAdapterStatus.fallback_used || result.status == RecipeAdapterStatus.parse_error_mapped)
        assertTrue(result.partialDataUsable == true || result.recipes?.isNotEmpty() == true)
        assertFalse(result.recipes?.any { it.title.isBlank() } ?: false)
    }

    private class StaticA1LiveAccessGate(override val allowLiveSource: Boolean, override val oq1Approved: Boolean) : A1LiveAccessGate

    private class RecordingLiveClient(
        private val status: RecipeAdapterStatus,
        private val recipes: List<RecipeDetail> = emptyList(),
        private val partialDataUsable: Boolean? = null,
    ) : A1LiveRecipeClient {
        var calls = 0
        override suspend fun execute(request: RecipeSourceAdapterRequest): A1LiveClientResult {
            calls++
            return A1LiveClientResult(status = status, recipes = recipes, partialDataUsable = partialDataUsable)
        }
    }

    private class FixedFallbackSource(private val recipes: List<RecipeDetail>) : CuratedRecipeFallbackSource {
        override suspend fun find(request: RecipeSourceAdapterRequest): List<RecipeDetail> = recipes
        override fun sourceStatus(): RecipeSourceStatus = RecipeSourceStatus(
            RecipeSourceType.CURATED_SYDANMERKKI_FALLBACK, Freshness.CACHED, fallbackUsed = true,
            fallbackReason = FallbackReason("CURATED_FALLBACK", "Curated fallback."),
        )
    }
}
