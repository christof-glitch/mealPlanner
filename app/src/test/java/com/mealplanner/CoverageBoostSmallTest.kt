package com.mealplanner

import com.mealplanner.domain.model.DayOfWeek
import com.mealplanner.domain.model.FallbackReason
import com.mealplanner.domain.model.MealSlotType
import com.mealplanner.domain.model.PlanSlotRef
import com.mealplanner.domain.model.ShoppingListExportDto
import com.mealplanner.platform.SharePlatformResult
import com.mealplanner.platform.withTypedTimeout
import com.mealplanner.ui.MealPlannerUiState
import com.mealplanner.ui.navigation.MealPlannerRoute
import com.mealplanner.ui.navigation.SlotContextNav
import com.mealplanner.ui.platform.DefaultPermissionStatusProvider
import com.mealplanner.ui.platform.ShareCopyPlatformBoundary
import com.mealplanner.ui.state.LocationPermissionUiState
import com.mealplanner.ui.state.SlotActionContext
import com.mealplanner.ui.state.SlotOrigin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CoverageBoostSmallTest {
    @Test
    fun routesEncodeAndBottomNavPresent() {
        val detail = MealPlannerRoute.RecipeDetail("r/1", "ctx").route
        assertTrue(detail.contains("recipe/"))
        val replace = MealPlannerRoute.ReplaceAlternatives("a|b").route
        assertTrue(replace.startsWith("replace/"))
        val store = MealPlannerRoute.SelectPrismaStore("s1").route
        assertTrue(store.contains("currentStoreId"))
        assertEquals(4, com.mealplanner.ui.navigation.BottomNavRoutes.size)
    }

    @Test
    fun slotContextNavEncodeDecodeAndInvalid() {
        val ctx = SlotActionContext(
            slotRef = PlanSlotRef("plan-ui", DayOfWeek.MONDAY, MealSlotType.BREAKFAST),
            currentRecipeId = "r1",
            origin = SlotOrigin.TODAY,
        )
        val encoded = SlotContextNav.encode(ctx)
        val decoded = SlotContextNav.decode(encoded)
        assertNotNull(decoded)
        assertEquals("r1", decoded.currentRecipeId)
        assertEquals(SlotOrigin.TODAY, decoded.origin)
        assertEquals(ctx.slotId, SlotContextNav.stableSlotKey(ctx))
        assertNull(SlotContextNav.decode(null))
        assertNull(SlotContextNav.decode(""))
        assertNull(SlotContextNav.decode("bad"))
    }

    @Test
    fun platformBoundariesAndHelpers() = runBlocking {
        val provider = DefaultPermissionStatusProvider { LocationPermissionUiState.GRANTED }
        assertEquals(LocationPermissionUiState.GRANTED, provider.locationPermission())

        val share = ShareCopyPlatformBoundary()
        val dto = ShoppingListExportDto(
            shoppingListId = "l1",
            snapshotId = "s1",
            items = emptyList(),
            staleWarning = null,
        )
        val fallback = share.exportFallback(dto, "t")
        assertEquals("l1", fallback.payload?.shoppingListId)
        val copy = share.copyTextFallback(dto)
        assertTrue(copy.instructions!!.contains("Copy"))

        val ok = withTypedTimeout(1000, { _: FallbackReason -> "fb" }) { "ok" }
        assertEquals("ok", ok)
        val timedOut = withTypedTimeout(1, { r: FallbackReason -> r.code }) {
            delay(50)
            "late"
        }
        assertEquals("TIMEOUT", timedOut)
        val failed = withTypedTimeout(1000, { r: FallbackReason -> r.code }) {
            error("boom")
        }
        assertEquals("FAILED", failed)

        val res: SharePlatformResult = SharePlatformResult.Failed(FallbackReason("X", "m", "a"))
        assertTrue(res is SharePlatformResult.Failed)
    }

    @Test
    fun uiStateAndAppClass() {
        val state = MealPlannerUiState(statusMessage = "hi")
        assertEquals("hi", state.statusMessage)
        assertNull(state.plan)
        val app = MealPlannerApp()
        assertNotNull(app)
    }
}
