package es.pedrazamiguez.expenseshareapp.features.profile.navigation.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.NavigationBarIcon
import es.pedrazamiguez.expenseshareapp.features.profile.R
import es.pedrazamiguez.expenseshareapp.features.profile.navigation.profileGraph

class ProfileNavigationProviderImpl(
    override val route: String = Routes.PROFILE,
    override val requiresSelectedGroup: Boolean = false,
    override val order: Int = 90,
) : NavigationProvider {

    @Composable
    override fun Icon(isSelected: Boolean, tint: Color) = NavigationBarIcon(
        icon = if (isSelected) Icons.Filled.Person else Icons.Outlined.Person,
        contentDescription = getLabel(),
        isSelected = isSelected,
        tint = tint
    )

    @Composable
    override fun getLabel(): String = stringResource(R.string.profile_title)

    override fun buildGraph(builder: NavGraphBuilder) {
        builder.profileGraph()
    }

}
