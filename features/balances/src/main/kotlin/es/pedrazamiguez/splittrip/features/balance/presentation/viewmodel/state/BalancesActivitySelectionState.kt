package es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.state

import es.pedrazamiguez.splittrip.features.balance.presentation.model.CashWithdrawalUiModel
import es.pedrazamiguez.splittrip.features.balance.presentation.model.ContributionUiModel

/**
 * Holds ephemeral delete-selection state for the activity list.
 *
 * Kept as a separate [MutableStateFlow] so it can be mutated by
 * [es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.handler.BalancesActivityEventHandlerImpl]
 * without interfering with the Room-derived main data flow in [BalancesViewModel].
 * The two flows are [kotlinx.coroutines.flow.combine]d into the exposed [BalancesUiState].
 */
data class BalancesActivitySelectionState(
    val contributionToDelete: ContributionUiModel? = null,
    val withdrawalToDelete: CashWithdrawalUiModel? = null
)
