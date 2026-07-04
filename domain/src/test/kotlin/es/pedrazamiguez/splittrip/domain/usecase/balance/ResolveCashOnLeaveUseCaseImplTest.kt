package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.enums.CashWithdrawalReason
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.CurrencyAmount
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.ResolveCashOnLeaveUseCaseImpl
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
    private lateinit var useCase: ResolveCashOnLeaveUseCase

    private val groupId = "group-123"
    private val userId = "user-1"
    private val groupCurrency = "EUR"

    @BeforeEach
    fun setUp() {
        cashWithdrawalRepository = mockk()
        useCase = ResolveCashOnLeaveUseCaseImpl(cashWithdrawalRepository)
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
        coEvery { cashWithdrawalRepository.addWithdrawal(groupId, capture(withdrawalSlot)) } just Runs

        val result = useCase(groupId, userId, balance, groupCurrency)

        assertTrue(result.isSuccess)
        val withdrawal = withdrawalSlot.captured
        val expectedRate = BigDecimal(1000L).divide(BigDecimal(37037L), 6, RoundingMode.HALF_UP)
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
}
