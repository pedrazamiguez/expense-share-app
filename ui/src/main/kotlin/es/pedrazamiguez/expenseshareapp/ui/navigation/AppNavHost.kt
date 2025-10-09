package es.pedrazamiguez.expenseshareapp.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import es.pedrazamiguez.expenseshareapp.core.config.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalRootNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.ui.authentication.navigation.loginGraph
import es.pedrazamiguez.expenseshareapp.ui.main.navigation.mainGraph
import es.pedrazamiguez.expenseshareapp.ui.onboarding.navigation.onboardingGraph
import es.pedrazamiguez.expenseshareapp.ui.settings.navigation.settingsGraph
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import timber.log.Timber

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {

    val koin = remember { GlobalContext.get() }
    val navigationProviders = remember { koin.getAll<NavigationProvider>() }
    val screenUiProviders = remember { koin.getAll<ScreenUiProvider>() }
    val userPreferences = remember { koin.get<UserPreferences>() }
    val authenticationService = remember { koin.get<AuthenticationService>() }
    val sharedViewModel = remember { koin.get<SharedViewModel>() }
    val scope = rememberCoroutineScope()

    val selectedGroupId by sharedViewModel.selectedGroupId.collectAsState()

    // Keep all providers to maintain stable navigation structure, but filter UI providers
    val allProviders = navigationProviders
    val visibleProviders = remember(navigationProviders, selectedGroupId) {
        filterVisibleProviders(navigationProviders, selectedGroupId)
    }
    val visibleRoutes = remember(visibleProviders) {
        visibleProviders.map { it.route }.toSet()
    }

    val routeToUiProvider = remember(
        visibleProviders,
        screenUiProviders
    ) {
        // Include ALL screen UI providers, not just visible navigation tabs
        // This ensures screens like CreateGroup show their topBars
        screenUiProviders.associateBy { it.route }
    }

    val isUserLoggedIn by authenticationService.authState.collectAsState(initial = null)
    val onboardingCompleted by userPreferences.isOnboardingComplete.collectAsState(initial = null)

    val startDestination: String? = when {
        isUserLoggedIn == null || onboardingCompleted == null -> null
        isUserLoggedIn == false -> Routes.LOGIN
        onboardingCompleted == false -> Routes.ONBOARDING
        else -> Routes.MAIN
    }

    CompositionLocalProvider(LocalRootNavController provides navController) {

        if (startDestination == null) {

            // FIXME: Show a proper splash screen
            CircularProgressIndicator()

        } else {

            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = modifier,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }) {

                loginGraph(
                    onLoginSuccess = {
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    })

                onboardingGraph(
                    onOnboardingComplete = {
                        scope.launch {
                            try {
                                userPreferences.setOnboardingComplete()
                            } catch (t: Throwable) {
                                Timber.e(
                                    t,
                                    "Error setting onboarding complete"
                                )
                            }
                            navController.navigate(Routes.MAIN) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        }
                    })

                mainGraph(
                    navigationProviders = allProviders,
                    screenUiProviders = routeToUiProvider.values.toList(),
                    visibleProviders = visibleProviders
                )

                settingsGraph()

            }

        }

    }

}

private fun filterVisibleProviders(
    providers: List<NavigationProvider>,
    selectedGroupId: String?
): List<NavigationProvider> {

    val filtered = mutableListOf<NavigationProvider>()
    for (provider in providers) {
        try {
            if (!provider.requiresSelectedGroup || selectedGroupId != null) {
                filtered.add(provider)
            }
        } catch (t: Throwable) {
            Timber.e(
                t,
                "Error checking visibility for provider ${provider.route}"
            )
        }
    }

    return filtered.sortedBy { it.order }
}
