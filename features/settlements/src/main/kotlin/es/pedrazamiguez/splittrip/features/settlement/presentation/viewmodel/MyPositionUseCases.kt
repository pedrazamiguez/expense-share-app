package es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel

import es.pedrazamiguez.splittrip.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupSettlementsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.splittrip.domain.usecase.subunit.GetGroupSubunitsFlowUseCase

data class MyPositionUseCases(
    val getGroupByIdUseCase: GetGroupByIdUseCase,
    val getGroupContributionsFlowUseCase: GetGroupContributionsFlowUseCase,
    val getCashWithdrawalsFlowUseCase: GetCashWithdrawalsFlowUseCase,
    val getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase,
    val getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase,
    val getMemberBalancesFlowUseCase: GetMemberBalancesFlowUseCase,
    val getGroupSettlementsFlowUseCase: GetGroupSettlementsFlowUseCase
)
