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

        if (totalDebt == 0L || totalCredit == 0L || totalDebt == totalCredit) {
            return nodes
        }

        if (totalDebt > totalCredit) {
            // Scale debtors down to match totalCredit
            val scaledDebtors = scaleNodes(
                nodesToScale = debtors,
                targetTotal = totalCredit
            ) { node, assigned -> node.copy(balance = -assigned) }
            return creditors + scaledDebtors
        } else {
            // Scale creditors down to match totalDebt
            val scaledCreditors = scaleNodes(
                nodesToScale = creditors,
                targetTotal = totalDebt
            ) { node, assigned -> node.copy(balance = assigned) }
            return debtors + scaledCreditors
        }
    }

    private fun scaleNodes(
        nodesToScale: List<CashDebtNode>,
        targetTotal: Long,
        mapper: (CashDebtNode, Long) -> CashDebtNode
    ): List<CashDebtNode> {
        val assignedAmounts = nodesToScale.associate { it.userId to 0L }.toMutableMap()
        var remainingToDistribute = targetTotal

        while (remainingToDistribute > 0L) {
            val distributedInThisRound = distributeRound(
                remainingToDistribute = remainingToDistribute,
                nodesToScale = nodesToScale,
                assignedAmounts = assignedAmounts
            )
            if (distributedInThisRound == 0L) {
                break
            }
            remainingToDistribute -= distributedInThisRound
        }

        return nodesToScale.map {
            mapper(it, assignedAmounts[it.userId] ?: 0L)
        }
    }

    private fun distributeRound(
        remainingToDistribute: Long,
        nodesToScale: List<CashDebtNode>,
        assignedAmounts: MutableMap<String, Long>
    ): Long {
        val activeNodes = nodesToScale.filter {
            val assigned = assignedAmounts[it.userId] ?: 0L
            assigned < Math.abs(it.balance)
        }

        if (activeNodes.isEmpty()) return 0L

        val activeWeights = activeNodes.map { BigDecimal(it.weight) }
        val activeSum = activeWeights.fold(BigDecimal.ZERO, BigDecimal::add)

        return if (activeSum.compareTo(BigDecimal.ZERO) == 0) {
            distributeEqually(remainingToDistribute, activeNodes, assignedAmounts)
        } else {
            distributeProportionally(remainingToDistribute, activeNodes, assignedAmounts, activeWeights)
        }
    }

    private fun distributeEqually(
        remainingToDistribute: Long,
        activeNodes: List<CashDebtNode>,
        assignedAmounts: MutableMap<String, Long>
    ): Long {
        val share = remainingToDistribute / activeNodes.size
        var remainder = remainingToDistribute % activeNodes.size
        var distributed = 0L

        for (node in activeNodes) {
            val assigned = assignedAmounts[node.userId] ?: 0L
            var amountToAdd = share + if (remainder > 0) 1 else 0
            if (remainder > 0) remainder--

            val maxToAdd = Math.abs(node.balance) - assigned
            if (amountToAdd > maxToAdd) {
                amountToAdd = maxToAdd
            }

            assignedAmounts[node.userId] = assigned + amountToAdd
            distributed += amountToAdd
        }
        return distributed
    }

    private fun distributeProportionally(
        remainingToDistribute: Long,
        activeNodes: List<CashDebtNode>,
        assignedAmounts: MutableMap<String, Long>,
        activeWeights: List<BigDecimal>
    ): Long {
        val proposedDistribution = remainderDistributionService.distributeByWeights(
            total = remainingToDistribute,
            weights = activeWeights
        )
        var distributed = 0L

        activeNodes.forEachIndexed { index, node ->
            val proposedAmount = proposedDistribution[index]
            if (proposedAmount > 0) {
                val assigned = assignedAmounts[node.userId] ?: 0L
                val maxToAdd = Math.abs(node.balance) - assigned
                val amountToAdd = if (proposedAmount > maxToAdd) maxToAdd else proposedAmount
                assignedAmounts[node.userId] = assigned + amountToAdd
                distributed += amountToAdd
            }
        }
        return distributed
    }
}
