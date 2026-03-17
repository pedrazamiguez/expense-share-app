package es.pedrazamiguez.expenseshareapp.domain.usecase.expense

import es.pedrazamiguez.expenseshareapp.domain.model.CashTranche
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PreviewCashExchangeRateUseCaseTest {

    private lateinit var cashWithdrawalRepository: CashWithdrawalRepository
    private lateinit var expenseCalculatorService: ExpenseCalculatorService
    private lateinit var useCase: PreviewCashExchangeRateUseCase

    private val groupId = "group-123"
    private val currency = "THB"

    private val withdrawal1 = CashWithdrawal(
        id = "w-1",
        groupId = groupId,
        amountWithdrawn = 1000000L,  // 10,000 THB
        remainingAmount = 1000000L,
        currency = currency,
        deductedBaseAmount = 27000L,  // 270.00 EUR → rate ~37.037
        createdAt = LocalDateTime.of(2026, 1, 10, 12, 0)
    )

    private val withdrawal2 = CashWithdrawal(
        id = "w-2",
        groupId = groupId,
        amountWithdrawn = 500000L,   // 5,000 THB
        remainingAmount = 500000L,
        currency = currency,
        deductedBaseAmount = 13700L,  // 137.00 EUR → rate ~36.496
        createdAt = LocalDateTime.of(2026, 1, 12, 12, 0)
    )

    @BeforeEach
    fun setUp() {
        cashWithdrawalRepository = mockk()
        expenseCalculatorService = ExpenseCalculatorService() // Use real service for integration-style tests
        useCase = PreviewCashExchangeRateUseCase(
            cashWithdrawalRepository,
            expenseCalculatorService
        )
    }

    // ── No withdrawals ────────────────────────────────────────────────────────

    @Nested
    inner class NoWithdrawals {

        @Test
        fun `returns null when no withdrawals exist for currency`() = runTest {
            coEvery {
                cashWithdrawalRepository.getAvailableWithdrawals(groupId, currency)
            } returns emptyList()

            val result = useCase(groupId, currency, 50000L)

            assertNull(result)
        }

        @Test
        fun `returns null with zero source amount and no withdrawals`() = runTest {
            coEvery {
                cashWithdrawalRepository.getAvailableWithdrawals(groupId, currency)
            } returns emptyList()

            val result = useCase(groupId, currency, 0L)

            assertNull(result)
        }
    }

    // ── Zero source amount (weighted average preview) ─────────────────────────

    @Nested
    inner class WeightedAveragePreview {

        @Test
        fun `returns weighted average display rate when source amount is zero`() = runTest {
            coEvery {
                cashWithdrawalRepository.getAvailableWithdrawals(groupId, currency)
            } returns listOf(withdrawal1, withdrawal2)

            val result = useCase(groupId, currency, 0L)

            assertNotNull(result)
            // Weighted average: (1000000 + 500000) / (27000 + 13700) = 1500000 / 40700 ≈ 36.855037
            assertEquals(BigDecimal("36.855037"), result!!.displayRate)
            assertEquals(0L, result.groupAmountCents) // No FIFO simulation
        }

        @Test
        fun `returns weighted average for single withdrawal`() = runTest {
            coEvery {
                cashWithdrawalRepository.getAvailableWithdrawals(groupId, currency)
            } returns listOf(withdrawal1)

            val result = useCase(groupId, currency, 0L)

            assertNotNull(result)
            // Single withdrawal: 1000000 / 27000 ≈ 37.037037
            assertEquals(BigDecimal("37.037037"), result!!.displayRate)
        }

        @Test
        fun `treats negative source amount as zero and returns weighted average preview`() = runTest {
            coEvery {
                cashWithdrawalRepository.getAvailableWithdrawals(groupId, currency)
            } returns listOf(withdrawal1)

            val result = useCase(groupId, currency, -100L)

            // Negative is treated like zero → weighted average preview
            assertNotNull(result)
        }
    }

    // ── FIFO-simulated preview ────────────────────────────────────────────────

    @Nested
    inner class FifoSimulatedPreview {

        @Test
        fun `returns FIFO-blended rate for single-tranche expense`() = runTest {
            coEvery {
                cashWithdrawalRepository.getAvailableWithdrawals(groupId, currency)
            } returns listOf(withdrawal1)

            // 500 THB (50000 cents) from withdrawal1
            val result = useCase(groupId, currency, 50000L)

            assertNotNull(result)
            // FIFO uses withdrawal1's rate: deductedBaseAmount / amountWithdrawn = 27000/1000000
            // groupAmount = 50000 * (27000/1000000) = 50000 * 0.027 = 1350
            assertEquals(1350L, result!!.groupAmountCents)
            // Display rate = 50000 / 1350 ≈ 37.037037
            assertEquals(BigDecimal("37.037037"), result.displayRate)
        }

        @Test
        fun `returns FIFO-blended rate for multi-tranche expense`() = runTest {
            // First withdrawal has only 200 THB remaining
            val partialW1 = withdrawal1.copy(remainingAmount = 20000L)

            coEvery {
                cashWithdrawalRepository.getAvailableWithdrawals(groupId, currency)
            } returns listOf(partialW1, withdrawal2)

            // 500 THB (50000 cents): 200 THB from w1 (rate ~37.037) + 300 THB from w2 (rate ~36.496)
            val result = useCase(groupId, currency, 50000L)

            assertNotNull(result)
            // w1: 20000 * (27000/1000000) = 20000 * 0.027 = 540
            // w2: 30000 * (13700/500000)  = 30000 * 0.0274 = 822
            // total = 540 + 822 = 1362
            assertEquals(1362L, result!!.groupAmountCents)
            assertTrue(result.displayRate > BigDecimal.ONE)
        }

        @Test
        fun `returns null when insufficient cash for requested amount`() = runTest {
            // Only 10,000 THB available
            coEvery {
                cashWithdrawalRepository.getAvailableWithdrawals(groupId, currency)
            } returns listOf(withdrawal1)

            // Request 20,000 THB (2000000 cents) — exceeds available
            val result = useCase(groupId, currency, 2000000L)

            assertNull(result)
        }
    }

    // Helper to make assertions cleaner
    private fun assertTrue(condition: Boolean) {
        org.junit.jupiter.api.Assertions.assertTrue(condition)
    }
}


