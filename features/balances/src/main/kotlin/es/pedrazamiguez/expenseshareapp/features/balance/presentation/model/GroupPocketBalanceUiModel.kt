package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * UI model representing the group pocket balance summary.
 * All amounts are pre-formatted by the mapper for direct display.
 */
data class GroupPocketBalanceUiModel(
    val groupName: String = "",
    val formattedBalance: String = "",
    val formattedTotalContributed: String = "",
    val formattedTotalSpent: String = "",
    val currency: String = "",
    val cashBalances: ImmutableList<CashBalanceUiModel> = persistentListOf(),
    val formattedTotalCashEquivalent: String = "",
    /**
     * Pre-formatted "available" balance (balance − scheduled holds).
     * Null when there are no future scheduled expenses, so the UI hides the row.
     */
    val formattedAvailableBalance: String? = null
)
