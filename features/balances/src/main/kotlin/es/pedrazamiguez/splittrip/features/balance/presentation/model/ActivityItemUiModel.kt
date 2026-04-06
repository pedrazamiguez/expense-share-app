package es.pedrazamiguez.splittrip.features.balance.presentation.model

/**
 * Sealed interface representing a single item in the unified activity history list.
 * Merges contributions and cash withdrawals into a single sortable timeline.
 */
sealed interface ActivityItemUiModel {
    val sortTimestamp: Long

    data class ContributionItem(val contribution: ContributionUiModel, override val sortTimestamp: Long) :
        ActivityItemUiModel

    data class CashWithdrawalItem(val withdrawal: CashWithdrawalUiModel, override val sortTimestamp: Long) :
        ActivityItemUiModel
}
