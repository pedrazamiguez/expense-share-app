package es.pedrazamiguez.expenseshareapp.ui.presentation.navigation

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
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.ui.auth.navigation.LOGIN_ROUTE
import es.pedrazamiguez.expenseshareapp.ui.auth.navigation.loginGraph
import es.pedrazamiguez.expenseshareapp.ui.main.navigation.MAIN_ROUTE
import es.pedrazamiguez.expenseshareapp.ui.main.navigation.mainGraph
import es.pedrazamiguez.expenseshareapp.ui.onboarding.navigation.ONBOARDING_ROUTE
import es.pedrazamiguez.expenseshareapp.ui.onboarding.navigation.onboardingGraph
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
    val scope = rememberCoroutineScope()

    val visibleProviders by produceState(
        initialValue = emptyList(),
        navigationProviders
    ) {
        value = filterVisibleProviders(navigationProviders)
    }

    val routeToUiProvider = remember(
        visibleProviders,
        screenUiProviders
    ) {
        val visibleRoutes = visibleProviders.map { it.route }.toSet()
        screenUiProviders.filter { it.route in visibleRoutes }.associateBy { it.route }
    }

    val onboardingCompleted = userPreferences.isOnboardingComplete.collectAsState(initial = null)
    val startDestination = when (onboardingCompleted.value) {
        null -> return
        true -> MAIN_ROUTE
        false -> ONBOARDING_ROUTE
    }

    CompositionLocalProvider(LocalNavController provides navController) {

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {

            loginGraph(
                onLoginSuccess = {
                    navController.navigate(ONBOARDING_ROUTE) {
                        popUpTo(LOGIN_ROUTE) { inclusive = true }
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
                        navController.navigate(MAIN_ROUTE) {
                            popUpTo(ONBOARDING_ROUTE) { inclusive = true }
                        }
                    }
                })

            mainGraph(
                navigationProviders = visibleProviders,
                screenUiProviders = routeToUiProvider.values.toList()
            )

        }

    }

}

private suspend fun filterVisibleProviders(
    providers: List<NavigationProvider>
): List<NavigationProvider> {

    val filtered = mutableListOf<NavigationProvider>()
    for (provider in providers) {
        try {
            if (provider.isVisible()) {
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

