package es.pedrazamiguez.splittrip.features.withdrawal.presentation.viewmodel.handler

import es.pedrazamiguez.splittrip.features.withdrawal.presentation.viewmodel.action.AddCashWithdrawalUiAction
import es.pedrazamiguez.splittrip.features.withdrawal.presentation.viewmodel.state.AddCashWithdrawalUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Base contract for event handlers that manage a sub-domain of AddCashWithdrawal logic.
 *
 * Handlers are plain classes (NOT ViewModels) that receive the shared state
 * and actions flows to read/write UI state. They are injected into the
 * [AddCashWithdrawalViewModel][es.pedrazamiguez.splittrip.features.withdrawal.presentation.viewmodel.AddCashWithdrawalViewModel]
 * via co-creation in the Koin `viewModel` block.
 */
interface AddCashWithdrawalEventHandler {

    /**
     * Binds the handler to the ViewModel's shared state and action flows.
     * Must be called once during ViewModel initialisation, before any event handling.
     */
    fun bind(
        stateFlow: MutableStateFlow<AddCashWithdrawalUiState>,
        actionsFlow: MutableSharedFlow<AddCashWithdrawalUiAction>,
        scope: CoroutineScope
    )
}
