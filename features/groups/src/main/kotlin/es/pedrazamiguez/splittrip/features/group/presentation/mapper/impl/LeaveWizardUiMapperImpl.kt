package es.pedrazamiguez.splittrip.features.group.presentation.mapper.impl

import es.pedrazamiguez.splittrip.core.common.enums.SelfIdentificationContext
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.R as DesignSystemR
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.splittrip.core.designsystem.presentation.mapper.UserUiMapper
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.domain.service.DebtSimplificationService
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.mapper.LeaveWizardUiMapper
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveBalanceSummaryUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveCashResolutionUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveSettlementStatusType
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveSettlementUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveSubunitImpactUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.NetPositionUiModel
import kotlin.math.abs
import kotlinx.collections.immutable.toImmutableList

class LeaveWizardUiMapperImpl(
    private val formattingHelper: FormattingHelper,
    private val resourceProvider: ResourceProvider,
    private val userUiMapper: UserUiMapper,
    private val debtSimplificationService: DebtSimplificationService
) : LeaveWizardUiMapper {

    override fun toBalanceSummaryUiModel(
        memberBalance: MemberBalance,
        memberBalances: List<MemberBalance>,
        currentUserId: String,
        memberProfiles: Map<String, User>,
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
            ),
            perPersonNetPositions = toPerPersonNetPositions(
                memberBalances = memberBalances,
                currentUserId = currentUserId,
                memberProfiles = memberProfiles,
                groupCurrency = currency
            ).toImmutableList()
        )
    }

    private fun toPerPersonNetPositions(
        memberBalances: List<MemberBalance>,
        currentUserId: String,
        memberProfiles: Map<String, User>,
        groupCurrency: String
    ): List<NetPositionUiModel> {
        val netSettlements = debtSimplificationService.simplify(memberBalances)
        return netSettlements.mapNotNull { settlement ->
            val otherUserId = when {
                settlement.fromUserId == currentUserId -> settlement.toUserId
                settlement.toUserId == currentUserId -> settlement.fromUserId
                else -> return@mapNotNull null
            }
            val amount = settlement.amount
            val isPositive = settlement.toUserId == currentUserId
            val isNegative = settlement.fromUserId == currentUserId
            val otherMemberName = resolveMemberName(otherUserId, memberProfiles, currentUserId)
            val amountFormatted = formattingHelper.formatCentsWithCurrency(amount, groupCurrency)
            NetPositionUiModel(
                memberName = otherMemberName,
                amountFormatted = amountFormatted,
                isPositive = isPositive,
                isNegative = isNegative
            )
        }
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
            val directionTitle = resolveDirectionTitle(isDebtor, isCreditor, debtorName, creditorName)

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

            val (statusType, statusLabel) = resolveStatusInfo(
                record,
                isDebtor,
                isCreditor,
                canConfirm,
                debtorName,
                creditorName
            )

            LeaveSettlementUiModel(
                settlementId = record.id,
                debtorName = debtorName,
                creditorName = creditorName,
                directionTitle = directionTitle,
                formattedAmount = formattingHelper.formatCentsWithCurrency(
                    record.settlement.amount,
                    record.settlement.currency
                ),
                pocketTypeLabel = resolvePocketTypeLabel(record.settlement.sourcePocket),
                pocketType = record.settlement.sourcePocket,
                statusLabel = statusLabel,
                statusType = statusType,
                isCurrentUserDebtor = isDebtor,
                isCurrentUserCreditor = isCreditor,
                canCurrentUserConfirm = canConfirm,
                isConfirmed = isConfirmed
            )
        }
    }

    private fun resolveDirectionTitle(
        isDebtor: Boolean,
        isCreditor: Boolean,
        debtorName: String,
        creditorName: String
    ): String = when {
        isDebtor -> resourceProvider.getString(R.string.leave_wizard_settlement_you_owe, creditorName)
        isCreditor -> resourceProvider.getString(R.string.leave_wizard_settlement_owes_you, debtorName)
        else -> resourceProvider.getString(R.string.leave_wizard_settlement_other_owes, debtorName, creditorName)
    }

    private fun resolveStatusInfo(
        record: SettlementRecord,
        isDebtor: Boolean,
        isCreditor: Boolean,
        canConfirm: Boolean,
        debtorName: String,
        creditorName: String
    ): Pair<LeaveSettlementStatusType, String> = when {
        record.status == SettlementStatus.RESOLVED ->
            LeaveSettlementStatusType.CONFIRMED to
                resourceProvider.getString(DesignSystemR.string.settlement_status_confirmed)
        canConfirm ->
            LeaveSettlementStatusType.ACTION_REQUIRED_BY_USER to
                resourceProvider.getString(R.string.leave_wizard_settlement_action_required)
        record.status == SettlementStatus.CONFIRMED_BY_PAYER && isDebtor ->
            LeaveSettlementStatusType.WAITING_FOR_OTHER to
                resourceProvider.getString(R.string.leave_wizard_settlement_waiting_other, creditorName)
        record.status == SettlementStatus.SUGGESTED && isCreditor ->
            LeaveSettlementStatusType.WAITING_FOR_OTHER to
                resourceProvider.getString(R.string.leave_wizard_settlement_waiting_other, debtorName)
        else ->
            LeaveSettlementStatusType.WAITING_FOR_OTHER to
                resourceProvider.getString(R.string.settlement_overview_status_suggested)
    }

    override fun toCashResolutionUiModel(
        memberBalance: MemberBalance,
        currency: String
    ): LeaveCashResolutionUiModel {
        val cashInHand = memberBalance.cashInHand
        val formattedAmount = formattingHelper.formatCentsWithCurrency(
            abs(cashInHand),
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
        return userUiMapper.mapToDisplayName(
            user = profiles[userId],
            fallbackUserId = userId,
            currentUserId = currentUserId,
            selfIdentificationContext = SelfIdentificationContext.NOMINATIVE
        )
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
