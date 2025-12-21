package es.pedrazamiguez.expenseshareapp.core.designsystem.preview

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Groups2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Groups2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.designsystem.R
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.NavigationBarIcon

/**
 * Preview data for navigation-related components.
 *
 * This object provides sample [NavigationProvider] implementations for use in Compose previews.
 * These are not production providers but simplified versions for preview purposes.
 */
object PreviewNavigationProviders {

    val groups: NavigationProvider = createProvider(
        route = Routes.GROUPS,
        order = 10,
        requiresSelectedGroup = false,
        labelResId = R.string.preview_nav_groups,
        selectedIcon = Icons.Filled.Groups2,
        unselectedIcon = Icons.Outlined.Groups2
    )

    val balances: NavigationProvider = createProvider(
        route = Routes.BALANCES,
        order = 20,
        requiresSelectedGroup = true,
        labelResId = R.string.preview_nav_balances,
        selectedIcon = Icons.Filled.Balance,
        unselectedIcon = Icons.Outlined.Balance
    )

    val expenses: NavigationProvider = createProvider(
        route = Routes.EXPENSES,
        order = 50,
        requiresSelectedGroup = true,
        labelResId = R.string.preview_nav_expenses,
        selectedIcon = Icons.AutoMirrored.Filled.ReceiptLong,
        unselectedIcon = Icons.AutoMirrored.Outlined.ReceiptLong
    )

    val profile: NavigationProvider = createProvider(
        route = Routes.PROFILE,
        order = 90,
        requiresSelectedGroup = false,
        labelResId = R.string.preview_nav_profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    /**
     * A minimal set of navigation items (Groups, Profile) for compact previews.
     */
    val minimal: List<NavigationProvider> = listOf(
        groups,
        profile
    )

    /**
     * A full set of navigation items representing the main app navigation.
     */
    val full: List<NavigationProvider> = listOf(
        groups,
        balances,
        expenses,
        profile
    )

    private fun createProvider(
        route: String,
        order: Int,
        requiresSelectedGroup: Boolean,
        @StringRes labelResId: Int,
        selectedIcon: ImageVector,
        unselectedIcon: ImageVector
    ): NavigationProvider = object : NavigationProvider {
        override val route: String = route
        override val order: Int = order
        override val requiresSelectedGroup: Boolean = requiresSelectedGroup

        @Composable
        override fun Icon(isSelected: Boolean, tint: Color) = NavigationBarIcon(
            icon = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = getLabel(),
            isSelected = isSelected,
            tint = tint
        )

        @Composable
        override fun getLabel(): String = stringResource(labelResId)

        override fun buildGraph(builder: NavGraphBuilder) {
            // No-op for previews
        }
    }
}

