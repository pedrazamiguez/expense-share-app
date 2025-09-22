package es.pedrazamiguez.expenseshareapp.ui.main.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.util.DoubleTapBackToExitHandler
import es.pedrazamiguez.expenseshareapp.ui.main.presentation.component.BottomNavigationBar

@Composable
fun MainScreen(
    navigationProviders: List<NavigationProvider>,
    doubleTapBackHandler: DoubleTapBackToExitHandler = remember { DoubleTapBackToExitHandler() }
) {

    val navControllers = navigationProviders.associate { provider ->
        provider.route to rememberNavController()
    }

    var selectedRoute by rememberSaveable { mutableStateOf(navigationProviders.first().route) }
    val activity = LocalActivity.current

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedRoute = selectedRoute,
                onTabSelected = { route -> selectedRoute = route },
                items = navigationProviders
            )
        }) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding)) {
            Crossfade(
                targetState = selectedRoute,
                label = "TabSwitch"
            ) { currentRoute ->
                val selectedProvider = navigationProviders.first { it.route == currentRoute }
                val selectedNavController = navControllers[currentRoute]!!

                NavHost(
                    navController = selectedNavController,
                    startDestination = selectedProvider.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    selectedProvider.buildGraph(this)
                }
            }
        }
    }

    BackHandler {
        val currentNavController = navControllers[selectedRoute]!!
        val didPop = currentNavController.popBackStack()
        if (!didPop && doubleTapBackHandler.shouldExit()) {
            activity?.finish()
        }
    }

}
