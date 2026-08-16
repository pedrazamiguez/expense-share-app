package es.pedrazamiguez.splittrip.domain.service

import es.pedrazamiguez.splittrip.domain.model.Expense

/**
 * Service for filtering and searching expenses by matching queries against
 * expense properties (e.g. title, vendor, notes).
 */
interface ExpenseSearchService {

    /**
     * Filters the given list of [Expense] items matching the provided search [query].
     *
     * If [query] is blank, returns the original [expenses] list unchanged.
     * Otherwise, performs a case-insensitive search matching against expense title, vendor, and notes.
     *
     * @param expenses The list of domain expenses to search within.
     * @param query The search query string.
     * @return Filtered list of expenses matching the query.
     */
    fun search(expenses: List<Expense>, query: String): List<Expense>
}
