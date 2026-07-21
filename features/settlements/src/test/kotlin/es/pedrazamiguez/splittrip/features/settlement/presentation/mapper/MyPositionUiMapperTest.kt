package es.pedrazamiguez.splittrip.features.settlement.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.domain.model.CurrencyAmount
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.NetPositionStatus
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MyPositionUiMapperTest {

    private lateinit var localeProvider: LocaleProvider
    private lateinit var mapper: MyPositionUiMapper

    @BeforeEach
    fun setUp() {
        localeProvider = mockk()
        every { localeProvider.getCurrentLocale() } returns Locale.US
        mapper = MyPositionUiMapper(localeProvider)
    }

    @Test
    fun `toPersonalPosition with positive balance`() {
        val balance = MemberBalance(
            userId = "user-1",
            contributed = 200000,
            withdrawn = 50000,
            cashSpent = 35000,
            nonCashSpent = 25000,
            pocketBalance = 125000,
            cashInHand = 15000
        )

        val result = mapper.toPersonalPosition(balance, "EUR")

        assertEquals("EUR", result.groupCurrencyCode)
        assertEquals(NetPositionStatus.POSITIVE, result.netPositionStatus)
        assertTrue(result.formattedNetPosition.isNotBlank())
        assertTrue(result.formattedPocketBalance.isNotBlank())
        assertTrue(result.formattedCashInHand.isNotBlank())
        assertFalse(result.hasNegativeCashInHand)
    }

    @Test
    fun `toPersonalPosition with zero balance`() {
        val balance = MemberBalance(
            userId = "user-1",
            pocketBalance = 0,
            cashInHand = 0
        )

        val result = mapper.toPersonalPosition(balance, "EUR")

        assertEquals(NetPositionStatus.NEUTRAL, result.netPositionStatus)
    }

    @Test
    fun `toPersonalPosition with negative total balance`() {
        val balance = MemberBalance(
            userId = "user-1",
            pocketBalance = -60000,
            cashInHand = 10000
        )

        val result = mapper.toPersonalPosition(balance, "EUR")

        assertEquals(NetPositionStatus.NEGATIVE, result.netPositionStatus)
    }

    @Test
    fun `toPersonalPosition with negative cashInHand`() {
        val balance = MemberBalance(
            userId = "user-1",
            pocketBalance = 50000,
            cashInHand = -3000
        )

        val result = mapper.toPersonalPosition(balance, "EUR")

        assertEquals(MyPositionUiMapper.EM_DASH, result.formattedCashInHand)
        assertTrue(result.hasNegativeCashInHand)
    }

    @Test
    fun `toPersonalPosition maps breakdown figures`() {
        val balance = MemberBalance(
            userId = "user-1",
            contributed = 200000,
            cashSpent = 50000,
            nonCashSpent = 30000,
            refundableSpent = 10000,
            cashInHandByCurrency = listOf(CurrencyAmount("USD", 2000, 1800)),
            cashSpentByCurrency = listOf(CurrencyAmount("EUR", 50000, 50000)),
            nonCashSpentByCurrency = listOf(CurrencyAmount("EUR", 30000, 30000))
        )

        val result = mapper.toPersonalPosition(balance, "EUR")

        assertTrue(result.formattedTotalContributed.isNotBlank())
        assertTrue(result.formattedTotalSpent.isNotBlank())
        assertTrue(result.formattedCashSpent.isNotBlank())
        assertTrue(result.formattedNonCashSpent.isNotBlank())
        assertNotNull(result.formattedRefundableSpent)
        assertEquals(1, result.cashInHandByCurrency.size)
        assertEquals("USD", result.cashInHandByCurrency[0].currency)
    }

    @Test
    fun `toPersonalPosition with zero refundableSpent returns null formattedRefundableSpent`() {
        val balance = MemberBalance(
            userId = "user-1",
            refundableSpent = 0
        )

        val result = mapper.toPersonalPosition(balance, "EUR")

        assertNull(result.formattedRefundableSpent)
    }
}
