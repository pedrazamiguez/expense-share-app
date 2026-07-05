package es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.state

import es.pedrazamiguez.splittrip.features.group.presentation.model.SettlementRowUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class GroupSettlementOverviewUiState(
    val pendingSettlements: ImmutableList<SettlementRowUiModel> = persistentListOf(),
    val disputedSettlements: ImmutableList<SettlementRowUiModel> = persistentListOf(),
    val resolvedSettlements: ImmutableList<SettlementRowUiModel> = persistentListOf(),
    val areAllSettlementsResolved: Boolean = false,
    val isLoading: Boolean = true,
    val isArchiving: Boolean = false,
    val activeDisputeSettlementId: String? = null,
    val disputeReasonInput: String = "",
    val hasError: Boolean = false
)
