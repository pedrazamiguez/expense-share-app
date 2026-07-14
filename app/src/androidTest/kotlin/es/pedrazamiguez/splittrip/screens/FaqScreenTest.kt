package es.pedrazamiguez.splittrip.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import es.pedrazamiguez.splittrip.core.designsystem.foundation.SplitTripTheme
import es.pedrazamiguez.splittrip.features.settings.R
import es.pedrazamiguez.splittrip.features.settings.presentation.screen.FaqScreen
import es.pedrazamiguez.splittrip.helpers.ScreenshotRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests for [FaqScreen].
 */
@RunWith(AndroidJUnit4::class)
class FaqScreenTest {

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @get:Rule(order = 2)
    val screenshotRule = ScreenshotRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun rendersFaqScreen_showsQuestions() {
        val question1Text = context.getString(R.string.faq_question_1)
        val question2Text = context.getString(R.string.faq_question_2)

        composeRule.setContent {
            SplitTripTheme {
                FaqScreen()
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText(question1Text).assertIsDisplayed()
        composeRule.onNodeWithText(question2Text).assertIsDisplayed()
    }

    @Test
    fun clickingQuestion_expandsAnswer() {
        val questionText = context.getString(R.string.faq_question_1)
        val answerText = context.getString(R.string.faq_answer_1)

        composeRule.setContent {
            SplitTripTheme {
                FaqScreen()
            }
        }

        composeRule.waitForIdle()

        // Answer is collapsed initially
        composeRule.onNodeWithText(answerText).assertDoesNotExist()

        // Expand it
        composeRule.onNodeWithText(questionText).performClick()
        composeRule.waitForIdle()

        // Answer should be visible
        composeRule.onNodeWithText(answerText).assertIsDisplayed()
    }
}
