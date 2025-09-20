package es.pedrazamiguez.expenseshareapp.ui.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.ui.auth.navigation.LOGIN_ROUTE
import es.pedrazamiguez.expenseshareapp.ui.auth.navigation.loginGraph
import es.pedrazamiguez.expenseshareapp.ui.main.navigation.MAIN_ROUTE
import es.pedrazamiguez.expenseshareapp.ui.main.navigation.mainGraph
import es.pedrazamiguez.expenseshareapp.ui.onboarding.navigation.ONBOARDING_ROUTE
import es.pedrazamiguez.expenseshareapp.ui.onboarding.navigation.onboardingGraph
import org.koin.core.context.GlobalContext

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()
) {

    val koin = remember { GlobalContext.get() }
    val navigationProviders = remember { koin.getAll<NavigationProvider>() }

    NavHost(
        navController = navController, startDestination = ONBOARDING_ROUTE, modifier = modifier
    ) {

        loginGraph(
            onLoginSuccess = {
                navController.navigate(ONBOARDING_ROUTE) {
                    popUpTo(LOGIN_ROUTE) { inclusive = true }
                }
            })

        onboardingGraph(
            onOnboardingComplete = {
                navController.navigate(MAIN_ROUTE) {
                    popUpTo(ONBOARDING_ROUTE) { inclusive = true }
                }
            })

        mainGraph(navigationProviders = navigationProviders)

    }
}
