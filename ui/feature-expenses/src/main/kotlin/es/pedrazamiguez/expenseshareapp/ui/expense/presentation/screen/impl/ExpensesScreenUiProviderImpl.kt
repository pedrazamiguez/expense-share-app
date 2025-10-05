package es.pedrazamiguez.expenseshareapp.ui.expense.presentation.screen.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.ui.extension.hardcoded
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.ui.screen.ScreenUiProvider

class ExpensesScreenUiProviderImpl(override val route: String = Routes.EXPENSES) : ScreenUiProvider {

    @OptIn(ExperimentalMaterial3Api::class)
    override val topBar: @Composable () -> Unit = {
        TopAppBar(
            title = { Text("Expenses".hardcoded) },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Outlined.FilterAlt,
                        contentDescription = null
                    )
                }
            })
    }

}
