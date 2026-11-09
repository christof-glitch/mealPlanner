package com.mealplanner.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mealplanner.domain.model.PrismaStoreRef
import com.mealplanner.ui.screens.grocery.GroceryScreen
import com.mealplanner.ui.screens.onboarding.OnboardingScreen
import com.mealplanner.ui.screens.plan.PlanScreen
import com.mealplanner.ui.screens.recipe.RecipeDetailScreen
import com.mealplanner.ui.screens.recipe.ReplaceAlternativesScreen
import com.mealplanner.ui.screens.store.SelectPrismaStoreScreen
import com.mealplanner.ui.screens.today.TodayScreen
import com.mealplanner.ui.screens.user.UserScreen
import com.mealplanner.ui.state.GroceryIntent
import com.mealplanner.ui.state.GroceryScreenState
import com.mealplanner.ui.state.PlanScreenState
import com.mealplanner.ui.state.SlotActionContext
import com.mealplanner.ui.state.StoreSelectionUiState
import com.mealplanner.ui.state.TodayScreenState
import com.mealplanner.ui.state.UserSettingsState

/**
 * Step 6 navigation shell (RM-01–RM-09). Bottom nav: Today/Plan/Grocery/User.
 * Pushed: RecipeDetail. Full-screen: ReplaceAlternatives, SelectPrismaStore.
 * R-06: slot context encoded in route args and preserved across back-stack transitions.
 * Regenerate confirmation dialog is wired/rendered by Grocery screen state (Step 9) using shared primitive.
 */
