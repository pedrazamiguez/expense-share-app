package es.pedrazamiguez.splittrip.domain.usecase.group.impl

import es.pedrazamiguez.splittrip.domain.enums.GroupStatus
import es.pedrazamiguez.splittrip.domain.exception.CannotLeaveGroupException
import es.pedrazamiguez.splittrip.domain.exception.GroupArchivedException
import es.pedrazamiguez.splittrip.domain.exception.UnresolvedSettlementsException
import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.repository.ExpenseRepository
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.repository.SubunitRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.AreMemberSettlementsResolvedUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.PhysicalContributionAttributionStrategy
import es.pedrazamiguez.splittrip.domain.usecase.balance.ResolveCashOnLeaveUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.LeaveGroupUseCase
import es.pedrazamiguez.splittrip.domain.usecase.subunit.ReassignSubunitSharesUseCase
import kotlinx.coroutines.flow.first

@Suppress("LongParameterList")
class LeaveGroupUseCaseImpl(
    private val groupRepository: GroupRepository,
    private val authenticationService: AuthenticationService,
    private val getSettlementSuggestionsUseCase: GetSettlementSuggestionsUseCase,
    private val areMemberSettlementsResolvedUseCase: AreMemberSettlementsResolvedUseCase,
    private val reassignSubunitSharesUseCase: ReassignSubunitSharesUseCase,
    private val expenseRepository: ExpenseRepository,
    private val contributionRepository: ContributionRepository,
    private val cashWithdrawalRepository: CashWithdrawalRepository,
    private val subunitRepository: SubunitRepository,
    private val getMemberBalancesFlowUseCase: GetMemberBalancesFlowUseCase,
    private val resolveCashOnLeaveUseCase: ResolveCashOnLeaveUseCase,
    private val settlementRepository: SettlementRepository
) : LeaveGroupUseCase {

    override suspend operator fun invoke(groupId: String): Result<Unit> = runCatching {
        val currentUserId = authenticationService.requireUserId()
        val group = groupRepository.getGroupById(groupId)
            ?: throw IllegalArgumentException("Group not found: $groupId")

        if (group.status == GroupStatus.ARCHIVED) throw GroupArchivedException(groupId)
        if (currentUserId !in
            group.members
        ) {
            throw CannotLeaveGroupException(CannotLeaveGroupException.Reason.NOT_A_MEMBER)
        }
        if (group.createdBy ==
            currentUserId
        ) {
            throw CannotLeaveGroupException(CannotLeaveGroupException.Reason.IS_CREATOR)
        }

        getSettlementSuggestionsUseCase.persistForGroup(groupId)

        val unresolvedSettlements = areMemberSettlementsResolvedUseCase(groupId, currentUserId)
        if (unresolvedSettlements.isNotEmpty()) {
            throw UnresolvedSettlementsException(groupId, unresolvedSettlements)
        }

        reassignSubunitSharesUseCase(groupId, currentUserId).getOrThrow()

        val expenses = expenseRepository.getGroupExpensesFlow(groupId).first()
        val contributions = contributionRepository.getGroupContributionsFlow(groupId).first()
        val withdrawals = cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId).first()
        val subunits = subunitRepository.getGroupSubunits(groupId)
        val settlements = settlementRepository.getGroupSettlements(groupId)

        val balances = getMemberBalancesFlowUseCase.computeMemberBalances(
            contributions = contributions,
            withdrawals = withdrawals,
            expenses = expenses,
            subunits = subunits,
            groupMemberIds = group.members,
            groupCurrency = group.currency,
            settlements = settlements,
            attributionStrategy = PhysicalContributionAttributionStrategy
        )

        val userBalance = balances.find { it.userId == currentUserId }
            ?: throw CannotLeaveGroupException(CannotLeaveGroupException.Reason.USER_NOT_IN_BALANCES)

        if (userBalance.totalBalance != 0L) {
            throw CannotLeaveGroupException(CannotLeaveGroupException.Reason.NON_ZERO_POCKET_BALANCE)
        }

        // totalBalance == 0: leaving is permitted. Create audit records for any physical cash held.
        if (userBalance.cashInHand != 0L) {
            resolveCashOnLeaveUseCase(groupId, currentUserId, userBalance, group.currency).getOrThrow()
        }

        groupRepository.leaveGroup(groupId)
    }
}
