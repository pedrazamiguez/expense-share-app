package es.pedrazamiguez.expenseshareapp.ui.group.presentation.screen.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder
import es.pedrazamiguez.expenseshareapp.core.ui.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.ui.group.navigation.GROUPS_ROUTE

class GroupsScreenUiProviderImpl(override val route: String = GROUPS_ROUTE) : ScreenUiProvider {

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

}
