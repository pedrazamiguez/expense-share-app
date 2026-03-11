package es.pedrazamiguez.expenseshareapp.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import es.pedrazamiguez.expenseshareapp.core.designsystem.foundation.ExpenseShareAppTheme
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.model.AuthenticationUiState
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.screen.LoginScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests for [LoginScreen].
 *
 * Verifies the stateless Screen composable renders correctly with
 * different [AuthenticationUiState] configurations. No ViewModel or
 * Koin dependencies needed — pure data in, UI out.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ═════════════════════════════════════════════════════════════════════
    //  Default idle state
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun rendersLoginForm_inDefaultState() {
        composeRule.setContent {
            ExpenseShareAppTheme {
                LoginScreen(uiState = AuthenticationUiState())
            }
        }

        composeRule.waitForIdle()

        // Email and password fields should be present
        composeRule.onNodeWithText("Email").assertIsDisplayed()
        composeRule.onNodeWithText("Password").assertIsDisplayed()

        // Login button should be enabled
        composeRule.onNodeWithText("Let's go!").assertIsDisplayed().assertIsEnabled()

        // Google sign-in should be shown by default
        composeRule.onNodeWithText("Continue with Google").assertIsDisplayed()
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Loading state disables controls
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun disablesControls_whenLoading() {
        composeRule.setContent {
            ExpenseShareAppTheme {
                LoginScreen(
                    uiState = AuthenticationUiState(isLoading = true)
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Email").assertIsNotEnabled()
        composeRule.onNodeWithText("Password").assertIsNotEnabled()
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Error state shows error message
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun showsErrorMessage_whenErrorIsPresent() {
        val errorMessage = "Hmm, that doesn't look right. Try again?"

        composeRule.setContent {
            ExpenseShareAppTheme {
                LoginScreen(
                    uiState = AuthenticationUiState(
                        error = es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText.DynamicString(
                            errorMessage
                        )
                    )
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Google sign-in hidden when unavailable
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun hidesGoogleSignIn_whenNotAvailable() {
        composeRule.setContent {
            ExpenseShareAppTheme {
                LoginScreen(
                    uiState = AuthenticationUiState(),
                    isGoogleSignInAvailable = false
                )
            }
        }

        composeRule.waitForIdle()

        // Google button should not exist
        composeRule.onNodeWithText("Continue with Google").assertDoesNotExist()

        // "or" divider should not exist
        composeRule.onNodeWithText("or").assertDoesNotExist()
    }
}

