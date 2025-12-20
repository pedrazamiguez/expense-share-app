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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Groups2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Groups2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.NavigationBarIcon
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.features.main.R
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

@PreviewComplete
@Composable
private fun BottomNavigationBarPreview() {

    val navigationProviders = listOf(

        // Groups
        object : NavigationProvider {
            override val route: String = Routes.GROUPS
            override val order: Int = 10
            override val requiresSelectedGroup: Boolean = false

            @Composable
            override fun Icon(
                isSelected: Boolean, tint: Color
            ) = NavigationBarIcon(
                icon = if (isSelected) Icons.Filled.Groups2 else Icons.Outlined.Groups2,
                contentDescription = getLabel(),
                isSelected = isSelected,
                tint = tint
            )

            @Composable
            override fun getLabel(): String = stringResource(R.string.preview_groups_title)

            override fun buildGraph(builder: NavGraphBuilder) {

            }
        },

        // Balances
        object : NavigationProvider {
            override val route: String = Routes.BALANCES
            override val order: Int = 20
            override val requiresSelectedGroup: Boolean = true

            @Composable
            override fun Icon(
                isSelected: Boolean, tint: Color
            ) = NavigationBarIcon(
                icon = if (isSelected) Icons.Filled.Balance else Icons.Outlined.Balance,
                contentDescription = getLabel(),
                isSelected = isSelected,
                tint = tint
            )

            @Composable
            override fun getLabel(): String = stringResource(R.string.preview_balances_title)

            override fun buildGraph(builder: NavGraphBuilder) {

            }
        },

        // Expenses
        object : NavigationProvider {
            override val route: String = Routes.EXPENSES
            override val order: Int = 50
            override val requiresSelectedGroup: Boolean = true

            @Composable
            override fun Icon(
                isSelected: Boolean, tint: Color
            ) = NavigationBarIcon(
                icon = if (isSelected) Icons.AutoMirrored.Filled.ReceiptLong else Icons.AutoMirrored.Outlined.ReceiptLong,
                contentDescription = getLabel(),
                isSelected = isSelected,
                tint = tint
            )

            @Composable
            override fun getLabel(): String = stringResource(R.string.preview_expenses_title)

            override fun buildGraph(builder: NavGraphBuilder) {

            }
        },

        // Profile
        object : NavigationProvider {
            override val route: String = Routes.PROFILE
            override val order: Int = 90
            override val requiresSelectedGroup: Boolean = false

            @Composable
            override fun Icon(
                isSelected: Boolean, tint: Color
            ) = NavigationBarIcon(
                icon = if (isSelected) Icons.Filled.Person else Icons.Outlined.Person,
                contentDescription = getLabel(),
                isSelected = isSelected,
                tint = tint
            )

            @Composable
            override fun getLabel(): String = stringResource(R.string.preview_profile_title)

            override fun buildGraph(builder: NavGraphBuilder) {

            }
        }

    )

    PreviewThemeWrapper {
        BottomNavigationBar(
            selectedRoute = Routes.EXPENSES, items = navigationProviders
        )
    }

}

@PreviewComplete
@Composable
private fun BottomNavigationBarWithTwoItemsPreview() {

    val navigationProviders = listOf(

        // Groups
        object : NavigationProvider {
            override val route: String = Routes.GROUPS
            override val order: Int = 10
            override val requiresSelectedGroup: Boolean = false

            @Composable
            override fun Icon(
                isSelected: Boolean, tint: Color
            ) = NavigationBarIcon(
                icon = if (isSelected) Icons.Filled.Groups2 else Icons.Outlined.Groups2,
                contentDescription = getLabel(),
                isSelected = isSelected,
                tint = tint
            )

            @Composable
            override fun getLabel(): String = stringResource(R.string.preview_groups_title)

            override fun buildGraph(builder: NavGraphBuilder) {

            }
        },

        // Profile
        object : NavigationProvider {
            override val route: String = Routes.PROFILE
            override val order: Int = 90
            override val requiresSelectedGroup: Boolean = false

            @Composable
            override fun Icon(
                isSelected: Boolean, tint: Color
            ) = NavigationBarIcon(
                icon = if (isSelected) Icons.Filled.Person else Icons.Outlined.Person,
                contentDescription = getLabel(),
                isSelected = isSelected,
                tint = tint
            )

            @Composable
            override fun getLabel(): String = stringResource(R.string.preview_profile_title)

            override fun buildGraph(builder: NavGraphBuilder) {

            }
        }

    )

    PreviewThemeWrapper {
        BottomNavigationBar(
            selectedRoute = Routes.GROUPS, items = navigationProviders
        )
    }

}
