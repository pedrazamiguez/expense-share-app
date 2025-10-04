package es.pedrazamiguez.expenseshareapp.ui.group.presentation.feature

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalRootNavController
import es.pedrazamiguez.expenseshareapp.ui.group.presentation.screen.GroupsScreen

@Composable
fun GroupsFeature(
    navController: NavController = LocalRootNavController.current
) {
    GroupsScreen()
}