@Composable
fun MealPlannerScaffold(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startOnOnboarding: Boolean = false,
    planState: PlanScreenState = PlanScreenState(),
    todayState: TodayScreenState = TodayScreenState(),
    groceryState: GroceryScreenState = GroceryScreenState(),
    userState: UserSettingsState = UserSettingsState(),
    storeSelectionState: StoreSelectionUiState = StoreSelectionUiState(),
    onPlanIntent: (com.mealplanner.ui.state.PlanIntent) -> Unit = {},
    onPlanViewRecipe: (SlotActionContext) -> Unit = {},
    onPlanReplace: (SlotActionContext) -> Unit = {},
    onPlanChooseRecipe: (SlotActionContext) -> Unit = {},
    onPlanRetry: () -> Unit = {},
    onTodayViewRecipe: (SlotActionContext) -> Unit = {},
    onTodayReplace: (SlotActionContext) -> Unit = {},
    onTodayChooseRecipe: (SlotActionContext) -> Unit = {},
    onTodayRetry: () -> Unit = {},
    onGroceryIntent: (GroceryIntent) -> Unit = {},
    onSelectStore: () -> Unit = { navController.navigate(MealPlannerRoute.SelectPrismaStore().route) },
    onStoreUseSuggestion: () -> Unit = {},
    onStoreSelectManual: (PrismaStoreRef) -> Unit = {},
    onStoreConfirm: () -> Unit = { navController.popBackStack() },
    onStoreManagePermission: () -> Unit = {},
    onUseForSlot: (SlotActionContext, String) -> Unit = { _, _ -> },
    onChooseDifferentForSlot: (SlotActionContext) -> Unit = {},
    onSelectAlternative: (SlotActionContext, String) -> Unit = { _, _ -> },
    onUserToggleSeasonOverride: (Boolean) -> Unit = {},
    onUserSeasonSelected: (String) -> Unit = {},
    onUserAreaLabelChanged: (String) -> Unit = {},
    onUserResetSeason: () -> Unit = {},
    onUserManagePermission: () -> Unit = {},
    onOnboardingContinue: () -> Unit = { navController.navigate(MealPlannerRoute.Today.route) { popUpTo(MealPlannerRoute.Onboarding.route) { inclusive = true } } },
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = BottomNavRoutes.any { route -> currentDestination?.hierarchy?.any { it.route == route.route } == true }

    Scaffold(
        modifier = modifier.testTag("meal-planner-scaffold"),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(modifier = Modifier.testTag("bottom-navigation")) {
                    BottomNavRoutes.forEach { route ->
                        val selected = currentDestination?.hierarchy?.any { it.route == route.route } == true
                        val label = bottomNavLabel(route)
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(route.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(bottomNavIcon(route), contentDescription = label) },
                            label = { Text(label) },
                            modifier = Modifier.testTag("bottom-nav-${route.route}").semantics { contentDescription = "$label destination" },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (startOnOnboarding) MealPlannerRoute.Onboarding.route else MealPlannerRoute.Today.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(MealPlannerRoute.Onboarding.route) {
                OnboardingScreen(
                    permission = com.mealplanner.ui.state.LocationPermissionUiState.NOT_ASKED,
                    r05State = null,
                    onAllowLocation = {},
                    onChooseManuallyLater = onOnboardingContinue,
                    onContinue = onOnboardingContinue,
                    onRetry = {},
                    onUseBundledFallback = {},
                    onContinueManualPlanning = onOnboardingContinue,
                )
            }
            composable(MealPlannerRoute.Today.route) {
                TodayScreen(
                    state = todayState,
                    onViewRecipe = { ctx ->
                        onTodayViewRecipe(ctx)
                        val recipeId = ctx.currentRecipeId ?: return@TodayScreen
                        navController.navigate(MealPlannerRoute.RecipeDetail(recipeId, SlotContextNav.encode(ctx)).route)
                    },
                    onReplace = { ctx ->
                        onTodayReplace(ctx)
                        navController.navigate(MealPlannerRoute.ReplaceAlternatives(SlotContextNav.encode(ctx)).route)
                    },
                    onChooseRecipe = { ctx ->
                        onTodayChooseRecipe(ctx)
                        navController.navigate(MealPlannerRoute.ReplaceAlternatives(SlotContextNav.encode(ctx)).route)
                    },
                    onRetry = onTodayRetry,
                )
            }
            composable(MealPlannerRoute.Plan.route) {
                PlanScreen(
                    state = planState,
                    onViewRecipe = { ctx ->
                        onPlanViewRecipe(ctx)
                        val recipeId = ctx.currentRecipeId ?: return@PlanScreen
                        navController.navigate(MealPlannerRoute.RecipeDetail(recipeId, SlotContextNav.encode(ctx)).route)
                    },
                    onReplace = { ctx ->
                        onPlanReplace(ctx)
                        navController.navigate(MealPlannerRoute.ReplaceAlternatives(SlotContextNav.encode(ctx)).route)
                    },
                    onChooseRecipe = { ctx ->
                        onPlanChooseRecipe(ctx)
                        navController.navigate(MealPlannerRoute.ReplaceAlternatives(SlotContextNav.encode(ctx)).route)
                    },
                    onLockRecipes = { onPlanIntent(com.mealplanner.ui.state.PlanIntent.LockRecipesForShopping) },
                    onNavigateGrocery = { navController.navigate(MealPlannerRoute.Grocery.route) },
                    onRetry = onPlanRetry,
                )
            }
            composable(MealPlannerRoute.Grocery.route) {
                GroceryScreen(
                    state = groceryState,
                    onIntent = onGroceryIntent,
                    onNavigatePlan = { navController.navigate(MealPlannerRoute.Plan.route) },
                    onSelectStore = onSelectStore,
                )
            }
            composable(MealPlannerRoute.User.route) {
                UserScreen(
                    state = userState,
                    onToggleSeasonOverride = onUserToggleSeasonOverride,
                    onSeasonSelected = onUserSeasonSelected,
                    onAreaLabelChanged = onUserAreaLabelChanged,
                    onResetSeason = onUserResetSeason,
                    onManagePermission = onUserManagePermission,
                )
            }
            composable(
                route = MealPlannerRoute.RecipeDetail.PATTERN,
                arguments = listOf(
                    navArgument(MealPlannerRoute.RecipeDetail.ARG_RECIPE_ID) { type = NavType.StringType },
                    navArgument(MealPlannerRoute.RecipeDetail.ARG_SLOT_CONTEXT) { type = NavType.StringType; nullable = true; defaultValue = null },
                ),
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString(MealPlannerRoute.RecipeDetail.ARG_RECIPE_ID).orEmpty()
                val slotContext = SlotContextNav.decode(backStackEntry.arguments?.getString(MealPlannerRoute.RecipeDetail.ARG_SLOT_CONTEXT))
                RecipeDetailScreen(
                    recipeId = recipeId,
                    slotContext = slotContext,
                    onUseForSlot = { ctx, selectedRecipeId ->
                        onUseForSlot(ctx, selectedRecipeId)
                        navController.popBackStack()
                    },
                    onChooseDifferent = { ctx ->
                        onChooseDifferentForSlot(ctx)
                        navController.navigate(MealPlannerRoute.ReplaceAlternatives(SlotContextNav.encode(ctx)).route)
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = MealPlannerRoute.ReplaceAlternatives.PATTERN,
                arguments = listOf(navArgument(MealPlannerRoute.ReplaceAlternatives.ARG_SLOT_CONTEXT) { type = NavType.StringType }),
            ) { backStackEntry ->
                val slotContext = SlotContextNav.decode(backStackEntry.arguments?.getString(MealPlannerRoute.ReplaceAlternatives.ARG_SLOT_CONTEXT))
                    ?: return@composable
                ReplaceAlternativesScreen(
                    slotContext = slotContext,
                    onView = { ctx, recipeId ->
                        navController.navigate(MealPlannerRoute.RecipeDetail(recipeId, SlotContextNav.encode(ctx)).route)
                    },
                    onSelect = { ctx, recipeId ->
                        onSelectAlternative(ctx, recipeId)
                        // R-06: return to origin (Today/Plan) with shared state updated; focus handled by screen/state in Step 7/8.
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() },
                )
            }
            composable(
                route = MealPlannerRoute.SelectPrismaStore.PATTERN,
                arguments = listOf(
                    navArgument(MealPlannerRoute.SelectPrismaStore.ARG_CURRENT_STORE_ID) { type = NavType.StringType; nullable = true; defaultValue = null },
                ),
            ) {
                SelectPrismaStoreScreen(
                    state = storeSelectionState,
                    onUseSuggestion = onStoreUseSuggestion,
                    onSelectManual = onStoreSelectManual,
                    onConfirm = onStoreConfirm,
                    onManagePermission = onStoreManagePermission,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

private fun bottomNavLabel(route: MealPlannerRoute): String = when (route) {
    MealPlannerRoute.Today -> "Today"
    MealPlannerRoute.Plan -> "Plan"
    MealPlannerRoute.Grocery -> "Grocery"
    MealPlannerRoute.User -> "User"
    else -> route.route
}

private fun bottomNavIcon(route: MealPlannerRoute) = when (route) {
    MealPlannerRoute.Today -> Icons.Filled.Home
    MealPlannerRoute.Plan -> Icons.AutoMirrored.Filled.List
    MealPlannerRoute.Grocery -> Icons.Filled.ShoppingCart
    MealPlannerRoute.User -> Icons.Filled.Person
    else -> Icons.Filled.Home
}
