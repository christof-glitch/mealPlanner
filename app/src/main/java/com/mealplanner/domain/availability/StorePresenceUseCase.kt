package com.mealplanner.domain.availability

import com.mealplanner.adapters.PresenceMappingBoundary
import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.MapIngredientsToStorePresenceBatchRequest
import com.mealplanner.domain.model.PresenceBatchOverallStatus
import com.mealplanner.domain.model.PresenceBatchStatus
import com.mealplanner.domain.model.PresenceCheckBatchResult
import com.mealplanner.domain.model.PresenceCheckQuery
import com.mealplanner.domain.model.PresenceCheckResult
import com.mealplanner.domain.model.PresenceQueryItem
import com.mealplanner.domain.model.StorePresenceStatus
import com.mealplanner.domain.model.StoreRef
import java.time.Instant

/**
 * C5/A2 use case: best-effort PRISMA name/presence checks only (FR8/BR3.7).
 * Never mutates shopping items (BR3.5/R-02). Returns typed fallback outcomes.
 */
class StorePresenceUseCase(
    private val boundary: PresenceMappingBoundary,
) {
    suspend fun checkPresence(
        store: StoreRef,
        queries: List<PresenceCheckQuery>,
        checkedAt: Instant = Instant.now(),
    ): PresenceCheckBatchResult {
        if (queries.isEmpty()) {
            return PresenceCheckBatchResult(
                storeId = store.storeId,
                storeRef = store,
                results = emptyList(),
                batchStatus = PresenceBatchStatus.OK,
                message = "No items to check.",
            )
        }
        val mapped = boundary.mapIngredientsToStorePresenceBatch(
            MapIngredientsToStorePresenceBatchRequest(
                ingredientRefs = emptyList(),
                queryItems = queries.map { PresenceQueryItem(queryText = it.queryName, normalizedName = it.normalizedQueryName ?: it.queryName) },
                storeRef = store,
            ),
        )
        val batch = mapped.batchResult
        val byQuery = batch.results.groupBy { normalize(it.queryText) }
        val results = queries.map { q ->
            val matches = byQuery[normalize(q.queryName)].orEmpty()
            val chosen = when (matches.size) {
                1 -> matches.first()
                0 -> null
                else -> matches.firstOrNull { it.status != StorePresenceStatus.UNKNOWN } ?: matches.first()
            }
            val status = chosen?.status?.takeIf { it == StorePresenceStatus.PRESENT || it == StorePresenceStatus.NOT_FOUND || it == StorePresenceStatus.UNKNOWN }
                ?: StorePresenceStatus.UNKNOWN
            PresenceCheckResult(
                itemRef = q.itemRef,
                queryName = q.queryName,
                resultStatus = status,
                reason = chosen?.reason?.message ?: chosen?.reason?.code ?: if (matches.size > 1) "AMBIGUOUS_MAPPING" else "NO_RESULT",
                checkedAt = chosen?.checkedAt ?: checkedAt,
                storeId = store.storeId,
                checkScope = "PRISMA_NAME_PRESENCE",
                raw = chosen,
            )
        }
        val batchStatus = mapBatchStatus(batch.overallStatus, batch.fallbackReason, mapped.mappingWarnings.mapNotNull { it.code })
        return PresenceCheckBatchResult(
            storeId = store.storeId,
            storeRef = store,
            results = results,
            batchStatus = batchStatus,
            message = batch.fallbackReason?.message ?: mapped.mappingWarnings.firstOrNull()?.message,
            fallbackReason = batch.fallbackReason ?: mapped.mappingWarnings.firstOrNull()?.let { FallbackReason(it.code, it.message, it.userActionRequired) },
        )
    }

    private fun mapBatchStatus(
        overall: PresenceBatchOverallStatus,
        fallback: FallbackReason?,
        warningCodes: List<String>,
    ): PresenceBatchStatus {
        val code = (fallback?.code ?: warningCodes.firstOrNull()).orEmpty().uppercase()
        return when {
            code.contains("DENIED") -> PresenceBatchStatus.DENIED
            code.contains("UNAVAILABLE") || code.contains("NOT_CONFIGURED") -> PresenceBatchStatus.UNAVAILABLE
            code.contains("FAILED") || code.contains("ERROR") || code.contains("TIMEOUT") -> PresenceBatchStatus.FAILED_MAPPED
            overall == PresenceBatchOverallStatus.COMPLETE -> PresenceBatchStatus.OK
            overall == PresenceBatchOverallStatus.PARTIAL -> PresenceBatchStatus.PARTIAL
            overall == PresenceBatchOverallStatus.FAILED -> PresenceBatchStatus.FAILED_MAPPED
            else -> PresenceBatchStatus.PARTIAL
        }
    }

    private fun normalize(value: String): String = value.trim().lowercase().replace(Regex("\\s+"), " ")
}
