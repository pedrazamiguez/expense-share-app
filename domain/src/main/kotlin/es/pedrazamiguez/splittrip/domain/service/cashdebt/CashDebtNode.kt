package es.pedrazamiguez.splittrip.domain.service.cashdebt

/**
 * Represents a member's position in a physical cash pool.
 *
 * @property userId The ID of the member.
 * @property balance The member's current unspent cash (capacity to absorb debt if > 0, or overspent debt if < 0).
 * @property weight The member's original contribution to the cash pool (typically the amount withdrawn), used as the proportional weight.
 */
data class CashDebtNode(
    val userId: String,
    val balance: Long,
    val weight: Long = 0L
)
