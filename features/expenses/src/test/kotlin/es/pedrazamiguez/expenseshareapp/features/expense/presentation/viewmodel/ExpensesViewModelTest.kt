package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel

import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.ExpenseUiMapper
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase
    private lateinit var expenseUiMapper: ExpenseUiMapper
    private lateinit var viewModel: ExpensesViewModel

    private val testGroupId = "group-123"
    private val testExpense1 = Expense(
        id = "expense-1",
        groupId = testGroupId,
        title = "Dinner",
        sourceAmount = 5000L,
        sourceCurrency = "EUR",
        groupAmount = 5000L,
        groupCurrency = "EUR",
        paymentMethod = PaymentMethod.CREDIT_CARD,
        createdBy = "user-1",
        createdAt = LocalDateTime.of(2024, 1, 15, 12, 30)
    )

    private val testExpense2 = Expense(
        id = "expense-2",
        groupId = testGroupId,
        title = "Taxi",
        sourceAmount = 2000L,
        sourceCurrency = "EUR",
        groupAmount = 2000L,
        groupCurrency = "EUR",
        paymentMethod = PaymentMethod.CASH,
        createdBy = "user-2",
        createdAt = LocalDateTime.of(2024, 1, 16, 10, 0)
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getGroupExpensesFlowUseCase = mockk()
        expenseUiMapper = ExpenseUiMapper()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class StateManagement {

        @Test
        fun `initial state is loading`() = runTest(testDispatcher) {
            // Given
            every { getGroupExpensesFlowUseCase(any()) } returns flowOf(emptyList())

            // When
            viewModel = ExpensesViewModel(getGroupExpensesFlowUseCase, expenseUiMapper)

            // Then
            val state = viewModel.uiState.value
            assertTrue(state.isLoading)
            assertTrue(state.expenses.isEmpty())
            assertNull(state.groupId)
        }

        @Test
        fun `setSelectedGroup updates state with expenses`() = runTest(testDispatcher) {
            // Given
            every { getGroupExpensesFlowUseCase(testGroupId) } returns flowOf(
                listOf(testExpense1, testExpense2)
            )
            viewModel = ExpensesViewModel(getGroupExpensesFlowUseCase, expenseUiMapper)

            // When
            viewModel.setSelectedGroup(testGroupId)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(2, state.expenses.size)
            assertEquals(testGroupId, state.groupId)
            assertEquals("Dinner", state.expenses[0].title)
            assertEquals("Taxi", state.expenses[1].title)
        }

        @Test
        fun `changing group triggers new data load`() = runTest(testDispatcher) {
            // Given
            val group1Id = "group-1"
            val group2Id = "group-2"
            every { getGroupExpensesFlowUseCase(group1Id) } returns flowOf(listOf(testExpense1))
            every { getGroupExpensesFlowUseCase(group2Id) } returns flowOf(listOf(testExpense2))
            
            viewModel = ExpensesViewModel(getGroupExpensesFlowUseCase, expenseUiMapper)

            // When - Load first group
            viewModel.setSelectedGroup(group1Id)
            advanceUntilIdle()

            // Then - Verify first group loaded
            assertEquals(group1Id, viewModel.uiState.value.groupId)
            assertEquals(1, viewModel.uiState.value.expenses.size)
            assertEquals("Dinner", viewModel.uiState.value.expenses[0].title)

            // When - Switch to second group
            viewModel.setSelectedGroup(group2Id)
            advanceUntilIdle()

            // Then - Verify second group loaded
            assertEquals(group2Id, viewModel.uiState.value.groupId)
            assertEquals(1, viewModel.uiState.value.expenses.size)
            assertEquals("Taxi", viewModel.uiState.value.expenses[0].title)
        }

        @Test
        fun `setSelectedGroup with same groupId does not reload`() = runTest(testDispatcher) {
            // Given
            var callCount = 0
            every { getGroupExpensesFlowUseCase(testGroupId) } answers {
                callCount++
                flowOf(listOf(testExpense1))
            }
            viewModel = ExpensesViewModel(getGroupExpensesFlowUseCase, expenseUiMapper)

            // When - Set same group twice
            viewModel.setSelectedGroup(testGroupId)
            advanceUntilIdle()
            val initialCallCount = callCount

            viewModel.setSelectedGroup(testGroupId)
            advanceUntilIdle()

            // Then - Should not trigger additional calls
            assertEquals(initialCallCount, callCount)
        }
    }

    @Nested
    inner class GracePeriodLogic {

        @Test
        fun `empty list shows loading state during grace period`() = runTest(testDispatcher) {
            // Given
            every { getGroupExpensesFlowUseCase(testGroupId) } returns flowOf(emptyList())
            viewModel = ExpensesViewModel(getGroupExpensesFlowUseCase, expenseUiMapper)

            // When
            viewModel.setSelectedGroup(testGroupId)
            advanceTimeBy(50) // Advance less than grace period (300ms)

            // Then - Should still be in loading state during grace period
            assertTrue(viewModel.uiState.value.isLoading)
        }

        @Test
        fun `empty list shows empty state after grace period`() = runTest(testDispatcher) {
            // Given
            every { getGroupExpensesFlowUseCase(testGroupId) } returns flowOf(emptyList())
            viewModel = ExpensesViewModel(getGroupExpensesFlowUseCase, expenseUiMapper)

            // When
            viewModel.setSelectedGroup(testGroupId)
            advanceTimeBy(350) // Advance past grace period (300ms)

            // Then - Should show empty state
            assertFalse(viewModel.uiState.value.isLoading)
            assertTrue(viewModel.uiState.value.expenses.isEmpty())
            assertEquals(testGroupId, viewModel.uiState.value.groupId)
        }

        @Test
        fun `non-empty list bypasses grace period`() = runTest(testDispatcher) {
            // Given
            every { getGroupExpensesFlowUseCase(testGroupId) } returns flowOf(
                listOf(testExpense1)
            )
            viewModel = ExpensesViewModel(getGroupExpensesFlowUseCase, expenseUiMapper)

            // When
            viewModel.setSelectedGroup(testGroupId)
            advanceTimeBy(50) // Advance minimal time

            // Then - Should immediately show data without grace period delay
            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(1, viewModel.uiState.value.expenses.size)
        }

        @Test
        fun `grace period prevents flicker when switching from loading to empty`() = runTest(testDispatcher) {
            // Given
            every { getGroupExpensesFlowUseCase(testGroupId) } returns flowOf(emptyList())
            viewModel = ExpensesViewModel(getGroupExpensesFlowUseCase, expenseUiMapper)

            // When - Set selected group
            viewModel.setSelectedGroup(testGroupId)

            // Then - Initial loading state
            advanceTimeBy(10)
            var state = viewModel.uiState.value
            assertTrue(state.isLoading)
            assertEquals(testGroupId, state.groupId)

            // Then - Still loading during grace period (no empty state flicker)
            advanceTimeBy(100)
            state = viewModel.uiState.value
            assertTrue(state.isLoading)
            assertEquals(testGroupId, state.groupId)

            // Then - Finally shows empty state after grace period
            advanceTimeBy(300)
            state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.expenses.isEmpty())
            assertEquals(testGroupId, state.groupId)
        }
    }

    @Nested
    inner class ErrorHandling {

        @Test
        fun `error in flow sets error state`() = runTest(testDispatcher) {
            // Given
            val errorMessage = "Network error"
            every { getGroupExpensesFlowUseCase(testGroupId) } returns flow {
                throw RuntimeException(errorMessage)
            }
            viewModel = ExpensesViewModel(getGroupExpensesFlowUseCase, expenseUiMapper)

            // When
            viewModel.setSelectedGroup(testGroupId)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNotNull(state.errorMessage)
            assertTrue(state.expenses.isEmpty())
        }
    }

    @Nested
    inner class RefreshLogic {

        @Test
        fun `LoadExpenses event triggers refresh`() = runTest(testDispatcher) {
            // Given
            var emissionCount = 0
            every { getGroupExpensesFlowUseCase(testGroupId) } answers {
                emissionCount++
                flowOf(listOf(testExpense1))
            }
            viewModel = ExpensesViewModel(getGroupExpensesFlowUseCase, expenseUiMapper)
            
            viewModel.setSelectedGroup(testGroupId)
            advanceUntilIdle()
            val initialEmissions = emissionCount

            // When - Trigger refresh
            viewModel.onEvent(es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.ExpensesUiEvent.LoadExpenses)
            advanceUntilIdle()

            // Then - Should have triggered new emissions
            assertTrue(emissionCount > initialEmissions, "Expected more emissions after refresh")
        }

        @Test
        fun `refresh does not change selected group`() = runTest(testDispatcher) {
            // Given
            every { getGroupExpensesFlowUseCase(testGroupId) } returns flowOf(listOf(testExpense1))
            viewModel = ExpensesViewModel(getGroupExpensesFlowUseCase, expenseUiMapper)
            
            viewModel.setSelectedGroup(testGroupId)
            advanceUntilIdle()

            // When
            viewModel.onEvent(es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.ExpensesUiEvent.LoadExpenses)
            advanceUntilIdle()

            // Then
            assertEquals(testGroupId, viewModel.uiState.value.groupId)
        }
    }

    @Nested
    inner class ScrollPositionTracking {

        @Test
        fun `ScrollPositionChanged updates scroll state`() = runTest(testDispatcher) {
            // Given
            every { getGroupExpensesFlowUseCase(any()) } returns flowOf(emptyList())
            viewModel = ExpensesViewModel(getGroupExpensesFlowUseCase, expenseUiMapper)

            // When
            viewModel.onEvent(
                es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.ExpensesUiEvent.ScrollPositionChanged(
                    index = 5,
                    offset = 100
                )
            )
            advanceUntilIdle()

            // Then
            assertEquals(5, viewModel.uiState.value.scrollPosition)
            assertEquals(100, viewModel.uiState.value.scrollOffset)
        }

        @Test
        fun `scroll position persists across group changes`() = runTest(testDispatcher) {
            // Given
            every { getGroupExpensesFlowUseCase(any()) } returns flowOf(listOf(testExpense1))
            viewModel = ExpensesViewModel(getGroupExpensesFlowUseCase, expenseUiMapper)

            // When - Set scroll position and change group
            viewModel.onEvent(
                es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.ExpensesUiEvent.ScrollPositionChanged(
                    index = 3,
                    offset = 50
                )
            )
            viewModel.setSelectedGroup("group-1")
            viewModel.setSelectedGroup("group-2")
            advanceUntilIdle()

            // Then - Scroll position should persist
            assertEquals(3, viewModel.uiState.value.scrollPosition)
            assertEquals(50, viewModel.uiState.value.scrollOffset)
        }
    }
}
