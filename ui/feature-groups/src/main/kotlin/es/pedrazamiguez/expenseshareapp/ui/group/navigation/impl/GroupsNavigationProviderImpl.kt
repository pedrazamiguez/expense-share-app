package es.pedrazamiguez.expenseshareapp.ui.group.navigation.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups2
import androidx.compose.material.icons.outlined.Groups2
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.component.NavigationBarIcon
import es.pedrazamiguez.expenseshareapp.ui.group.R
import es.pedrazamiguez.expenseshareapp.ui.group.navigation.groupsGraph

class GroupsNavigationProviderImpl(
    override val route: String = Routes.GROUPS,
    override val requiresSelectedGroup: Boolean = false,
    override val order: Int = 10
) : NavigationProvider {

    @Composable
    override fun Icon(isSelected: Boolean) = NavigationBarIcon(
        icon = if (isSelected) Icons.Filled.Groups2 else Icons.Outlined.Groups2,
        contentDescription = getLabel(),
        isSelected = isSelected
    )

    @Composable
    override fun getLabel(): String = stringResource(R.string.groups_title)

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.groupsGraph()
    }

}
