package com.mealplanner.domain.availability

import com.mealplanner.adapters.PresenceMappingBoundary
import com.mealplanner.domain.model.BatchPresenceResult
import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.MapIngredientsToStorePresenceBatchRequest
import com.mealplanner.domain.model.MapIngredientsToStorePresenceBatchResult
import com.mealplanner.domain.model.PresenceBatchOverallStatus
import com.mealplanner.domain.model.PresenceBatchStatus
import com.mealplanner.domain.model.PresenceCheckQuery
import com.mealplanner.domain.model.PresenceQueryItem
import com.mealplanner.domain.model.PresenceSource
import com.mealplanner.domain.model.PrismaStoreRef
import com.mealplanner.domain.model.StorePresenceResult
import com.mealplanner.domain.model.StorePresenceStatus
import com.mealplanner.domain.model.StoreRef
import com.mealplanner.domain.model.StoreSelectionMode
import com.mealplanner.domain.model.StoreSelectionState
import com.mealplanner.domain.model.StoreSuggestionResult
import com.mealplanner.domain.model.StoreSuggestionStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StoreAvailabilityContractsTest {
    private val store = StoreRef(storeId = "prisma-1", displayName = "Prisma Kamppi", chain = com.mealplanner.domain.model.StoreChain.PRISMA)

    @Test
    fun `PRISMA presence mapping is name presence only with PRESENT NOT_FOUND UNKNOWN`() = runTest {
        val boundary = object : PresenceMappingBoundary {
            override suspend fun mapIngredientsToStorePresenceBatch(request: MapIngredientsToStorePresenceBatchRequest): MapIngredientsToStorePresenceBatchResult {
                return MapIngredientsToStorePresenceBatchResult(
                    batchResult = BatchPresenceResult(
                        storeRef = request.storeRef,
                        results = listOf(
                            StorePresenceResult(queryText = "tomato", storeRef = request.storeRef, status = StorePresenceStatus.PRESENT, source = PresenceSource.PRISMA),
                            StorePresenceResult(queryText = "dragonfruit", storeRef = request.storeRef, status = StorePresenceStatus.NOT_FOUND, source = PresenceSource.PRISMA),
                            StorePresenceResult(queryText = "mystery", storeRef = request.storeRef, status = StorePresenceStatus.UNKNOWN, source = PresenceSource.PRISMA),
                        ),
                        overallStatus = PresenceBatchOverallStatus.COMPLETE,
                    ),
                )
            }
        }
        val useCase = StorePresenceUseCase(boundary)
        val result = useCase.checkPresence(
            store = store,
            queries = listOf(
                PresenceCheckQuery(itemRef = "i1", queryName = "tomato"),
                PresenceCheckQuery(itemRef = "i2", queryName = "dragonfruit"),
                PresenceCheckQuery(itemRef = "i3", queryName = "mystery"),
            ),
        )
        assertEquals(PresenceBatchStatus.OK, result.batchStatus)
        assertEquals(setOf(StorePresenceStatus.PRESENT, StorePresenceStatus.NOT_FOUND, StorePresenceStatus.UNKNOWN), result.results.map { it.resultStatus }.toSet())
        // FR8: no stock/guarantee fields exist on result contract
    }

    @Test
    fun `batch partial unavailable denied and failed map to typed outcomes`() = runTest {
        val denied = object : PresenceMappingBoundary {
            override suspend fun mapIngredientsToStorePresenceBatch(request: MapIngredientsToStorePresenceBatchRequest): MapIngredientsToStorePresenceBatchResult =
                MapIngredientsToStorePresenceBatchResult(
                    batchResult = BatchPresenceResult(request.storeRef, emptyList(), PresenceBatchOverallStatus.FAILED, FallbackReason("DENIED", "Location/lookup denied")),
                    mappingWarnings = emptyList(),
                )
        }
        val result = StorePresenceUseCase(denied).checkPresence(store, listOf(PresenceCheckQuery("i1", "tomato")))
        assertTrue(result.batchStatus in setOf(PresenceBatchStatus.DENIED, PresenceBatchStatus.FAILED_MAPPED, PresenceBatchStatus.UNAVAILABLE))
        assertNotNull(result.message ?: result.fallbackReason?.message)
    }

    @Test
    fun `manual store selection is always available`() {
        val state = StoreSelectionUseCase().selectStoreManually(PrismaStoreRef(storeId = "manual-1", displayName = "My Prisma"))
        assertEquals(StoreSelectionMode.MANUAL, state.selectionMode)
        assertNotNull(state.selectedStore)
        assertEquals("manual-1", state.selectedStore?.storeId)
    }

    @Test
    fun `nearest suggestion failure keeps manual fallback available`() = runTest {
        val useCase = StoreSelectionUseCase(object : NearestStoreSuggestionPort {
            override suspend fun suggestNearest(): StoreSuggestionResult =
                StoreSuggestionResult(status = StoreSuggestionStatus.DENIED, message = "Location denied")
        })
        val suggestion = useCase.suggestNearestStore()
        assertEquals(StoreSuggestionStatus.DENIED, suggestion.status)
        val manual = useCase.selectStoreManually(PrismaStoreRef("s1", "Manual"))
        assertEquals(StoreSelectionMode.MANUAL, manual.selectionMode)
    }

    @Test
    fun `C5 use case never mutates shopping items`() = runTest {
        val boundary = object : PresenceMappingBoundary {
            override suspend fun mapIngredientsToStorePresenceBatch(request: MapIngredientsToStorePresenceBatchRequest): MapIngredientsToStorePresenceBatchResult =
                MapIngredientsToStorePresenceBatchResult(BatchPresenceResult(request.storeRef, listOf(StorePresenceResult(queryText = "tomato", storeRef = request.storeRef, status = StorePresenceStatus.PRESENT, source = PresenceSource.PRISMA)), PresenceBatchOverallStatus.COMPLETE))
        }
        val query = PresenceCheckQuery(itemRef = "original-id", queryName = "tomato")
        val result = StorePresenceUseCase(boundary).checkPresence(store, listOf(query))
        assertEquals("original-id", result.results.first().itemRef)
        // No ShoppingListItem type accepted/mutated by C5 port/use case API.
    }
}
