package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.CurrencyAmount
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.model.MemberBalance
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
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
 *   Split into CASH vs non-CASH by [Expense.paymentMethod].
 *   **Add-ons (fees, tips, surcharges) increase the effective group amount.**
 *
 * Financial model per member (mirrors group-level exactly):
 * - pocketBalance = contributed − withdrawn − nonCashSpent
 * - cashInHand = withdrawn − cashSpent
 * - totalSpent = cashSpent + nonCashSpent
 *
 * Per-currency breakdowns track native amounts and group-currency equivalents
 * for cashInHand, cashSpent, and nonCashSpent, enabling the UI to display
 * multi-currency detail in the expanded member card.
 */
class GetMemberBalancesFlowUseCase(
    private val expenseCalculatorService: ExpenseCalculatorService = ExpenseCalculatorService()
) {

    fun computeMemberBalances(
        contributions: List<Contribution>,
        withdrawals: List<CashWithdrawal>,
        expenses: List<Expense>,
        subunits: List<Subunit>,
        groupMemberIds: List<String>,
        groupCurrency: String = ""
    ): List<MemberBalance> {
        val subunitMap = subunits.associateBy { it.id }

        // 1. Attribute contributions
        val contributedMap = attributeContributions(contributions, subunitMap, groupMemberIds)

        // 2. Attribute withdrawals (group currency totals + per-currency native amounts)
        val withdrawalResult = attributeWithdrawals(withdrawals, subunitMap, groupMemberIds)

        // 3. Sum expense splits per user, separated by payment method (+ per-currency tracking)
        val expenseResult = attributeExpensesByPaymentMethod(expenses)

        // 4. Build MemberBalance for every group member (including those with all zeroes)
        val allUserIds = buildSet {
            addAll(groupMemberIds)
            addAll(contributedMap.keys)
            addAll(withdrawalResult.groupCurrencyMap.keys)
            addAll(expenseResult.cashSpentMap.keys)
            addAll(expenseResult.nonCashSpentMap.keys)
        }

        return allUserIds.map { userId ->
            val contributed = contributedMap[userId] ?: 0L
            val withdrawn = withdrawalResult.groupCurrencyMap[userId] ?: 0L
            val cashSpent = expenseResult.cashSpentMap[userId] ?: 0L
            val nonCashSpent = expenseResult.nonCashSpentMap[userId] ?: 0L
            val totalSpent = cashSpent + nonCashSpent

            // Per-currency breakdowns
            val withdrawnByCurrency = withdrawalResult.byCurrency[userId] ?: emptyMap()
            val cashSpentByCurrency = expenseResult.cashSpentByCurrency[userId] ?: emptyMap()
            val nonCashSpentByCurrency = expenseResult.nonCashSpentByCurrency[userId] ?: emptyMap()
            val cashEquivByCurrency = expenseResult.cashEquivByCurrency[userId] ?: emptyMap()
            val nonCashEquivByCurrency = expenseResult.nonCashEquivByCurrency[userId] ?: emptyMap()

            val cashInHandByCurrencyList = buildCashInHandByCurrency(
                withdrawnByCurrency = withdrawnByCurrency,
                cashSpentByCurrency = cashSpentByCurrency,
                groupCurrency = groupCurrency
            )

            val cashSpentByCurrencyList = buildCurrencyAmountList(
                byCurrencyMap = cashSpentByCurrency,
                equivByCurrency = cashEquivByCurrency,
                groupCurrency = groupCurrency
            )

            val nonCashSpentByCurrencyList = buildCurrencyAmountList(
                byCurrencyMap = nonCashSpentByCurrency,
                equivByCurrency = nonCashEquivByCurrency,
                groupCurrency = groupCurrency
            )

            MemberBalance(
                userId = userId,
                contributed = contributed,
                withdrawn = withdrawn,
                cashSpent = cashSpent,
                nonCashSpent = nonCashSpent,
                totalSpent = totalSpent,
                pocketBalance = contributed - withdrawn - nonCashSpent,
                cashInHand = withdrawn - cashSpent,
                cashInHandByCurrency = cashInHandByCurrencyList,
                cashSpentByCurrency = cashSpentByCurrencyList,
                nonCashSpentByCurrency = nonCashSpentByCurrencyList
            )
        }
    }

    /**
     * Distributes contribution amounts to individual members.
     *
     * - GROUP contributions → equal split among all group members.
     * - SUBUNIT contributions → distribute by member shares with remainder allocation.
     * - USER contributions (individual) → full amount to userId.
     */
    private fun attributeContributions(
        contributions: List<Contribution>,
        subunitMap: Map<String, Subunit>,
        groupMemberIds: List<String>
    ): Map<String, Long> {
        val result = mutableMapOf<String, Long>()

        for (contribution in contributions) {
            val distributions: Map<String, Long> = when (contribution.contributionScope) {
                PayerType.GROUP -> distributeEvenly(contribution.amount, groupMemberIds)
                PayerType.SUBUNIT -> {
                    val subunit = subunitMap[contribution.subunitId]
                    if (subunit == null || subunit.memberShares.isEmpty()) {
                        // Fallback: attribute to the contributor if subunit not found
                        mapOf(contribution.userId to contribution.amount)
                    } else {
                        distributeByShares(contribution.amount, subunit.memberShares)
                    }
                }
                PayerType.USER -> mapOf(contribution.userId to contribution.amount)
            }

            for ((userId, amount) in distributions) {
                result[userId] = (result[userId] ?: 0L) + amount
            }
        }

        return result
    }

    /**
     * Distributes withdrawal deducted amounts to individual members based on scope,
     * tracking both group-currency totals and per-currency native amounts.
     *
     * - GROUP → equal split among all group members.
     * - SUBUNIT → distribute by memberShares.
     * - USER → full amount to withdrawnBy.
     *
     * **Add-ons (ATM fees) increase the effective deducted amount** via
     * [ExpenseCalculatorService.calculateEffectiveDeductedAmount].
     *
     * @return [WithdrawalResult] containing group-currency map and per-member per-currency breakdown.
     */
    private fun attributeWithdrawals(
        withdrawals: List<CashWithdrawal>,
        subunitMap: Map<String, Subunit>,
        groupMemberIds: List<String>
    ): WithdrawalResult {
        val groupCurrencyResult = mutableMapOf<String, Long>()
        // userId → currency → WithdrawalCurrencyAttribution
        val byCurrency = mutableMapOf<String, MutableMap<String, WithdrawalCurrencyAttribution>>()

        for (withdrawal in withdrawals) {
            // Use effective deducted amount that includes ATM fee add-ons
            val effectiveDeducted = expenseCalculatorService.calculateEffectiveDeductedAmount(
                withdrawal.deductedBaseAmount,
                withdrawal.addOns
            )

            val distributions: Map<String, Long> = when (withdrawal.withdrawalScope) {
                PayerType.GROUP -> distributeEvenly(effectiveDeducted, groupMemberIds)
                PayerType.SUBUNIT -> {
                    val subunit = subunitMap[withdrawal.subunitId]
                    if (subunit == null || subunit.memberShares.isEmpty()) {
                        mapOf(withdrawal.withdrawnBy to effectiveDeducted)
                    } else {
                        distributeByShares(effectiveDeducted, subunit.memberShares)
                    }
                }
                PayerType.USER -> mapOf(withdrawal.withdrawnBy to effectiveDeducted)
            }

            // Also distribute native amounts using the same proportion
            val nativeDistributions: Map<String, Long> = when (withdrawal.withdrawalScope) {
                PayerType.GROUP -> distributeEvenly(withdrawal.amountWithdrawn, groupMemberIds)
                PayerType.SUBUNIT -> {
                    val subunit = subunitMap[withdrawal.subunitId]
                    if (subunit == null || subunit.memberShares.isEmpty()) {
                        mapOf(withdrawal.withdrawnBy to withdrawal.amountWithdrawn)
                    } else {
                        distributeByShares(withdrawal.amountWithdrawn, subunit.memberShares)
                    }
                }
                PayerType.USER -> mapOf(withdrawal.withdrawnBy to withdrawal.amountWithdrawn)
            }

            // Accumulate group-currency totals
            for ((userId, amount) in distributions) {
                groupCurrencyResult[userId] = (groupCurrencyResult[userId] ?: 0L) + amount
            }

            // Accumulate per-currency native + group-equivalent amounts
            for ((userId, nativeAmount) in nativeDistributions) {
                val groupEquivalent = distributions[userId] ?: 0L
                val userMap = byCurrency.getOrPut(userId) { mutableMapOf() }
                val existing = userMap[withdrawal.currency]
                if (existing != null) {
                    userMap[withdrawal.currency] = WithdrawalCurrencyAttribution(
                        nativeAmount = existing.nativeAmount + nativeAmount,
                        groupEquivalent = existing.groupEquivalent + groupEquivalent
                    )
                } else {
                    userMap[withdrawal.currency] = WithdrawalCurrencyAttribution(
                        nativeAmount = nativeAmount,
                        groupEquivalent = groupEquivalent
                    )
                }
            }
        }

        return WithdrawalResult(
            groupCurrencyMap = groupCurrencyResult,
            byCurrency = byCurrency
        )
    }

    /**
     * Sums expense split amounts per user, separated by payment method (CASH vs non-CASH),
     * tracking both group-currency totals and per-source-currency native amounts.
     *
     * Expense splits store `amountCents` in the **source currency**
     * (e.g., THB for a Thai expense). For balance calculation we need amounts in
     * the group's base currency (e.g., EUR). Each expense carries its own
     * [Expense.sourceAmount] and [Expense.groupAmount] which serve as the
     * conversion bridge.
     *
     * **Add-ons (fees, tips, surcharges) are included via [ExpenseCalculatorService.calculateEffectiveGroupAmount].**
     * The effective group amount is used for conversion instead of the raw groupAmount,
     * ensuring that balances reflect the full cost including add-ons.
     *
     * @return [ExpenseResult] containing per-user group-currency maps and per-currency breakdowns
     *         (both native amounts and their exact group-currency equivalents).
     */
    private fun attributeExpensesByPaymentMethod(
        expenses: List<Expense>
    ): ExpenseResult {
        val cashResult = mutableMapOf<String, Long>()
        val nonCashResult = mutableMapOf<String, Long>()
        // userId → sourceCurrency → native amountCents
        val cashByCurrency = mutableMapOf<String, MutableMap<String, Long>>()
        val nonCashByCurrency = mutableMapOf<String, MutableMap<String, Long>>()
        // userId → sourceCurrency → group-currency equivalent (exact per-user sum)
        val cashEquivByCurrency = mutableMapOf<String, MutableMap<String, Long>>()
        val nonCashEquivByCurrency = mutableMapOf<String, MutableMap<String, Long>>()

        for (expense in expenses) {
            val isCash = expense.paymentMethod == PaymentMethod.CASH
            val targetMap = if (isCash) cashResult else nonCashResult
            val targetByCurrency = if (isCash) cashByCurrency else nonCashByCurrency
            val targetEquivByCurrency = if (isCash) cashEquivByCurrency else nonCashEquivByCurrency

            // Use effective group amount (including add-ons) for conversion
            val effectiveGroupAmount = expenseCalculatorService.calculateEffectiveGroupAmount(
                expense.groupAmount,
                expense.addOns
            )

            for (split in expense.splits) {
                if (!split.isExcluded) {
                    val spentInGroupCurrency = convertSplitToGroupCurrency(
                        splitAmountCents = split.amountCents,
                        sourceAmount = expense.sourceAmount,
                        groupAmount = effectiveGroupAmount
                    )
                    targetMap[split.userId] = (targetMap[split.userId] ?: 0L) + spentInGroupCurrency

                    // Track per-currency native amounts
                    val userCurrencyMap = targetByCurrency.getOrPut(split.userId) { mutableMapOf() }
                    userCurrencyMap[expense.sourceCurrency] =
                        (userCurrencyMap[expense.sourceCurrency] ?: 0L) + split.amountCents

                    // Track per-currency group-equivalent amounts (exact per-user)
                    val userEquivMap = targetEquivByCurrency.getOrPut(split.userId) { mutableMapOf() }
                    userEquivMap[expense.sourceCurrency] =
                        (userEquivMap[expense.sourceCurrency] ?: 0L) + spentInGroupCurrency
                }
            }
        }

        return ExpenseResult(
            cashSpentMap = cashResult,
            nonCashSpentMap = nonCashResult,
            cashSpentByCurrency = cashByCurrency,
            nonCashSpentByCurrency = nonCashByCurrency,
            cashEquivByCurrency = cashEquivByCurrency,
            nonCashEquivByCurrency = nonCashEquivByCurrency
        )
    }

    /**
     * Builds per-currency [CurrencyAmount] list for cash in hand (withdrawn − cashSpent per currency).
     * Filters out currencies with zero or negative remaining amounts.
     * Equivalents are computed proportionally from the withdrawal's deducted-to-withdrawn ratio.
     */
    private fun buildCashInHandByCurrency(
        withdrawnByCurrency: Map<String, WithdrawalCurrencyAttribution>,
        cashSpentByCurrency: Map<String, Long>,
        groupCurrency: String
    ): List<CurrencyAmount> {
        return withdrawnByCurrency.mapNotNull { (currency, attribution) ->
            val spent = cashSpentByCurrency[currency] ?: 0L
            val remaining = attribution.nativeAmount - spent
            if (remaining <= 0) return@mapNotNull null

            val equivalent = if (currency == groupCurrency) {
                remaining
            } else if (attribution.nativeAmount > 0) {
                // Proportional equivalent: (remaining / nativeTotal) * groupEquivalent
                BigDecimal(remaining)
                    .multiply(BigDecimal(attribution.groupEquivalent))
                    .divide(BigDecimal(attribution.nativeAmount), 0, RoundingMode.HALF_UP)
                    .toLong()
            } else {
                0L
            }

            CurrencyAmount(
                currency = currency,
                amountCents = remaining,
                equivalentCents = equivalent
            )
        }.sortedBy { it.currency }
    }

    /**
     * Builds per-currency [CurrencyAmount] list for expense breakdowns.
     * Uses the exact per-user/per-currency group equivalents computed during attribution,
     * avoiding the need for derived ratios.
     */
    private fun buildCurrencyAmountList(
        byCurrencyMap: Map<String, Long>,
        equivByCurrency: Map<String, Long>,
        groupCurrency: String
    ): List<CurrencyAmount> {
        if (byCurrencyMap.isEmpty()) return emptyList()

        return byCurrencyMap.map { (currency, nativeAmountCents) ->
            val equivalent = if (currency == groupCurrency) {
                nativeAmountCents
            } else {
                equivByCurrency[currency] ?: 0L
            }
            CurrencyAmount(
                currency = currency,
                amountCents = nativeAmountCents,
                equivalentCents = equivalent
            )
        }.sortedBy { it.currency }
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

    // ── Internal data classes ─────────────────────────────────────────────

    /** Per-currency withdrawal attribution for a single member in a single currency. */
    private data class WithdrawalCurrencyAttribution(
        val nativeAmount: Long,
        val groupEquivalent: Long
    )

    /** Result of [attributeWithdrawals]. */
    private data class WithdrawalResult(
        val groupCurrencyMap: Map<String, Long>,
        val byCurrency: Map<String, Map<String, WithdrawalCurrencyAttribution>>
    )

    /** Result of [attributeExpensesByPaymentMethod]. */
    private data class ExpenseResult(
        val cashSpentMap: Map<String, Long>,
        val nonCashSpentMap: Map<String, Long>,
        /** userId → sourceCurrency → native amountCents */
        val cashSpentByCurrency: Map<String, Map<String, Long>>,
        val nonCashSpentByCurrency: Map<String, Map<String, Long>>,
        /** userId → sourceCurrency → exact group-currency equivalent */
        val cashEquivByCurrency: Map<String, Map<String, Long>>,
        val nonCashEquivByCurrency: Map<String, Map<String, Long>>
    )

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
