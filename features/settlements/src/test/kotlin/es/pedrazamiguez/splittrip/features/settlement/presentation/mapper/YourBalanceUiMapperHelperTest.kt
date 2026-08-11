package es.pedrazamiguez.splittrip.features.settlement.presentation.mapper

import es.pedrazamiguez.splittrip.domain.enums.AddOnType
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.AddOn
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.Subunit
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class YourBalanceUiMapperHelperTest {

    @Test
    fun `computeUserNativeShare for USER scope`() {
        val withdrawal = CashWithdrawal(
            id = "w1",
            withdrawnBy = "user1",
            amountWithdrawn = 1000L,
            remainingAmount = 1000L,
            withdrawalScope = PayerType.USER
        )

        assertEquals(1000L, computeUserNativeShare(withdrawal, "user1", emptyList(), emptyMap()))
        assertEquals(0L, computeUserNativeShare(withdrawal, "user2", emptyList(), emptyMap()))
    }

    @Test
    fun `computeUserNativeShare for GROUP scope`() {
        val withdrawal = CashWithdrawal(
            id = "w1",
            amountWithdrawn = 1000L,
            remainingAmount = 1000L,
            withdrawalScope = PayerType.GROUP
        )

        assertEquals(0L, computeUserNativeShare(withdrawal, "user1", emptyList(), emptyMap()))
        assertEquals(500L, computeUserNativeShare(withdrawal, "user1", listOf("user1", "user2"), emptyMap()))
    }

    @Test
    fun `computeUserNativeShare for SUBUNIT scope`() {
        val subunit = Subunit(
            id = "s1",
            groupId = "g1",
            name = "Subunit 1",
            memberShares = mapOf("user1" to BigDecimal("0.60"))
        )
        val withdrawal = CashWithdrawal(
            id = "w1",
            subunitId = "s1",
            amountWithdrawn = 1000L,
            remainingAmount = 1000L,
            withdrawalScope = PayerType.SUBUNIT
        )

        val subunitsMap = mapOf("s1" to subunit)
        assertEquals(600L, computeUserNativeShare(withdrawal, "user1", emptyList(), subunitsMap))
        assertEquals(0L, computeUserNativeShare(withdrawal, "user2", emptyList(), subunitsMap))
    }

    @Test
    fun `computeMemberTotalFees sums non-discount add-ons`() {
        val subunit = Subunit(
            id = "s1",
            groupId = "g1",
            name = "Subunit 1",
            memberShares = mapOf("user1" to BigDecimal("0.50"))
        )
        val subunitsMap = mapOf("s1" to subunit)

        val withdrawal1 = CashWithdrawal(
            id = "w1",
            withdrawnBy = "user1",
            amountWithdrawn = 1000L,
            remainingAmount = 1000L,
            withdrawalScope = PayerType.USER,
            addOns = listOf(
                AddOn(id = "a1", type = AddOnType.FEE, groupAmountCents = 100L),
                AddOn(id = "a2", type = AddOnType.DISCOUNT, groupAmountCents = 50L)
            )
        )
        val withdrawal2 = CashWithdrawal(
            id = "w2",
            withdrawnBy = "user2",
            amountWithdrawn = 1000L,
            remainingAmount = 1000L,
            withdrawalScope = PayerType.GROUP,
            addOns = listOf(
                AddOn(id = "a3", type = AddOnType.FEE, groupAmountCents = 200L)
            )
        )
        val withdrawal3 = CashWithdrawal(
            id = "w3",
            subunitId = "s1",
            amountWithdrawn = 1000L,
            remainingAmount = 1000L,
            withdrawalScope = PayerType.SUBUNIT,
            addOns = listOf(
                AddOn(id = "a4", type = AddOnType.FEE, groupAmountCents = 400L)
            )
        )
        val zeroRemaining = CashWithdrawal(
            id = "w4",
            withdrawnBy = "user1",
            amountWithdrawn = 1000L,
            remainingAmount = 0L,
            withdrawalScope = PayerType.USER,
            addOns = listOf(AddOn(id = "a5", type = AddOnType.FEE, groupAmountCents = 100L))
        )
        val zeroAmount = CashWithdrawal(
            id = "w5",
            withdrawnBy = "user1",
            amountWithdrawn = 0L,
            remainingAmount = 0L,
            withdrawalScope = PayerType.USER
        )

        val withdrawals = listOf(withdrawal1, withdrawal2, withdrawal3, zeroRemaining, zeroAmount)
        val memberIds = listOf("user1", "user2")

        // user1 fee: 100 (w1 personal) + 100 (w2 group 200/2) + 200 (w3 subunit 400*0.5) = 400
        val totalFees = computeMemberTotalFees("user1", withdrawals, memberIds, subunitsMap)
        assertEquals(400L, totalFees)
    }

    @Test
    fun `computeAddOnShare returns zero when groupMemberIds is empty on GROUP scope`() {
        val addOn = AddOn(id = "a1", type = AddOnType.FEE, groupAmountCents = 100L)
        val share = computeAddOnShare(addOn, PayerType.GROUP, "user1", "user1", emptyList(), null)
        assertEquals(0L, share)
    }
}
