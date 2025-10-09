package es.pedrazamiguez.expenseshareapp.ui.main.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.ui.main.presentation.component.BottomNavigationBar
import es.pedrazamiguez.expenseshareapp.ui.main.presentation.viewmodel.MainViewModel

@Composable
fun MainScreen(
    navigationProviders: List<NavigationProvider>,
    screenUiProviders: List<ScreenUiProvider>,
    mainViewModel: MainViewModel = viewModel()
) {
    // Clear any saved bundles when navigationProviders change to prevent restoration crashes
    LaunchedEffect(navigationProviders) {
        val currentRoutes = navigationProviders.map { it.route }.toSet()
        mainViewModel.clearInvisibleBundles(currentRoutes)
    }

    // Build a stable map of NavHostControllers in a composable-safe way
    val navControllers = remember(navigationProviders) {
        mutableMapOf<NavigationProvider, NavHostController>()
    }

    for (provider in navigationProviders) {
        val navController = rememberNavController()
        navControllers[provider] = navController
    }

    // Use rememberSaveable to persist across recompositions (e.g., after popping back from settings)
    var selectedRoute by rememberSaveable {
        mutableStateOf(navigationProviders.first().route)
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
                    provider.route,
                    navController.saveState()
                )
            }
        }
    }

    // Wrap Scaffold in CompositionLocalProvider to provide LocalTabNavController for topBar/FAB
    CompositionLocalProvider(LocalTabNavController provides selectedNavController) {
        Scaffold(
            topBar = { currentUiProvider?.topBar?.invoke() },
            floatingActionButton = { currentUiProvider?.fab?.invoke() },
            bottomBar = {
                BottomNavigationBar(
                    selectedRoute = selectedRoute,
                    onTabSelected = { route -> selectedRoute = route },
                    items = navigationProviders
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

                    CompositionLocalProvider(LocalTabNavController provides navController) {
                        NavHost(
                            navController = navController,
                            startDestination = provider.route,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = if (isSelected) 1f else 0f },
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None },
                            popEnterTransition = { EnterTransition.None },
                            popExitTransition = { ExitTransition.None }) {
                            provider.buildGraph(this)
                        }
                    }
                }
            }
        }
    }
}
