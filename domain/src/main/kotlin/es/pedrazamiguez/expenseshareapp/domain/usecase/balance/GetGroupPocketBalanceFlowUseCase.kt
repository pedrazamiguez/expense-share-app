package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.model.GroupPocketBalance
import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Combines contributions and expenses flows for a group to compute the
 * real-time pocket balance.
 *
 * Balance = Sum(contributions.amount) - Sum(expenses.groupAmount)
 */
class GetGroupPocketBalanceFlowUseCase(
    private val contributionRepository: ContributionRepository,
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(groupId: String, currency: String): Flow<GroupPocketBalance> =
        combine(
            contributionRepository.getGroupContributionsFlow(groupId),
            expenseRepository.getGroupExpensesFlow(groupId)
        ) { contributions, expenses ->
            val totalContributions = contributions.sumOf { it.amount }
            val totalExpenses = expenses.sumOf { it.groupAmount }
            GroupPocketBalance(
                totalContributions = totalContributions,
                totalExpenses = totalExpenses,
                balance = totalContributions - totalExpenses,
                currency = currency
            )
        }
}

