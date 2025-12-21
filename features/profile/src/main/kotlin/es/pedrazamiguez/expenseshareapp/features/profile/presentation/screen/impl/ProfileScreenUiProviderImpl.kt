package es.pedrazamiguez.expenseshareapp.features.profile.presentation.screen.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalRootNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar.DynamicTopAppBar
import es.pedrazamiguez.expenseshareapp.features.profile.R

class ProfileScreenUiProviderImpl(
    override val route: String = Routes.PROFILE
) : ScreenUiProvider {

    @OptIn(ExperimentalMaterial3Api::class)
    override val topBar: @Composable () -> Unit = {
        val navController = LocalRootNavController.current
        DynamicTopAppBar(
            title = stringResource(R.string.profile_title),
            subtitle = stringResource(R.string.profile_subtitle),
            actions = {
                IconButton(onClick = {
                    navController.navigate(Routes.SETTINGS)
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.profile_settings)
                    )
                }
            }
        )
    }

}
