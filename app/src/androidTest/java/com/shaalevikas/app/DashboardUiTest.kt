package com.shaalevikas.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.shaalevikas.app.ui.ShaaleVikasApp
import org.junit.Rule
import org.junit.Test

class DashboardUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRoleSelectionAndNavigation() {
        // Start the app
        composeTestRule.setContent {
            ShaaleVikasApp()
        }

        // Check if role cards exist
        composeTestRule.onNodeWithText("I am an Alumni").assertExists()
        composeTestRule.onNodeWithText("I am a Headmaster").assertExists()

        // Select Headmaster role
        composeTestRule.onNodeWithText("I am a Headmaster").performClick()
        
        // Click Get Started
        composeTestRule.onNodeWithText("Get Started").performClick()

        // Verify we are on Login screen
        composeTestRule.onNodeWithText("Welcome back!").assertExists()
    }
}
