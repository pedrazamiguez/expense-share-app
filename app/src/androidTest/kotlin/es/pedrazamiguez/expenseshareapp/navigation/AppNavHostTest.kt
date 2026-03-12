package es.pedrazamiguez.expenseshareapp.navigation

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import es.pedrazamiguez.expenseshareapp.core.designsystem.foundation.ExpenseShareAppTheme
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.expenseshareapp.di.createAppNavHostTestModule
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetSelectedGroupIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetSelectedGroupNameUseCase
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.viewmodel.AuthenticationViewModel
import es.pedrazamiguez.expenseshareapp.features.main.presentation.viewmodel.MainViewModel
import es.pedrazamiguez.expenseshareapp.features.navigation.AppNavHost
import es.pedrazamiguez.expenseshareapp.helpers.FakeNavigationProvider
import es.pedrazamiguez.expenseshareapp.helpers.ScreenshotRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Instrumentation tests for [AppNavHost] navigation flow.
 *
 * These tests verify that the correct start destination is resolved based on
 * authentication and onboarding state, using a lightweight Koin context with
 * mock dependencies injected via [KoinApplication].
 */
@RunWith(AndroidJUnit4::class)
class AppNavHostTest {

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @get:Rule(order = 2)
    val screenshotRule = ScreenshotRule()

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Creates a Koin module that provides mock ViewModels and NavigationProviders
     * needed by the sub-graphs rendered inside the NavHost.
     */
    private fun createFeatureTestModule() = module {

        // ── ViewModels required by feature screens ────────────────────
        // Login screen needs AuthenticationViewModel
        viewModel {
            AuthenticationViewModel(
                signInWithEmailUseCase = mockk(relaxed = true),
                signInWithGoogleUseCase = mockk(relaxed = true)
            )
        }

        // Main screen needs MainViewModel
        viewModel {
            MainViewModel(
                registerDeviceTokenUseCase = mockk<RegisterDeviceTokenUseCase>(relaxed = true)
            )
        }

        // Main screen needs SharedViewModel (activity-scoped)
        viewModel {
            val getSelectedGroupIdUseCase = mockk<GetSelectedGroupIdUseCase>().apply {
                every { this@apply.invoke() } returns flowOf(null)
            }
            val getSelectedGroupNameUseCase = mockk<GetSelectedGroupNameUseCase>().apply {
                every { this@apply.invoke() } returns flowOf(null)
            }
            SharedViewModel(
                getSelectedGroupIdUseCase = getSelectedGroupIdUseCase,
                getSelectedGroupNameUseCase = getSelectedGroupNameUseCase,
                setSelectedGroupUseCase = mockk(relaxed = true)
            )
        }

        // ── Navigation/Screen providers ───────────────────────────────
        // MainScreen requires at least one NavigationProvider to avoid
        // NoSuchElementException on visibleProviders.first().
        factory {
            FakeNavigationProvider(
                route = Routes.GROUPS,
                order = 10,
                requiresSelectedGroup = false,
                label = "Groups"
            )
        } bind NavigationProvider::class

        factory {
            FakeNavigationProvider(
                route = Routes.PROFILE,
                order = 90,
                requiresSelectedGroup = false,
                label = "Profile"
            )
        } bind NavigationProvider::class
    }

    /**
     * Builds a [TestNavHostController] with a [ComposeNavigator].
     */
    private fun buildTestNavController(): TestNavHostController {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return TestNavHostController(context).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Loading state
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun showsLoadingIndicator_whenAuthAndOnboardingStatesAreUnknown() {
        // Both authState and onboarding never emit → startDestination stays null
        val authState = MutableSharedFlow<Boolean>() // never emits
        val onboarding = MutableSharedFlow<Boolean>() // never emits

        composeRule.setContent {
            KoinApplication(application = {
                modules(
                    createAppNavHostTestModule(
                        authStateFlow = authState,
                        onboardingFlow = onboarding,
                    ),
                    createFeatureTestModule()
                )
            }) {
                ExpenseShareAppTheme {
                    AppNavHost()
                }
            }
        }

        // CircularProgressIndicator is shown while undetermined
        composeRule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)
        ).assertIsDisplayed()
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Not logged in → LOGIN
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun navigatesToLogin_whenUserIsNotLoggedIn() {
        val navController = buildTestNavController()

        composeRule.setContent {
            KoinApplication(application = {
                modules(
                    createAppNavHostTestModule(
                        authStateFlow = flowOf(false),
                        onboardingFlow = flowOf(true),
                    ),
                    createFeatureTestModule()
                )
            }) {
                ExpenseShareAppTheme {
                    AppNavHost(navController = navController)
                }
            }
        }

        composeRule.waitForIdle()

        assertEquals(
            Routes.LOGIN,
            navController.currentDestination?.route
        )
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Logged in, onboarding NOT complete → ONBOARDING
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun navigatesToOnboarding_whenLoggedInButOnboardingNotComplete() {
        val navController = buildTestNavController()

        composeRule.setContent {
            KoinApplication(application = {
                modules(
                    createAppNavHostTestModule(
                        authStateFlow = flowOf(true),
                        onboardingFlow = flowOf(false),
                    ),
                    createFeatureTestModule()
                )
            }) {
                ExpenseShareAppTheme {
                    AppNavHost(navController = navController)
                }
            }
        }

        composeRule.waitForIdle()

        assertEquals(
            Routes.ONBOARDING,
            navController.currentDestination?.route
        )
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Logged in + onboarding done → MAIN
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun navigatesToMain_whenLoggedInAndOnboardingComplete() {
        val navController = buildTestNavController()

        composeRule.setContent {
            KoinApplication(application = {
                modules(
                    createAppNavHostTestModule(
                        authStateFlow = flowOf(true),
                        onboardingFlow = flowOf(true),
                    ),
                    createFeatureTestModule()
                )
            }) {
                ExpenseShareAppTheme {
                    AppNavHost(navController = navController)
                }
            }
        }

        composeRule.waitForIdle()

        assertEquals(
            Routes.MAIN,
            navController.currentDestination?.route
        )
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Start destination latching — graph NOT recreated on state change
    // ═════════════════════════════════════════════════════════════════════

    @Test
    fun startDestinationIsLatched_doesNotChangeAfterInitialResolution() {
        val navController = buildTestNavController()

        // Start as not logged in → LOGIN
        val authState = MutableStateFlow(false)

        composeRule.setContent {
            KoinApplication(application = {
                modules(
                    createAppNavHostTestModule(
                        authStateFlow = authState,
                        onboardingFlow = flowOf(true),
                    ),
                    createFeatureTestModule()
                )
            }) {
                ExpenseShareAppTheme {
                    AppNavHost(navController = navController)
                }
            }
        }

        composeRule.waitForIdle()
        assertEquals(Routes.LOGIN, navController.currentDestination?.route)

        // Now simulate login success by changing auth state.
        // The start destination should NOT change (latched).
        // Navigation should only happen via imperative navigate() calls.
        authState.value = true
        composeRule.waitForIdle()

        // The graph's startDestination is still LOGIN (latched).
        // navController may or may not have navigated (depends on onLoginSuccess callback),
        // but the graph was NOT recreated. We verify the start destination of the graph itself.
        val graphStartRoute = navController.graph.startDestinationRoute
        assertEquals(Routes.LOGIN, graphStartRoute)
    }
}
