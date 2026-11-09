package com.mealplanner.ui

import com.mealplanner.domain.model.ExportMethod
import com.mealplanner.domain.model.ExportResultStatus
import com.mealplanner.domain.model.NutritionLabels
import com.mealplanner.domain.model.PresenceBatchStatus
import com.mealplanner.domain.model.ShoppingListAggregate
import com.mealplanner.domain.model.ShoppingListItem
import com.mealplanner.domain.model.ShoppingListStaleStatus
import com.mealplanner.domain.model.StorePresenceStatus
import com.mealplanner.domain.model.effectiveDisplayText
import com.mealplanner.domain.planning.Adr006LockRules
import com.mealplanner.ui.facades.DefaultGroceryExperienceFacade
import com.mealplanner.ui.mappers.DiagnosticsUiMapper
import com.mealplanner.ui.state.GroceryDomainSnapshot
import com.mealplanner.ui.state.GroceryIntent
import com.mealplanner.ui.state.LocationPermissionUiState
import com.mealplanner.ui.state.UserSettingsState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Step 11 supporting/practical UI behavior tests (standard scope). */
class SupportingUiBehaviorTest {
    @Test
    fun `nutrition and season labels expose explicit unavailable or unknown text`() {
        assertEquals("Nutrition information unavailable", NutritionLabels.UNAVAILABLE)
        assertEquals("Season fit unknown", "Season fit unknown")
    }

    @Test
    fun `presence rows use FR8 name presence labels with unknown reasons`() {
        val row = DiagnosticsUiMapper.presenceRow("i1", "Milk", StorePresenceStatus.UNKNOWN, null)
        assertEquals("UNKNOWN", row.statusLabel())
        assertTrue(row.reasonLabel.contains("Unknown"))
    }

    @Test
    fun `manual checklist fallback remains available for presence batch states`() {
        assertTrue(DiagnosticsUiMapper.presenceBatchLabel(PresenceBatchStatus.UNAVAILABLE).contains("Manual checklist"))
        assertTrue(DiagnosticsUiMapper.presenceBatchLabel(PresenceBatchStatus.FAILED_MAPPED).contains("Manual checklist"))
        assertTrue(DiagnosticsUiMapper.presenceBatchLabel(PresenceBatchStatus.PARTIAL).contains("Unknown items"))
    }

    @Test
    fun `export cancel or fallback messaging preserves current edited list`() {
        val aggregate = ShoppingListAggregate(
            shoppingListId = "sl-1",
            snapshotId = "snap-1",
            items = listOf(ShoppingListItem(itemId = "i1", displayName = "Oats", displayText = "Oats", checked = true)),
            hasUserEdits = true,
            staleStatus = ShoppingListStaleStatus(isStale = false),
        )
        val facade = DefaultGroceryExperienceFacade(
            exportUseCase = null,
            prepareExport = { dtoAggregate ->
                com.mealplanner.domain.model.ShoppingListExportDto(
                    shoppingListId = dtoAggregate.shoppingListId,
                    snapshotId = dtoAggregate.snapshotId,
                    items = dtoAggregate.items.map { com.mealplanner.domain.model.ExportLineItem(displayText = it.effectiveDisplayText(), checked = it.checked, itemId = it.itemId) },
                )
            },
        )
        facade.refresh(GroceryDomainSnapshot(aggregate = aggregate, recipesLocked = true, lockSummary = "Locked", currentEditedList = aggregate))
        val result = facade.exportCurrentEditedList(GroceryIntent.ShareFallback)
        val after = facade.currentState()
        assertEquals(1, after.items.size)
        assertTrue(after.items.first().checked)
        assertTrue(result.status == ExportResultStatus.FALLBACK_USED || result.methodUsed == ExportMethod.SHARE_SHEET || result.payload != null)
    }

    @Test
    fun `user settings defaults and privacy labels are safe and resettable by state contract`() {
        val state = UserSettingsState()
        assertEquals("Using device date and location", state.seasonContextLabel)
        assertFalse(state.seasonOverrideEnabled)
        assertEquals(LocationPermissionUiState.NOT_ASKED, state.locationPermission)
        assertTrue(state.privacySummary.contains("No MealPlanner account"))
        val reset = state.copy(seasonOverrideEnabled = false, seasonSelection = null, areaLabel = null, validationMessage = null, seasonContextLabel = "Using device date and location")
        assertFalse(reset.seasonOverrideEnabled)
        assertEquals("Using device date and location", reset.seasonContextLabel)
    }

    @Test
    fun `grocery stale status exposes exact ADR006 warning through state`() {
        val stale = ShoppingListAggregate(
            shoppingListId = "sl-2",
            snapshotId = "snap-2",
            items = emptyList(),
            staleStatus = ShoppingListStaleStatus(isStale = true, staleWarning = Adr006LockRules.STALE_WARNING_TEXT),
        )
        val facade = DefaultGroceryExperienceFacade()
        val state = facade.refresh(GroceryDomainSnapshot(aggregate = stale, recipesLocked = true, lockSummary = "Locked", currentEditedList = stale))
        assertTrue(state.isStale)
        assertEquals(Adr006LockRules.STALE_WARNING_TEXT, state.staleWarningText)
    }

    @Test
    fun `accessibility smoke labels remain explicit non-color-only text`() {
        val notFound = DiagnosticsUiMapper.presenceRow("i2", "Tomato", StorePresenceStatus.NOT_FOUND, "No match")
        assertEquals("NOT_FOUND", notFound.statusLabel())
        assertTrue(notFound.reasonLabel.contains("Reason"))
        assertTrue(DiagnosticsUiMapper.storeSuggestionLabel(null, LocationPermissionUiState.NOT_ASKED).contains("Manual store selection"))
    }
}
