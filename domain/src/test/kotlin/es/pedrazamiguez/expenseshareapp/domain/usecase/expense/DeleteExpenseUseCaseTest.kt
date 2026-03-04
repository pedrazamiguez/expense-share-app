package es.pedrazamiguez.expenseshareapp.domain.usecase.expense

import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.CashTranche
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DeleteExpenseUseCaseTest {

    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var cashWithdrawalRepository: CashWithdrawalRepository
    private lateinit var useCase: DeleteExpenseUseCase

    @BeforeEach
    fun setUp() {
        expenseRepository = mockk()
        cashWithdrawalRepository = mockk(relaxed = true)
        useCase = DeleteExpenseUseCase(expenseRepository, cashWithdrawalRepository)
    }

    @Nested
    inner class Invocation {

        @Test
        fun `delegates to repository deleteExpense`() = runTest {
            // Given
            val groupId = "group-123"
            val expenseId = "expense-456"
            coEvery { expenseRepository.getExpenseById(expenseId) } returns null
            coEvery { expenseRepository.deleteExpense(groupId, expenseId) } just Runs

            // When
            useCase(groupId, expenseId)

            // Then
            coVerify(exactly = 1) { expenseRepository.deleteExpense(groupId, expenseId) }
        }

        @Test
        fun `passes correct groupId and expenseId to repository`() = runTest {
            // Given
            val groupId = "specific-group-id-789"
            val expenseId = "specific-expense-id-012"
            coEvery { expenseRepository.getExpenseById(any()) } returns null
            coEvery { expenseRepository.deleteExpense(any(), any()) } just Runs

            // When
            useCase(groupId, expenseId)

            // Then
            coVerify { expenseRepository.deleteExpense(groupId, expenseId) }
        }

        @Test
        fun `propagates exception from repository`() = runTest {
            // Given
            val groupId = "group-123"
            val expenseId = "expense-456"
            val exception = RuntimeException("Delete failed")
            coEvery { expenseRepository.getExpenseById(any()) } returns null
            coEvery { expenseRepository.deleteExpense(groupId, expenseId) } throws exception

            // When/Then
            try {
                useCase(groupId, expenseId)
                assert(false) { "Expected exception to be thrown" }
            } catch (e: RuntimeException) {
                assert(e.message == "Delete failed")
            }
        }
    }

    @Nested
    inner class CashTrancheRefund {

        @Test
        fun `refunds cash tranches when deleting cash expense`() = runTest {
            // Given
            val groupId = "group-123"
            val expenseId = "expense-456"
            val expense = Expense(
                id = expenseId,
                groupId = groupId,
                title = "Souvenir",
                sourceAmount = 23000L,
                sourceCurrency = "THB",
                groupAmount = 621L,
                groupCurrency = "EUR",
                paymentMethod = PaymentMethod.CASH,
                cashTranches = listOf(
                    CashTranche(withdrawalId = "w-1", amountConsumed = 5000L),
                    CashTranche(withdrawalId = "w-2", amountConsumed = 18000L)
                )
            )
            coEvery { expenseRepository.getExpenseById(expenseId) } returns expense
            coEvery { expenseRepository.deleteExpense(groupId, expenseId) } just Runs
            coEvery { cashWithdrawalRepository.refundTranche(any(), any()) } just Runs

            // When
            useCase(groupId, expenseId)

            // Then - Should refund both tranches
            coVerify { cashWithdrawalRepository.refundTranche("w-1", 5000L) }
            coVerify { cashWithdrawalRepository.refundTranche("w-2", 18000L) }
            coVerify { expenseRepository.deleteExpense(groupId, expenseId) }
        }

        @Test
        fun `does not refund when expense has no cash tranches`() = runTest {
            // Given
            val groupId = "group-123"
            val expenseId = "expense-456"
            val expense = Expense(
                id = expenseId,
                groupId = groupId,
                title = "Dinner",
                sourceAmount = 5000L,
                sourceCurrency = "EUR",
                groupAmount = 5000L,
                groupCurrency = "EUR",
                paymentMethod = PaymentMethod.CREDIT_CARD,
                cashTranches = emptyList()
            )
            coEvery { expenseRepository.getExpenseById(expenseId) } returns expense
            coEvery { expenseRepository.deleteExpense(groupId, expenseId) } just Runs

            // When
            useCase(groupId, expenseId)

            // Then - No refund calls
            coVerify(exactly = 0) { cashWithdrawalRepository.refundTranche(any(), any()) }
            coVerify { expenseRepository.deleteExpense(groupId, expenseId) }
        }

        @Test
        fun `does not refund when expense not found`() = runTest {
            // Given
            val groupId = "group-123"
            val expenseId = "expense-456"
            coEvery { expenseRepository.getExpenseById(expenseId) } returns null
            coEvery { expenseRepository.deleteExpense(groupId, expenseId) } just Runs

            // When
            useCase(groupId, expenseId)

            // Then - No refund calls
            coVerify(exactly = 0) { cashWithdrawalRepository.refundTranche(any(), any()) }
            coVerify { expenseRepository.deleteExpense(groupId, expenseId) }
        }
    }
}

