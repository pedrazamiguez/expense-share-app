package es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.hardcoded
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.ErrorView
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.BalanceList
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.BalanceUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.BalanceUiState

@Composable
fun BalancesScreen(
    uiState: BalanceUiState,
    onEvent: (BalanceUiEvent) -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Balances".hardcoded)
    }

    when {
        uiState.isLoading -> CircularProgressIndicator()
        uiState.error != null -> ErrorView()
        else -> BalanceList(
            balances = uiState.balances,
            onGroupClick = { id ->
                onEvent(BalanceUiEvent.OnGroupSelected(id))
                onNavigateToGroup(id)
            })
    }

}
