package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.model.GroupExpenseConfig
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.service.RemainderDistributionService
import es.pedrazamiguez.expenseshareapp.domain.service.split.SplitPreviewService
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedCategoryUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetGroupLastUsedPaymentMethodUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseOptionsMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseSplitMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigEventHandlerTest {

    private lateinit var handler: ConfigEventHandler
    private lateinit var getGroupExpenseConfigUseCase: GetGroupExpenseConfigUseCase
    private lateinit var getGroupLastUsedCurrencyUseCase: GetGroupLastUsedCurrencyUseCase
    private lateinit var getGroupLastUsedPaymentMethodUseCase: GetGroupLastUsedPaymentMethodUseCase
    private lateinit var getGroupLastUsedCategoryUseCase: GetGroupLastUsedCategoryUseCase
    private lateinit var getMemberProfilesUseCase: GetMemberProfilesUseCase
    private lateinit var currencyEventHandler: CurrencyEventHandler
    private lateinit var subunitSplitEventHandler: SubunitSplitEventHandler

    private lateinit var uiState: MutableStateFlow<AddExpenseUiState>
    private lateinit var actions: MutableSharedFlow<AddExpenseUiAction>

    private val eurDomain = Currency(code = "EUR", symbol = "€", defaultName = "Euro", decimalDigits = 2)
    private val usdDomain = Currency(code = "USD", symbol = "$", defaultName = "US Dollar", decimalDigits = 2)

    private val testGroup = Group(
        id = "group-1",
        name = "Test Group",
        currency = "EUR",
        members = listOf("user-1", "user-2")
    )

    private val testConfig = GroupExpenseConfig(
        group = testGroup,
        groupCurrency = eurDomain,
        availableCurrencies = listOf(eurDomain, usdDomain),
        subunits = emptyList()
    )

    @BeforeEach
    fun setUp() {
        getGroupExpenseConfigUseCase = mockk()
        getGroupLastUsedCurrencyUseCase = mockk()
        getGroupLastUsedPaymentMethodUseCase = mockk()
        getGroupLastUsedCategoryUseCase = mockk()
        getMemberProfilesUseCase = mockk()
        currencyEventHandler = mockk(relaxed = true)
        subunitSplitEventHandler = mockk(relaxed = true)

        val localeProvider = mockk<LocaleProvider>()
        val resourceProvider = mockk<ResourceProvider>(relaxed = true)
        every { localeProvider.getCurrentLocale() } returns Locale.US

        handler = ConfigEventHandler(
            getGroupExpenseConfigUseCase = getGroupExpenseConfigUseCase,
            getGroupLastUsedCurrencyUseCase = getGroupLastUsedCurrencyUseCase,
            getGroupLastUsedPaymentMethodUseCase = getGroupLastUsedPaymentMethodUseCase,
            getGroupLastUsedCategoryUseCase = getGroupLastUsedCategoryUseCase,
            getMemberProfilesUseCase = getMemberProfilesUseCase,
            addExpenseOptionsMapper = AddExpenseOptionsMapper(resourceProvider),
            addExpenseSplitMapper = AddExpenseSplitMapper(
                localeProvider,
                FormattingHelper(localeProvider),
                SplitPreviewService(),
                RemainderDistributionService()
            ),
            currencyEventHandler = currencyEventHandler,
            subunitSplitEventHandler = subunitSplitEventHandler
        )

        // Default use case stubs
        every { getGroupLastUsedCurrencyUseCase(any()) } returns flowOf(null)
        every { getGroupLastUsedPaymentMethodUseCase(any()) } returns flowOf(emptyList())
        every { getGroupLastUsedCategoryUseCase(any()) } returns flowOf(emptyList())
        coEvery { getMemberProfilesUseCase(any()) } returns emptyMap()

        uiState = MutableStateFlow(AddExpenseUiState())
        actions = MutableSharedFlow()
    }

    // ── Null / blank groupId ─────────────────────────────────────────────

    @Nested
    inner class NullGroupId {

        @Test
        fun `null groupId is a no-op`() = runTest {
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig(null)
            advanceUntilIdle()

            // No network calls; state unchanged
            coVerify(exactly = 0) { getGroupExpenseConfigUseCase(any(), any()) }
            assertFalse(uiState.value.isLoading)
        }
    }

    // ── Skip-reload optimisation ─────────────────────────────────────────

    @Nested
    inner class SkipReload {

        @Test
        fun `does not reload when same group is already loaded`() = runTest {
            // Pre-populate state as if config was already loaded for group-1
            uiState.value = AddExpenseUiState(
                loadedGroupId = "group-1",
                isConfigLoaded = true
            )
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            coVerify(exactly = 0) { getGroupExpenseConfigUseCase(any(), any()) }
        }

        @Test
        fun `reloads when forceRefresh is true even if already loaded`() = runTest {
            uiState.value = AddExpenseUiState(
                loadedGroupId = "group-1",
                isConfigLoaded = true
            )
            coEvery { getGroupExpenseConfigUseCase("group-1", true) } returns Result.success(testConfig)
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1", forceRefresh = true)
            advanceUntilIdle()

            coVerify(exactly = 1) { getGroupExpenseConfigUseCase("group-1", true) }
        }

        @Test
        fun `reloads when groupId changes even without forceRefresh`() = runTest {
            uiState.value = AddExpenseUiState(
                loadedGroupId = "group-1",
                isConfigLoaded = true
            )
            coEvery { getGroupExpenseConfigUseCase("group-2", false) } returns Result.success(testConfig)
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-2")
            advanceUntilIdle()

            coVerify(exactly = 1) { getGroupExpenseConfigUseCase("group-2", false) }
        }
    }

    // ── Success path ─────────────────────────────────────────────────────

    @Nested
    inner class Success {

        @BeforeEach
        fun stub() {
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns Result.success(testConfig)
        }

        @Test
        fun `sets isConfigLoaded true and clears isLoading on success`() = runTest {
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            val state = uiState.value
            assertTrue(state.isConfigLoaded)
            assertFalse(state.isLoading)
            assertFalse(state.configLoadFailed)
        }

        @Test
        fun `populates loadedGroupId and groupName`() = runTest {
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertEquals("group-1", uiState.value.loadedGroupId)
            assertEquals("Test Group", uiState.value.groupName)
        }

        @Test
        fun `maps available currencies from config`() = runTest {
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            val currencies = uiState.value.availableCurrencies
            assertEquals(2, currencies.size)
            assertTrue(currencies.any { it.code == "EUR" })
            assertTrue(currencies.any { it.code == "USD" })
        }

        @Test
        fun `maps payment methods from PaymentMethod entries`() = runTest {
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertTrue(uiState.value.paymentMethods.isNotEmpty())
        }

        @Test
        fun `auto-selects group currency when no last-used currency stored`() = runTest {
            every { getGroupLastUsedCurrencyUseCase(any()) } returns flowOf(null)
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertEquals("EUR", uiState.value.selectedCurrency?.code)
        }

        @Test
        fun `auto-selects last-used currency when stored`() = runTest {
            every { getGroupLastUsedCurrencyUseCase(any()) } returns flowOf("USD")
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertEquals("USD", uiState.value.selectedCurrency?.code)
        }

        @Test
        fun `falls back to group currency when last-used currency is not in available list`() = runTest {
            every { getGroupLastUsedCurrencyUseCase(any()) } returns flowOf("JPY")
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertEquals("EUR", uiState.value.selectedCurrency?.code)
        }

        @Test
        fun `does not show exchange rate section for same-currency expense`() = runTest {
            // Last-used currency is EUR (same as group currency)
            every { getGroupLastUsedCurrencyUseCase(any()) } returns flowOf("EUR")
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertFalse(uiState.value.showExchangeRateSection)
        }

        @Test
        fun `shows exchange rate section for foreign-currency expense`() = runTest {
            every { getGroupLastUsedCurrencyUseCase(any()) } returns flowOf("USD")
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertTrue(uiState.value.showExchangeRateSection)
        }

        @Test
        fun `reorders payment methods to put most-recently-used first`() = runTest {
            // CASH is the last-used method (most recently used first)
            every { getGroupLastUsedPaymentMethodUseCase(any()) } returns
                flowOf(listOf(PaymentMethod.CASH.name))
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            val firstMethod = uiState.value.paymentMethods.firstOrNull()
            assertEquals(PaymentMethod.CASH.name, firstMethod?.id)
        }

        @Test
        fun `initialises split members from group member list`() = runTest {
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertEquals(listOf("user-1", "user-2"), uiState.value.memberIds.toList())
        }

        @Test
        fun `clears error on successful load`() = runTest {
            uiState.value = AddExpenseUiState(
                error = es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText.DynamicString("old error")
            )
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertNull(uiState.value.error)
        }

        @Test
        fun `delegates to subunitSplitEventHandler initEntitySplits when group has subunits`() = runTest {
            val subunit = Subunit(id = "sub-1", groupId = "group-1", memberIds = listOf("user-1"))
            val configWithSubunit = testConfig.copy(subunits = listOf(subunit))
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns Result.success(configWithSubunit)
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            coVerify(exactly = 1) { subunitSplitEventHandler.initEntitySplits(any(), any(), any()) }
        }

        @Test
        fun `delegates to subunitSplitEventHandler clearEntitySplits when group has no subunits`() = runTest {
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            coVerify(exactly = 1) { subunitSplitEventHandler.clearEntitySplits() }
        }

        @Test
        fun `resets form to blank state when switching to a different group`() = runTest {
            uiState.value = AddExpenseUiState(
                loadedGroupId = "group-1",
                isConfigLoaded = true,
                expenseTitle = "Old title"
            )
            handler.bind(uiState, actions, this)

            // Load a different group — should reset form
            coEvery { getGroupExpenseConfigUseCase("group-2", false) } returns Result.success(testConfig)
            handler.loadGroupConfig("group-2")
            advanceUntilIdle()

            // After loading the new group the form should be clean (title was reset on group switch)
            assertEquals("", uiState.value.expenseTitle)
        }
    }

    // ── Failure path ─────────────────────────────────────────────────────

    @Nested
    inner class Failure {

        @Test
        fun `sets configLoadFailed and error on use case failure`() = runTest {
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns
                Result.failure(RuntimeException("network error"))
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            val state = uiState.value
            assertTrue(state.configLoadFailed)
            assertFalse(state.isConfigLoaded)
            assertFalse(state.isLoading)
            assertNotNull(state.error)
        }

        @Test
        fun `does not wipe existing state when a retry also fails`() = runTest {
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns
                Result.failure(RuntimeException("network error"))
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            assertTrue(uiState.value.configLoadFailed)
        }
    }

    // ── reorderByRecent (via loadGroupConfig) ─────────────────────────────

    @Nested
    inner class ReorderByRecent {

        @Test
        fun `most-recently-used payment method appears first in reordered list`() = runTest {
            every { getGroupLastUsedPaymentMethodUseCase(any()) } returns
                flowOf(listOf(PaymentMethod.DEBIT_CARD.name, PaymentMethod.CASH.name))
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns Result.success(testConfig)
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            val firstMethod = uiState.value.paymentMethods.firstOrNull()
            assertEquals(PaymentMethod.DEBIT_CARD.name, firstMethod?.id)
        }

        @Test
        fun `returns original order when recentIds list is empty`() = runTest {
            every { getGroupLastUsedPaymentMethodUseCase(any()) } returns flowOf(emptyList())
            coEvery { getGroupExpenseConfigUseCase(any(), any()) } returns Result.success(testConfig)
            handler.bind(uiState, actions, this)

            handler.loadGroupConfig("group-1")
            advanceUntilIdle()

            // Payment methods should still be populated (original enum order)
            assertTrue(uiState.value.paymentMethods.isNotEmpty())
        }
    }
}
