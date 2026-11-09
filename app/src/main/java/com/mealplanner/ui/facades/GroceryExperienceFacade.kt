package com.mealplanner.ui.facades

import com.mealplanner.domain.model.ExportResult
import com.mealplanner.ui.state.GroceryDomainSnapshot
import com.mealplanner.ui.state.GroceryIntent
import com.mealplanner.ui.state.GroceryScreenState

/**
 * Thin UI facade for Grocery (ADR-006/R-01-R-03, FR10, BR3.8).
 * Domain/C4/C6 own generation/merge/stale/export rules; UI owns confirmation gating and rendering.
 */
interface GroceryExperienceFacade {
    fun currentState(): GroceryScreenState
    /** Refreshes from domain snapshot (locked state, current edited list, stale status). */
    fun refresh(snapshot: GroceryDomainSnapshot): GroceryScreenState
    fun handle(intent: GroceryIntent): GroceryScreenState
    /** Exports the current edited list (never a stale pre-edit snapshot). */
    fun exportCurrentEditedList(intent: GroceryIntent): ExportResult
}
