package es.pedrazamiguez.expenseshareapp.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import es.pedrazamiguez.expenseshareapp.core.designsystem.foundation.ExpenseShareAppTheme
import es.pedrazamiguez.expenseshareapp.features.onboarding.presentation.screen.OnboardingScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests for [OnboardingScreen].
 *
 * The onboarding screen is a simple composable with a single button.
 * These tests verify it renders without crashing and the completion
 * button is visible.
 */
@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ═════════════════════════════════════════════════════════════════════
    //  Default rendering
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun rendersOnboardingScreen_withCompleteButton() {
        composeRule.setContent {
            ExpenseShareAppTheme {
                OnboardingScreen()
            }
        }

        composeRule.waitForIdle()

        // The "Let's start!" button should be visible
        composeRule.onNodeWithText("Let's start!").assertIsDisplayed()
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Completion callback
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun completeButton_isClickable() {
        var wasCompleted = false

        composeRule.setContent {
            ExpenseShareAppTheme {
                OnboardingScreen(onOnboardingComplete = { wasCompleted = true })
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Let's start!").assertIsDisplayed()

        // Click the button and verify callback fires
        composeRule.onNodeWithText("Let's start!").performClick()
        composeRule.waitForIdle()

        assert(wasCompleted) { "Expected onOnboardingComplete callback to fire" }
    }
}


