package com.mealplanner.ui

import kotlin.test.Test
import kotlin.test.assertTrue

/** Step 2 runner verification: confirms `:app:testDebugUnitTest --tests "com.mealplanner.ui.*"` discovers UI tests. */
class UiRunnerSmokeTest {
    @Test
    fun uiTestRunnerDiscoversPackage() {
        assertTrue(true)
    }
}
