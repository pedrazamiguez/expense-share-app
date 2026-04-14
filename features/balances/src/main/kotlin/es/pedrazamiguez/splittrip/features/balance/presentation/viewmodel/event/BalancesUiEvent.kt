package es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.event

import es.pedrazamiguez.splittrip.features.balance.presentation.model.CashWithdrawalUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.model.ContributionUiModel

sealed interface BalancesUiEvent {

    // Balance animation
    data object BalanceAnimationComplete : BalancesUiEvent

    // Contribution delete flow
    data class DeleteContributionRequested(val contribution: ContributionUiModel) : BalancesUiEvent
    data object DeleteContributionDismissed : BalancesUiEvent
    data class DeleteContributionConfirmed(val contributionId: String) : BalancesUiEvent

    // Cash withdrawal delete flow
    data class DeleteWithdrawalRequested(val withdrawal: CashWithdrawalUiModel) : BalancesUiEvent
    data object DeleteWithdrawalDismissed : BalancesUiEvent
    data class DeleteWithdrawalConfirmed(val withdrawalId: String) : BalancesUiEvent
}
