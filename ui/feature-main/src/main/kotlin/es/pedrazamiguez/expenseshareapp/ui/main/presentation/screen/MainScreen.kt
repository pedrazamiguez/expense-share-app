package es.pedrazamiguez.expenseshareapp.ui.main.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.ui.main.presentation.component.BottomNavigationBar

@Composable
fun MainScreen(
    navigationProviders: List<NavigationProvider>
) {

    val navControllers = navigationProviders.associate { provider ->
        provider.route to rememberNavController()
    }

    var selectedRoute by rememberSaveable { mutableStateOf(navigationProviders.first().route) }

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
                animationSpec = tween(durationMillis = 150),
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
        // Prevent back button from switching tabs unintentionally
    }

}
