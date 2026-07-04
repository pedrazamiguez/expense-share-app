package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.enums.CashWithdrawalReason
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.CurrencyAmount
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.splittrip.domain.result.ExchangeRateWithStaleness
import es.pedrazamiguez.splittrip.domain.service.ExchangeRateCalculationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.ResolveCashOnLeaveUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.currency.GetExchangeRateUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ResolveCashOnLeaveUseCaseImplTest {

    private lateinit var cashWithdrawalRepository: CashWithdrawalRepository
    private lateinit var exchangeRateCalculationService: ExchangeRateCalculationService
    private lateinit var getExchangeRateUseCase: GetExchangeRateUseCase
    private lateinit var useCase: ResolveCashOnLeaveUseCase

    private val groupId = "group-123"
    private val userId = "user-1"
    private val groupCurrency = "EUR"

    @BeforeEach
    fun setUp() {
        cashWithdrawalRepository = mockk()
        exchangeRateCalculationService = mockk()
        getExchangeRateUseCase = mockk()
        useCase = ResolveCashOnLeaveUseCaseImpl(
            cashWithdrawalRepository = cashWithdrawalRepository,
            exchangeRateCalculationService = exchangeRateCalculationService,
            getExchangeRateUseCase = getExchangeRateUseCase
        )
    }

    @Test
    fun `no-op when cashInHand is zero`() = runTest {
        val balance = MemberBalance(
            userId = userId,
            cashInHand = 0L,
            cashInHandByCurrency = emptyList()
        )

        val result = useCase(groupId, userId, balance, groupCurrency)

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { cashWithdrawalRepository.addWithdrawal(any(), any()) }
    }

    @Test
    fun `creates one leave deposit withdrawal for single EUR bucket`() = runTest {
        val balance = MemberBalance(
            userId = userId,
            cashInHand = 1000L,
            cashInHandByCurrency = listOf(
                CurrencyAmount(currency = "EUR", amountCents = 1000L, equivalentCents = 1000L)
            )
        )
        val withdrawalSlot = slot<CashWithdrawal>()
        coEvery { cashWithdrawalRepository.addWithdrawal(groupId, capture(withdrawalSlot)) } just Runs

        val result = useCase(groupId, userId, balance, groupCurrency)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { cashWithdrawalRepository.addWithdrawal(groupId, any()) }
        val withdrawal = withdrawalSlot.captured
        assertEquals(groupId, withdrawal.groupId)
        assertEquals(userId, withdrawal.withdrawnBy)
        assertEquals(userId, withdrawal.createdBy)
        assertEquals(PayerType.USER, withdrawal.withdrawalScope)
        assertEquals(-1000L, withdrawal.amountWithdrawn)
        assertEquals(-1000L, withdrawal.remainingAmount)
        assertEquals("EUR", withdrawal.currency)
        assertEquals(-1000L, withdrawal.deductedBaseAmount)
        assertEquals(CashWithdrawalReason.LEAVE_DEPOSIT, withdrawal.reason)
    }

    @Test
    fun `creates two leave deposit withdrawals for EUR + THB buckets`() = runTest {
        val balance = MemberBalance(
            userId = userId,
            cashInHand = 2000L,
            cashInHandByCurrency = listOf(
                CurrencyAmount(currency = "EUR", amountCents = 1000L, equivalentCents = 1000L),
                CurrencyAmount(currency = "THB", amountCents = 40000L, equivalentCents = 1000L)
            )
        )
        val expectedRate = BigDecimal(1000L).divide(BigDecimal(40000L), 6, RoundingMode.HALF_UP)
        coEvery { cashWithdrawalRepository.getAvailableWithdrawals(any(), any(), any(), any()) } returns listOf(mockk())
        coEvery { exchangeRateCalculationService.calculateBlendedRate(40000L, 1000L) } returns expectedRate
        coEvery { exchangeRateCalculationService.calculateGroupAmount(BigDecimal(40000L), expectedRate, 0) } returns
            BigDecimal(1000L)
        coEvery { cashWithdrawalRepository.addWithdrawal(groupId, any()) } just Runs

        val result = useCase(groupId, userId, balance, groupCurrency)

        assertTrue(result.isSuccess)
        coVerify(exactly = 2) { cashWithdrawalRepository.addWithdrawal(groupId, any()) }
    }

    @Test
    fun `skips bucket with zero amountCents`() = runTest {
        val balance = MemberBalance(
            userId = userId,
            cashInHand = 1000L,
            cashInHandByCurrency = listOf(
                CurrencyAmount(currency = "EUR", amountCents = 1000L, equivalentCents = 1000L),
                CurrencyAmount(currency = "THB", amountCents = 0L, equivalentCents = 0L)
            )
        )
        coEvery { cashWithdrawalRepository.addWithdrawal(groupId, any()) } just Runs

        val result = useCase(groupId, userId, balance, groupCurrency)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { cashWithdrawalRepository.addWithdrawal(groupId, any()) }
    }

    @Test
    fun `calculates blended exchange rate with RATE_PRECISION scale`() = runTest {
        val balance = MemberBalance(
            userId = userId,
            cashInHand = 1000L,
            cashInHandByCurrency = listOf(
                CurrencyAmount(currency = "THB", amountCents = 37037L, equivalentCents = 1000L)
            )
        )
        val withdrawalSlot = slot<CashWithdrawal>()
        val expectedRate = BigDecimal(1000L).divide(BigDecimal(37037L), 6, RoundingMode.HALF_UP)
        coEvery { cashWithdrawalRepository.getAvailableWithdrawals(any(), any(), any(), any()) } returns listOf(mockk())
        coEvery { exchangeRateCalculationService.calculateBlendedRate(37037L, 1000L) } returns expectedRate
        coEvery { exchangeRateCalculationService.calculateGroupAmount(BigDecimal(37037L), expectedRate, 0) } returns
            BigDecimal(1000L)
        coEvery { cashWithdrawalRepository.addWithdrawal(groupId, capture(withdrawalSlot)) } just Runs

        val result = useCase(groupId, userId, balance, groupCurrency)

        assertTrue(result.isSuccess)
        val withdrawal = withdrawalSlot.captured
        assertEquals(expectedRate, withdrawal.exchangeRate)
        assertEquals(6, withdrawal.exchangeRate.scale())
    }

    @Test
    fun `deposit withdrawal amounts are negative (amountWithdrawn, remainingAmount, deductedBaseAmount)`() = runTest {
        val balance = MemberBalance(
            userId = userId,
            cashInHand = 1000L,
            cashInHandByCurrency = listOf(
                CurrencyAmount(currency = "EUR", amountCents = 1000L, equivalentCents = 1000L)
            )
        )
        val withdrawalSlot = slot<CashWithdrawal>()
        coEvery { cashWithdrawalRepository.addWithdrawal(groupId, capture(withdrawalSlot)) } just Runs

        val result = useCase(groupId, userId, balance, groupCurrency)

        assertTrue(result.isSuccess)
        val withdrawal = withdrawalSlot.captured
        assertTrue(withdrawal.amountWithdrawn < 0)
        assertTrue(withdrawal.remainingAmount < 0)
        assertTrue(withdrawal.deductedBaseAmount < 0)
    }

    @Test
    fun `propagates failure from cash withdrawal repository`() = runTest {
        val balance = MemberBalance(
            userId = userId,
            cashInHand = 1000L,
            cashInHandByCurrency = listOf(
                CurrencyAmount(currency = "EUR", amountCents = 1000L, equivalentCents = 1000L)
            )
        )
        val exception = RuntimeException("DB error")
        coEvery { cashWithdrawalRepository.addWithdrawal(groupId, any()) } throws exception

        val result = useCase(groupId, userId, balance, groupCurrency)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `skips resolution for negative cashInHand — no repository calls`() = runTest {
        val balance = MemberBalance(
            userId = userId,
            cashInHand = -1000L,
            cashInHandByCurrency = emptyList()
        )

        val result = useCase(groupId, userId, balance, groupCurrency)

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { cashWithdrawalRepository.addWithdrawal(any(), any()) }
    }

    @Test
    fun `foreign currency bucket with withdrawals uses blended rate`() = runTest {
        val balance = MemberBalance(
            userId = userId,
            cashInHand = 1000L,
            cashInHandByCurrency = listOf(
                CurrencyAmount(currency = "THB", amountCents = 37037L, equivalentCents = 1000L)
            )
        )
        val withdrawalSlot = slot<CashWithdrawal>()
        val expectedRate = BigDecimal(1000L).divide(BigDecimal(37037L), 6, RoundingMode.HALF_UP)
        coEvery { cashWithdrawalRepository.getAvailableWithdrawals(groupId, "THB", PayerType.USER, userId) } returns
            listOf(mockk(), mockk())
        coEvery { exchangeRateCalculationService.calculateBlendedRate(37037L, 1000L) } returns expectedRate
        coEvery { exchangeRateCalculationService.calculateGroupAmount(BigDecimal(37037L), expectedRate, 0) } returns
            BigDecimal(1000L)
        coEvery { cashWithdrawalRepository.addWithdrawal(groupId, capture(withdrawalSlot)) } just Runs

        val result = useCase(groupId, userId, balance, groupCurrency)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { exchangeRateCalculationService.calculateBlendedRate(37037L, 1000L) }
        coVerify(exactly = 1) {
            exchangeRateCalculationService.calculateGroupAmount(BigDecimal(37037L), expectedRate, 0)
        }
        val withdrawal = withdrawalSlot.captured
        assertEquals(expectedRate, withdrawal.exchangeRate)
        assertEquals(-1000L, withdrawal.deductedBaseAmount)
        assertEquals(-37037L, withdrawal.amountWithdrawn)
    }

    @Test
    fun `foreign currency bucket without withdrawals falls back to live rate`() = runTest {
        val balance = MemberBalance(
            userId = userId,
            cashInHand = 1000L,
            cashInHandByCurrency = listOf(
                CurrencyAmount(currency = "THB", amountCents = 37037L, equivalentCents = 1000L)
            )
        )
        val withdrawalSlot = slot<CashWithdrawal>()
        val displayRate = BigDecimal("37.037")
        val calculationRate = BigDecimal.ONE.divide(displayRate, 6, RoundingMode.HALF_UP)
        val exchangeRateWithStaleness = ExchangeRateWithStaleness(rate = displayRate, isStale = false)
        coEvery { cashWithdrawalRepository.getAvailableWithdrawals(groupId, "THB", PayerType.USER, userId) } returns
            emptyList()
        coEvery { getExchangeRateUseCase(groupCurrency, "THB") } returns exchangeRateWithStaleness
        coEvery { exchangeRateCalculationService.displayRateToCalculationRate(displayRate.toPlainString()) } returns
            calculationRate
        coEvery { exchangeRateCalculationService.calculateGroupAmount(BigDecimal(37037L), calculationRate, 0) } returns
            BigDecimal(1000L)
        coEvery { cashWithdrawalRepository.addWithdrawal(groupId, capture(withdrawalSlot)) } just Runs

        val result = useCase(groupId, userId, balance, groupCurrency)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { getExchangeRateUseCase(groupCurrency, "THB") }
        coVerify(exactly = 1) {
            exchangeRateCalculationService.displayRateToCalculationRate(displayRate.toPlainString())
        }
        coVerify(exactly = 0) { exchangeRateCalculationService.calculateBlendedRate(any(), any()) }
        val withdrawal = withdrawalSlot.captured
        assertEquals(calculationRate, withdrawal.exchangeRate)
        assertEquals(-1000L, withdrawal.deductedBaseAmount)
    }

    @Test
    fun `foreign currency bucket without withdrawals and no live rate uses identity`() = runTest {
        val balance = MemberBalance(
            userId = userId,
            cashInHand = 1000L,
            cashInHandByCurrency = listOf(
                CurrencyAmount(currency = "THB", amountCents = 37037L, equivalentCents = 1000L)
            )
        )
        val withdrawalSlot = slot<CashWithdrawal>()
        coEvery { cashWithdrawalRepository.getAvailableWithdrawals(groupId, "THB", PayerType.USER, userId) } returns
            emptyList()
        coEvery { getExchangeRateUseCase(groupCurrency, "THB") } returns null
        coEvery { exchangeRateCalculationService.calculateGroupAmount(BigDecimal(37037L), BigDecimal.ONE, 0) } returns
            BigDecimal(37037L)
        coEvery { cashWithdrawalRepository.addWithdrawal(groupId, capture(withdrawalSlot)) } just Runs

        val result = useCase(groupId, userId, balance, groupCurrency)

        assertTrue(result.isSuccess)
        val withdrawal = withdrawalSlot.captured
        assertEquals(-37037L, withdrawal.deductedBaseAmount)
        assertEquals(BigDecimal.ONE, withdrawal.exchangeRate)
    }

    @Test
    fun `multiple foreign currencies each resolved correctly`() = runTest {
        val balance = MemberBalance(
            userId = userId,
            cashInHand = 3000L,
            cashInHandByCurrency = listOf(
                CurrencyAmount(currency = "THB", amountCents = 37037L, equivalentCents = 1000L),
                CurrencyAmount(currency = "USD", amountCents = 5000L, equivalentCents = 5000L)
            )
        )
        val thbRate = BigDecimal(1000L).divide(BigDecimal(37037L), 6, RoundingMode.HALF_UP)
        coEvery { cashWithdrawalRepository.getAvailableWithdrawals(groupId, "THB", PayerType.USER, userId) } returns
            listOf(mockk())
        coEvery { cashWithdrawalRepository.getAvailableWithdrawals(groupId, "USD", PayerType.USER, userId) } returns
            listOf(mockk())
        coEvery { exchangeRateCalculationService.calculateBlendedRate(37037L, 1000L) } returns thbRate
        coEvery { exchangeRateCalculationService.calculateBlendedRate(5000L, 5000L) } returns BigDecimal.ONE
        coEvery { exchangeRateCalculationService.calculateGroupAmount(BigDecimal(37037L), thbRate, 0) } returns
            BigDecimal(1000L)
        coEvery { exchangeRateCalculationService.calculateGroupAmount(BigDecimal(5000L), BigDecimal.ONE, 0) } returns
            BigDecimal(5000L)
        coEvery { cashWithdrawalRepository.addWithdrawal(groupId, any()) } just Runs

        val result = useCase(groupId, userId, balance, groupCurrency)

        assertTrue(result.isSuccess)
        coVerify(exactly = 2) { cashWithdrawalRepository.addWithdrawal(groupId, any()) }
        coVerify(exactly = 1) { exchangeRateCalculationService.calculateBlendedRate(37037L, 1000L) }
        coVerify(exactly = 1) { exchangeRateCalculationService.calculateBlendedRate(5000L, 5000L) }
    }

    @Test
    fun `same currency bucket uses identity path`() = runTest {
        val balance = MemberBalance(
            userId = userId,
            cashInHand = 1000L,
            cashInHandByCurrency = listOf(
                CurrencyAmount(currency = "EUR", amountCents = 1000L, equivalentCents = 1000L)
            )
        )
        val withdrawalSlot = slot<CashWithdrawal>()
        coEvery { cashWithdrawalRepository.addWithdrawal(groupId, capture(withdrawalSlot)) } just Runs

        val result = useCase(groupId, userId, balance, groupCurrency)

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { exchangeRateCalculationService.calculateBlendedRate(any(), any()) }
        coVerify(exactly = 0) { exchangeRateCalculationService.calculateGroupAmount(any(), any(), any()) }
        coVerify(exactly = 0) { cashWithdrawalRepository.getAvailableWithdrawals(any(), any(), any(), any()) }
        coVerify(exactly = 0) { getExchangeRateUseCase(any(), any()) }
        val withdrawal = withdrawalSlot.captured
        assertEquals(-1000L, withdrawal.deductedBaseAmount)
        assertEquals(BigDecimal.ONE, withdrawal.exchangeRate)
    }

    @Test
    fun `foreign currency does not affect same currency bucket in mixed entries`() = runTest {
        val balance = MemberBalance(
            userId = userId,
            cashInHand = 2000L,
            cashInHandByCurrency = listOf(
                CurrencyAmount(currency = "EUR", amountCents = 1000L, equivalentCents = 1000L),
                CurrencyAmount(currency = "THB", amountCents = 37037L, equivalentCents = 1000L)
            )
        )
        val thbRate = BigDecimal(1000L).divide(BigDecimal(37037L), 6, RoundingMode.HALF_UP)
        coEvery { cashWithdrawalRepository.getAvailableWithdrawals(groupId, "THB", PayerType.USER, userId) } returns
            listOf(mockk())
        coEvery { exchangeRateCalculationService.calculateBlendedRate(37037L, 1000L) } returns thbRate
        coEvery { exchangeRateCalculationService.calculateGroupAmount(BigDecimal(37037L), thbRate, 0) } returns
            BigDecimal(1000L)
        val capturedWithdrawals = mutableListOf<CashWithdrawal>()
        coEvery { cashWithdrawalRepository.addWithdrawal(groupId, capture(capturedWithdrawals)) } just Runs

        val result = useCase(groupId, userId, balance, groupCurrency)

        assertTrue(result.isSuccess)
        coVerify(exactly = 2) { cashWithdrawalRepository.addWithdrawal(groupId, any()) }
        assertEquals(2, capturedWithdrawals.size)
        val eurWithdrawal = capturedWithdrawals.first { it.currency == "EUR" }
        val thbWithdrawal = capturedWithdrawals.first { it.currency == "THB" }
        assertEquals(BigDecimal.ONE, eurWithdrawal.exchangeRate)
        assertEquals(-1000L, eurWithdrawal.deductedBaseAmount)
        assertEquals(-1000L, eurWithdrawal.amountWithdrawn)
        assertEquals(thbRate, thbWithdrawal.exchangeRate)
        assertEquals(-1000L, thbWithdrawal.deductedBaseAmount)
        assertEquals(-37037L, thbWithdrawal.amountWithdrawn)
    }
}
