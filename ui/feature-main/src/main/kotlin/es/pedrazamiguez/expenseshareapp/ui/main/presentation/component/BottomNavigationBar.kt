package es.pedrazamiguez.expenseshareapp.ui.main.presentation.component

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider

@Composable
fun BottomNavigationBar(
    navController: NavHostController, items: List<NavigationProvider>
) {

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(selected = currentRoute == item.route, onClick = {
                navController.navigate(item.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }, icon = { Icon(item.icon, contentDescription = item.label) }, label = { Text(item.label) })
        }
    }

}
