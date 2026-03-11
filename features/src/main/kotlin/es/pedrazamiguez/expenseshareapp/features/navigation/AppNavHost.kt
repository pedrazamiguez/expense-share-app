package es.pedrazamiguez.expenseshareapp.features.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalRootNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationUtils
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.IsOnboardingCompleteUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetOnboardingCompleteUseCase
import es.pedrazamiguez.expenseshareapp.features.authentication.navigation.loginGraph
import es.pedrazamiguez.expenseshareapp.features.main.navigation.mainGraph
import es.pedrazamiguez.expenseshareapp.features.onboarding.navigation.onboardingGraph
import es.pedrazamiguez.expenseshareapp.features.settings.navigation.settingsGraph
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import timber.log.Timber

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()
) {

    val koin = remember { GlobalContext.get() }
    val navigationProviders = remember { koin.getAll<NavigationProvider>() }
    val screenUiProviders = remember { koin.getAll<ScreenUiProvider>() }
    val isOnboardingCompleteUseCase = remember { koin.get<IsOnboardingCompleteUseCase>() }
    val setOnboardingCompleteUseCase = remember { koin.get<SetOnboardingCompleteUseCase>() }
    val authenticationService = remember { koin.get<AuthenticationService>() }
    val scope = rememberCoroutineScope()

    val routeToUiProvider = remember(screenUiProviders) {
        screenUiProviders.associateBy { it.route }
    }

    val isUserLoggedIn by authenticationService.authState.collectAsStateWithLifecycle(initialValue = null)
    val onboardingCompleted by isOnboardingCompleteUseCase().collectAsStateWithLifecycle(
        initialValue = null
    )

    // Determine the start destination reactively
    val startDestination = NavigationUtils.resolveStartDestination(
        isUserLoggedIn = isUserLoggedIn,
        onboardingCompleted = onboardingCompleted,
    )

    // Latch the first resolved startDestination so the NavHost graph is never
    // recreated when auth/onboarding state changes AFTER initial graph creation.
    // All subsequent transitions (sign-in, sign-out) are handled imperatively
    // via navController.navigate() in onLoginSuccess / onOnboardingComplete / sign-out.
    // Using `remember` (not rememberSaveable) ensures a fresh value on Activity
    // recreation after process death, while surviving normal recompositions.
    val stableStartDestination = remember { mutableStateOf<String?>(null) }
    if (stableStartDestination.value == null && startDestination != null) {
        stableStartDestination.value = startDestination
    }

    // Wrap changing values used inside the NavHost builder in rememberUpdatedState
    // so the builder lambda captures stable State references (same instance across
    // recompositions) instead of raw changing values. This prevents the Compose
    // compiler from recreating the lambda, which in turn prevents NavHost's
    // remember(startDestination, builder) from invalidating and recreating the graph.
    val currentOnboardingCompleted = rememberUpdatedState(onboardingCompleted)

    CompositionLocalProvider(LocalRootNavController provides navController) {

        if (stableStartDestination.value == null) {

            // FIXME: Show a proper splash screen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

        } else {

            NavHost(
                navController = navController,
                startDestination = stableStartDestination.value!!,
                modifier = modifier,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }) {

                loginGraph(
                    onLoginSuccess = {
                        val destination =
                            NavigationUtils.resolvePostLoginDestination(currentOnboardingCompleted.value)
                        navController.navigate(destination) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    })

                onboardingGraph(
                    onOnboardingComplete = {
                        scope.launch {
                            try {
                                setOnboardingCompleteUseCase()
                            } catch (t: Throwable) {
                                Timber.e(
                                    t, "Error setting onboarding complete"
                                )
                            }
                            navController.navigate(Routes.MAIN) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        }
                    })

                mainGraph(
                    navigationProviders = navigationProviders,
                    screenUiProviders = routeToUiProvider.values.toList()
                )

                settingsGraph()

            }

        }

    }

}
