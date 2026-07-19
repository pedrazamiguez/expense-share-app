package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.balanceDistributeByShares
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.distributeByScope

/**
 * Strategy interface to define how group contributions are attributed to members.
 */
interface ContributionAttributionStrategy {
    fun attribute(
        contributions: List<Contribution>,
        subunitMap: Map<String, Subunit>,
        groupMemberIds: List<String>
    ): Map<String, Long>
}

/**
 * Standard strategy: splits GROUP and SUBUNIT scoped contributions evenly or by shares.
 * Used for standard dashboard and balance calculations.
 */
object StandardContributionAttributionStrategy : ContributionAttributionStrategy {
    override fun attribute(
        contributions: List<Contribution>,
        subunitMap: Map<String, Subunit>,
        groupMemberIds: List<String>
    ): Map<String, Long> {
        val result = mutableMapOf<String, Long>()
        for (contribution in contributions) {
            val distributions = distributeByScope(
                contribution.amount,
                contribution.contributionScope,
                contribution.userId,
                subunitMap,
                contribution.subunitId,
                groupMemberIds
            )
            for ((userId, amount) in distributions) {
                result[userId] = (result[userId] ?: 0L) + amount
            }
        }
        return result
    }
}

/**
 * Physical/Leave strategy: attributes contributions entirely to the payer who physically made them.
 * Used for settlements and leave group flows to determine correct cash return/settlement debts.
 */
object PhysicalContributionAttributionStrategy : ContributionAttributionStrategy {
    override fun attribute(
        contributions: List<Contribution>,
        subunitMap: Map<String, Subunit>,
        groupMemberIds: List<String>
    ): Map<String, Long> {
        val result = mutableMapOf<String, Long>()
        for (contribution in contributions) {
            val distributions = when (contribution.contributionScope) {
                PayerType.SUBUNIT -> {
                    val subunit = contribution.subunitId?.let { subunitMap[it] }
                    if (subunit == null || subunit.memberShares.isEmpty()) {
                        mapOf(contribution.userId to contribution.amount)
                    } else {
                        balanceDistributeByShares(contribution.amount, subunit.memberShares)
                    }
                }
                PayerType.GROUP,
                PayerType.USER -> {
                    mapOf(contribution.userId to contribution.amount)
                }
            }
            for ((userId, amount) in distributions) {
                result[userId] = (result[userId] ?: 0L) + amount
            }
        }
        return result
    }
}
