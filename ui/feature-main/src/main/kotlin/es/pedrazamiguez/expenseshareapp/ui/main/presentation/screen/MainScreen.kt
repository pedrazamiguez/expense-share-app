package es.pedrazamiguez.expenseshareapp.ui.main.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.ui.main.presentation.component.BottomNavigationBar

@Composable
fun MainScreen(
    navigationProviders: List<NavigationProvider>,
    screenUiProviders: List<ScreenUiProvider>
) {
    // Create NavControllers per tab inside @Composable scope
    val navControllers = navigationProviders.associateWith { rememberNavController() }

    var selectedRoute by remember { mutableStateOf(navigationProviders.first().route) }

    val activeNavController = navControllers.entries.first { it.key.route == selectedRoute }.value
    val navBackStackEntry by activeNavController.currentBackStackEntryAsState()
    val currentScreenRoute = navBackStackEntry?.destination?.route
    screenUiProviders.firstOrNull { it.route == currentScreenRoute }

    BackHandler {

    }

    // Use Crossfade to switch tabs
    Crossfade(
        targetState = selectedRoute,
        animationSpec = tween(durationMillis = 150),
        label = "TabSwitch"
    ) { currentRoute ->
        val selectedProvider = navigationProviders.first { it.route == currentRoute }
        val selectedNavController = navControllers[selectedProvider]!!

        // Provide the per-tab NavController before calling FAB/topBar
        CompositionLocalProvider(LocalTabNavController provides selectedNavController) {
            Scaffold(
                topBar = {
                    screenUiProviders.firstOrNull { it.route == (selectedNavController.currentBackStackEntryAsState().value?.destination?.route ?: selectedProvider.route) }
                        ?.topBar?.invoke()
                },
                floatingActionButton = {
                    screenUiProviders.firstOrNull { it.route == (selectedNavController.currentBackStackEntryAsState().value?.destination?.route ?: selectedProvider.route) }
                        ?.fab?.invoke()
                },
                bottomBar = {
                    BottomNavigationBar(
                        selectedRoute = selectedRoute,
                        onTabSelected = { route -> selectedRoute = route },
                        items = navigationProviders
                    )
                }) { innerPadding ->
                NavHost(
                    navController = selectedNavController,
                    startDestination = selectedProvider.route,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    selectedProvider.buildGraph(this)
                }
            }
        }
    }
}
