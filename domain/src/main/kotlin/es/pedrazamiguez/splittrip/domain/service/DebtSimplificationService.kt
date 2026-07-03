package es.pedrazamiguez.splittrip.domain.service

import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Settlement

/**
 * Domain service to compute the minimum number of peer-to-peer transactions
 * required to resolve all member balances to zero.
 */
interface DebtSimplificationService {
    /**
     * Simplifies member balances into minimum peer-to-peer NET transactions. Signature unchanged.
     */
    fun simplify(memberBalances: List<MemberBalance>): List<Settlement>

    /**
     * Produces per-pocket-type settlements.
     * Virtual-pocket debts (POCKET) are resolved independently from cash-in-hand debts (CASH).
     * For multi-currency groups, cash settlements are produced per ISO 4217 currency.
     */
    fun simplifyByPocket(memberBalances: List<MemberBalance>, groupCurrency: String): List<Settlement>
}
