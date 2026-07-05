package es.pedrazamiguez.splittrip.features.group.presentation.mapper.impl

import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.R as DesignSystemR
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.mapper.LeaveWizardUiMapper
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveBalanceSummaryUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveCashResolutionUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveSettlementUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveSubunitImpactUiModel
import kotlinx.collections.immutable.toImmutableList

class LeaveWizardUiMapperImpl(
    private val formattingHelper: FormattingHelper,
    private val resourceProvider: ResourceProvider
) : LeaveWizardUiMapper {

    override fun toBalanceSummaryUiModel(
        memberBalance: MemberBalance,
        currency: String
    ): LeaveBalanceSummaryUiModel {
        return LeaveBalanceSummaryUiModel(
            pocketBalanceFormatted = formattingHelper.formatCentsWithCurrency(
                memberBalance.pocketBalance,
                currency
            ),
            cashInHandFormatted = formattingHelper.formatCentsWithCurrency(
                memberBalance.cashInHand,
                currency
            ),
            totalBalanceFormatted = formattingHelper.formatCentsWithCurrency(
                memberBalance.totalBalance,
                currency
            )
        )
    }

    override fun toSettlementUiModels(
        settlements: List<SettlementRecord>,
        memberProfiles: Map<String, User>,
        currentUserId: String
    ): List<LeaveSettlementUiModel> {
        return settlements.map { record ->
            val isDebtor = record.settlement.fromUserId == currentUserId
            val isCreditor = record.settlement.toUserId == currentUserId

            val debtorName = resolveMemberName(record.settlement.fromUserId, memberProfiles, currentUserId)
            val creditorName = resolveMemberName(record.settlement.toUserId, memberProfiles, currentUserId)

            val canConfirm = when (record.status) {
                SettlementStatus.SUGGESTED -> isDebtor
                SettlementStatus.CONFIRMED_BY_PAYER -> isCreditor
                else -> false
            }

            val isConfirmed = when (record.status) {
                SettlementStatus.CONFIRMED_BY_PAYER -> isDebtor
                SettlementStatus.RESOLVED -> true
                else -> false
            }

            LeaveSettlementUiModel(
                settlementId = record.id,
                debtorName = debtorName,
                creditorName = creditorName,
                formattedAmount = formattingHelper.formatCentsWithCurrency(
                    record.settlement.amount,
                    record.settlement.currency
                ),
                pocketTypeLabel = resolvePocketTypeLabel(record.settlement.sourcePocket),
                isCurrentUserDebtor = isDebtor,
                isCurrentUserCreditor = isCreditor,
                canCurrentUserConfirm = canConfirm,
                isConfirmed = isConfirmed
            )
        }
    }

    override fun toCashResolutionUiModel(
        memberBalance: MemberBalance,
        currency: String
    ): LeaveCashResolutionUiModel {
        val cashInHand = memberBalance.cashInHand
        val formattedAmount = formattingHelper.formatCentsWithCurrency(
            kotlin.math.abs(cashInHand),
            currency
        )
        return LeaveCashResolutionUiModel(
            requiresDeposit = cashInHand > 0,
            requiresReimbursement = cashInHand < 0,
            formattedAmount = formattedAmount
        )
    }

    override fun toSubunitImpactUiModel(userSubunits: List<Subunit>): LeaveSubunitImpactUiModel {
        if (userSubunits.isEmpty()) {
            return LeaveSubunitImpactUiModel(
                hasSubunitImpact = false,
                message = resourceProvider.getString(R.string.leave_wizard_subunit_impact_none)
            )
        }

        val subunitNames = userSubunits.map { it.name }.toImmutableList()
        val formattedNames = subunitNames.joinToString(", ")
        val message = resourceProvider.getString(
            R.string.leave_wizard_subunit_impact_redistribute,
            formattedNames
        )

        return LeaveSubunitImpactUiModel(
            hasSubunitImpact = true,
            affectedSubunitNames = subunitNames,
            message = message
        )
    }

    private fun resolveMemberName(
        userId: String,
        profiles: Map<String, User>,
        currentUserId: String
    ): String {
        if (userId == currentUserId) {
            return resourceProvider.getString(DesignSystemR.string.balance_you)
        }
        return profiles[userId]?.displayName
            ?: resourceProvider.getString(DesignSystemR.string.user_pending_fallback)
    }

    private fun resolvePocketTypeLabel(type: SettlementPocketType): String {
        return when (type) {
            SettlementPocketType.POCKET ->
                resourceProvider.getString(DesignSystemR.string.settlement_pocket_type_virtual)
            SettlementPocketType.CASH ->
                resourceProvider.getString(DesignSystemR.string.settlement_pocket_type_cash)
            SettlementPocketType.NET ->
                resourceProvider.getString(DesignSystemR.string.settlement_pocket_type_net)
        }
    }
}
