package es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.handler

import es.pedrazamiguez.splittrip.features.balance.presentation.model.CashWithdrawalUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.model.ContributionUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.action.BalancesUiAction
import es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.state.BalancesActivitySelectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Contract for the event handler that manages delete-flow events in the Balances screen.
 *
 * Handles contribution and cash-withdrawal delete requests, dismissals, and confirmations.
 * Operates on a separate [BalancesActivitySelectionState] flow (not the full [BalancesUiState])
 * because the main data state is derived from Room observations and cannot be mutated directly.
 *
 * Co-created inside `viewModel { }` in [es.pedrazamiguez.splittrip.features.balance.di.BalancesUiModule]
 * alongside the ViewModel.
 */
interface BalancesActivityEventHandler {

    /**
     * Binds this handler to the ViewModel's shared state and action flows.
     * Must be called once during ViewModel initialisation, before any event handling.
     */
    fun bind(
        selectionFlow: MutableStateFlow<BalancesActivitySelectionState>,
        actionsFlow: MutableSharedFlow<BalancesUiAction>,
        scope: CoroutineScope
    )

    fun handleDeleteContributionRequested(contribution: ContributionUiModel)
    fun handleDeleteContributionDismissed()
    fun handleDeleteContributionConfirmed(groupId: String, contributionId: String)
    fun handleDeleteWithdrawalRequested(withdrawal: CashWithdrawalUiModel)
    fun handleDeleteWithdrawalDismissed()
    fun handleDeleteWithdrawalConfirmed(groupId: String, withdrawalId: String)
}
