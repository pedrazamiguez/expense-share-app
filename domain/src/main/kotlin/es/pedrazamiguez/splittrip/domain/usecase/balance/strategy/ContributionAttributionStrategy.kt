package es.pedrazamiguez.splittrip.domain.usecase.balance.strategy

import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.Subunit
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
                contribution.equivalentBaseAmount ?: contribution.amount,
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
