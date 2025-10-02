package es.pedrazamiguez.expenseshareapp.ui.profile.presentation.screen.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.ui.screen.ScreenUiProvider

class ProfileScreenUiProviderImpl(
    override val route: String = Routes.PROFILE
) : ScreenUiProvider {

    @OptIn(ExperimentalMaterial3Api::class)
    override val topBar: @Composable () -> Unit = {
        val navController = LocalNavController.current
        TopAppBar(
            title = { Text("@andrespmi".placeholder) },
            actions = {
                IconButton(onClick = {
                    navController.navigate(Routes.SETTINGS)
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings"
                    )
                }
            })
    }

}
