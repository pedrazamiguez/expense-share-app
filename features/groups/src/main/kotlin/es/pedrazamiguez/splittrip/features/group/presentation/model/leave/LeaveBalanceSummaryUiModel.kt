package es.pedrazamiguez.splittrip.features.group.presentation.model.leave

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class LeaveBalanceSummaryUiModel(
    val pocketBalanceFormatted: String = "",
    val cashInHandFormatted: String = "",
    val totalBalanceFormatted: String = "",
    val perPersonNetPositions: ImmutableList<NetPositionUiModel> = persistentListOf()
)

data class NetPositionUiModel(
    val memberName: String,
    val amountFormatted: String,
    val isPositive: Boolean,
    val isNegative: Boolean
)
