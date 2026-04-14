package es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.handler

import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.domain.usecase.balance.DeleteCashWithdrawalUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.DeleteContributionUseCase
import es.pedrazamiguez.splittrip.features.balance.R
import es.pedrazamiguez.splittrip.features.balance.presentation.model.CashWithdrawalUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.model.ContributionUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.action.BalancesUiAction
import es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.state.BalancesActivitySelectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Handles activity-list delete events (contributions and cash withdrawals) for the Balances screen.
 *
 * Operates on the dedicated [BalancesActivitySelectionState] flow — not the full Balances UiState
 * — because the main data state is derived from Room observations and cannot be directly mutated.
 */
class BalancesActivityEventHandlerImpl(
    private val deleteContributionUseCase: DeleteContributionUseCase,
    private val deleteCashWithdrawalUseCase: DeleteCashWithdrawalUseCase
) : BalancesActivityEventHandler {

    private lateinit var _selectionState: MutableStateFlow<BalancesActivitySelectionState>
    private lateinit var _actions: MutableSharedFlow<BalancesUiAction>
    private lateinit var scope: CoroutineScope

    override fun bind(
        selectionFlow: MutableStateFlow<BalancesActivitySelectionState>,
        actionsFlow: MutableSharedFlow<BalancesUiAction>,
        scope: CoroutineScope
    ) {
        _selectionState = selectionFlow
        _actions = actionsFlow
        this.scope = scope
    }

    // ── Contribution ─────────────────────────────────────────────────────────

    override fun handleDeleteContributionRequested(contribution: ContributionUiModel) {
        _selectionState.update { it.copy(contributionToDelete = contribution) }
    }

    override fun handleDeleteContributionDismissed() {
        _selectionState.update { it.copy(contributionToDelete = null) }
    }

    override fun handleDeleteContributionConfirmed(groupId: String, contributionId: String) {
        _selectionState.update { it.copy(contributionToDelete = null) }
        scope.launch {
            runCatching { deleteContributionUseCase(groupId, contributionId) }
                .onSuccess {
                    _actions.emit(
                        BalancesUiAction.ShowDeleteContributionSuccess(
                            UiText.StringResource(R.string.balances_delete_contribution_success)
                        )
                    )
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to delete contribution $contributionId in group $groupId")
                    _actions.emit(
                        BalancesUiAction.ShowDeleteContributionError(
                            UiText.StringResource(R.string.balances_delete_contribution_error)
                        )
                    )
                }
        }
    }

    // ── Cash Withdrawal ───────────────────────────────────────────────────────

    override fun handleDeleteWithdrawalRequested(withdrawal: CashWithdrawalUiModel) {
        _selectionState.update { it.copy(withdrawalToDelete = withdrawal) }
    }

    override fun handleDeleteWithdrawalDismissed() {
        _selectionState.update { it.copy(withdrawalToDelete = null) }
    }

    override fun handleDeleteWithdrawalConfirmed(groupId: String, withdrawalId: String) {
        _selectionState.update { it.copy(withdrawalToDelete = null) }
        scope.launch {
            runCatching { deleteCashWithdrawalUseCase(groupId, withdrawalId) }
                .onSuccess {
                    _actions.emit(
                        BalancesUiAction.ShowDeleteWithdrawalSuccess(
                            UiText.StringResource(R.string.balances_delete_withdrawal_success)
                        )
                    )
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to delete withdrawal $withdrawalId in group $groupId")
                    _actions.emit(
                        BalancesUiAction.ShowDeleteWithdrawalError(
                            UiText.StringResource(R.string.balances_delete_withdrawal_error)
                        )
                    )
                }
        }
    }
}
