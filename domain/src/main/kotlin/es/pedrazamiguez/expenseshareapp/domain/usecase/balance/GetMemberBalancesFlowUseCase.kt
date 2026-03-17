package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.model.MemberBalance
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Pure computation class that derives per-member financial balances from
 * pre-loaded domain data. Does NOT subscribe to repository flows — the caller
 * (typically ViewModel) is responsible for collecting the data streams and
 * passing them in, avoiding duplicate Firestore snapshot listeners.
 *
 * Attribution rules:
 * - **Contributions:** Individual → full amount to userId.
 *   Sub-unit → distributed among sub-unit members by [Subunit.memberShares].
 * - **Withdrawals:** GROUP → equal split among all group members.
 *   SUBUNIT → distributed by memberShares. USER → full amount to withdrawnBy.
 * - **Expenses:** Already per-user via [es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit].
 *
 * Net balance per member = contributed − withdrawn (pocket share).
 * Available cash per member = withdrawn − spent.
 */
class GetMemberBalancesFlowUseCase {

    fun computeMemberBalances(
        contributions: List<Contribution>,
        withdrawals: List<CashWithdrawal>,
        expenses: List<Expense>,
        subunits: List<Subunit>,
        groupMemberIds: List<String>
    ): List<MemberBalance> {
        val subunitMap = subunits.associateBy { it.id }

        // 1. Attribute contributions
        val contributedMap = attributeContributions(contributions, subunitMap)

        // 2. Attribute withdrawals
        val withdrawnMap = attributeWithdrawals(withdrawals, subunitMap, groupMemberIds)

        // 3. Sum expense splits per user (converted from source to group currency here)
        val spentMap = attributeExpenses(expenses)

        // 4. Build MemberBalance for every group member (including those with all zeroes)
        val allUserIds = buildSet {
            addAll(groupMemberIds)
            addAll(contributedMap.keys)
            addAll(withdrawnMap.keys)
            addAll(spentMap.keys)
        }

        return allUserIds.map { userId ->
            val contributed = contributedMap[userId] ?: 0L
            val withdrawn = withdrawnMap[userId] ?: 0L
            val spent = spentMap[userId] ?: 0L
            MemberBalance(
                userId = userId,
                contributed = contributed,
                withdrawn = withdrawn,
                spent = spent,
                available = withdrawn - spent,
                netBalance = contributed - withdrawn
            )
        }
    }

    /**
     * Distributes contribution amounts to individual members.
     *
     * - Individual contributions (subunitId == null) → full amount to userId.
     * - Sub-unit contributions → distribute by member shares with remainder allocation.
     */
    private fun attributeContributions(
        contributions: List<Contribution>,
        subunitMap: Map<String, Subunit>
    ): Map<String, Long> {
        val result = mutableMapOf<String, Long>()

        for (contribution in contributions) {
            if (contribution.subunitId == null) {
                // Individual contribution
                result[contribution.userId] = (result[contribution.userId] ?: 0L) + contribution.amount
            } else {
                // Sub-unit contribution — distribute by member shares
                val subunit = subunitMap[contribution.subunitId]
                if (subunit == null || subunit.memberShares.isEmpty()) {
                    // Fallback: attribute to the contributor if subunit not found
                    result[contribution.userId] =
                        (result[contribution.userId] ?: 0L) + contribution.amount
                } else {
                    distributeByShares(contribution.amount, subunit.memberShares).forEach { (userId, amount) ->
                        result[userId] = (result[userId] ?: 0L) + amount
                    }
                }
            }
        }

        return result
    }

    /**
     * Distributes withdrawal deducted amounts to individual members based on scope.
     *
     * - GROUP → equal split among all group members.
     * - SUBUNIT → distribute by memberShares.
     * - USER → full amount to withdrawnBy.
     */
    private fun attributeWithdrawals(
        withdrawals: List<CashWithdrawal>,
        subunitMap: Map<String, Subunit>,
        groupMemberIds: List<String>
    ): Map<String, Long> {
        val result = mutableMapOf<String, Long>()

        for (withdrawal in withdrawals) {
            when (withdrawal.withdrawalScope) {
                PayerType.GROUP -> {
                    distributeEvenly(withdrawal.deductedBaseAmount, groupMemberIds).forEach { (userId, amount) ->
                        result[userId] = (result[userId] ?: 0L) + amount
                    }
                }

                PayerType.SUBUNIT -> {
                    val subunit = subunitMap[withdrawal.subunitId]
                    if (subunit == null || subunit.memberShares.isEmpty()) {
                        // Fallback: attribute to the withdrawer if subunit not found
                        result[withdrawal.withdrawnBy] =
                            (result[withdrawal.withdrawnBy] ?: 0L) + withdrawal.deductedBaseAmount
                    } else {
                        distributeByShares(withdrawal.deductedBaseAmount, subunit.memberShares)
                            .forEach { (userId, amount) ->
                                result[userId] = (result[userId] ?: 0L) + amount
                            }
                    }
                }

                PayerType.USER -> {
                    result[withdrawal.withdrawnBy] =
                        (result[withdrawal.withdrawnBy] ?: 0L) + withdrawal.deductedBaseAmount
                }
            }
        }

        return result
    }

