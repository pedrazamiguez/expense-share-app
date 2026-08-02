package es.pedrazamiguez.splittrip.domain.service.impl

import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.service.DebtSimplificationService
import es.pedrazamiguez.splittrip.domain.service.RemainderDistributionService
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
            runGreedyAlgorithm(
                balances = scaleBalancesForCash(balances),
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
                runGreedyAlgorithm(
                    balances = scaleBalancesForCash(balances),
                    sourcePocket = SettlementPocketType.CASH,
                    currency = currencyCode
                )
            }
        }
    }

    private fun scaleBalancesForCash(balances: List<Pair<String, Long>>): List<Pair<String, Long>> {
        val debtors = balances.filter { it.second < 0L }
        val creditors = balances.filter { it.second > 0L }

        val totalDebt = debtors.sumOf { -it.second }
        val totalCredit = creditors.sumOf { it.second }

        if (totalDebt == 0L || totalCredit == 0L) {
            return balances
        }

        val scaledCreditAmounts = remainderDistributionService.rescaleAmounts(
            originalTotal = totalCredit,
            newTotal = totalDebt,
            amounts = creditors.map { it.second }
        )

        val scaledCreditors = creditors.mapIndexed { index, pair ->
            pair.first to scaledCreditAmounts[index]
        }

        return debtors + scaledCreditors
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
