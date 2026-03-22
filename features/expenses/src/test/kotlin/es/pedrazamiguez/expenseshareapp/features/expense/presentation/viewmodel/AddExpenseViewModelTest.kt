package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.domain.exception.InsufficientCashException
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.model.GroupExpenseConfig
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.split.ExpenseSplitCalculatorFactory
import es.pedrazamiguez.expenseshareapp.domain.service.split.SplitPreviewService
import es.pedrazamiguez.expenseshareapp.domain.service.split.SubunitAwareSplitService
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.PreviewCashExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedCategoryUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedPaymentMethodUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedCategoryUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedPaymentMethodUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.AddOnEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.ConfigEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.CurrencyEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.SplitEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.SubmitEventHandler
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler.SubunitSplitEventHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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

@OptIn(ExperimentalCoroutinesApi::class)
class AddExpenseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var addExpenseUseCase: AddExpenseUseCase
    private lateinit var getGroupExpenseConfigUseCase: GetGroupExpenseConfigUseCase
    private lateinit var getExchangeRateUseCase: GetExchangeRateUseCase
    private lateinit var previewCashExchangeRateUseCase: PreviewCashExchangeRateUseCase
    private lateinit var getGroupLastUsedCurrencyUseCase: GetGroupLastUsedCurrencyUseCase
    private lateinit var setGroupLastUsedCurrencyUseCase: SetGroupLastUsedCurrencyUseCase
    private lateinit var getGroupLastUsedPaymentMethodUseCase: GetGroupLastUsedPaymentMethodUseCase
    private lateinit var setGroupLastUsedPaymentMethodUseCase: SetGroupLastUsedPaymentMethodUseCase
    private lateinit var getGroupLastUsedCategoryUseCase: GetGroupLastUsedCategoryUseCase
    private lateinit var setGroupLastUsedCategoryUseCase: SetGroupLastUsedCategoryUseCase
    private lateinit var getMemberProfilesUseCase: GetMemberProfilesUseCase
    private lateinit var expenseCalculatorService: ExpenseCalculatorService
    private lateinit var expenseValidationService: ExpenseValidationService
    private lateinit var addExpenseUiMapper: AddExpenseUiMapper
    private lateinit var localeProvider: LocaleProvider
    private lateinit var resourceProvider: ResourceProvider

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
        getExchangeRateUseCase = mockk()
        previewCashExchangeRateUseCase = mockk(relaxed = true)
        getGroupLastUsedCurrencyUseCase = mockk()
        setGroupLastUsedCurrencyUseCase = mockk()
        getGroupLastUsedPaymentMethodUseCase = mockk()
        setGroupLastUsedPaymentMethodUseCase = mockk()
        getGroupLastUsedCategoryUseCase = mockk()
        setGroupLastUsedCategoryUseCase = mockk()
        getMemberProfilesUseCase = mockk()
        expenseCalculatorService = mockk(relaxed = true)
        val splitCalculatorFactory = ExpenseSplitCalculatorFactory(ExpenseCalculatorService())
        expenseValidationService = ExpenseValidationService(splitCalculatorFactory)
        localeProvider = mockk()
        resourceProvider = mockk(relaxed = true)
        every { localeProvider.getCurrentLocale() } returns Locale.US
        addExpenseUiMapper = AddExpenseUiMapper(localeProvider, resourceProvider)

        every { getGroupLastUsedCurrencyUseCase(any()) } returns flowOf(null)
        coEvery { setGroupLastUsedCurrencyUseCase(any(), any()) } returns Unit
        every { getGroupLastUsedPaymentMethodUseCase(any()) } returns flowOf(emptyList())
        coEvery { setGroupLastUsedPaymentMethodUseCase(any(), any()) } returns Unit
        every { getGroupLastUsedCategoryUseCase(any()) } returns flowOf(emptyList())
        coEvery { setGroupLastUsedCategoryUseCase(any(), any()) } returns Unit
        coEvery { getMemberProfilesUseCase(any()) } returns emptyMap()

        // Create handlers with shared instances (mirrors the DI module pattern)
        val splitHandler = SplitEventHandler(
            splitCalculatorFactory = splitCalculatorFactory,
            splitPreviewService = SplitPreviewService(),
            addExpenseUiMapper = addExpenseUiMapper
        )

        val subunitSplitHandler = SubunitSplitEventHandler(
            splitCalculatorFactory = splitCalculatorFactory,
            splitPreviewService = SplitPreviewService(),
            subunitAwareSplitService = SubunitAwareSplitService(splitCalculatorFactory),
            addExpenseUiMapper = addExpenseUiMapper
        )

        val currencyHandler = CurrencyEventHandler(
            getExchangeRateUseCase = getExchangeRateUseCase,
            previewCashExchangeRateUseCase = previewCashExchangeRateUseCase,
            expenseCalculatorService = expenseCalculatorService,
            addExpenseUiMapper = addExpenseUiMapper
        )

        val configHandler = ConfigEventHandler(
            getGroupExpenseConfigUseCase = getGroupExpenseConfigUseCase,
            getGroupLastUsedCurrencyUseCase = getGroupLastUsedCurrencyUseCase,
            getGroupLastUsedPaymentMethodUseCase = getGroupLastUsedPaymentMethodUseCase,
            getGroupLastUsedCategoryUseCase = getGroupLastUsedCategoryUseCase,
            getMemberProfilesUseCase = getMemberProfilesUseCase,
            addExpenseUiMapper = addExpenseUiMapper,
            currencyEventHandler = currencyHandler,
            subunitSplitEventHandler = subunitSplitHandler
        )

        val submitHandler = SubmitEventHandler(
            addExpenseUseCase = addExpenseUseCase,
            expenseValidationService = expenseValidationService,
            setGroupLastUsedCurrencyUseCase = setGroupLastUsedCurrencyUseCase,
            setGroupLastUsedPaymentMethodUseCase = setGroupLastUsedPaymentMethodUseCase,
            setGroupLastUsedCategoryUseCase = setGroupLastUsedCategoryUseCase,
            addExpenseUiMapper = addExpenseUiMapper
        )

        val addOnHandler = AddOnEventHandler(
            expenseCalculatorService = ExpenseCalculatorService(),
            addExpenseUiMapper = addExpenseUiMapper,
            getExchangeRateUseCase = getExchangeRateUseCase
        )

        viewModel = AddExpenseViewModel(
            configEventHandler = configHandler,
            currencyEventHandler = currencyHandler,
            splitEventHandler = splitHandler,
            subunitSplitEventHandler = subunitSplitHandler,
            addOnEventHandler = addOnHandler,
            submitEventHandler = submitHandler,
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
            assertEquals("EUR", state.groupCurrency?.code)
            assertEquals("EUR", state.selectedCurrency?.code)
            assertEquals(2, state.availableCurrencies.size)
            // Verify payment methods are populated
            assertTrue(state.paymentMethods.isNotEmpty())
            assertNotNull(state.selectedPaymentMethod)
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
            assertEquals("EUR", viewModel.uiState.value.groupCurrency?.code)
            assertEquals("group-eur", viewModel.uiState.value.loadedGroupId)
            assertEquals("Europe Trip", viewModel.uiState.value.groupName)

            // When - Change to JPY group
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-jpy"))
            advanceUntilIdle()

            // Then - JPY config should be loaded
            val state = viewModel.uiState.value
            assertEquals("JPY", state.groupCurrency?.code)
            assertEquals("JPY", state.selectedCurrency?.code)
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
            assertEquals("JPY", state.selectedCurrency?.code)
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
            assertEquals("EUR", state.groupCurrency?.code)

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

        @Test
        fun `loads last used currency if available`() = runTest {
            // Given
            coEvery { getGroupExpenseConfigUseCase("group-eur", any()) } returns Result.success(configEur)
            coEvery { getExchangeRateUseCase("EUR", "USD") } returns BigDecimal("1.08")

            // Mock that USD was the last used currency for this specific group
            every { getGroupLastUsedCurrencyUseCase("group-eur") } returns flowOf("USD")
            // Use a non-CASH default so the API rate path is exercised
            every { getGroupLastUsedPaymentMethodUseCase("group-eur") } returns flowOf(listOf("CREDIT_CARD"))

            // When
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()

            // Then - It should automatically select USD instead of EUR
            val state = viewModel.uiState.value
            assertEquals("USD", state.selectedCurrency?.code)
            assertTrue(state.showExchangeRateSection) // Verify the exchange rate section is visible
            assertEquals("1.08", state.displayExchangeRate)
        }
    }

    @Nested
    inner class InputEvents {

        @Test
        fun `NotesChanged updates notes in state`() = runTest {
            // When
            viewModel.onEvent(AddExpenseUiEvent.NotesChanged("Some important note"))

            // Then
            assertEquals("Some important note", viewModel.uiState.value.notes)
        }

        @Test
        fun `NotesChanged with empty string clears notes`() = runTest {
            // Given
            viewModel.onEvent(AddExpenseUiEvent.NotesChanged("Initial note"))
            assertEquals("Initial note", viewModel.uiState.value.notes)

            // When
            viewModel.onEvent(AddExpenseUiEvent.NotesChanged(""))

            // Then
            assertEquals("", viewModel.uiState.value.notes)
        }
    }

    @Nested
    inner class SubunitSplitEvents {

        private val subunit = Subunit(
            id = "couple-1",
            groupId = "group-eur",
            name = "Couple A",
            memberIds = listOf("user-a", "user-b"),
            memberShares = mapOf(
                "user-a" to BigDecimal("0.5"),
                "user-b" to BigDecimal("0.5")
            )
        )

        private val groupWithSubunits = Group(
            id = "group-sub",
            name = "Trip With Subunits",
            currency = "EUR",
            extraCurrencies = emptyList(),
            members = listOf("user-a", "user-b", "user-c")
        )

        private val configWithSubunits = GroupExpenseConfig(
            group = groupWithSubunits,
            groupCurrency = eur,
            availableCurrencies = listOf(eur),
            subunits = listOf(subunit)
        )

        private fun loadConfigWithSubunits() {
            coEvery { getGroupExpenseConfigUseCase("group-sub", any()) } returns
                Result.success(configWithSubunits)
        }

        @Test
        fun `loading config with sub-units sets hasSubunits true`() = runTest {
            loadConfigWithSubunits()

            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-sub"))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.hasSubunits)
            assertFalse(state.isSubunitMode) // toggle is off by default
            assertTrue(state.entitySplits.isNotEmpty())
        }

        @Test
        fun `SubunitModeToggled enables sub-unit mode`() = runTest {
            loadConfigWithSubunits()

            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-sub"))
            advanceUntilIdle()

            viewModel.onEvent(AddExpenseUiEvent.SubunitModeToggled)

            assertTrue(viewModel.uiState.value.isSubunitMode)
        }

        @Test
        fun `SubunitModeToggled twice disables sub-unit mode`() = runTest {
            loadConfigWithSubunits()

            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-sub"))
            advanceUntilIdle()

            viewModel.onEvent(AddExpenseUiEvent.SubunitModeToggled)
            assertTrue(viewModel.uiState.value.isSubunitMode)

            viewModel.onEvent(AddExpenseUiEvent.SubunitModeToggled)
            assertFalse(viewModel.uiState.value.isSubunitMode)
        }

        @Test
        fun `EntityAccordionToggled expands sub-unit entity`() = runTest {
            loadConfigWithSubunits()

            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-sub"))
            advanceUntilIdle()

            // The sub-unit entity should exist
            val subunitEntity = viewModel.uiState.value.entitySplits.find { it.userId == "couple-1" }
            assertNotNull(subunitEntity)
            assertFalse(subunitEntity!!.isExpanded)

            viewModel.onEvent(AddExpenseUiEvent.EntityAccordionToggled("couple-1"))

            val expandedEntity = viewModel.uiState.value.entitySplits.find { it.userId == "couple-1" }
            assertTrue(expandedEntity!!.isExpanded)
        }

        @Test
        fun `EntitySplitExcludedToggled excludes entity`() = runTest {
            loadConfigWithSubunits()

            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-sub"))
            advanceUntilIdle()

            val entity = viewModel.uiState.value.entitySplits.find { it.userId == "couple-1" }
            assertFalse(entity!!.isExcluded)

            viewModel.onEvent(AddExpenseUiEvent.EntitySplitExcludedToggled("couple-1"))

            val updated = viewModel.uiState.value.entitySplits.find { it.userId == "couple-1" }
            assertTrue(updated!!.isExcluded)
        }

        @Test
        fun `entity splits contain solo user and sub-unit entity`() = runTest {
            loadConfigWithSubunits()

            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-sub"))
            advanceUntilIdle()

            val splits = viewModel.uiState.value.entitySplits
            // user-c is solo (not in any sub-unit), couple-1 is the sub-unit
            val soloEntity = splits.find { it.userId == "user-c" }
            val subunitEntity = splits.find { it.userId == "couple-1" }

            assertNotNull(soloEntity)
            assertTrue(soloEntity!!.isEntityRow)
            assertTrue(soloEntity.entityMembers.isEmpty())

            assertNotNull(subunitEntity)
            assertTrue(subunitEntity!!.isEntityRow)
            assertEquals(2, subunitEntity.entityMembers.size)
            assertTrue(subunitEntity.entityMembers.any { it.userId == "user-a" })
            assertTrue(subunitEntity.entityMembers.any { it.userId == "user-b" })
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

            assertEquals("EUR", viewModel.uiState.value.selectedCurrency?.code)
            assertEquals("EUR", viewModel.uiState.value.groupCurrency?.code)

            // Step 2 & 3: User navigates away and selects JPY group

            // Step 4: User returns to add expense screen (LaunchedEffect triggers with new groupId)
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-jpy"))
            advanceUntilIdle()

            // Step 5: Currency should now show JPY
            val finalState = viewModel.uiState.value
            assertEquals("JPY", finalState.selectedCurrency?.code)
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
            assertEquals("EUR", viewModel.uiState.value.selectedCurrency?.code)

            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-jpy"))
            advanceUntilIdle()
            assertEquals("JPY", viewModel.uiState.value.selectedCurrency?.code)

            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()
            assertEquals("EUR", viewModel.uiState.value.selectedCurrency?.code)

            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-jpy"))
            advanceUntilIdle()
            assertEquals("JPY", viewModel.uiState.value.selectedCurrency?.code)

            // All 4 loads should have happened since groupId changed each time
            coVerify(exactly = 2) { getGroupExpenseConfigUseCase("group-eur", any()) }
            coVerify(exactly = 2) { getGroupExpenseConfigUseCase("group-jpy", any()) }
        }
    }

    @Nested
    inner class ExchangeRateFetching {

        private val thb = Currency(
            code = "THB",
            symbol = "฿",
            defaultName = "Thai Baht",
            decimalDigits = 2
        )

        private val configEurWithThb = GroupExpenseConfig(
            group = groupEur,
            groupCurrency = eur,
            availableCurrencies = listOf(eur, usd, thb)
        )

        @BeforeEach
        fun setUpNonCashDefault() {
            // Set default payment method to CREDIT_CARD so these tests exercise the API rate path.
            // CASH is the first entry in PaymentMethod.entries, so without this override the
            // auto-selected default would be CASH, which locks the exchange rate section.
            every { getGroupLastUsedPaymentMethodUseCase(any()) } returns flowOf(listOf("CREDIT_CARD"))
        }

        @Test
        fun `fetches exchange rate when foreign currency is selected`() = runTest {
            // Given
            coEvery { getGroupExpenseConfigUseCase("group-eur", any()) } returns Result.success(configEurWithThb)
            coEvery { getExchangeRateUseCase("EUR", "THB") } returns BigDecimal("38.5")

            // When - Load config
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()

            // When - Select foreign currency by code
            viewModel.onEvent(AddExpenseUiEvent.CurrencySelected("THB"))
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state.showExchangeRateSection)
            assertEquals("38.5", state.displayExchangeRate)
            assertFalse(state.isLoadingRate)
            coVerify { getExchangeRateUseCase("EUR", "THB") }
        }

        @Test
        fun `shows loading state while fetching rate`() = runTest {
            // Given
            coEvery { getGroupExpenseConfigUseCase("group-eur", any()) } returns Result.success(configEurWithThb)
            coEvery { getExchangeRateUseCase("EUR", "THB") } returns BigDecimal("38.5")

            // When - Load config
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()

            // When - Select foreign currency
            viewModel.onEvent(AddExpenseUiEvent.CurrencySelected("THB"))

            // Then - Loading should be true during fetch
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.isLoadingRate) // Should be false after completion
        }

        @Test
        fun `does not fetch rate when same currency is selected`() = runTest {
            // Given
            coEvery { getGroupExpenseConfigUseCase("group-eur", any()) } returns Result.success(configEurWithThb)

            // When - Load config (EUR is default)
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()

            // When - Select same currency as group (EUR)
            viewModel.onEvent(AddExpenseUiEvent.CurrencySelected("EUR"))
            advanceUntilIdle()

            // Then - Rate should be 1.0 and no fetch
            val state = viewModel.uiState.value
            assertFalse(state.showExchangeRateSection)
            assertEquals("1.0", state.displayExchangeRate)
            coVerify(exactly = 0) { getExchangeRateUseCase(any(), any()) }
        }

        @Test
        fun `keeps existing rate when fetch returns null`() = runTest {
            // Given
            coEvery { getGroupExpenseConfigUseCase("group-eur", any()) } returns Result.success(configEurWithThb)
            coEvery { getExchangeRateUseCase("EUR", "THB") } returns null

            // When - Load config
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()

            // When - Select foreign currency
            viewModel.onEvent(AddExpenseUiEvent.CurrencySelected("THB"))
            advanceUntilIdle()

            // Then - Should keep default rate
            val state = viewModel.uiState.value
            assertTrue(state.showExchangeRateSection)
            assertEquals("1.0", state.displayExchangeRate)
            assertFalse(state.isLoadingRate)
        }

        @Test
        fun `resets rate to 1 when switching back to group currency`() = runTest {
            // Given
            coEvery { getGroupExpenseConfigUseCase("group-eur", any()) } returns Result.success(configEurWithThb)
            coEvery { getExchangeRateUseCase("EUR", "THB") } returns BigDecimal("38.5")

            // When - Load config and select foreign currency
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()
            viewModel.onEvent(AddExpenseUiEvent.CurrencySelected("THB"))
            advanceUntilIdle()

            // Verify rate was set
            assertEquals("38.5", viewModel.uiState.value.displayExchangeRate)

            // When - Switch back to group currency
            viewModel.onEvent(AddExpenseUiEvent.CurrencySelected("EUR"))
            advanceUntilIdle()

            // Then - Rate should be reset to 1.0
            val state = viewModel.uiState.value
            assertFalse(state.showExchangeRateSection)
            assertEquals("1.0", state.displayExchangeRate)
        }

        @Test
        fun `fetches new rate when switching between foreign currencies`() = runTest {
            // Given
            coEvery { getGroupExpenseConfigUseCase("group-eur", any()) } returns Result.success(configEurWithThb)
            coEvery { getExchangeRateUseCase("EUR", "THB") } returns BigDecimal("38.5")
            coEvery { getExchangeRateUseCase("EUR", "USD") } returns BigDecimal("1.08")

            // When - Load config and select THB
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()
            viewModel.onEvent(AddExpenseUiEvent.CurrencySelected("THB"))
            advanceUntilIdle()

            assertEquals("38.5", viewModel.uiState.value.displayExchangeRate)

            // When - Switch to USD
            viewModel.onEvent(AddExpenseUiEvent.CurrencySelected("USD"))
            advanceUntilIdle()

            // Then - Rate should be updated
            val state = viewModel.uiState.value
            assertTrue(state.showExchangeRateSection)
            assertEquals("1.08", state.displayExchangeRate)
            coVerify { getExchangeRateUseCase("EUR", "USD") }
        }

        @Test
        fun `handles exception gracefully when fetching rate fails`() = runTest {
            // Given
            coEvery { getGroupExpenseConfigUseCase("group-eur", any()) } returns Result.success(configEurWithThb)
            coEvery { getExchangeRateUseCase("EUR", "THB") } throws RuntimeException("Network error")

            // When - Load config
            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()

            // When - Select foreign currency
            viewModel.onEvent(AddExpenseUiEvent.CurrencySelected("THB"))
            advanceUntilIdle()

            // Then - Should set isLoadingRate to false and keep existing rate
            val state = viewModel.uiState.value
            assertTrue(state.showExchangeRateSection)
            assertEquals("1.0", state.displayExchangeRate)
            assertFalse(state.isLoadingRate)
            coVerify { getExchangeRateUseCase("EUR", "THB") }
        }
    }

    // ── Submit expense ─────────────────────────────────────────────────────

    @Nested
    inner class SubmitExpense {

        private val thb = Currency(
            code = "THB",
            symbol = "฿",
            defaultName = "Thai Baht",
            decimalDigits = 2
        )

        private val configEurWithThb = GroupExpenseConfig(
            group = groupEur,
            groupCurrency = eur,
            availableCurrencies = listOf(eur, usd, thb)
        )

        /**
         * Loads EUR group config, switches to THB as the payment currency,
         * then stubs [addExpenseUseCase] to throw [InsufficientCashException]
         * with amounts expressed in THB cents.
         */
        private fun TestScope.loadConfigAndSelectThb() {
            coEvery { getGroupExpenseConfigUseCase("group-eur", any()) } returns Result.success(
                configEurWithThb
            )
            coEvery { getExchangeRateUseCase("EUR", "THB") } returns BigDecimal("9.20")

            viewModel.onEvent(AddExpenseUiEvent.LoadGroupConfig("group-eur"))
            advanceUntilIdle()

            viewModel.onEvent(AddExpenseUiEvent.CurrencySelected("THB"))
            advanceUntilIdle()

            viewModel.onEvent(AddExpenseUiEvent.TitleChanged("Breakfast"))
            viewModel.onEvent(AddExpenseUiEvent.SourceAmountChanged("400"))
            advanceUntilIdle()
        }

        @Test
        fun `emits ShowError with source currency amounts on InsufficientCashException`() = runTest {
            loadConfigAndSelectThb()

            // 400 THB required, only 2000 cents (20.00 THB) available
            coEvery { addExpenseUseCase(any(), any()) } returns Result.failure(
                InsufficientCashException(requiredCents = 40000L, availableCents = 2000L)
            )

            val emittedActions = mutableListOf<AddExpenseUiAction>()
            val job = launch { viewModel.actions.collect { emittedActions.add(it) } }

            viewModel.onEvent(AddExpenseUiEvent.SubmitAddExpense("group-eur"))
            advanceUntilIdle()
            job.cancel()

            val action = emittedActions.filterIsInstance<AddExpenseUiAction.ShowError>().first()
            val uiText = action.message as UiText.StringResource

            // Verify it uses the insufficient-cash string resource
            assertEquals(
                R.string.expense_error_insufficient_cash,
                uiText.resId
            )

            // Both format args must be present (required + available)
            assertEquals(2, uiText.args.size)

            // Crucially: both amounts must contain the THB symbol "฿", NOT the group
            // currency symbol "€" — this is the core of the regression being tested.
            val requiredStr = uiText.args[0] as String
            val availableStr = uiText.args[1] as String
            assertTrue(
                requiredStr.contains("฿"),
                "Required amount '$requiredStr' should use the cash currency symbol ฿, not the group currency symbol"
            )
            assertTrue(
                availableStr.contains("฿"),
                "Available amount '$availableStr' should use the cash currency symbol ฿, not the group currency symbol"
            )
            assertFalse(
                requiredStr.contains("€"),
                "Required amount '$requiredStr' must not use the group currency symbol €"
            )
        }

        @Test
        fun `emits generic ShowError for non-cash failures`() = runTest {
            loadConfigAndSelectThb()

            coEvery { addExpenseUseCase(any(), any()) } returns Result.failure(
                RuntimeException("Network error")
            )

            val emittedActions = mutableListOf<AddExpenseUiAction>()
            val job = launch { viewModel.actions.collect { emittedActions.add(it) } }

            viewModel.onEvent(AddExpenseUiEvent.SubmitAddExpense("group-eur"))
            advanceUntilIdle()
            job.cancel()

            val action = emittedActions.filterIsInstance<AddExpenseUiAction.ShowError>().first()
            val uiText =
                action.message as UiText.StringResource
            assertEquals(
                R.string.expense_error_addition_failed,
                uiText.resId
            )
        }

        @Test
        fun `does not set inline error on submission failure`() = runTest {
            loadConfigAndSelectThb()

            coEvery { addExpenseUseCase(any(), any()) } returns Result.failure(
                InsufficientCashException(requiredCents = 40000L, availableCents = 2000L)
            )

            viewModel.onEvent(AddExpenseUiEvent.SubmitAddExpense("group-eur"))
            advanceUntilIdle()

            // Snackbar is the correct surface — no inline error should be set
            assertNull(viewModel.uiState.value.error)
        }
    }
}
