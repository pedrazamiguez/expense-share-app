package es.pedrazamiguez.splittrip.domain.service

import es.pedrazamiguez.splittrip.domain.service.cashdebt.CashDebtNode

/**
 * Domain service to handle the scaling and distribution of remaining cash pool debts
 * based on members' weights (typically their withdrawn amounts).
 */
interface CashDebtScalingService {
    /**
     * Scales the creditors' unspent balances using a water-filling proportional distribution
     * so that the total available credit exactly matches the total debt, ensuring that
     * an overspender's debt is distributed to the members whose cash was spent.
     */
    fun scaleBalances(nodes: List<CashDebtNode>): List<CashDebtNode>
}
