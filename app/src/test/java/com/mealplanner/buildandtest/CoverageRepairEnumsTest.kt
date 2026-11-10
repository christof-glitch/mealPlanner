package com.mealplanner.buildandtest

import com.mealplanner.diagnostics.DiagnosticCategory
import com.mealplanner.diagnostics.NoOpDiagnosticsSink
import com.mealplanner.diagnostics.PrivacySafeDiagnostics
import com.mealplanner.diagnostics.DiagnosticEvent
import com.mealplanner.domain.model.A1InvestigationOutcome
import com.mealplanner.domain.model.ApplyPresenceStatus
import com.mealplanner.domain.model.DataSourceStatus
import com.mealplanner.domain.model.DayOfWeek as ModelDayOfWeek
import com.mealplanner.domain.model.GenerationStatus
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.PlanLockState
import com.mealplanner.domain.model.PresenceBatchStatus
import com.mealplanner.domain.model.SeasonFit
import com.mealplanner.domain.model.ShareMethod
import com.mealplanner.domain.model.SlotStatus
import com.mealplanner.domain.model.StorePresenceStatus
import com.mealplanner.domain.model.TimeConfidence
import com.mealplanner.domain.model.dayKind
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CoverageRepairEnumsTest {
    @Test
    fun `enum entries are loadable`() {
        assertTrue(ModelDayOfWeek.entries.isNotEmpty())
        ModelDayOfWeek.entries.forEach { it.dayKind() }
        assertTrue(MealSlotType.entries.isNotEmpty())
        assertTrue(SlotStatus.entries.isNotEmpty())
        assertTrue(PlanLockState.entries.isNotEmpty())
        assertTrue(GenerationStatus.entries.isNotEmpty())
        assertTrue(StorePresenceStatus.entries.isNotEmpty())
        assertTrue(PresenceBatchStatus.entries.isNotEmpty())
        assertTrue(ShareMethod.entries.isNotEmpty())
        assertTrue(TimeConfidence.entries.isNotEmpty())
        assertTrue(SeasonFit.entries.isNotEmpty())
    }

    @Test
    fun `privacy safe diagnostics filters forbidden keys`() {
        NoOpDiagnosticsSink().record(DiagnosticEvent(name = "x", category = DiagnosticCategory.APP))
        val sanitized = PrivacySafeDiagnostics.sanitizeAttributes(mapOf("lat" to "1", "ok" to "v", "gpsHint" to "x", "emailRaw" to "a"))
        assertFalse(sanitized.containsKey("lat"))
        assertFalse(sanitized.containsKey("gpsHint"))
        assertTrue(sanitized.containsKey("ok"))
        val event = PrivacySafeDiagnostics.event("n", DiagnosticCategory.EXPORT, mapOf("latitude" to "x", "safe" to "y"))
        assertFalse(event.attributes.containsKey("latitude"))
    }
}
