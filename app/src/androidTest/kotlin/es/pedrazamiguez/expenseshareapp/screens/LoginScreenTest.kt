package es.pedrazamiguez.expenseshareapp.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.core.designsystem.foundation.ExpenseShareAppTheme
import es.pedrazamiguez.expenseshareapp.features.authentication.R
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.model.AuthenticationUiState
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.screen.LoginScreen
import es.pedrazamiguez.expenseshareapp.helpers.ScreenshotRule
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

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @get:Rule(order = 2)
    val screenshotRule = ScreenshotRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    // ═════════════════════════════════════════════════════════════════════
    //  Default idle state
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun rendersLoginForm_inDefaultState() {
        val emailLabel = context.getString(R.string.login_email_label)
        val passwordLabel = context.getString(R.string.login_password_label)
        val loginButton = context.getString(R.string.login_button)
        val googleButton = context.getString(R.string.login_google_button)

        composeRule.setContent {
            ExpenseShareAppTheme {
                LoginScreen(uiState = AuthenticationUiState())
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText(emailLabel).assertIsDisplayed()
        composeRule.onNodeWithText(passwordLabel).assertIsDisplayed()
        composeRule.onNodeWithText(loginButton).assertIsDisplayed().assertIsEnabled()
        composeRule.onNodeWithText(googleButton).assertIsDisplayed()
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Loading state disables controls
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun disablesControls_whenLoading() {
        val emailLabel = context.getString(R.string.login_email_label)
        val passwordLabel = context.getString(R.string.login_password_label)

        composeRule.setContent {
            ExpenseShareAppTheme {
                LoginScreen(
                    uiState = AuthenticationUiState(isLoading = true)
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText(emailLabel).assertIsNotEnabled()
        composeRule.onNodeWithText(passwordLabel).assertIsNotEnabled()
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Error state shows error message
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun showsErrorMessage_whenErrorIsPresent() {
        val errorMessage = "Test error message"

        composeRule.setContent {
            ExpenseShareAppTheme {
                LoginScreen(
                    uiState = AuthenticationUiState(
                        error = UiText.DynamicString(errorMessage)
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
        val googleButton = context.getString(R.string.login_google_button)
        val orDivider = context.getString(R.string.login_or_divider)

        composeRule.setContent {
            ExpenseShareAppTheme {
                LoginScreen(
                    uiState = AuthenticationUiState(),
                    isGoogleSignInAvailable = false
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText(googleButton).assertDoesNotExist()
        composeRule.onNodeWithText(orDivider).assertDoesNotExist()
    }
}
