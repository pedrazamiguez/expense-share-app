package es.pedrazamiguez.expenseshareapp.ui.balance.presentation.ui

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.ui.component.ErrorView
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.component.BalanceList
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.model.BalanceUiEvent
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.model.BalanceUiState

@Composable
fun BalanceScreen(
    state: BalanceUiState, onEvent: (BalanceUiEvent) -> Unit, onNavigateToGroup: (String) -> Unit
) {

    when {
        state.isLoading -> CircularProgressIndicator()
        state.error != null -> ErrorView()
        else -> BalanceList(
            balances = state.balances, onGroupClick = { id ->
                onEvent(BalanceUiEvent.OnGroupSelected(id))
                onNavigateToGroup(id)
            })
    }

}
