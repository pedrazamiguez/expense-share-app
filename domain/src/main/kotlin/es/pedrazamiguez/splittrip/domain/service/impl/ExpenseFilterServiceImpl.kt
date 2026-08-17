package es.pedrazamiguez.splittrip.domain.service.impl

import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.domain.model.ExpenseFilterCriteria
import es.pedrazamiguez.splittrip.domain.service.ExpenseFilterService
import es.pedrazamiguez.splittrip.domain.service.ExpenseSearchService
import java.time.LocalDate

class ExpenseFilterServiceImpl(
    private val expenseSearchService: ExpenseSearchService
) : ExpenseFilterService {

    override fun filter(expenses: List<Expense>, criteria: ExpenseFilterCriteria): List<Expense> {
        if (!criteria.isActive) return expenses

        var candidateExpenses = expenses

        // 1. Text Search Filter
        if (criteria.isSearchFiltered) {
            candidateExpenses = expenseSearchService.search(candidateExpenses, criteria.searchQuery)
        }

        // 2. Category & Subcategory Filter
        if (criteria.isCategoryFiltered) {
            candidateExpenses = candidateExpenses.filter { expense ->
                matchesCategory(expense, criteria)
            }
        }

        // 3. Member / Payer Filter
        if (criteria.isMemberFiltered) {
            candidateExpenses = candidateExpenses.filter { expense ->
                matchesMember(expense, criteria.selectedMemberIds)
            }
        }

        // 4. Date Range Filter
        if (criteria.isDateFiltered) {
            candidateExpenses = candidateExpenses.filter { expense ->
                matchesDateRange(expense, criteria.startDate, criteria.endDate)
            }
        }

        return candidateExpenses
    }

    private fun matchesCategory(expense: Expense, criteria: ExpenseFilterCriteria): Boolean {
        val hasCategories = criteria.selectedCategories.isNotEmpty()
        val hasSubcategories = criteria.selectedSubcategories.isNotEmpty()

        return when {
            hasCategories && hasSubcategories -> {
                expense.category in criteria.selectedCategories ||
                    expense.subcategory in criteria.selectedSubcategories
            }
            hasCategories -> expense.category in criteria.selectedCategories
            hasSubcategories -> expense.subcategory in criteria.selectedSubcategories
            else -> true
        }
    }

    private fun matchesMember(expense: Expense, selectedMemberIds: Set<String>): Boolean {
        val isPayer = expense.payerId != null && expense.payerId in selectedMemberIds
        val isInvolvedInSplit = expense.splits.any { it.userId in selectedMemberIds && !it.isExcluded }
        return isPayer || isInvolvedInSplit
    }

    private fun matchesDateRange(
        expense: Expense,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Boolean {
        val expenseDate = expense.createdAt?.toLocalDate() ?: return false
        if (startDate != null && expenseDate.isBefore(startDate)) return false
        if (endDate != null && expenseDate.isAfter(endDate)) return false
        return true
    }
}
