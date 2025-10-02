package es.pedrazamiguez.expenseshareapp.ui.group.presentation.screen.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.ui.screen.ScreenUiProvider

class GroupsScreenUiProviderImpl(
    override val route: String = Routes.GROUPS
) : ScreenUiProvider {


    @OptIn(ExperimentalMaterial3Api::class)
    override val topBar: @Composable () -> Unit = {
        TopAppBar(
            title = { Text("Groups".placeholder) },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null
                    )
                }
            })
    }

    override val fab: @Composable () -> Unit = {
        val navController = LocalNavController.current
        FloatingActionButton(
            onClick = {
                navController.navigate(Routes.CREATE_GROUP)
            }) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = "Create Group"
            )
        }
    }

}
