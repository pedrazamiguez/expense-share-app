package es.pedrazamiguez.splittrip.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import es.pedrazamiguez.splittrip.core.designsystem.foundation.SplitTripTheme
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.ArithmeticKeyboardState
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.ArithmeticOperatorBar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [ArithmeticOperatorBar].
 */
@RunWith(AndroidJUnit4::class)
class ArithmeticOperatorBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun hidesOperatorBar_whenKeyboardIsHidden_evenIfStateIsVisible() {
        val state = ArithmeticKeyboardState(isVisible = true)

        composeRule.setContent {
            SplitTripTheme {
                ArithmeticOperatorBar(state = state)
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Plus").assertDoesNotExist()
    }

    @Test
    fun hidesOperatorBar_whenStateIsNotVisible() {
        val state = ArithmeticKeyboardState(isVisible = false)

        composeRule.setContent {
            SplitTripTheme {
                ArithmeticOperatorBar(state = state)
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Plus").assertDoesNotExist()
    }
}
