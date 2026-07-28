package es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel

import es.pedrazamiguez.splittrip.domain.usecase.balance.ConfirmSettlementUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.DisputeSettlementUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupSettlementsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
import es.pedrazamiguez.splittrip.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.splittrip.domain.usecase.settlement.GetNudgeTimestampsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.settlement.NudgeDebtorUseCase
import es.pedrazamiguez.splittrip.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.user.GetMemberProfilesUseCase

data class YourPositionUseCases(
    val getGroupByIdUseCase: GetGroupByIdUseCase,
    val getGroupContributionsFlowUseCase: GetGroupContributionsFlowUseCase,
    val getCashWithdrawalsFlowUseCase: GetCashWithdrawalsFlowUseCase,
    val getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase,
    val getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase,
    val getMemberBalancesFlowUseCase: GetMemberBalancesFlowUseCase,
    val getGroupSettlementsFlowUseCase: GetGroupSettlementsFlowUseCase,
    val confirmSettlementUseCase: ConfirmSettlementUseCase,
    val disputeSettlementUseCase: DisputeSettlementUseCase,
    val getMemberProfilesUseCase: GetMemberProfilesUseCase,
    val getSettlementSuggestionsUseCase: GetSettlementSuggestionsUseCase,
    val nudgeDebtorUseCase: NudgeDebtorUseCase,
    val getNudgeTimestampsFlowUseCase: GetNudgeTimestampsFlowUseCase
)
