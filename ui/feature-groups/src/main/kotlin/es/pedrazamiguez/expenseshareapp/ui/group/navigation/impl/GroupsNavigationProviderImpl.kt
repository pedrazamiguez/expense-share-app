package es.pedrazamiguez.expenseshareapp.ui.group.navigation.impl

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import compose.icons.TablerIcons
import compose.icons.tablericons.Users
import es.pedrazamiguez.expenseshareapp.core.ui.component.NavigationBarIcon
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.ui.group.navigation.GROUPS_ROUTE
import es.pedrazamiguez.expenseshareapp.ui.group.navigation.groupsGraph

class GroupsNavigationProviderImpl : NavigationProvider {

    override val route: String = GROUPS_ROUTE

    @Composable
    override fun Icon(isSelected: Boolean) {
        NavigationBarIcon(
            icon = TablerIcons.Users,
            contentDescription = label,
            isSelected = isSelected
        )
    }

    override val label: String = "Groups".placeholder

    override val order: Int = 90

    override suspend fun isVisible(): Boolean = true

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.groupsGraph()
    }

}
