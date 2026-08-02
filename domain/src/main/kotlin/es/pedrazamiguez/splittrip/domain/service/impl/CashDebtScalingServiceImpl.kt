package es.pedrazamiguez.splittrip.domain.service.impl

import es.pedrazamiguez.splittrip.domain.service.CashDebtScalingService
import es.pedrazamiguez.splittrip.domain.service.RemainderDistributionService
import es.pedrazamiguez.splittrip.domain.service.cashdebt.CashDebtNode
import java.math.BigDecimal

class CashDebtScalingServiceImpl(
    private val remainderDistributionService: RemainderDistributionService
) : CashDebtScalingService {

    override fun scaleBalances(nodes: List<CashDebtNode>): List<CashDebtNode> {
        val debtors = nodes.filter { it.balance < 0L }
        val creditors = nodes.filter { it.balance > 0L }

        val totalDebt = debtors.sumOf { -it.balance }
        val totalCredit = creditors.sumOf { it.balance }

        if (totalDebt == 0L || totalCredit == 0L) {
            return nodes
        }

        val assignedDebts = creditors.associate { it.userId to 0L }.toMutableMap()
        var remainingDebtToDistribute = totalDebt

        while (remainingDebtToDistribute > 0L) {
            val debtDistributedInThisRound = distributeRound(
                remainingDebt = remainingDebtToDistribute,
                creditors = creditors,
                assignedDebts = assignedDebts
            )
            if (debtDistributedInThisRound == 0L) {
                break
            }
            remainingDebtToDistribute -= debtDistributedInThisRound
        }

        val scaledCreditors = creditors.map {
            it.copy(balance = assignedDebts[it.userId] ?: 0L)
        }

        return debtors + scaledCreditors
    }

    private fun distributeRound(
        remainingDebt: Long,
        creditors: List<CashDebtNode>,
        assignedDebts: MutableMap<String, Long>
    ): Long {
        val activeCreditors = creditors.filter {
            val assigned = assignedDebts[it.userId] ?: 0L
            assigned < it.balance
        }

        if (activeCreditors.isEmpty()) return 0L

        val activeWeights = activeCreditors.map { BigDecimal(it.weight) }
        val activeSum = activeWeights.fold(BigDecimal.ZERO, BigDecimal::add)

        return if (activeSum.compareTo(BigDecimal.ZERO) == 0) {
            distributeEqually(remainingDebt, activeCreditors, assignedDebts)
        } else {
            distributeProportionally(remainingDebt, activeCreditors, assignedDebts, activeWeights)
        }
    }

    private fun distributeEqually(
        remainingDebt: Long,
        activeCreditors: List<CashDebtNode>,
        assignedDebts: MutableMap<String, Long>
    ): Long {
        val share = remainingDebt / activeCreditors.size
        var remainder = remainingDebt % activeCreditors.size
        var distributed = 0L

        for (creditor in activeCreditors) {
            val assigned = assignedDebts[creditor.userId] ?: 0L
            var amountToAdd = share + if (remainder > 0) 1 else 0
            if (remainder > 0) remainder--

            val maxToAdd = creditor.balance - assigned
            if (amountToAdd > maxToAdd) {
                amountToAdd = maxToAdd
            }

            assignedDebts[creditor.userId] = assigned + amountToAdd
            distributed += amountToAdd
        }
        return distributed
    }

    private fun distributeProportionally(
        remainingDebt: Long,
        activeCreditors: List<CashDebtNode>,
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
                val assigned = assignedDebts[creditor.userId] ?: 0L
                val maxToAdd = creditor.balance - assigned
                val amountToAdd = if (proposedAmount > maxToAdd) maxToAdd else proposedAmount
                assignedDebts[creditor.userId] = assigned + amountToAdd
                distributed += amountToAdd
            }
        }
        return distributed
    }
}
