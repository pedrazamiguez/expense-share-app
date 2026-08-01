package es.pedrazamiguez.splittrip.features.settlement.presentation.model

import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.CashBreakdownItemUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class PersonalPositionUiModel(
    val groupCurrencyCode: String = "",
    val formattedNetPosition: String = "",
    val netPositionStatus: NetPositionStatus = NetPositionStatus.NEUTRAL,
    val formattedPocketBalance: String = "",
    val formattedCashInHand: String = "",
    val hasNegativeCashInHand: Boolean = false,
    val formattedTotalContributed: String = "",
    val formattedTotalSpent: String = "",
    val formattedCashSpent: String = "",
    val formattedNonCashSpent: String = "",
    val formattedRefundableSpent: String? = null,
    val formattedTotalFees: String? = null,
    val cashInHandByCurrency: ImmutableList<CurrencyBreakdownUiModel> = persistentListOf(),
    val cashSpentByCurrency: ImmutableList<CurrencyBreakdownUiModel> = persistentListOf(),
    val nonCashSpentByCurrency: ImmutableList<CurrencyBreakdownUiModel> = persistentListOf(),
    val refundableSpentByCurrency: ImmutableList<CurrencyBreakdownUiModel> = persistentListOf(),
    val cashBreakdown: ImmutableList<CashBreakdownItemUiModel> = persistentListOf()
)

enum class NetPositionStatus {
    POSITIVE,
    NEUTRAL,
    NEGATIVE
}

data class CurrencyBreakdownUiModel(
    val currency: String = "",
    val formattedAmount: String = "",
    val formattedEquivalent: String = ""
)
