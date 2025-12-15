package es.pedrazamiguez.expenseshareapp.features.main.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.features.main.presentation.component.navbar.FloatingNavItem
import es.pedrazamiguez.expenseshareapp.features.main.presentation.component.navbar.NavBarDefaults
import es.pedrazamiguez.expenseshareapp.features.main.presentation.component.navbar.SlidingIndicator

/**
 * A floating bottom navigation bar with Material 3 Expressive styling.
 *
 * Features:
 * - Floating pill shape with rounded corners
 * - Sliding indicator that animates between items
 * - Bouncy, expressive animations on selection
 * - Elevated shadow for depth
 */
@Composable
fun BottomNavigationBar(
    selectedRoute: String,
    onTabSelected: (String) -> Unit,
    items: List<NavigationProvider>
) {
    val selectedIndex = items.indexOfFirst { it.route == selectedRoute }.coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = NavBarDefaults.HorizontalPadding)
            .padding(bottom = NavBarDefaults.BottomPadding),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(NavBarDefaults.BarCornerRadius),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = NavBarDefaults.TonalElevation,
            shadowElevation = NavBarDefaults.ShadowElevation
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NavBarDefaults.BarHeight)
                    .padding(
                        horizontal = NavBarDefaults.InnerHorizontalPadding,
                        vertical = NavBarDefaults.InnerVerticalPadding
                    )
            ) {
                // Sliding indicator behind items
                SlidingIndicator(
                    selectedIndex = selectedIndex,
                    itemCount = items.size,
                    itemWidth = NavBarDefaults.ItemWidth,
                    containerWidth = maxWidth
                )

                // Navigation items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        FloatingNavItem(
                            item = item,
                            isSelected = index == selectedIndex,
                            onClick = { onTabSelected(item.route) },
                            modifier = Modifier.width(NavBarDefaults.ItemWidth)
                        )
                    }
                }
            }
        }
    }
}

