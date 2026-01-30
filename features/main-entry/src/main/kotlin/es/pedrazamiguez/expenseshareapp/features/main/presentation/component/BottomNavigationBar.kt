package es.pedrazamiguez.expenseshareapp.features.main.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
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
 * - Translucent "glassmorphism" effect using haze for the surrounding area
 */
@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    selectedRoute: String = "",
    onTabSelected: (String) -> Unit = {},
    items: List<NavigationProvider> = emptyList(),
    hazeState: HazeState? = null
) {
    val selectedIndex = items.indexOfFirst { it.route == selectedRoute }.coerceAtLeast(0)

    Box(
        modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter
    ) {
        // SURROUNDING BLUR SCRIM
        // This creates the "fade-to-blur" effect at the bottom of the screen.
        if (hazeState != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // Height = Bar Height + Bottom Padding + Extra buffer (32.dp) for the smooth fade
                    .height(NavBarDefaults.BarHeight + NavBarDefaults.BottomPadding + 32.dp)
                    .hazeEffect(
                        state = hazeState, style = HazeStyle(
                            blurRadius = 24.dp, tint = HazeTint(
                                Color.Transparent
                            )
                        )
                    ) {
                        // Gradient Mask: Transparent (Top) -> Black (Bottom)
                        // This makes the blur "fade in" from top to bottom
                        mask = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black, Color.Black),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    })
        }

        // THE FLOATING PILL (Navigation Bar)
        // This sits ON TOP of the scrim.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = NavBarDefaults.HorizontalPadding)
                .padding(bottom = NavBarDefaults.BottomPadding)
                .shadow(NavBarDefaults.ShadowElevation, CircleShape)
                .clip(CircleShape)
                .then(
                    Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ), contentAlignment = Alignment.Center
        ) {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0)
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

}

