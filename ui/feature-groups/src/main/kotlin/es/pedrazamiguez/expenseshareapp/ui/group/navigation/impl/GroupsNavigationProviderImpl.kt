package es.pedrazamiguez.expenseshareapp.ui.group.navigation.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups2
import androidx.compose.material.icons.outlined.Groups2
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.ui.component.NavigationBarIcon
import es.pedrazamiguez.expenseshareapp.core.ui.extension.hardcoded
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.ui.group.navigation.groupsGraph

class GroupsNavigationProviderImpl(override val route: String = Routes.GROUPS) : NavigationProvider {

    @Composable
    override fun Icon(isSelected: Boolean) {
        NavigationBarIcon(
            icon = if (isSelected) Icons.Filled.Groups2 else Icons.Outlined.Groups2,
            contentDescription = label,
            isSelected = isSelected
        )
    }

    override val label: String = "Groups".hardcoded

    override val order: Int = 10

    override suspend fun isVisible(): Boolean = true

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.groupsGraph()
    }

}
