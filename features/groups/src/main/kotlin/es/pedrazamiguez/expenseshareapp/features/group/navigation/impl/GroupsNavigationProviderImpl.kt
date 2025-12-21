package es.pedrazamiguez.expenseshareapp.features.group.navigation.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups2
import androidx.compose.material.icons.outlined.Groups2
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.NavigationBarIcon
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.navigation.groupsGraph

class GroupsNavigationProviderImpl(
    override val route: String = Routes.GROUPS,
    override val requiresSelectedGroup: Boolean = false,
    override val order: Int = 10
) : NavigationProvider {

    @Composable
    override fun Icon(isSelected: Boolean, tint: Color) = NavigationBarIcon(
        icon = if (isSelected) Icons.Filled.Groups2 else Icons.Outlined.Groups2,
        contentDescription = getLabel(),
        isSelected = isSelected,
        tint = tint
    )

    @Composable
    override fun getLabel(): String = stringResource(R.string.groups_title)

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.groupsGraph()
    }

}
