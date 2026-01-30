package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel

import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.model.GroupExpenseConfig
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddExpenseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var addExpenseUseCase: AddExpenseUseCase
    private lateinit var getGroupExpenseConfigUseCase: GetGroupExpenseConfigUseCase
    private lateinit var expenseCalculatorService: ExpenseCalculatorService
    private lateinit var addExpenseUiMapper: AddExpenseUiMapper

    private lateinit var viewModel: AddExpenseViewModel

    private val eur = Currency(
        code = "EUR",
        symbol = "€",
        defaultName = "Euro",
        decimalDigits = 2
    )

    private val jpy = Currency(
        code = "JPY",
        symbol = "¥",
        defaultName = "Japanese Yen",
        decimalDigits = 0
    )

    private val usd = Currency(
        code = "USD",
        symbol = "$",
        defaultName = "US Dollar",
        decimalDigits = 2
    )

    private val groupEur = Group(
        id = "group-eur",
        name = "Europe Trip",
        currency = "EUR",
        extraCurrencies = listOf("USD")
    )

    private val groupJpy = Group(
        id = "group-jpy",
        name = "Japan Trip",
        currency = "JPY",
        extraCurrencies = listOf("USD")
    )

    private val configEur = GroupExpenseConfig(
        group = groupEur,
        groupCurrency = eur,
        availableCurrencies = listOf(eur, usd)
    )

    private val configJpy = GroupExpenseConfig(
        group = groupJpy,
        groupCurrency = jpy,
        availableCurrencies = listOf(jpy, usd)
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        addExpenseUseCase = mockk()
        getGroupExpenseConfigUseCase = mockk()
        expenseCalculatorService = mockk(relaxed = true)
        addExpenseUiMapper = AddExpenseUiMapper()

        viewModel = AddExpenseViewModel(
            addExpenseUseCase = addExpenseUseCase,
            getGroupExpenseConfigUseCase = getGroupExpenseConfigUseCase,
            expenseCalculatorService = expenseCalculatorService,
            addExpenseUiMapper = addExpenseUiMapper
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class LoadGroupConfig {

        @Test
        fun `loads config successfully and updates state`() = runTest {
            // Given
            coEvery { getGroupExpenseConfigUseCase("group-eur", any()) } returns Result.success(
                configEur
            )

            // When
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state.isConfigLoaded)
            assertFalse(state.configLoadFailed)
            assertEquals("group-eur", state.loadedGroupId)
            assertEquals("Europe Trip", state.groupName)
            assertEquals(eur, state.groupCurrency)
            assertEquals(eur, state.selectedCurrency)
            assertEquals(2, state.availableCurrencies.size)
        }

        @Test
        fun `does not reload config for same group on subsequent calls`() = runTest {
            // Given
            coEvery { getGroupExpenseConfigUseCase("group-eur", any()) } returns Result.success(
                configEur
            )

            // When - First load
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()

            // When - Second load for same group (e.g., screen rotation)
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()

            // Then - UseCase should only be called once
            coVerify(exactly = 1) { getGroupExpenseConfigUseCase("group-eur", any()) }
        }

        @Test
        fun `reloads config when group changes - EUR to JPY`() = runTest {
            // Given
            coEvery { getGroupExpenseConfigUseCase("group-eur", any()) } returns Result.success(
                configEur
            )
            coEvery { getGroupExpenseConfigUseCase("group-jpy", any()) } returns Result.success(
                configJpy
            )

            // When - Load EUR group first
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()

            // Verify EUR config loaded
            assertEquals(eur, viewModel.uiState.value.groupCurrency)
            assertEquals("group-eur", viewModel.uiState.value.loadedGroupId)
            assertEquals("Europe Trip", viewModel.uiState.value.groupName)

            // When - Change to JPY group
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-jpy"))
            advanceUntilIdle()

            // Then - JPY config should be loaded
            val state = viewModel.uiState.value
            assertEquals(jpy, state.groupCurrency)
            assertEquals(jpy, state.selectedCurrency)
            assertEquals("group-jpy", state.loadedGroupId)
            assertEquals("Japan Trip", state.groupName)
            assertTrue(state.availableCurrencies.any { it.code == "JPY" })
            assertFalse(state.availableCurrencies.any { it.code == "EUR" })
        }

        @Test
        fun `resets form state when group changes`() = runTest {
            // Given
            coEvery { getGroupExpenseConfigUseCase("group-eur", any()) } returns Result.success(
                configEur
            )
            coEvery { getGroupExpenseConfigUseCase("group-jpy", any()) } returns Result.success(
                configJpy
            )

            // When - Load EUR group and fill form
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()
            viewModel.onEvent(AddExpenseUiEvent.TitleChanged("Dinner"))
            viewModel.onEvent(AddExpenseUiEvent.SourceAmountChanged("50.00"))

            // Verify form has data
            assertEquals("Dinner", viewModel.uiState.value.expenseTitle)
            assertEquals("50.00", viewModel.uiState.value.sourceAmount)

            // When - Change to JPY group
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-jpy"))
            advanceUntilIdle()

            // Then - Form should be reset
            val state = viewModel.uiState.value
            assertEquals("", state.expenseTitle)
            assertEquals("", state.sourceAmount)
            assertEquals(jpy, state.selectedCurrency)
        }

        @Test
        fun `handles config load failure`() = runTest {
            // Given
            coEvery { getGroupExpenseConfigUseCase("group-eur", any()) } returns
                    Result.failure(RuntimeException("Network error"))

            // When
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isConfigLoaded)
            assertTrue(state.configLoadFailed)
            assertFalse(state.isLoading)
        }

        @Test
        fun `retry loads config with forceRefresh`() = runTest {
            // Given - First load fails
            coEvery { getGroupExpenseConfigUseCase("group-eur", false) } returns
                    Result.failure(RuntimeException("Network error"))
            coEvery { getGroupExpenseConfigUseCase("group-eur", true) } returns
                    Result.success(configEur)

            // When - Initial load fails
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.configLoadFailed)

            // When - Retry
            viewModel.onEvent(AddExpenseUiEvent.RetryLoadConfig("group-eur"))
            advanceUntilIdle()

            // Then - Config should be loaded
            val state = viewModel.uiState.value
            assertTrue(state.isConfigLoaded)
            assertFalse(state.configLoadFailed)
            assertEquals(eur, state.groupCurrency)

            // Verify forceRefresh was true on retry
            coVerify { getGroupExpenseConfigUseCase("group-eur", true) }
        }

        @Test
        fun `ignores null groupId`() = runTest {
            // When
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig(null))
            advanceUntilIdle()

            // Then - State should remain initial
            val state = viewModel.uiState.value
            assertFalse(state.isConfigLoaded)
            assertFalse(state.configLoadFailed)
            assertFalse(state.isLoading)
        }
    }

    @Nested
    inner class GroupChangeScenarios {

        /**
         * This test reproduces the exact bug scenario:
         * 1. User opens add expense for group A (EUR)
         * 2. User navigates away without adding expense
         * 3. User selects group B (JPY)
         * 4. User returns to add expense screen
         * 5. Currency should now show JPY, not EUR
         */
        @Test
        fun `currency updates when user switches groups while on add expense screen`() = runTest {
            // Given
            coEvery { getGroupExpenseConfigUseCase("group-eur", any()) } returns Result.success(
                configEur
            )
            coEvery { getGroupExpenseConfigUseCase("group-jpy", any()) } returns Result.success(
                configJpy
            )

            // Step 1: User opens add expense for EUR group
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()

            assertEquals(eur, viewModel.uiState.value.selectedCurrency)
            assertEquals("EUR", viewModel.uiState.value.groupCurrency?.code)

            // Step 2 & 3: User navigates away and selects JPY group
            // (This happens via SharedViewModel, simulated by calling LoadGroupConfig with new groupId)

            // Step 4: User returns to add expense screen (LaunchedEffect triggers with new groupId)
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-jpy"))
            advanceUntilIdle()

            // Step 5: Currency should now show JPY
            val finalState = viewModel.uiState.value
            assertEquals(jpy, finalState.selectedCurrency)
            assertEquals("JPY", finalState.groupCurrency?.code)
            assertEquals("group-jpy", finalState.loadedGroupId)
        }

        @Test
        fun `switching groups multiple times always shows correct currency`() = runTest {
            // Given
            coEvery { getGroupExpenseConfigUseCase("group-eur", any()) } returns Result.success(
                configEur
            )
            coEvery { getGroupExpenseConfigUseCase("group-jpy", any()) } returns Result.success(
                configJpy
            )

            // EUR -> JPY -> EUR -> JPY
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()
            assertEquals(eur, viewModel.uiState.value.selectedCurrency)

            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-jpy"))
            advanceUntilIdle()
            assertEquals(jpy, viewModel.uiState.value.selectedCurrency)

            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()
            assertEquals(eur, viewModel.uiState.value.selectedCurrency)

            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-jpy"))
            advanceUntilIdle()
            assertEquals(jpy, viewModel.uiState.value.selectedCurrency)

            // All 4 loads should have happened since groupId changed each time
            coVerify(exactly = 2) { getGroupExpenseConfigUseCase("group-eur", any()) }
            coVerify(exactly = 2) { getGroupExpenseConfigUseCase("group-jpy", any()) }
        }
    }
}
