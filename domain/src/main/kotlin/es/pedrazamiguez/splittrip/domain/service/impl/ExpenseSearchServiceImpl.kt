package es.pedrazamiguez.splittrip.domain.service.impl

import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.domain.service.ExpenseSearchService

class ExpenseSearchServiceImpl : ExpenseSearchService {

    override fun search(expenses: List<Expense>, query: String): List<Expense> {
        if (query.isBlank()) return expenses
        val queryTrimmed = query.trim()
        return expenses.filter { expense ->
            expense.title.contains(queryTrimmed, ignoreCase = true) ||
                (expense.notes?.contains(queryTrimmed, ignoreCase = true) == true)
        }
    }
}
