package es.pedrazamiguez.expenseshareapp.ui.balance.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.model.BalanceUiAction
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.viewmodel.BalanceViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun BalanceRoute(
    viewModel: BalanceViewModel = koinViewModel(),
    onNavigateToGroup: (String) -> Unit
) {

    val state by viewModel.uiState.collectAsState()
    val actions = viewModel.actions.collectAsStateWithLifecycle(null)

    LaunchedEffect(actions.value) {
        when (val action = actions.value) {
            is BalanceUiAction.NavigateToGroup -> onNavigateToGroup(action.groupId)
            is BalanceUiAction.ShowError -> {
                // Show snackbar / toast
            }

            null -> {}
        }
    }

    BalanceScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateToGroup = onNavigateToGroup
    )

}
