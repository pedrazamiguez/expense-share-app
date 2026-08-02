package es.pedrazamiguez.splittrip.domain.service.impl

import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.service.DebtSimplificationService
import es.pedrazamiguez.splittrip.domain.service.RemainderDistributionService
import java.math.BigDecimal
import kotlin.math.min

class DebtSimplificationServiceImpl(
    private val remainderDistributionService: RemainderDistributionService
) : DebtSimplificationService {
    override fun simplify(memberBalances: List<MemberBalance>): List<Settlement> =
        runGreedyAlgorithm(
            balances = memberBalances.map { it.userId to it.totalBalance },
            sourcePocket = SettlementPocketType.NET,
            currency = ""
        )

    override fun simplifyByPocket(
        memberBalances: List<MemberBalance>,
        groupCurrency: String
    ): List<Settlement> =
        runGreedyAlgorithm(
            balances = memberBalances.map { it.userId to it.pocketBalance },
            sourcePocket = SettlementPocketType.POCKET,
            currency = groupCurrency
        ) + buildCashSettlements(memberBalances, groupCurrency)

    private fun buildCashSettlements(
        memberBalances: List<MemberBalance>,
        groupCurrency: String
    ): List<Settlement> {
        val cashCurrencies = memberBalances
            .flatMap { mb ->
                mb.withdrawnByCurrency.map { it.currency } +
                    mb.cashSpentByCurrency.map { it.currency } +
                    mb.cashInHandByCurrency.map { it.currency }
            }
            .distinct()

        return if (cashCurrencies.isEmpty()) {
            val balances = memberBalances.map { mb ->
                mb.userId to (mb.withdrawn - mb.cashSpent)
            }
            val weights = memberBalances.associate { mb -> mb.userId to mb.withdrawn }
            runGreedyAlgorithm(
                balances = scaleBalancesForCash(balances, weights),
                sourcePocket = SettlementPocketType.CASH,
                currency = groupCurrency
            )
        } else {
            cashCurrencies.flatMap { currencyCode ->
                val balances = memberBalances.map { mb ->
                    val withdrawn = mb.withdrawnByCurrency
                        .find { it.currency == currencyCode }?.amountCents ?: 0L
                    val spent = mb.cashSpentByCurrency
                        .find { it.currency == currencyCode }?.amountCents ?: 0L
                    mb.userId to (withdrawn - spent)
                }
                val weights = memberBalances.associate { mb ->
                    val withdrawn = mb.withdrawnByCurrency
                        .find { it.currency == currencyCode }?.amountCents ?: 0L
                    mb.userId to withdrawn
                }
                runGreedyAlgorithm(
                    balances = scaleBalancesForCash(balances, weights),
                    sourcePocket = SettlementPocketType.CASH,
                    currency = currencyCode
                )
            }
        }
    }

    private fun scaleBalancesForCash(
        balances: List<Pair<String, Long>>,
        weights: Map<String, Long>
    ): List<Pair<String, Long>> {
        val debtors = balances.filter { it.second < 0L }
        val creditors = balances.filter { it.second > 0L }

        val totalDebt = debtors.sumOf { -it.second }
        val totalCredit = creditors.sumOf { it.second }

        if (totalDebt == 0L || totalCredit == 0L) {
            return balances
        }

        val assignedDebts = creditors.associate { it.first to 0L }.toMutableMap()
        var remainingDebtToDistribute = totalDebt

        while (remainingDebtToDistribute > 0L) {
            val debtDistributedInThisRound = distributeRound(
                remainingDebt = remainingDebtToDistribute,
                creditors = creditors,
                assignedDebts = assignedDebts,
                weights = weights
            )
            if (debtDistributedInThisRound == 0L) {
                break
            }
            remainingDebtToDistribute -= debtDistributedInThisRound
        }

        val scaledCreditors = creditors.map {
            it.first to (assignedDebts[it.first] ?: 0L)
        }

        return debtors + scaledCreditors
    }

    private fun distributeRound(
        remainingDebt: Long,
        creditors: List<Pair<String, Long>>,
        assignedDebts: MutableMap<String, Long>,
        weights: Map<String, Long>
    ): Long {
        val activeCreditors = creditors.filter {
            val assigned = assignedDebts[it.first] ?: 0L
            assigned < it.second
        }

        if (activeCreditors.isEmpty()) return 0L

        val activeWeights = activeCreditors.map { BigDecimal(weights[it.first] ?: 0L) }
        val activeSum = activeWeights.fold(BigDecimal.ZERO, BigDecimal::add)

        return if (activeSum.compareTo(BigDecimal.ZERO) == 0) {
            distributeEqually(remainingDebt, activeCreditors, assignedDebts)
        } else {
            distributeProportionally(remainingDebt, activeCreditors, assignedDebts, activeWeights)
        }
    }

    private fun distributeEqually(
        remainingDebt: Long,
        activeCreditors: List<Pair<String, Long>>,
        assignedDebts: MutableMap<String, Long>
    ): Long {
        val share = remainingDebt / activeCreditors.size
        var remainder = remainingDebt % activeCreditors.size
        var distributed = 0L

        for (creditor in activeCreditors) {
            val assigned = assignedDebts[creditor.first] ?: 0L
            var amountToAdd = share + if (remainder > 0) 1 else 0
            if (remainder > 0) remainder--

            val maxToAdd = creditor.second - assigned
            if (amountToAdd > maxToAdd) {
                amountToAdd = maxToAdd
            }

            assignedDebts[creditor.first] = assigned + amountToAdd
            distributed += amountToAdd
        }
        return distributed
    }

    private fun distributeProportionally(
        remainingDebt: Long,
        activeCreditors: List<Pair<String, Long>>,
        assignedDebts: MutableMap<String, Long>,
        activeWeights: List<BigDecimal>
    ): Long {
        val proposedDistribution = remainderDistributionService.distributeByWeights(
            total = remainingDebt,
            weights = activeWeights
        )
        var distributed = 0L

        activeCreditors.forEachIndexed { index, creditor ->
            val proposedAmount = proposedDistribution[index]
            if (proposedAmount > 0) {
                val assigned = assignedDebts[creditor.first] ?: 0L
                val maxToAdd = creditor.second - assigned
                val amountToAdd = if (proposedAmount > maxToAdd) maxToAdd else proposedAmount
                assignedDebts[creditor.first] = assigned + amountToAdd
                distributed += amountToAdd
            }
        }
        return distributed
    }

    private fun runGreedyAlgorithm(
        balances: List<Pair<String, Long>>,
        sourcePocket: SettlementPocketType,
        currency: String
    ): List<Settlement> {
        val debtors = balances
            .filter { (_, bal) -> bal < 0L }
            .map { (id, bal) -> id to -bal }
            .sortedByDescending { it.second }
            .toMutableList()

        val creditors = balances
            .filter { (_, bal) -> bal > 0L }
            .sortedByDescending { it.second }
            .toMutableList()

        val settlements = mutableListOf<Settlement>()
        var dIdx = 0
        var cIdx = 0

        while (dIdx < debtors.size && cIdx < creditors.size) {
            val debtor = debtors[dIdx]
            val creditor = creditors[cIdx]
            val settleAmount = min(debtor.second, creditor.second)

            if (settleAmount > 0L) {
                settlements += Settlement(
                    fromUserId = debtor.first,
                    toUserId = creditor.first,
                    amount = settleAmount,
                    currency = currency,
                    sourcePocket = sourcePocket
                )
            }

            debtors[dIdx] = debtor.first to (debtor.second - settleAmount)
            creditors[cIdx] = creditor.first to (creditor.second - settleAmount)

            if (debtors[dIdx].second == 0L) dIdx++
            if (creditors[cIdx].second == 0L) cIdx++
        }

        return settlements
    }
}
