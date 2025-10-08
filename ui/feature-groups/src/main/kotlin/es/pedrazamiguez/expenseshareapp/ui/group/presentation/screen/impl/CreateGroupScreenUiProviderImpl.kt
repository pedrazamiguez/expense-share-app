package es.pedrazamiguez.expenseshareapp.ui.group.presentation.screen.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.ui.group.R

class CreateGroupScreenUiProviderImpl(
    override val route: String = Routes.CREATE_GROUP
) : ScreenUiProvider {

    @OptIn(ExperimentalMaterial3Api::class)
    override val topBar: @Composable () -> Unit = {
        val navController = LocalTabNavController.current
        TopAppBar(
            title = { Text(stringResource(R.string.groups_create)) },
            navigationIcon = {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.groups_back)
                    )
                }
            })
    }

}