    /**
     * Sums expense split amounts per user from all expenses, converting from
     * source currency to group currency.
     *
     * Expense splits store `amountCents` in the **source currency**
     * (e.g., THB for a Thai expense). For balance calculation we need amounts in
     * the group's base currency (e.g., EUR). Each expense carries its own
     * [Expense.sourceAmount] and [Expense.groupAmount] which serve as the
     * conversion bridge.
     *
     * Same-currency expenses (sourceAmount == groupAmount) pass through unchanged.
     */
    private fun attributeExpenses(expenses: List<Expense>): Map<String, Long> {
        val result = mutableMapOf<String, Long>()

        for (expense in expenses) {
            for (split in expense.splits) {
                if (!split.isExcluded) {
                    val spentInGroupCurrency = convertSplitToGroupCurrency(
                        splitAmountCents = split.amountCents,
                        sourceAmount = expense.sourceAmount,
                        groupAmount = expense.groupAmount
                    )
                    result[split.userId] = (result[split.userId] ?: 0L) + spentInGroupCurrency
                }
            }
        }

        return result
    }

    /**
     * Converts a split amount from source currency to group currency.
     *
     * When the expense is in the group currency ([sourceAmount] == [groupAmount]),
     * this is a no-op. For multi-currency expenses, it proportionally scales
     * the split amount using the expense's pre-computed exchange:
     *
     * `splitGroupAmount = splitAmountCents × groupAmount ÷ sourceAmount`
     *
     * Uses [BigDecimal] with [RoundingMode.HALF_UP] to avoid floating-point drift.
     */
    private fun convertSplitToGroupCurrency(
        splitAmountCents: Long,
        sourceAmount: Long,
        groupAmount: Long
    ): Long {
        if (sourceAmount == 0L) return 0L
        if (sourceAmount == groupAmount) return splitAmountCents
        return BigDecimal(splitAmountCents)
            .multiply(BigDecimal(groupAmount))
            .divide(BigDecimal(sourceAmount), 0, RoundingMode.HALF_UP)
            .toLong()
    }

    companion object {

        /**
         * Distributes a total amount (in cents) among members according to their
         * [BigDecimal] share weights. Uses DOWN rounding per member and allocates
         * the remainder (1 cent at a time) in a deterministic round-robin over
         * members sorted by userId.
         *
         * This guarantees: sum of distributed amounts == totalAmount (no cents lost),
         * even when share weights sum to slightly less than 1.0 (within validation
         * tolerance) which may cause a remainder larger than the member count.
         */
        internal fun distributeByShares(
            totalAmount: Long,
            memberShares: Map<String, BigDecimal>
        ): Map<String, Long> {
            if (memberShares.isEmpty()) return emptyMap()

            // Sort keys for deterministic remainder allocation across runs/devices
            val sortedKeys = memberShares.keys.sorted()
            val totalBd = BigDecimal(totalAmount)
            val distributed = mutableMapOf<String, Long>()
            var allocated = 0L

            // First pass: floor each member's share
            for (userId in sortedKeys) {
                val share = memberShares[userId] ?: BigDecimal.ZERO
                val memberAmount = totalBd
                    .multiply(share)
                    .setScale(0, RoundingMode.DOWN)
                    .toLong()
                distributed[userId] = memberAmount
                allocated += memberAmount
            }

            // Second pass: round-robin remainder allocation (1 cent per member until exhausted)
            var remainder = totalAmount - allocated
            var index = 0
            while (remainder > 0) {
                val userId = sortedKeys[index % sortedKeys.size]
                distributed[userId] = (distributed[userId] ?: 0L) + 1
                remainder--
                index++
            }

            return distributed
        }

        /**
         * Distributes a total amount (in cents) equally among members.
         * Uses integer division with remainder allocated 1 cent at a time.
         */
        internal fun distributeEvenly(
            totalAmount: Long,
            memberIds: List<String>
        ): Map<String, Long> {
            if (memberIds.isEmpty()) return emptyMap()

            val perMember = totalAmount / memberIds.size
            var remainder = totalAmount - (perMember * memberIds.size)

            return memberIds.associateWith { _ ->
                val amount = if (remainder > 0) {
                    remainder--
                    perMember + 1
                } else {
                    perMember
                }
                amount
            }
        }
    }
}
