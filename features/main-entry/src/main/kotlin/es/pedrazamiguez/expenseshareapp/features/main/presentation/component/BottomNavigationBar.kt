package es.pedrazamiguez.expenseshareapp.features.main.presentation.component

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider

@Composable
fun BottomNavigationBar(
    selectedRoute: String,
    onTabSelected: (String) -> Unit,
    items: List<NavigationProvider>
) {

    NavigationBar {
        items.forEach { item ->
            val isSelected = selectedRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.route) },
                icon = { item.Icon(isSelected = isSelected) },
                label = { Text(item.getLabel()) })
        }
    }

}
