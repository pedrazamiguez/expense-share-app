package es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen.impl

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar.DynamicTopAppBar
import es.pedrazamiguez.expenseshareapp.features.expense.R

class ExpensesScreenUiProviderImpl(override val route: String = Routes.EXPENSES) : ScreenUiProvider {

    @OptIn(ExperimentalMaterial3Api::class)
    override val topBar: @Composable () -> Unit = {
        DynamicTopAppBar(
            title = stringResource(R.string.expenses_title),
            subtitle = stringResource(R.string.expenses_subtitle)
        )
    }

    // FAB is now handled inside ExpensesScreen for proper shared element transitions
}
