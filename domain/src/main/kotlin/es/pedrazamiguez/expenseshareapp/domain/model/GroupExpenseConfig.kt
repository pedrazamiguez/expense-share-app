package es.pedrazamiguez.expenseshareapp.domain.model

/**
 * Configuration data needed for adding an expense to a group.
 * Encapsulates all the context the Add Expense screen needs.
 *
 * @param subunits Sub-units defined in the group. When non-empty, the split editor
 *                 can offer a "Split by sub-unit" mode alongside the standard flat split.
 */
data class GroupExpenseConfig(
    val group: Group,
    val groupCurrency: Currency,
    val availableCurrencies: List<Currency>,
    val subunits: List<Subunit> = emptyList()
)
