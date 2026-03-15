package es.pedrazamiguez.expenseshareapp.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import es.pedrazamiguez.expenseshareapp.core.designsystem.foundation.ExpenseShareAppTheme
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.SyncPendingTokenUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetSelectedGroupIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetSelectedGroupNameUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetSelectedGroupUseCase
import es.pedrazamiguez.expenseshareapp.features.main.presentation.screen.MainScreen
import es.pedrazamiguez.expenseshareapp.features.main.presentation.viewmodel.MainViewModel
import es.pedrazamiguez.expenseshareapp.helpers.FakeNavigationProvider
import es.pedrazamiguez.expenseshareapp.helpers.ScreenshotRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for [MainScreen] tab visibility and interaction.
 *
 * These tests use fake [NavigationProvider] implementations that render
 * trivial content, avoiding real feature ViewModels and Koin dependencies.
 */
@RunWith(AndroidJUnit4::class)
class MainScreenTest {

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @get:Rule(order = 2)
    val screenshotRule = ScreenshotRule()

    // ── Provider instances ────────────────────────────────────────────

    private val groupsProvider = FakeNavigationProvider(
        route = "groups",
        order = 10,
        requiresSelectedGroup = false,
        label = "Groups"
    )

    private val balancesProvider = FakeNavigationProvider(
        route = "balances",
        order = 20,
        requiresSelectedGroup = true,
        label = "Balances"
    )

    private val expensesProvider = FakeNavigationProvider(
        route = "expenses",
        order = 50,
        requiresSelectedGroup = true,
        label = "Expenses"
    )

    private val profileProvider = FakeNavigationProvider(
        route = "profile",
        order = 90,
        requiresSelectedGroup = false,
        label = "Profile"
    )

    private val allProviders = listOf(
        groupsProvider,
        balancesProvider,
        expensesProvider,
        profileProvider
    )

    // ── ViewModel helpers ────────────────────────────────────────────

    private fun createMainViewModel(): MainViewModel = MainViewModel(
        registerDeviceTokenUseCase = mockk(relaxed = true),
        syncPendingTokenUseCase = mockk<SyncPendingTokenUseCase>(relaxed = true)
    )

    private fun createSharedViewModel(selectedGroupId: String? = null): SharedViewModel {
        val getGroupId = mockk<GetSelectedGroupIdUseCase>().apply {
            every { this@apply.invoke() } returns flowOf(selectedGroupId)
        }
        val getGroupName = mockk<GetSelectedGroupNameUseCase>().apply {
            every { this@apply.invoke() } returns flowOf(
                if (selectedGroupId != null) "Test Group" else null
            )
        }
        val setGroup = mockk<SetSelectedGroupUseCase>(relaxed = true)

        return SharedViewModel(
            getSelectedGroupIdUseCase = getGroupId,
            getSelectedGroupNameUseCase = getGroupName,
            setSelectedGroupUseCase = setGroup
        )
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Tab visibility: No group selected
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun showsOnlyNonGroupDependentTabs_whenNoGroupIsSelected() {
        composeRule.setContent {
            ExpenseShareAppTheme {
                MainScreen(
                    navigationProviders = allProviders,
                    screenUiProviders = emptyList(),
                    mainViewModel = createMainViewModel(),
                    sharedViewModel = createSharedViewModel(selectedGroupId = null)
                )
            }
        }

        composeRule.waitForIdle()

        // Non-group tabs should be visible
        composeRule.onNodeWithText("Groups").assertIsDisplayed()
        composeRule.onNodeWithText("Profile").assertIsDisplayed()

        // Group-dependent tabs should NOT be displayed
        composeRule.onNodeWithText("Balances").assertDoesNotExist()
        composeRule.onNodeWithText("Expenses").assertDoesNotExist()
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Tab visibility: Group selected
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun showsAllTabs_whenGroupIsSelected() {
        composeRule.setContent {
            ExpenseShareAppTheme {
                MainScreen(
                    navigationProviders = allProviders,
                    screenUiProviders = emptyList(),
                    mainViewModel = createMainViewModel(),
                    sharedViewModel = createSharedViewModel(selectedGroupId = "group-123")
                )
            }
        }

        composeRule.waitForIdle()

        // All tabs should be visible
        composeRule.onNodeWithText("Groups").assertIsDisplayed()
        composeRule.onNodeWithText("Balances").assertIsDisplayed()
        composeRule.onNodeWithText("Expenses").assertIsDisplayed()
        composeRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Tab selection
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun tappingTab_changesSelectedContent() {
        composeRule.setContent {
            ExpenseShareAppTheme {
                MainScreen(
                    navigationProviders = allProviders,
                    screenUiProviders = emptyList(),
                    mainViewModel = createMainViewModel(),
                    sharedViewModel = createSharedViewModel(selectedGroupId = "group-123")
                )
            }
        }

        composeRule.waitForIdle()

        // Initially the first visible tab is selected (Groups)
        composeRule.onNodeWithText("Content: Groups").assertIsDisplayed()

        // Tap Profile tab
        composeRule.onNodeWithText("Profile").performClick()
        composeRule.waitForIdle()

        // Profile content should now be visible
        composeRule.onNodeWithText("Content: Profile").assertIsDisplayed()
    }
}
