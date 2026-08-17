package es.pedrazamiguez.splittrip.domain.service

import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.domain.model.ExpenseFilterCriteria

interface ExpenseFilterService {
    /**
     * Filters [expenses] against the provided [criteria].
     * Evaluates in-memory predicates:
     * - Search query (normalized title, vendor, notes via ExpenseSearchService)
     * - Categories (selectedCategories / selectedSubcategories)
     * - Members (payerId or participant in splits)
     * - Date range (createdAt.toLocalDate() in [startDate, endDate])
     */
    fun filter(expenses: List<Expense>, criteria: ExpenseFilterCriteria): List<Expense>
}
