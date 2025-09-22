package es.pedrazamiguez.expenseshareapp.ui.main.presentation.component

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider

@Composable
fun BottomNavigationBar(
    selectedRoute: String,
    onTabSelected: (String) -> Unit,
    items: List<NavigationProvider>
) {

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedRoute == item.route,
                onClick = { onTabSelected(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) })
        }
    }

}
