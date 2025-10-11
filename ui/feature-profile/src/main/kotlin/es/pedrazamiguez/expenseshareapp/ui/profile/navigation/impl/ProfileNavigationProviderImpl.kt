package es.pedrazamiguez.expenseshareapp.ui.profile.navigation.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.component.NavigationBarIcon
import es.pedrazamiguez.expenseshareapp.ui.profile.R
import es.pedrazamiguez.expenseshareapp.ui.profile.navigation.profileGraph

class ProfileNavigationProviderImpl(
    override val route: String = Routes.PROFILE,
    override val requiresSelectedGroup: Boolean = false,
    override val order: Int = 90,
) : NavigationProvider {

    @Composable
    override fun Icon(isSelected: Boolean) = NavigationBarIcon(
        icon = if (isSelected) Icons.Filled.Person else Icons.Outlined.Person,
        contentDescription = getLabel(),
        isSelected = isSelected
    )

    @Composable
    override fun getLabel(): String = stringResource(R.string.profile_title)

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.profileGraph()
    }

}
