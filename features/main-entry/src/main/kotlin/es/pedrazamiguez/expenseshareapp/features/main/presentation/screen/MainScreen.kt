package es.pedrazamiguez.expenseshareapp.features.main.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar.ProvideTopAppBarState
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar.rememberTopAppBarState
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalSharedTransitionScope
import es.pedrazamiguez.expenseshareapp.features.main.presentation.component.BottomNavigationBar
import es.pedrazamiguez.expenseshareapp.features.main.presentation.viewmodel.MainViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen(
    navigationProviders: List<NavigationProvider>,
    screenUiProviders: List<ScreenUiProvider>,
    visibleProviders: List<NavigationProvider>,
    mainViewModel: MainViewModel = koinViewModel<MainViewModel>()
) {

    // Only clear invisible bundles when the visible providers change
    LaunchedEffect(visibleProviders) {
        val visibleRoutes = visibleProviders.map { it.route }.toSet()
        mainViewModel.clearInvisibleBundles(visibleRoutes)
    }

    // Build a stable map of NavHostControllers for ALL providers (maintains navigation stability)
    val navControllers = remember(navigationProviders) {
        mutableMapOf<NavigationProvider, NavHostController>()
    }

    for (provider in navigationProviders) {
        navControllers[provider] = rememberNavController()
    }

    // Use the properly ordered visible providers directly (preserves tab order)
    // No need to re-filter since visibleProviders is already correctly ordered from AppNavHost

    // Use rememberSaveable to persist across recompositions (e.g., after popping back from settings)
    var selectedRoute by rememberSaveable {
        mutableStateOf(visibleProviders.first().route)
    }

    val selectedProvider = navigationProviders.first { it.route == selectedRoute }
    val selectedNavController = navControllers.getValue(selectedProvider)

    // Extract current route/provider for cleaner topBar/FAB usage
    val currentScreenRoute by selectedNavController.currentBackStackEntryAsState()
    val currentRoute = currentScreenRoute?.destination?.route ?: selectedRoute
    val currentUiProvider = remember(currentRoute) {
        screenUiProviders.firstOrNull { it.route == currentRoute }
    }

    BackHandler {
        // Intentionally left blank to disable back navigation on main screen
    }

    // Save bundles on dispose (e.g., when navigating away to settings)
    for (provider in navigationProviders) {
        val navController = navControllers.getValue(provider)
        DisposableEffect(navController) {
            onDispose {
                mainViewModel.setBundle(
                    provider.route, navController.saveState()
                )
            }
        }
    }

    // Wrap Scaffold in CompositionLocalProvider to provide LocalTabNavController for topBar/FAB
    // Also provide TopAppBarState for scroll-aware top bars
    val topAppBarState = rememberTopAppBarState()

    CompositionLocalProvider(LocalTabNavController provides selectedNavController) {
        ProvideTopAppBarState(state = topAppBarState) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = { currentUiProvider?.topBar?.invoke() },
                floatingActionButton = { currentUiProvider?.fab?.invoke() },
                bottomBar = {
                    BottomNavigationBar(
                        selectedRoute = selectedRoute,
                        onTabSelected = { route -> selectedRoute = route },
                        items = visibleProviders
                    )
                }) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    for (provider in navigationProviders) {
                        val navController = navControllers.getValue(provider)
                        val isSelected = selectedRoute == provider.route

                        // Restore saved state when tab becomes selected
                        DisposableEffect(isSelected) {
                            if (isSelected) {
                                val savedBundle = mainViewModel.getBundle(provider.route)
                                if (savedBundle != null) {
                                    navController.restoreState(savedBundle)
                                }
                            }
                            onDispose {
                                if (isSelected) {
                                    mainViewModel.setBundle(
                                        provider.route, navController.saveState()
                                    )
                                }
                            }
                        }

                        // Only render the selected NavHost to avoid pointer input conflicts
                        if (isSelected) {
                            CompositionLocalProvider(LocalTabNavController provides navController) {
                                SharedTransitionLayout {
                                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                                        NavHost(
                                            navController = navController,
                                            startDestination = provider.route,
                                            modifier = Modifier.fillMaxSize(),
                                            enterTransition = { EnterTransition.None },
                                            exitTransition = { ExitTransition.None },
                                            popEnterTransition = { EnterTransition.None },
                                            popExitTransition = { ExitTransition.None }
                                        ) {
                                            provider.buildGraph(this)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
