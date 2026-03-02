package es.pedrazamiguez.expenseshareapp.domain.usecase.expense

import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DeleteExpenseUseCaseTest {

    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var useCase: DeleteExpenseUseCase

    @BeforeEach
    fun setUp() {
        expenseRepository = mockk()
        useCase = DeleteExpenseUseCase(expenseRepository)
    }

    @Nested
    inner class Invocation {

        @Test
        fun `delegates to repository deleteExpense`() = runTest {
            // Given
            val groupId = "group-123"
            val expenseId = "expense-456"
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
}

