package es.pedrazamiguez.expenseshareapp.ui.expense.presentation.screen.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.ui.expense.R

class ExpensesScreenUiProviderImpl(override val route: String = Routes.EXPENSES) :
    ScreenUiProvider {

    @OptIn(ExperimentalMaterial3Api::class)
    override val topBar: @Composable () -> Unit = {
        TopAppBar(
            title = { Text(stringResource(R.string.expenses_title)) },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Outlined.FilterAlt,
                        contentDescription = stringResource(R.string.expenses_filter)
                    )
                }
            })
    }

    override val fab: @Composable () -> Unit = {
        val navController = LocalTabNavController.current
        FloatingActionButton(
            onClick = {
                navController.navigate(Routes.ADD_EXPENSE)
            }) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = stringResource(R.string.expenses_add)
            )
        }
    }

}
