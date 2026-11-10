package com.mealplanner.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.PlanSlotRef
import com.mealplanner.domain.model.PrismaStoreRef
import com.mealplanner.ui.components.StatusBanner
import com.mealplanner.ui.screens.grocery.GroceryScreen
import com.mealplanner.ui.screens.onboarding.OnboardingScreen
import com.mealplanner.ui.screens.plan.PlanScreen
import com.mealplanner.ui.screens.recipe.RecipeDetailScreen
import com.mealplanner.ui.screens.store.SelectPrismaStoreScreen
import com.mealplanner.ui.screens.today.TodayScreen
import com.mealplanner.ui.screens.user.UserScreen
import com.mealplanner.ui.screens.recipe.AlternativeRecipeUiModel
import com.mealplanner.ui.screens.recipe.ReplaceAlternativesScreen
import com.mealplanner.ui.components.ErrorRetryPanel
import com.mealplanner.ui.components.GroceryChecklistRow
import com.mealplanner.ui.components.NutritionBalanceCueCard
import com.mealplanner.ui.components.R05FallbackPanel
import com.mealplanner.ui.components.RecipeMetaBadges
import com.mealplanner.ui.components.RegenerateEditedListConfirmationDialog
import com.mealplanner.ui.components.SeasonContextIndicator
import com.mealplanner.ui.components.StorePresenceStatusRow
import com.mealplanner.ui.state.GroceryItemUiModel
import com.mealplanner.ui.state.GroceryScreenState
import com.mealplanner.ui.state.LocationPermissionUiState
import com.mealplanner.ui.state.MealSlotUiModel
import com.mealplanner.ui.state.PresenceRowUiModel
import com.mealplanner.ui.state.PlanIntent
import com.mealplanner.ui.state.PlanScreenState
import com.mealplanner.ui.state.R05NextAction
import com.mealplanner.ui.state.R05State
import com.mealplanner.ui.state.SlotActionContext
import com.mealplanner.ui.state.SlotOrigin
import com.mealplanner.ui.state.StoreSelectionUiState
import com.mealplanner.ui.state.TodayScreenState
import com.mealplanner.ui.state.UserSettingsState
import com.mealplanner.domain.model.StoreChain
import com.mealplanner.domain.model.StorePresenceStatus
import com.mealplanner.domain.model.StoreRef
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UiRenderCoverageTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun slot(day: DayOfWeek, meal: MealSlotType, recipeId: String?, title: String?, origin: SlotOrigin): MealSlotUiModel =
        MealSlotUiModel(
            context = SlotActionContext(
                slotRef = PlanSlotRef("plan-ui", day, meal),
                dayOfWeek = day,
                mealType = meal,
                slotId = "plan-ui:$day:$meal",
                currentRecipeId = recipeId,
                origin = origin,
            ),
            recipeTitle = title,
            isEmpty = title == null,
            statusLabels = if (title == null) listOf("Empty") else emptyList(),
        )

    @Test
    fun planScreenRendersStatesAndGenerateAction() {
        val slots = listOf(
            slot(DayOfWeek.MONDAY, MealSlotType.BREAKFAST, "r1", "Oats", SlotOrigin.PLAN),
            slot(DayOfWeek.MONDAY, MealSlotType.DINNER, null, null, SlotOrigin.PLAN),
        )
        var generated = false
        composeRule.setContent {
            PlanScreen(
                state = PlanScreenState(
                    weekSlots = slots,
                    lockStateLabel = "Unlocked",
                    nutritionCueLabel = "Balanced",
                    seasonContextLabel = "Spring",
                    statusMessage = "Ready",
                ),
                onViewRecipe = {},
                onReplace = {},
                onChooseRecipe = {},
                onLockRecipes = {},
                onNavigateGrocery = {},
                onRetry = {},
                onIntent = { if (it == PlanIntent.GenerateWeekPlan) generated = true },
            )
        }
        composeRule.onNodeWithTag("plan-screen").assertIsDisplayed()
        composeRule.onNodeWithText("generate week plan").assertIsDisplayed().performClick()
        assert(generated)
    }

    @Test
    fun planScreenRendersLoadingFallbackAndError() {
        composeRule.setContent {
            PlanScreen(
                state = PlanScreenState(
                    loading = false,
                    errorMessage = "err",
                    fallbackMessage = "fallback",
                    r05State = R05State(
                        visible = true,
                        message = "No local data",
                        nextActions = listOf(R05NextAction.RETRY),
                    ),
                ),
                onViewRecipe = {}, onReplace = {}, onChooseRecipe = {}, onLockRecipes = {}, onNavigateGrocery = {}, onRetry = {},
            )
        }
        composeRule.onNodeWithTag("plan-screen").assertIsDisplayed()
    }

    @Test
    fun todayScreenRendersSlots() {
        composeRule.setContent {
            TodayScreen(
                state = TodayScreenState(
                    dateLabel = "Mon",
                    todaySlots = listOf(slot(DayOfWeek.MONDAY, MealSlotType.BREAKFAST, "r1", "Eggs", SlotOrigin.TODAY)),
                    nutritionCueLabel = "Cue",
                    seasonContextLabel = "Winter",
                ),
                onViewRecipe = {}, onReplace = {}, onChooseRecipe = {}, onRetry = {},
            )
        }
        composeRule.onNodeWithTag("today-screen").assertIsDisplayed()
    }

    @Test
    fun todayScreenRendersEmpty() {
        composeRule.setContent {
            TodayScreen(
                state = TodayScreenState(dateLabel = "Tue", todaySlots = emptyList()),
                onViewRecipe = {}, onReplace = {}, onChooseRecipe = {}, onRetry = {},
            )
        }
        composeRule.onNodeWithTag("today-screen").assertIsDisplayed()
    }

    @Test
    fun groceryScreenRendersKeyStates() {
        composeRule.setContent {
            GroceryScreen(
                state = GroceryScreenState(
                    recipesLocked = false,
                    lockSummary = "Recipes not locked",
                    canGenerate = false,
                    hasList = false,
                ),
                onIntent = {},
                onNavigatePlan = {},
                onSelectStore = {},
            )
        }
        composeRule.onNodeWithTag("grocery-screen").assertIsDisplayed()
    }

    @Test
    fun groceryScreenRendersListItemsStaleAndExportStates() {
        composeRule.setContent {
            GroceryScreen(
                state = GroceryScreenState(
                    recipesLocked = true,
                    lockSummary = "Locked 5 recipes",
                    canGenerate = true,
                    hasList = true,
                    shoppingListId = "s1",
                    hasUserEdits = true,
                    isStale = true,
                    staleWarningText = "Meal plan modified. Grocery list not up-to-date. Press the Regenerate button.",
                    items = listOf(
                        GroceryItemUiModel(itemId = "i1", displayText = "Milk", quantityText = "1 l", checked = false),
                        GroceryItemUiModel(itemId = "i2", displayText = "Eggs", quantityText = "6", checked = true, availabilityStatus = StorePresenceStatus.PRESENT),
                    ),
                    selectedStore = StoreRef("store1", "Prisma Tripla", StoreChain.PRISMA),
                    presenceStatusLabel = "Checked",
                    exportMessage = "Exported",
                    exportInstructions = "Use share",
                    errorMessage = "oops",
                    showRegenerateConfirmation = true,
                ),
                onIntent = {},
                onNavigatePlan = {},
                onSelectStore = {},
            )
        }
        composeRule.onNodeWithTag("grocery-screen").assertExists()
    }

    @Test
    fun replaceAlternativesScreenRendersStates() {
        val ctx = SlotActionContext(PlanSlotRef("plan-ui", DayOfWeek.TUESDAY, MealSlotType.DINNER), origin = SlotOrigin.PLAN, currentRecipeId = "r1")
        composeRule.setContent {
            ReplaceAlternativesScreen(
                slotContext = ctx,
                onView = { _, _ -> },
                onSelect = { _, _ -> },
                onCancel = {},
                alternatives = listOf(AlternativeRecipeUiModel("a1", "Alt 1", badges = listOf("fast"), explanation = "ok")),
            )
        }
        composeRule.onNodeWithTag("replace-alternatives-screen").assertIsDisplayed()
    }

    @Test
    fun replaceAlternativesScreenRendersLoading() {
        val ctx = SlotActionContext(PlanSlotRef("plan-ui", DayOfWeek.TUESDAY, MealSlotType.DINNER), origin = SlotOrigin.PLAN)
        composeRule.setContent {
            ReplaceAlternativesScreen(slotContext = ctx, onView = { _, _ -> }, onSelect = { _, _ -> }, onCancel = {}, loading = true)
        }
        composeRule.onNodeWithTag("replace-alternatives-screen").assertIsDisplayed()
    }

    @Test
    fun replaceAlternativesScreenRendersEmptyAndError() {
        val ctx = SlotActionContext(PlanSlotRef("plan-ui", DayOfWeek.TUESDAY, MealSlotType.DINNER), origin = SlotOrigin.PLAN)
        composeRule.setContent {
            ReplaceAlternativesScreen(
                slotContext = ctx,
                onView = { _, _ -> },
                onSelect = { _, _ -> },
                onCancel = {},
                alternatives = emptyList(),
                emptyReason = "None",
            )
        }
        composeRule.onNodeWithTag("replace-alternatives-screen").assertIsDisplayed()
    }

    @Test
    fun componentsRender() {
        composeRule.setContent {
            androidx.compose.foundation.layout.Column {
                NutritionBalanceCueCard(label = "Balanced", partial = true)
                NutritionBalanceCueCard(label = "", unavailable = true)
                SeasonContextIndicator("Winter")
                RecipeMetaBadges(listOf("Fast", "Veg"))
                ErrorRetryPanel(message = "Error", onRetry = {})
                R05FallbackPanel(state = R05State(visible = true, message = "No data"), onRetry = {}, onBundledFallback = {}, onManualPlanning = {})
                StorePresenceStatusRow(PresenceRowUiModel(itemId = "i1", queryName = "Milk", status = StorePresenceStatus.UNKNOWN, reasonLabel = "Unknown"))
                GroceryChecklistRow(
                    item = GroceryItemUiModel(itemId = "g1", displayText = "Bread", quantityText = "1", checked = true),
                    onToggle = {},
                    onRemove = {},
                )
            }
        }
        composeRule.onNodeWithText("Error").assertExists()
    }

    @Test
    fun onboardingScreenRenders() {
        composeRule.setContent {
            OnboardingScreen(
                permission = LocationPermissionUiState.NOT_ASKED,
                r05State = R05State(visible = true, message = "x"),
                onAllowLocation = {},
                onChooseManuallyLater = {},
                onContinue = {},
                onRetry = {},
                onUseBundledFallback = {},
                onContinueManualPlanning = {},
            )
        }
        composeRule.onNodeWithTag("onboarding-screen").assertIsDisplayed()
    }

    @Test
    fun userScreenRenders() {
        composeRule.setContent {
            UserScreen(
                state = UserSettingsState(seasonOverrideEnabled = true, seasonSelection = "Summer", areaLabel = "Helsinki", validationMessage = "note"),
                onToggleSeasonOverride = {},
                onSeasonSelected = {},
                onAreaLabelChanged = {},
                onResetSeason = {},
                onManagePermission = {},
            )
        }
        composeRule.onNodeWithTag("user-screen").assertIsDisplayed()
    }

    @Test
    fun selectStoreScreenRenders() {
        composeRule.setContent {
            SelectPrismaStoreScreen(
                state = StoreSelectionUiState(
                    suggestedStore = PrismaStoreRef("s1", "Prisma A"),
                    currentStore = PrismaStoreRef("s2", "Prisma B"),
                    locationPermission = LocationPermissionUiState.DENIED,
                    lookupStatusLabel = "lookup",
                    errorMessage = "err",
                ),
                onUseSuggestion = {},
                onSelectManual = {},
                onConfirm = {},
                onManagePermission = {},
                onBack = {},
            )
        }
        composeRule.onNodeWithTag("select-store-screen").assertIsDisplayed()
    }

    @Test
    fun recipeDetailScreenRenders() {
        val ctx = SlotActionContext(PlanSlotRef("plan-ui", DayOfWeek.MONDAY, MealSlotType.DINNER), origin = SlotOrigin.PLAN)
        composeRule.setContent {
            RecipeDetailScreen(
                recipeId = "r1",
                slotContext = ctx,
                onUseForSlot = { _, _ -> },
                onChooseDifferent = {},
                onBack = {},
                sourceLabel = "curated",
                timeLabel = "20 min",
                weekdayEligibilityLabel = "Eligible",
                ingredients = listOf("a", "b"),
                prepNotes = "note",
                missingDataLabels = listOf("missing"),
                badges = listOf("fast"),
            )
        }
        composeRule.onNodeWithTag("recipe-detail-screen").assertIsDisplayed()
    }

    @Test
    fun statusBannerRenders() {
        composeRule.setContent { StatusBanner("hello") }
        composeRule.onNodeWithText("hello").assertIsDisplayed()
    }
}
