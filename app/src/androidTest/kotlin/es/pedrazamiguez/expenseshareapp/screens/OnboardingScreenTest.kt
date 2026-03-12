package es.pedrazamiguez.expenseshareapp.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import es.pedrazamiguez.expenseshareapp.core.designsystem.foundation.ExpenseShareAppTheme
import es.pedrazamiguez.expenseshareapp.features.onboarding.R
import es.pedrazamiguez.expenseshareapp.features.onboarding.presentation.screen.OnboardingScreen
import es.pedrazamiguez.expenseshareapp.helpers.ScreenshotRule
import org.junit.Assert.assertTrue
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

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @get:Rule(order = 2)
    val screenshotRule = ScreenshotRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    // ═════════════════════════════════════════════════════════════════════
    //  Default rendering
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun rendersOnboardingScreen_withCompleteButton() {
        val completeButtonText = context.getString(R.string.onboarding_complete_button)

        composeRule.setContent {
            ExpenseShareAppTheme {
                OnboardingScreen()
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText(completeButtonText).assertIsDisplayed()
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Completion callback
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun completeButton_isClickable() {
        val completeButtonText = context.getString(R.string.onboarding_complete_button)
        var wasCompleted = false

        composeRule.setContent {
            ExpenseShareAppTheme {
                OnboardingScreen(onOnboardingComplete = { wasCompleted = true })
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText(completeButtonText).assertIsDisplayed()

        composeRule.onNodeWithText(completeButtonText).performClick()
        composeRule.waitForIdle()

        assertTrue("Expected onOnboardingComplete callback to fire", wasCompleted)
    }
}
