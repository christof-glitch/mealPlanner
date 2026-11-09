package com.mealplanner

import android.app.Application

/**
 * Application entry point (Step 10). DI/runtime wiring is intentionally minimal for MVP brownfield slice.
 * Platform boundaries (permission/share/copy) are confined to ui/platform; domain receives typed outcomes only.
 */
class MealPlannerApp : Application()
