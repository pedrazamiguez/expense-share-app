package es.pedrazamiguez.splittrip.features.group.presentation.screen.impl

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.topbar.DynamicTopAppBar

class GroupDetailScreenUiProviderImpl(override val route: String = Routes.GROUP_DETAIL) :
    ScreenUiProvider {

    @OptIn(ExperimentalMaterial3Api::class)
    override val topBar: @Composable () -> Unit = {
        val navController = LocalTabNavController.current
        DynamicTopAppBar(
            title = "",
            onBack = { navController.popBackStack() }
        )
    }
}
