package es.pedrazamiguez.splittrip.domain.usecase.balance.support

import java.math.BigDecimal
import java.math.RoundingMode

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
internal fun balanceDistributeByShares(
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
 *
 * Members are sorted by ID for deterministic remainder allocation
 * across runs/devices.
 */
internal fun balanceDistributeEvenly(
    totalAmount: Long,
    memberIds: List<String>
): Map<String, Long> {
    if (memberIds.isEmpty()) return emptyMap()

    val sortedIds = memberIds.sorted()
    val perMember = totalAmount / sortedIds.size
    var remainder = totalAmount - (perMember * sortedIds.size)

    return sortedIds.associateWith { _ ->
        val amount = if (remainder > 0) {
            remainder--
            perMember + 1
        } else {
            perMember
        }
        amount
    }
}
