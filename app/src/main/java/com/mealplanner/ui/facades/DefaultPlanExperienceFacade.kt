package com.mealplanner.ui.facades

import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.PlanLockState
import com.mealplanner.domain.model.PlanSlotRef
import com.mealplanner.domain.model.RecipeSummary
import com.mealplanner.domain.model.SlotSelectionChangeRequest
import com.mealplanner.domain.model.SlotSelectionOperation
import com.mealplanner.domain.model.WeeklyPlanState
import com.mealplanner.domain.planning.C3SlotSelectionService
import com.mealplanner.domain.planning.WeekdayEligibilityEvaluator
import com.mealplanner.domain.recipe.CuratedRecipeCatalog
import com.mealplanner.ui.state.MealSlotUiModel
import com.mealplanner.ui.state.PlanIntent
import com.mealplanner.ui.state.PlanScreenState
import com.mealplanner.ui.state.SlotActionContext
import com.mealplanner.ui.state.SlotOrigin
import com.mealplanner.ui.state.TodayScreenState
import java.time.LocalDate
import kotlin.random.Random

/**
 * Thin UI orchestration (ADR-005): shared Today/Plan slot state over C3 domain rules (R-06).
 * Unfilled slots remain empty; no auto-fill/hide. Target-slot updates only via domain.
 */
