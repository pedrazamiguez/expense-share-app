package es.pedrazamiguez.splittrip.features.settlement.presentation.model

import kotlinx.collections.immutable.ImmutableList

data class MemberSpendingChartUiModel(
    val bars: ImmutableList<MemberSpendingBarUiModel>,
    val formattedGroupTotal: String,
    val isCashOnly: Boolean
)
