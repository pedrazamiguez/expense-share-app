package es.pedrazamiguez.expenseshareapp.ui.balance.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import es.pedrazamiguez.expenseshareapp.core.ui.component.ErrorView
import es.pedrazamiguez.expenseshareapp.core.ui.extension.placeholder
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.component.BalanceList
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.model.BalanceUiEvent
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.model.BalanceUiState

@Composable
fun BalancesScreen(
    state: BalanceUiState, onEvent: (BalanceUiEvent) -> Unit, onNavigateToGroup: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Text("Balances".placeholder)
    }

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
