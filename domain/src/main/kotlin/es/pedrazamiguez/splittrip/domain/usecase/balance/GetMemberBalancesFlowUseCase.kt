package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.usecase.UseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.strategy.ContributionAttributionStrategy
import es.pedrazamiguez.splittrip.domain.usecase.balance.strategy.StandardContributionAttributionStrategy
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.balanceDistributeByShares
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.balanceDistributeEvenly
import java.math.BigDecimal

data class MemberBalanceCalculationInputs(
    val contributions: List<Contribution> = emptyList(),
    val withdrawals: List<CashWithdrawal> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val subunits: List<Subunit> = emptyList(),
    val groupMemberIds: List<String> = emptyList(),
    val groupCurrency: String = "",
    val settlements: List<SettlementRecord> = emptyList(),
    val attributionStrategy: ContributionAttributionStrategy = StandardContributionAttributionStrategy
)

interface GetMemberBalancesFlowUseCase : UseCase {
    fun computeMemberBalances(
        inputs: MemberBalanceCalculationInputs
    ): List<MemberBalance>

    companion object {
        /** Delegates to [balanceDistributeByShares] — accessible via `GetMemberBalancesFlowUseCase.distributeByShares(...)`. */
        internal fun distributeByShares(
            totalAmount: Long,
            memberShares: Map<String, BigDecimal>
        ): Map<String, Long> = balanceDistributeByShares(totalAmount, memberShares)

        /** Delegates to [balanceDistributeEvenly] — accessible via `GetMemberBalancesFlowUseCase.distributeEvenly(...)`. */
        internal fun distributeEvenly(
            totalAmount: Long,
            memberIds: List<String>
        ): Map<String, Long> = balanceDistributeEvenly(totalAmount, memberIds)
    }
}
