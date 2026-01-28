package es.pedrazamiguez.expenseshareapp.domain.model

/**
 * Configuration data needed for adding an expense to a group.
 * Encapsulates all the context the Add Expense screen needs.
 */
data class GroupExpenseConfig(
    val group: Group,
    val groupCurrency: Currency,
    val availableCurrencies: List<Currency>
)
