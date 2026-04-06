package es.pedrazamiguez.splittrip.features.subunit.presentation.screen.impl

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.topbar.DynamicTopAppBar
import es.pedrazamiguez.splittrip.features.subunit.R

class CreateEditSubunitScreenUiProviderImpl(override val route: String = Routes.CREATE_EDIT_SUBUNIT) :
    ScreenUiProvider {

    @OptIn(ExperimentalMaterial3Api::class)
    override val topBar: @Composable () -> Unit = {
        val navController = LocalTabNavController.current
        DynamicTopAppBar(
            title = stringResource(R.string.subunit_create_edit_title),
            onBack = { navController.popBackStack() },
            pinned = true
        )
    }
}
