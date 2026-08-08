package es.pedrazamiguez.splittrip.features.group.presentation.mapper

import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveBalanceSummaryUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveSubunitImpactUiModel

interface LeaveWizardUiMapper {
    fun toBalanceSummaryUiModel(
        memberBalance: MemberBalance,
        memberBalances: List<MemberBalance>,
        currentUserId: String,
        memberProfiles: Map<String, User>,
        currency: String
    ): LeaveBalanceSummaryUiModel

    fun hasUnresolvedSettlements(
        settlements: List<SettlementRecord>,
        currentUserId: String
    ): Boolean

    fun toSubunitImpactUiModel(
        userSubunits: List<Subunit>
    ): LeaveSubunitImpactUiModel
}
