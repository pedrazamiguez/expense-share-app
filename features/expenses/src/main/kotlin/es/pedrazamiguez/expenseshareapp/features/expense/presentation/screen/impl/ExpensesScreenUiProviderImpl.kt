package es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar.DynamicTopAppBar
import es.pedrazamiguez.expenseshareapp.features.expense.R

class ExpensesScreenUiProviderImpl(override val route: String = Routes.EXPENSES) :
    ScreenUiProvider {

    @OptIn(ExperimentalMaterial3Api::class)
    override val topBar: @Composable () -> Unit = {
        DynamicTopAppBar(
            title = stringResource(R.string.expenses_title),
            subtitle = stringResource(R.string.expenses_subtitle),
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
                Icons.Outlined.Add, contentDescription = stringResource(R.string.expenses_add)
            )
        }
    }

}
