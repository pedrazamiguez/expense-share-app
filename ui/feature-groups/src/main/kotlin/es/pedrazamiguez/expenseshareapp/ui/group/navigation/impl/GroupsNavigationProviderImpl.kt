package es.pedrazamiguez.expenseshareapp.ui.group.navigation.impl

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import compose.icons.TablerIcons
import compose.icons.tablericons.Users
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.ui.group.navigation.GROUPS_ROUTE
import es.pedrazamiguez.expenseshareapp.ui.group.navigation.groupsGraph

class GroupsNavigationProviderImpl : NavigationProvider {

    override val route: String = GROUPS_ROUTE

    @Composable
    override fun Icon(isSelected: Boolean) {
        val scale by animateFloatAsState(targetValue = if (isSelected) 1.2f else 1f)

        androidx.compose.material3.Icon(
            imageVector = TablerIcons.Users,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.size((24.dp * scale))
        )
    }

    override val label: String = "Groups".placeholder

    override val order: Int = 90

    override suspend fun isVisible(): Boolean = true

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.groupsGraph()
    }

}
