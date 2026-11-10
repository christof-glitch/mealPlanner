package com.mealplanner.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MealPlannerRootRenderTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun mealPlannerRootRendersScaffold() {
        composeRule.setContent {
            MealPlannerRoot()
        }
        // Today is start destination in scaffold
        composeRule.onNodeWithTag("today-screen").assertIsDisplayed()
    }
}
