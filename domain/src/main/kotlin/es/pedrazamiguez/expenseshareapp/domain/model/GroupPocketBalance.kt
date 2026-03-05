package es.pedrazamiguez.expenseshareapp.domain.model

/**
 * Represents the financial snapshot of a group's shared pocket.
 *
 * All monetary values are stored in minor units (cents) to avoid
 * floating-point precision issues, consistent with the Expense model.
 *
 * @param totalContributions Sum of all money added to the pocket (in cents).
 * @param totalExpenses Sum of all group expenses (in cents), including cash-paid expenses.
 * @param virtualBalance Net remaining virtual balance:
 *        totalContributions - nonCashExpenses - totalWithdrawals (in cents).
 *        Cash-paid expenses are excluded because they are funded from the physical
 *        cash pocket (already deducted via withdrawals).
 * @param currency The group's base currency code (e.g., "EUR").
 * @param cashBalances Map of currency code to remaining cash amount (in cents).
 *                     E.g., {"THB" -> 1000000, "USD" -> 5000} means 10000 THB and 50 USD in cash.
 * @param cashEquivalents Map of foreign currency code to approximate equivalent in group currency (in cents).
 *                        Computed proportionally from the remaining/original ratio of each withdrawal.
 * @param totalCashEquivalent Total remaining cash across all currencies, expressed in the group's
 *                            base currency (in cents). Includes base-currency cash at face value
 *                            plus foreign cash converted proportionally.
 */
data class GroupPocketBalance(
    val totalContributions: Long = 0,
    val totalExpenses: Long = 0,
    val virtualBalance: Long = 0,
    val currency: String = "EUR",
    val cashBalances: Map<String, Long> = emptyMap(),
    val cashEquivalents: Map<String, Long> = emptyMap(),
    val totalCashEquivalent: Long = 0
)

