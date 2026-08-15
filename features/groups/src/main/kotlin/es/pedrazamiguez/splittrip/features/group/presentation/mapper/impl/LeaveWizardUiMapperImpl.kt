package es.pedrazamiguez.splittrip.features.group.presentation.mapper.impl

import es.pedrazamiguez.splittrip.core.common.enums.SelfIdentificationContextEnum
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.splittrip.core.designsystem.presentation.mapper.UserUiMapper
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.model.User
import es.pedrazamiguez.splittrip.domain.service.DebtSimplificationService
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.mapper.LeaveWizardUiMapper
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveBalanceSummaryUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveSubunitImpactUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.NetPositionUiModel
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

    override fun hasUnresolvedSettlements(
        settlements: List<SettlementRecord>,
        currentUserId: String
    ): Boolean {
        return settlements.isNotEmpty()
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
            selfIdentificationContext = SelfIdentificationContextEnum.NOMINATIVE
        )
    }
}
