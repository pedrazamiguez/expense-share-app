package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel

import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupPocketBalanceFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetLastSeenBalanceUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetLastSeenBalanceUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase

/**
 * Bundles all use cases required by [BalancesViewModel] to reduce
 * constructor parameter count below the detekt `LongParameterList` threshold.
 */
data class BalancesUseCases(
    val getGroupPocketBalanceFlowUseCase: GetGroupPocketBalanceFlowUseCase,
    val getGroupContributionsFlowUseCase: GetGroupContributionsFlowUseCase,
    val getCashWithdrawalsFlowUseCase: GetCashWithdrawalsFlowUseCase,
    val getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase,
    val getMemberBalancesFlowUseCase: GetMemberBalancesFlowUseCase,
    val getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase,
    val getGroupByIdUseCase: GetGroupByIdUseCase,
    val getLastSeenBalanceUseCase: GetLastSeenBalanceUseCase,
    val setLastSeenBalanceUseCase: SetLastSeenBalanceUseCase,
    val getMemberProfilesUseCase: GetMemberProfilesUseCase
)
