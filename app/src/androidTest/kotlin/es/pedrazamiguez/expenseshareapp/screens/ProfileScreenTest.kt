package es.pedrazamiguez.expenseshareapp.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import es.pedrazamiguez.expenseshareapp.core.designsystem.foundation.ExpenseShareAppTheme
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.model.ProfileUiModel
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.screen.ProfileScreen
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.viewmodel.state.ProfileUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests for [ProfileScreen].
 *
 * Verifies the stateless Screen composable renders correctly with
 * different [ProfileUiState] configurations — loading, loaded, and
 * error states. No ViewModel or Koin dependencies needed.
 */
@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ═════════════════════════════════════════════════════════════════════
    //  Profile loaded — displays user info
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun rendersProfileInfo_whenProfileIsLoaded() {
        composeRule.setContent {
            ExpenseShareAppTheme {
                ProfileScreen(
                    uiState = ProfileUiState(
                        isLoading = false,
                        profile = ProfileUiModel(
                            displayName = "Jane Doe",
                            email = "jane@example.com",
                            profileImageUrl = null,
                            memberSinceText = "March 2025"
                        )
                    )
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Jane Doe").assertIsDisplayed()
        composeRule.onNodeWithText("jane@example.com").assertIsDisplayed()
        composeRule.onNodeWithText("March 2025", substring = true).assertIsDisplayed()
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Loading state — does not crash
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun rendersWithoutCrash_whenLoading() {
        composeRule.setContent {
            ExpenseShareAppTheme {
                ProfileScreen(
                    uiState = ProfileUiState(isLoading = true)
                )
            }
        }

        composeRule.waitForIdle()

        // No crash = success. Profile info should not be visible.
        composeRule.onNodeWithText("Jane Doe").assertDoesNotExist()
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Error state — shows error and retry button
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun showsError_whenErrorIsPresent() {
        val errorText = "Something went wrong"

        composeRule.setContent {
            ExpenseShareAppTheme {
                ProfileScreen(
                    uiState = ProfileUiState(
                        isLoading = false,
                        errorMessage = es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText.DynamicString(
                            errorText
                        )
                    )
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText(errorText).assertIsDisplayed()
    }
}

