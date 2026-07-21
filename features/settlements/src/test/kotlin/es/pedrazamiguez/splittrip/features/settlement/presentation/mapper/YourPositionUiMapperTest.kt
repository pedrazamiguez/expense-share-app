package es.pedrazamiguez.splittrip.features.settlement.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.domain.enums.AddOnType
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.AddOn
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.CurrencyAmount
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.NetPositionStatus
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class YourPositionUiMapperTest {

    private val localeProvider: LocaleProvider = mockk()
    private val resourceProvider: ResourceProvider = mockk()
    private lateinit var mapper: YourPositionUiMapper

    @BeforeEach
    fun setUp() {
        every { localeProvider.getCurrentLocale() } returns Locale.US
        every { resourceProvider.getString(any()) } returns "Label"
        every { resourceProvider.getString(any(), *anyVararg()) } returns "Formatted Label"
        mapper = YourPositionUiMapper(localeProvider, resourceProvider)
    }

    @Test
    fun `toPersonalPosition maps positive total balance correctly`() {
        val memberBalance = MemberBalance(
            userId = "user1",
            contributed = 200000L,
            withdrawn = 50000L,
            pocketBalance = 150000L,
            cashInHand = 50000L,
            totalSpent = 40000L,
            cashSpent = 10000L,
            nonCashSpent = 30000L,
            refundableSpent = 0L
        )

        val result = mapper.toPersonalPosition(memberBalance, "EUR")

        assertEquals("EUR", result.groupCurrencyCode)
        assertEquals("€2,000.00", result.formattedNetPosition)
        assertEquals(NetPositionStatus.POSITIVE, result.netPositionStatus)
        assertEquals("€1,500.00", result.formattedPocketBalance)
        assertEquals("€500.00", result.formattedCashInHand)
        assertFalse(result.hasNegativeCashInHand)
        assertEquals("€2,000.00", result.formattedTotalContributed)
        assertEquals("€400.00", result.formattedTotalSpent)
        assertEquals("€100.00", result.formattedCashSpent)
        assertEquals("€300.00", result.formattedNonCashSpent)
        assertNull(result.formattedRefundableSpent)
    }

    @Test
    fun `toPersonalPosition maps zero balance as neutral`() {
        val memberBalance = MemberBalance(
            userId = "user1",
            pocketBalance = 0L,
            cashInHand = 0L
        )

        val result = mapper.toPersonalPosition(memberBalance, "USD")

        assertEquals(NetPositionStatus.NEUTRAL, result.netPositionStatus)
    }

    @Test
    fun `toPersonalPosition maps negative total balance as negative`() {
        val memberBalance = MemberBalance(
            userId = "user1",
            pocketBalance = -50000L,
            cashInHand = 0L
        )

        val result = mapper.toPersonalPosition(memberBalance, "EUR")

        assertEquals(NetPositionStatus.NEGATIVE, result.netPositionStatus)
    }

    @Test
    fun `toPersonalPosition maps negative cash in hand to em-dash and sets flag`() {
        val memberBalance = MemberBalance(
            userId = "user1",
            cashInHand = -1500L
        )

        val result = mapper.toPersonalPosition(memberBalance, "EUR")

        assertEquals(YourPositionUiMapper.EM_DASH, result.formattedCashInHand)
        assertTrue(result.hasNegativeCashInHand)
    }

    @Test
    fun `toPersonalPosition maps refundable spent when greater than zero`() {
        val memberBalance = MemberBalance(
            userId = "user1",
            refundableSpent = 2500L
        )

        val result = mapper.toPersonalPosition(memberBalance, "EUR")

        assertEquals("€25.00", result.formattedRefundableSpent)
    }

    @Test
    fun `toPersonalPosition maps multi-currency breakdown lists`() {
        val memberBalance = MemberBalance(
            userId = "user1",
            cashInHandByCurrency = listOf(
                CurrencyAmount("USD", 1000L, 900L)
            )
        )

        val result = mapper.toPersonalPosition(memberBalance, "EUR")

        assertEquals(1, result.cashInHandByCurrency.size)
        assertEquals("USD", result.cashInHandByCurrency[0].currency)
        assertEquals("$10.00", result.cashInHandByCurrency[0].formattedAmount)
        assertEquals("€9.00", result.cashInHandByCurrency[0].formattedEquivalent)
    }

    @Test
    fun `toPersonalPosition calculates total fees and cash breakdown`() {
        val memberBalance = MemberBalance(
            userId = "user1",
            cashInHand = 1200L
        )
        val withdrawal = CashWithdrawal(
            id = "w1",
            withdrawnBy = "user1",
            amountWithdrawn = 1200L,
            remainingAmount = 1200L,
            currency = "EUR",
            withdrawalScope = PayerType.USER,
            addOns = listOf(
                AddOn(id = "a1", type = AddOnType.FEE, groupAmountCents = 100L)
            )
        )

        val result = mapper.toPersonalPosition(
            memberBalance = memberBalance,
            groupCurrencyCode = "EUR",
            withdrawals = listOf(withdrawal),
            groupMemberIds = listOf("user1")
        )

        assertEquals("€1.00", result.formattedTotalFees)
        assertEquals(1, result.cashBreakdown.size)
        assertEquals("€12.00", result.cashBreakdown[0].formattedNativeRemaining)
    }
}
