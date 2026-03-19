package es.pedrazamiguez.expenseshareapp.domain.model

import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Represents a single ATM cash withdrawal.
 *
 * All monetary values are stored in minor units (cents) to avoid
 * floating-point precision issues, consistent with the Expense model.
 *
 * @param id Unique identifier (UUID generated locally).
 * @param groupId The group this withdrawal belongs to.
 * @param withdrawnBy The userId who performed the withdrawal.
 * @param withdrawalScope Who the withdrawal is for: GROUP (all members), SUBUNIT (sub-unit members), or USER (personal).
 * @param subunitId The sub-unit this withdrawal is for (only when [withdrawalScope] is [PayerType.SUBUNIT]).
 * @param amountWithdrawn The amount withdrawn in the target currency (e.g., 10000 THB = 1000000 cents).
 * @param remainingAmount The remaining unspent cash (starts equal to amountWithdrawn, decreases via FIFO).
 * @param currency The currency of the withdrawn cash (e.g., "THB").
 * @param deductedBaseAmount The equivalent deducted from the virtual pocket in group currency (e.g., 270 EUR = 27000 cents).
 * @param exchangeRate The exact exchange rate applied at the ATM (e.g., 37.037), stored as BigDecimal for precision.
 * @param addOns Structured add-ons (e.g., ATM fees). Each add-on's [AddOn.groupAmountCents]
 *   is included in the effective deducted amount for balance calculations.
 * @param createdAt Timestamp of the withdrawal.
 * @param lastUpdatedAt Timestamp of the last update (e.g., after FIFO consumption).
 */
data class CashWithdrawal(
    val id: String = "",
    val groupId: String = "",
    val withdrawnBy: String = "",
    val withdrawalScope: PayerType = PayerType.GROUP,
    val subunitId: String? = null,
    val amountWithdrawn: Long = 0,
    val remainingAmount: Long = 0,
    val currency: String = "EUR",
    val deductedBaseAmount: Long = 0,
    val exchangeRate: BigDecimal = BigDecimal.ONE,
    val addOns: List<AddOn> = emptyList(),
    val createdAt: LocalDateTime? = null,
    val lastUpdatedAt: LocalDateTime? = null
)
