package es.pedrazamiguez.expenseshareapp.ui.main.presentation.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.ui.main.presentation.component.BottomNavigationBar

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(), navigationProviders: List<NavigationProvider>
) {

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, items = navigationProviders.map { it }) }) { innerPadding ->
        NavHost(
            navController = navController, startDestination = navigationProviders.first().route, modifier = Modifier.padding(innerPadding)
        ) {
            navigationProviders.forEach { provider ->
                provider.buildGraph(this)
            }
        }
    }

}
