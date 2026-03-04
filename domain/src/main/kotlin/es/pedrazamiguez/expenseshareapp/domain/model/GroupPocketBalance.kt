package es.pedrazamiguez.expenseshareapp.domain.model

/**
 * Represents the financial snapshot of a group's shared pocket.
 *
 * All monetary values are stored in minor units (cents) to avoid
 * floating-point precision issues, consistent with the Expense model.
 *
 * @param totalContributions Sum of all money added to the pocket (in cents).
 * @param totalExpenses Sum of all group expenses (in cents).
 * @param virtualBalance Net remaining virtual balance: totalContributions - totalExpenses (in cents).
 * @param currency The group's base currency code (e.g., "EUR").
 * @param cashBalances Map of currency code to remaining cash amount (in cents).
 *                     E.g., {"THB" -> 1000000, "USD" -> 5000} means 10000 THB and 50 USD in cash.
 */
data class GroupPocketBalance(
    val totalContributions: Long = 0,
    val totalExpenses: Long = 0,
    val virtualBalance: Long = 0,
    val currency: String = "EUR",
    val cashBalances: Map<String, Long> = emptyMap()
)

