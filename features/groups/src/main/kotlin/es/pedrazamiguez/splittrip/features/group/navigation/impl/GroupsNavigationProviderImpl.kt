package es.pedrazamiguez.splittrip.features.group.navigation.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.filled.UsersGroupFilled
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.UsersGroup
import es.pedrazamiguez.splittrip.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.core.designsystem.navigation.TabGraphContributor
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.scaffold.NavigationBarIcon
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.navigation.groupsGraph

class GroupsNavigationProviderImpl(
    private val graphContributors: List<TabGraphContributor> = emptyList(),
    override val route: String = Routes.GROUPS,
    override val requiresSelectedGroup: Boolean = false,
    override val order: Int = 10
) : NavigationProvider {

    @Composable
    override fun Icon(isSelected: Boolean, tint: Color) = NavigationBarIcon(
        icon = if (isSelected) TablerIcons.Filled.UsersGroupFilled else TablerIcons.Outline.UsersGroup,
        contentDescription = getLabel(),
        isSelected = isSelected,
        tint = tint
    )

    @Composable
    override fun getLabel(): String = stringResource(R.string.groups_title)

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.groupsGraph()
        graphContributors.forEach { it.contributeGraph(builder) }
    }
}
