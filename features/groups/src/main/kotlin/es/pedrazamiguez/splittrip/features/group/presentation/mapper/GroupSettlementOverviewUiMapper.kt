package es.pedrazamiguez.splittrip.features.group.presentation.mapper

import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.state.GroupSettlementOverviewUiState

interface GroupSettlementOverviewUiMapper {
    fun toUiState(
        settlements: List<SettlementRecord>,
        memberProfiles: Map<String, User>,
        currentUserId: String,
        groupCreatorId: String,
        groupName: String
    ): GroupSettlementOverviewUiState
}
