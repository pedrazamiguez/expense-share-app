package es.pedrazamiguez.expenseshareapp.domain.usecase.expense

import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.exception.InsufficientCashException
import es.pedrazamiguez.expenseshareapp.domain.model.CashTranche
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class AddExpenseUseCaseTest {

    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var cashWithdrawalRepository: CashWithdrawalRepository
    private lateinit var expenseCalculatorService: ExpenseCalculatorService
    private lateinit var useCase: AddExpenseUseCase

    private val groupId = "group-123"

    private val baseExpense = Expense(
        id = "expense-1",
        title = "Dinner",
        sourceAmount = 5000L,
        sourceCurrency = "EUR",
        groupAmount = 5000L,
        groupCurrency = "EUR",
        paymentMethod = PaymentMethod.CREDIT_CARD
    )

    @BeforeEach
    fun setUp() {
        expenseRepository = mockk(relaxed = true)
        cashWithdrawalRepository = mockk(relaxed = true)
        expenseCalculatorService = mockk()
        useCase =
            AddExpenseUseCase(expenseRepository, cashWithdrawalRepository, expenseCalculatorService)
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Nested
    inner class Validation {

        @Test
        fun `fails when groupId is null`() = runTest {
            val result = useCase(null, baseExpense)
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message?.contains("Group ID") == true)
        }

        @Test
        fun `fails when groupId is blank`() = runTest {
            val result = useCase("  ", baseExpense)
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message?.contains("Group ID") == true)
        }

        @Test
        fun `fails when sourceAmount is zero`() = runTest {
            val result = useCase(groupId, baseExpense.copy(sourceAmount = 0L))
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message?.contains("amount") == true)
        }

        @Test
        fun `fails when title is blank`() = runTest {
            val result = useCase(groupId, baseExpense.copy(title = ""))
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message?.contains("title") == true)
        }
    }

    // ── Non-cash expense ──────────────────────────────────────────────────────

    @Nested
    inner class NonCashExpense {

        @Test
        fun `saves expense directly without touching withdrawal repository`() = runTest {
            coEvery { expenseRepository.addExpense(any(), any()) } just Runs

            val result = useCase(groupId, baseExpense)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { expenseRepository.addExpense(groupId, baseExpense) }
            coVerify(exactly = 0) { cashWithdrawalRepository.getAvailableWithdrawals(any(), any()) }
            coVerify(exactly = 0) { cashWithdrawalRepository.updateRemainingAmounts(any(), any()) }
        }
    }

    // ── Cash expense: batch update ─────────────────────────────────────────────

    @Nested
    inner class CashExpenseBatchUpdate {

        private val cashExpense = baseExpense.copy(
            paymentMethod = PaymentMethod.CASH, sourceCurrency = "THB", sourceAmount = 23000L
        )

        private val withdrawal1 = CashWithdrawal(
            id = "w-1",
            groupId = groupId,
            amountWithdrawn = 1000000L,
            remainingAmount = 5000L,
            currency = "THB",
            deductedBaseAmount = 26400L,
            createdAt = LocalDateTime.of(2026, 1, 10, 12, 0)
        )

        private val withdrawal2 = CashWithdrawal(
            id = "w-2",
            groupId = groupId,
            amountWithdrawn = 500000L,
            remainingAmount = 500000L,
            currency = "THB",
            deductedBaseAmount = 13587L,
            createdAt = LocalDateTime.of(2026, 1, 12, 12, 0)
        )

        private val fifoResult = ExpenseCalculatorService.FifoCashResult(
            groupAmountCents = 621L, tranches = listOf(
                CashTranche(withdrawalId = "w-1", amountConsumed = 5000L),
                CashTranche(withdrawalId = "w-2", amountConsumed = 18000L)
            )
        )

        @BeforeEach
        fun setUpCash() {
            coEvery {
                cashWithdrawalRepository.getAvailableWithdrawals(groupId, "THB")
            } returns listOf(withdrawal1, withdrawal2)

            // Sufficient cash – the guard must pass so the FIFO path is exercised.
            every {
                expenseCalculatorService.hasInsufficientCash(
                    cashExpense.sourceAmount, listOf(withdrawal1, withdrawal2)
                )
            } returns false

            coEvery {
                expenseCalculatorService.calculateFifoCashAmount(
                    amountToCover = cashExpense.sourceAmount,
                    availableWithdrawals = listOf(withdrawal1, withdrawal2)
                )
            } returns fifoResult

            coEvery { cashWithdrawalRepository.updateRemainingAmounts(any(), any()) } just Runs
            coEvery { expenseRepository.addExpense(any(), any()) } just Runs
        }

        @Test
        fun `calls updateRemainingAmounts exactly once for multi-tranche expense`() = runTest {
            useCase(groupId, cashExpense)

            // Must be exactly one batch call, never the single-update method
            coVerify(exactly = 1) { cashWithdrawalRepository.updateRemainingAmounts(any(), any()) }
            coVerify(exactly = 0) { cashWithdrawalRepository.updateRemainingAmount(any(), any()) }
        }

        @Test
        fun `passes groupId to updateRemainingAmounts`() = runTest {
            val groupIdSlot = slot<String>()
            coEvery {
                cashWithdrawalRepository.updateRemainingAmounts(capture(groupIdSlot), any())
            } just Runs

            useCase(groupId, cashExpense)

            assertEquals(groupId, groupIdSlot.captured)
        }

        @Test
        fun `passes correctly deducted withdrawal objects to updateRemainingAmounts`() = runTest {
            val withdrawalsSlot = slot<List<CashWithdrawal>>()
            coEvery {
                cashWithdrawalRepository.updateRemainingAmounts(any(), capture(withdrawalsSlot))
            } just Runs

            useCase(groupId, cashExpense)

            val updated = withdrawalsSlot.captured
            assertEquals(2, updated.size)

            val updatedW1 = updated.first { it.id == "w-1" }
            assertEquals(0L, updatedW1.remainingAmount) // 5000 - 5000

            val updatedW2 = updated.first { it.id == "w-2" }
            assertEquals(482000L, updatedW2.remainingAmount) // 500000 - 18000
        }

        @Test
        fun `attaches tranches and blended group amount to saved expense`() = runTest {
            val savedExpenseSlot = slot<Expense>()
            coEvery {
                expenseRepository.addExpense(any(), capture(savedExpenseSlot))
            } just Runs

            useCase(groupId, cashExpense)

            val saved = savedExpenseSlot.captured
            assertEquals(fifoResult.tranches, saved.cashTranches)
            assertEquals(fifoResult.groupAmountCents, saved.groupAmount)
        }

        @Test
        fun `single-tranche cash expense still uses batch call`() = runTest {
            val singleTranche = fifoResult.copy(
                tranches = listOf(CashTranche(withdrawalId = "w-1", amountConsumed = 5000L))
            )
            coEvery {
                expenseCalculatorService.calculateFifoCashAmount(any(), any())
            } returns singleTranche

            useCase(groupId, cashExpense)

            coVerify(exactly = 1) { cashWithdrawalRepository.updateRemainingAmounts(any(), any()) }
            coVerify(exactly = 0) { cashWithdrawalRepository.updateRemainingAmount(any(), any()) }
        }
    }

    // ── Insufficient cash ─────────────────────────────────────────────────────

    @Nested
    inner class InsufficientCash {

        private val cashExpense = baseExpense.copy(
            paymentMethod = PaymentMethod.CASH,
            sourceCurrency = "THB",
            sourceAmount = 50000L  // More than available
        )

        private val withdrawal = CashWithdrawal(
            id = "w-1",
            groupId = groupId,
            amountWithdrawn = 100000L,
            remainingAmount = 32000L,  // Less than required
            currency = "THB",
            deductedBaseAmount = 86400L,
            createdAt = LocalDateTime.of(2026, 1, 10, 12, 0)
        )

        @BeforeEach
        fun setUpInsufficientCash() {
            coEvery {
                cashWithdrawalRepository.getAvailableWithdrawals(groupId, "THB")
            } returns listOf(withdrawal)

            every {
                expenseCalculatorService.hasInsufficientCash(
                    cashExpense.sourceAmount,
                    listOf(withdrawal)
                )
            } returns true
        }

        @Test
        fun `fails with InsufficientCashException when cash is not enough`() = runTest {
            val result = useCase(groupId, cashExpense)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is InsufficientCashException)
        }

        @Test
        fun `exception carries required and available cent values`() = runTest {
            val result = useCase(groupId, cashExpense)

            val exception = result.exceptionOrNull() as InsufficientCashException
            assertEquals(cashExpense.sourceAmount, exception.requiredCents)
            assertEquals(listOf(withdrawal).sumOf { it.remainingAmount }, exception.availableCents)
        }

        @Test
        fun `does not save expense when cash is insufficient`() = runTest {
            useCase(groupId, cashExpense)

            coVerify(exactly = 0) { expenseRepository.addExpense(any(), any()) }
        }

        @Test
        fun `does not update withdrawals when cash is insufficient`() = runTest {
            useCase(groupId, cashExpense)

            coVerify(exactly = 0) { cashWithdrawalRepository.updateRemainingAmounts(any(), any()) }
        }
    }
}

