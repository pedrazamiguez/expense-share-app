package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel

import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.DeleteExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase

/**
 * Bundles all use cases required by [ExpensesViewModel] to reduce
 * constructor parameter count below the detekt `LongParameterList` threshold.
 */
data class ExpensesUseCases(
    val getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase,
    val deleteExpenseUseCase: DeleteExpenseUseCase,
    val getGroupByIdUseCase: GetGroupByIdUseCase,
    val getMemberProfilesUseCase: GetMemberProfilesUseCase,
    val getGroupContributionsFlowUseCase: GetGroupContributionsFlowUseCase,
    val getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase
)
