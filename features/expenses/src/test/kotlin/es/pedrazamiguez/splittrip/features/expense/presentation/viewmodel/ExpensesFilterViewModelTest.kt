package es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel

import es.pedrazamiguez.splittrip.domain.enums.ExpenseCategory
import es.pedrazamiguez.splittrip.domain.enums.PaymentMethod
import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.domain.model.ExpenseFilterCriteria
import es.pedrazamiguez.splittrip.domain.service.ExpenseFilterService
import es.pedrazamiguez.splittrip.domain.service.impl.ExpenseFilterServiceImpl
import es.pedrazamiguez.splittrip.domain.service.impl.ExpenseSearchServiceImpl
import es.pedrazamiguez.splittrip.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.action.ExpensesFilterUiAction
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.event.ExpensesFilterUiEvent
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesFilterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase
    private lateinit var expenseFilterService: ExpenseFilterService
    private lateinit var viewModel: ExpensesFilterViewModel

    private val testGroupId = "group-123"

    private val expense1 = Expense(
        id = "exp-1",
        groupId = testGroupId,
        title = "Dinner",
        category = ExpenseCategory.FOOD,
        sourceAmount = 5000L,
        groupAmount = 5000L,
        paymentMethod = PaymentMethod.CREDIT_CARD,
        createdBy = "user-1",
        createdAt = LocalDateTime.of(2024, 1, 15, 12, 30)
    )

    private val expense2 = Expense(
        id = "exp-2",
        groupId = testGroupId,
        title = "Taxi",
        category = ExpenseCategory.TRANSPORT,
        sourceAmount = 2000L,
        groupAmount = 2000L,
        paymentMethod = PaymentMethod.CASH,
        createdBy = "user-2",
        createdAt = LocalDateTime.of(2024, 1, 16, 10, 0)
    )

    private val allExpenses = listOf(expense1, expense2)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getGroupExpensesFlowUseCase = mockk()
        expenseFilterService = ExpenseFilterServiceImpl(expenseSearchService = ExpenseSearchServiceImpl())

        every { getGroupExpensesFlowUseCase(testGroupId) } returns flowOf(allExpenses)
        viewModel = ExpensesFilterViewModel(
            getGroupExpensesFlowUseCase = getGroupExpensesFlowUseCase,
            expenseFilterService = expenseFilterService
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    @DisplayName("State and Live Match Calculation")
    inner class StateAndMatching {

        @Test
        fun `initial state is loading`() = runTest(testDispatcher) {
            assertTrue(viewModel.uiState.value.isLoading)
        }

        @Test
        fun `setting group loads total count and matching count`() = runTest(testDispatcher) {
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.setSelectedGroup(testGroupId)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(2, state.totalExpensesCount)
            assertEquals(2, state.matchingExpensesCount)
            assertEquals(testGroupId, state.groupId)
            assertFalse(state.canReset)

            collectJob.cancel()
        }

        @Test
        fun `initialize sets draft criteria and calculates match count`() = runTest(testDispatcher) {
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setSelectedGroup(testGroupId)
            advanceUntilIdle()

            val initialCriteria = ExpenseFilterCriteria(
                selectedCategories = setOf(ExpenseCategory.FOOD)
            )
            viewModel.onEvent(ExpensesFilterUiEvent.Initialize(initialCriteria))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(initialCriteria, state.draftCriteria)
            assertEquals(1, state.matchingExpensesCount)
            assertEquals(2, state.totalExpensesCount)
            assertTrue(state.canReset)

            collectJob.cancel()
        }

        @Test
        fun `subsequent initialize calls are ignored`() = runTest(testDispatcher) {
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setSelectedGroup(testGroupId)
            advanceUntilIdle()

            val firstCriteria = ExpenseFilterCriteria(selectedCategories = setOf(ExpenseCategory.FOOD))
            val secondCriteria = ExpenseFilterCriteria(selectedCategories = setOf(ExpenseCategory.TRANSPORT))

            viewModel.onEvent(ExpensesFilterUiEvent.Initialize(firstCriteria))
            viewModel.onEvent(ExpensesFilterUiEvent.Initialize(secondCriteria))
            advanceUntilIdle()

            assertEquals(firstCriteria, viewModel.uiState.value.draftCriteria)
            assertEquals(1, viewModel.uiState.value.matchingExpensesCount)

            collectJob.cancel()
        }

        @Test
        fun `updateDraft recalculates matching expenses dynamically`() = runTest(testDispatcher) {
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setSelectedGroup(testGroupId)
            advanceUntilIdle()

            viewModel.onEvent(
                ExpensesFilterUiEvent.UpdateDraft(
                    ExpenseFilterCriteria(selectedCategories = setOf(ExpenseCategory.TRANSPORT))
                )
            )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(1, state.matchingExpensesCount)
            assertTrue(state.canReset)

            collectJob.cancel()
        }

        @Test
        fun `resetDraft clears non-search filter dimensions`() = runTest(testDispatcher) {
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.setSelectedGroup(testGroupId)
            advanceUntilIdle()

            val initialCriteria = ExpenseFilterCriteria(
                searchQuery = "Din",
                selectedCategories = setOf(ExpenseCategory.FOOD)
            )
            viewModel.onEvent(ExpensesFilterUiEvent.Initialize(initialCriteria))
            advanceUntilIdle()

            viewModel.onEvent(ExpensesFilterUiEvent.ResetDraft)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Din", state.draftCriteria.searchQuery)
            assertTrue(state.draftCriteria.selectedCategories.isEmpty())
            assertEquals(0, state.draftCriteria.activeFilterCount)
            assertFalse(state.canReset)

            collectJob.cancel()
        }
    }

    @Nested
    @DisplayName("Action Emissions")
    inner class ActionEmissions {

        @Test
        fun `applyFilters emits ApplyAndNavigateBack action with current draft criteria`() = runTest(testDispatcher) {
            val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
            val actions = mutableListOf<ExpensesFilterUiAction>()
            val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.actions.collect { actions.add(it) }
            }

            viewModel.setSelectedGroup(testGroupId)
            advanceUntilIdle()

            val filterCriteria = ExpenseFilterCriteria(selectedCategories = setOf(ExpenseCategory.FOOD))
            viewModel.onEvent(ExpensesFilterUiEvent.UpdateDraft(filterCriteria))
            advanceUntilIdle()

            viewModel.onEvent(ExpensesFilterUiEvent.ApplyFilters)
            advanceUntilIdle()

            assertEquals(1, actions.size)
            val action = actions.first() as ExpensesFilterUiAction.ApplyAndNavigateBack
            assertEquals(filterCriteria, action.appliedCriteria)

            actionsJob.cancel()
            collectJob.cancel()
        }
    }
}
