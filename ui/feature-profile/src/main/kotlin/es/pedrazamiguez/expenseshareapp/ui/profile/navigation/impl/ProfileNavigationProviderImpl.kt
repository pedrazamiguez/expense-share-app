package es.pedrazamiguez.expenseshareapp.ui.profile.navigation.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.ui.component.NavigationBarIcon
import es.pedrazamiguez.expenseshareapp.core.ui.extension.hardcoded
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.ui.profile.navigation.profileGraph

class ProfileNavigationProviderImpl(override val route: String = Routes.PROFILE) : NavigationProvider {

    @Composable
    override fun Icon(isSelected: Boolean) {
        NavigationBarIcon(
            icon = if (isSelected) Icons.Filled.Person else Icons.Outlined.Person,
            contentDescription = label,
            isSelected = isSelected
        )
    }

    override val label: String = "Profile".hardcoded

    override val order: Int = 90

    override suspend fun isVisible(): Boolean = true

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.profileGraph()
    }

}