class DefaultPlanExperienceFacade(
    initialPlan: WeeklyPlanState = WeekdayEligibilityEvaluator.createEmptyPlan("plan-1"),
    private val c3: C3SlotSelectionService = C3SlotSelectionService(),
    private val todayDayProvider: () -> DayOfWeek = {
        when (LocalDate.now().dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> DayOfWeek.MONDAY
            java.time.DayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
            java.time.DayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
            java.time.DayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
            java.time.DayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
            java.time.DayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
            java.time.DayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
        }
    },
    private val planId: String = initialPlan.planId,
    private val availableRecipesProvider: () -> List<RecipeSummary> = { CuratedRecipeCatalog.summaries() },
    private val random: Random = Random.Default,
) : PlanExperienceFacade {
    private var plan: WeeklyPlanState = initialPlan
    private var lastFocusedSlotId: String? = null
    private var statusMessage: String? = null
    private var r05Visible: Boolean = false
    private var bundledFallbackAvailable: Boolean = true

    internal fun setPlanState(next: WeeklyPlanState) { plan = next }

    override fun currentPlanState(): PlanScreenState = toPlanScreenState()

    override fun currentTodayState(): TodayScreenState = toTodayScreenState()

    override fun handle(intent: PlanIntent): PlanScreenState = when (intent) {
        is PlanIntent.SelectOrReplace -> {
            if (intent.recipeId.isNullOrBlank()) clearSlot(intent.context) else selectRecipe(intent.context, intent.recipeId)
        }
        is PlanIntent.ViewRecipe -> { lastFocusedSlotId = intent.context.slotId; toPlanScreenState() }
        is PlanIntent.ChooseRecipe -> { lastFocusedSlotId = intent.context.slotId; toPlanScreenState() }
        PlanIntent.LockRecipesForShopping -> lockRecipes()
        PlanIntent.Retry -> { statusMessage = "Retry requested."; r05Visible = false; toPlanScreenState() }
        PlanIntent.UseBundledFallbackCatalog -> { statusMessage = "Bundled fallback catalog selected."; r05Visible = false; toPlanScreenState() }
        PlanIntent.ContinueWithManualPlanning -> { statusMessage = "Continuing with manual planning."; r05Visible = false; toPlanScreenState() }
        PlanIntent.GenerateWeekPlan -> generateWeekPlan()
    }

    override fun selectRecipe(context: SlotActionContext, recipeId: String): PlanScreenState {
        val result = c3.apply(
            plan = plan,
            request = SlotSelectionChangeRequest(
                slotRef = context.slotRef,
                operation = SlotSelectionOperation.SET_SELECTION,
                recipeId = recipeId,
            ),
            lockOrListActive = lockOrListActive(plan),
        )
        plan = result.weeklyPlanState
        lastFocusedSlotId = context.slotId
        statusMessage = if (result.status.name == "APPLIED") "Recipe selected for ${context.dayOfWeek} ${context.mealType}." else result.rejectionReason
        return toPlanScreenState()
    }

    override fun clearSlot(context: SlotActionContext): PlanScreenState {
        val result = c3.apply(
            plan = plan,
            request = SlotSelectionChangeRequest(
                slotRef = context.slotRef,
                operation = SlotSelectionOperation.CLEAR_SELECTION,
            ),
            lockOrListActive = lockOrListActive(plan),
        )
        plan = result.weeklyPlanState
        lastFocusedSlotId = context.slotId
        statusMessage = "Slot cleared for ${context.dayOfWeek} ${context.mealType}."
        return toPlanScreenState()
    }

    /**
     * Fills coming-week slots (starting from current day) from available recipes:
     * breakfast slots <- breakfast recipes; lunch/dinner <- random non-breakfast recipes.
     * Each recipe appears at most once in the weekly calendar.
     */
    fun generateWeekPlan(): PlanScreenState {
        val all = availableRecipesProvider().distinctBy { it.recipeId }
        if (all.isEmpty()) {
            statusMessage = "No recipes available to generate a week plan."
            return toPlanScreenState()
        }
        val breakfastPool = all.filter { CuratedRecipeCatalog.isBreakfastTitle(it.title) }.shuffled(random).toMutableList()
        val nonBreakfastPool = all.filterNot { CuratedRecipeCatalog.isBreakfastTitle(it.title) }.shuffled(random).toMutableList()

        val orderedDays = comingWeekDomainDays()
        val used = mutableSetOf<String>()
        var next = plan
        var filled = 0
        var unfilled = 0

        orderedDays.forEach { day ->
            requiredMealTypes(day).forEach { mealType ->
                val slotRef = PlanSlotRef(next.planId, day, mealType)
                val pool = if (mealType == MealSlotType.BREAKFAST) breakfastPool else nonBreakfastPool
                val pick = pool.firstOrNull { it.recipeId !in used }
                if (pick != null) {
                    used += pick.recipeId
                    pool.remove(pick)
                    val result = c3.apply(
                        plan = next,
                        request = SlotSelectionChangeRequest(
                            slotRef = slotRef,
                            operation = SlotSelectionOperation.SET_SELECTION,
                            recipeId = pick.recipeId,
                        ),
                        lockOrListActive = lockOrListActive(next),
                    )
                    next = result.weeklyPlanState
                    if (result.status.name == "APPLIED") filled++ else unfilled++
                } else {
                    unfilled++
                }
            }
        }
        plan = next
        statusMessage = when {
            unfilled == 0 -> "Week plan generated."
            filled == 0 -> "No suitable recipes available to generate a week plan."
            else -> "Week plan generated for $filled slot(s); $unfilled slot(s) could not be filled with unique recipes."
        }
        return toPlanScreenState()
    }

    private fun lockRecipes(): PlanScreenState {
        val selectedIds = plan.slots.mapNotNull { it.assignedRecipeId }.distinct()
        plan = plan.copy(
            lockState = PlanLockState.LOCKED,
            lockedRecipeIds = selectedIds,
            staleWarning = null,
        )
        statusMessage = "Recipes locked for shopping list."
        return toPlanScreenState()
    }

    private fun lockOrListActive(p: WeeklyPlanState): Boolean =
        p.lockState != PlanLockState.UNLOCKED || p.activeLockSnapshotId != null

    private fun toPlanScreenState(): PlanScreenState {
        val recipeTitles = availableRecipesProvider().associate { it.recipeId to it.title }
        return PlanScreenState(
            planId = planId,
            weekSlots = plan.slots.map { slotToUiModel(it.slotRef, it.assignedRecipeId, SlotOrigin.PLAN, recipeTitles) },
            lockStateLabel = when (plan.lockState) {
                PlanLockState.LOCKED -> "Recipes locked"
                PlanLockState.STALE_LOCK -> "Recipes locked (changes detected)"
                PlanLockState.UNLOCKED -> "Recipes not locked"
            },
            lockSummary = "Recipes locked: ${plan.lockedRecipeIds.size}",
            isLocked = plan.lockState != PlanLockState.UNLOCKED,
            loading = false,
            r05State = if (r05Visible) com.mealplanner.ui.mappers.DiagnosticsUiMapper.r05NoLocalDataRemoteUnavailable(bundledFallbackAvailable) else null,
            lastFocusedSlotId = lastFocusedSlotId,
            statusMessage = statusMessage,
        )
    }

    private fun toTodayScreenState(): TodayScreenState {
        val today = todayDayProvider()
        val recipeTitles = availableRecipesProvider().associate { it.recipeId to it.title }
        val todaySlots = plan.slots.filter { it.slotRef.dayOfWeek == today }
            .map { slotToUiModel(it.slotRef, it.assignedRecipeId, SlotOrigin.TODAY, recipeTitles) }
        return TodayScreenState(
            dateLabel = today.name,
            todaySlots = todaySlots,
            seasonContextLabel = "Using device date and location",
            loading = false,
            r05State = if (r05Visible) com.mealplanner.ui.mappers.DiagnosticsUiMapper.r05NoLocalDataRemoteUnavailable(bundledFallbackAvailable) else null,
            lastFocusedSlotId = lastFocusedSlotId,
            statusMessage = statusMessage,
        )
    }

    private fun slotToUiModel(
        slotRef: PlanSlotRef,
        recipeId: String?,
        origin: SlotOrigin,
        recipeTitles: Map<String, String> = emptyMap(),
    ): MealSlotUiModel {
        val ctx = SlotActionContext(slotRef = slotRef, currentRecipeId = recipeId, origin = origin)
        return MealSlotUiModel(
            context = ctx,
            recipeTitle = recipeId?.let { recipeTitles[it] ?: "Recipe $it" },
            isEmpty = recipeId.isNullOrBlank(),
            statusLabels = if (recipeId.isNullOrBlank()) listOf("No recipe selected") else emptyList(),
        )
    }

    private fun comingWeekDomainDays(): List<DayOfWeek> {
        val start = todayDayProvider()
        val values = DayOfWeek.entries
        val startIndex = values.indexOf(start).coerceAtLeast(0)
        return (0..6).map { values[(startIndex + it) % values.size] }
    }

    private fun requiredMealTypes(day: DayOfWeek): List<MealSlotType> = when (day) {
        DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> listOf(MealSlotType.BREAKFAST, MealSlotType.LUNCH, MealSlotType.DINNER)
        else -> listOf(MealSlotType.BREAKFAST, MealSlotType.DINNER)
    }
}
