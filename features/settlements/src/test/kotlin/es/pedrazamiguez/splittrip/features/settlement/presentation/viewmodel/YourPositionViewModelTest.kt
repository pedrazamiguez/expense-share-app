package es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel

import es.pedrazamiguez.splittrip.core.common.network.NetworkMonitor
import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.service.AppConfigService
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.ConfirmSettlementUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.DisputeSettlementUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupSettlementsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
import es.pedrazamiguez.splittrip.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.splittrip.domain.usecase.settlement.GetNudgeTimestampsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.settlement.NudgeDebtorUseCase
import es.pedrazamiguez.splittrip.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.splittrip.features.settlement.presentation.mapper.MemberSpendingChartUiMapper
import es.pedrazamiguez.splittrip.features.settlement.presentation.mapper.SettlementConsensusUiMapper
import es.pedrazamiguez.splittrip.features.settlement.presentation.mapper.YourPositionUiMapper
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.MemberSpendingChartUiModel
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.action.YourPositionUiAction
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.event.YourPositionUiEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class YourPositionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getGroupByIdUseCase: GetGroupByIdUseCase = mockk()
    private val getGroupContributionsFlowUseCase: GetGroupContributionsFlowUseCase = mockk()
    private val getCashWithdrawalsFlowUseCase: GetCashWithdrawalsFlowUseCase = mockk()
    private val getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase = mockk()
    private val getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase = mockk()
    private val getMemberBalancesFlowUseCase: GetMemberBalancesFlowUseCase = mockk()
    private val getGroupSettlementsFlowUseCase: GetGroupSettlementsFlowUseCase = mockk()
    private val confirmSettlementUseCase: ConfirmSettlementUseCase = mockk()
    private val disputeSettlementUseCase: DisputeSettlementUseCase = mockk()
    private val getMemberProfilesUseCase: GetMemberProfilesUseCase = mockk()
    private val getSettlementSuggestionsUseCase: GetSettlementSuggestionsUseCase = mockk()
    private val nudgeDebtorUseCase: NudgeDebtorUseCase = mockk()
    private val getNudgeTimestampsFlowUseCase: GetNudgeTimestampsFlowUseCase = mockk()

    private val authenticationService: AuthenticationService = mockk()
    private val appConfigService: AppConfigService = mockk()
    private val networkMonitor: NetworkMonitor = mockk()
    private val isOnlineFlow = MutableStateFlow(true)
    private val localeProvider: LocaleProvider = mockk()
    private val resourceProvider: ResourceProvider = mockk()
    private val settlementConsensusUiMapper: SettlementConsensusUiMapper = mockk()
    private val memberSpendingChartUiMapper: MemberSpendingChartUiMapper = mockk()

    private lateinit var useCases: YourPositionUseCases
    private lateinit var mapper: YourPositionUiMapper
    private lateinit var viewModel: YourPositionViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        isOnlineFlow.value = true
        every { networkMonitor.isOnline } returns isOnlineFlow
        every { localeProvider.getCurrentLocale() } returns Locale.US
        every { resourceProvider.getString(any()) } returns "Label"
        every { resourceProvider.getString(any(), *anyVararg()) } returns "Formatted Label"
        every { appConfigService.defaultCurrencyCode } returns MutableStateFlow("EUR")
        every { appConfigService.balanceComputationDebounceMs } returns MutableStateFlow(0L)
        every { appConfigService.settlementNudgeRateLimitHours } returns MutableStateFlow(24L)
        every { authenticationService.currentUserId() } returns "user1"
        coEvery { getSettlementSuggestionsUseCase.persistForGroup(any(), any()) } returns emptyList()
        coEvery { getMemberProfilesUseCase(any()) } returns emptyMap()
        every { getNudgeTimestampsFlowUseCase() } returns flowOf(emptyMap())
        every { settlementConsensusUiMapper.toConsensusItems(any(), any(), any(), any(), any(), any(), any()) } returns
            persistentListOf()
        every { memberSpendingChartUiMapper.toChartUiModel(any(), any(), any(), any(), any()) } returns
            MemberSpendingChartUiModel(bars = persistentListOf(), formattedGroupTotal = "Total", isCashOnly = true)

        mapper = YourPositionUiMapper(localeProvider, resourceProvider)

        useCases = YourPositionUseCases(
            getGroupByIdUseCase = getGroupByIdUseCase,
            getGroupContributionsFlowUseCase = getGroupContributionsFlowUseCase,
            getCashWithdrawalsFlowUseCase = getCashWithdrawalsFlowUseCase,
            getGroupExpensesFlowUseCase = getGroupExpensesFlowUseCase,
            getGroupSubunitsFlowUseCase = getGroupSubunitsFlowUseCase,
            getMemberBalancesFlowUseCase = getMemberBalancesFlowUseCase,
            getGroupSettlementsFlowUseCase = getGroupSettlementsFlowUseCase,
            confirmSettlementUseCase = confirmSettlementUseCase,
            disputeSettlementUseCase = disputeSettlementUseCase,
            getMemberProfilesUseCase = getMemberProfilesUseCase,
            getSettlementSuggestionsUseCase = getSettlementSuggestionsUseCase,
            nudgeDebtorUseCase = nudgeDebtorUseCase,
            getNudgeTimestampsFlowUseCase = getNudgeTimestampsFlowUseCase
        )

        viewModel = YourPositionViewModel(
            useCases = useCases,
            authenticationService = authenticationService,
            yourPositionUiMapper = mapper,
            settlementConsensusUiMapper = settlementConsensusUiMapper,
            memberSpendingChartUiMapper = memberSpendingChartUiMapper,
            appConfigService = appConfigService,
            networkMonitor = networkMonitor,
            computationDispatcher = testDispatcher
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest(testDispatcher) {
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.personalPosition)
        assertFalse(viewModel.uiState.value.isCashBreakdownVisible)
    }

    @Test
    fun `setSelectedGroup loads position for current user`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        val group = Group(id = "group1", name = "Trip", currency = "EUR", members = listOf("user1"))
        coEvery { getGroupByIdUseCase("group1") } returns group
        every { getGroupContributionsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getCashWithdrawalsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupExpensesFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSubunitsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSettlementsFlowUseCase("group1") } returns flowOf(emptyList())

        val calculatedBalances = listOf(
            MemberBalance(
                userId = "user1",
                pocketBalance = 30000L,
                cashInHand = 20000L
            )
        )
        every { getMemberBalancesFlowUseCase.computeMemberBalances(any()) } returns calculatedBalances

        viewModel.setSelectedGroup("group1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isOffline)
        assertNotNull(state.personalPosition)
        assertEquals("€500.00", state.personalPosition?.formattedNetPosition)
        coVerify { getSettlementSuggestionsUseCase.persistForGroup("group1") }
    }

    @Test
    fun `isOffline is updated dynamically based on networkMonitor flow`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        val group = Group(id = "group1", name = "Trip", currency = "EUR", members = listOf("user1"))
        coEvery { getGroupByIdUseCase("group1") } returns group
        every { getGroupContributionsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getCashWithdrawalsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupExpensesFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSubunitsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSettlementsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getMemberBalancesFlowUseCase.computeMemberBalances(any()) } returns emptyList()

        viewModel.setSelectedGroup("group1")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isOffline)

        isOnlineFlow.value = false
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isOffline)

        isOnlineFlow.value = true
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isOffline)
    }

    @Test
    fun `cash breakdown visibility toggles on event`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        val group = Group(id = "group1", name = "Trip", currency = "EUR", members = listOf("user1"))
        coEvery { getGroupByIdUseCase("group1") } returns group
        every { getGroupContributionsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getCashWithdrawalsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupExpensesFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSubunitsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSettlementsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getMemberBalancesFlowUseCase.computeMemberBalances(any()) } returns emptyList()

        viewModel.setSelectedGroup("group1")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isCashBreakdownVisible)

        viewModel.onEvent(YourPositionUiEvent.ShowCashBreakdown)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isCashBreakdownVisible)

        viewModel.onEvent(YourPositionUiEvent.DismissCashBreakdown)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isCashBreakdownVisible)
    }

    @Test
    fun `ChartModeToggled event updates isChartCashOnly state`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect {} }

        val group = Group(id = "group1", name = "Trip", currency = "EUR", members = listOf("user1"))
        coEvery { getGroupByIdUseCase("group1") } returns group
        every { getGroupContributionsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getCashWithdrawalsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupExpensesFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSubunitsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSettlementsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getMemberBalancesFlowUseCase.computeMemberBalances(any()) } returns emptyList()

        viewModel.setSelectedGroup("group1")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isChartCashOnly)

        viewModel.onEvent(YourPositionUiEvent.ChartModeToggled(false))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isChartCashOnly)
    }

    @Test
    fun `ConfirmSettlement event calls confirmSettlementUseCase and emits success`() = runTest(testDispatcher) {
        val actions = mutableListOf<YourPositionUiAction>()
        val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.actions.collect { actions.add(it) }
        }
        backgroundScope.launch { viewModel.uiState.collect {} }

        val group = Group(id = "group1", name = "Trip", currency = "EUR", members = listOf("user1"))
        coEvery { getGroupByIdUseCase("group1") } returns group
        every { getGroupContributionsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getCashWithdrawalsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupExpensesFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSubunitsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSettlementsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getMemberBalancesFlowUseCase.computeMemberBalances(any()) } returns emptyList()
        coEvery { confirmSettlementUseCase("group1", "s1") } returns Result.success(mockk())

        viewModel.setSelectedGroup("group1")
        advanceUntilIdle()

        viewModel.onEvent(YourPositionUiEvent.ConfirmSettlement("s1"))
        advanceUntilIdle()

        coVerify(exactly = 1) { confirmSettlementUseCase("group1", "s1") }
        assertEquals(1, actions.size)
        assertTrue(actions[0] is YourPositionUiAction.ShowSuccess)
        actionsJob.cancel()
    }

    @Test
    fun `ConfirmSettlement failure emits ShowError action`() = runTest(testDispatcher) {
        val actions = mutableListOf<YourPositionUiAction>()
        val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.actions.collect { actions.add(it) }
        }
        backgroundScope.launch { viewModel.uiState.collect {} }

        val group = Group(id = "group1", name = "Trip", currency = "EUR", members = listOf("user1"))
        coEvery { getGroupByIdUseCase("group1") } returns group
        every { getGroupContributionsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getCashWithdrawalsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupExpensesFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSubunitsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSettlementsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getMemberBalancesFlowUseCase.computeMemberBalances(any()) } returns emptyList()
        coEvery { confirmSettlementUseCase("group1", "s1") } returns Result.failure(RuntimeException("Error"))

        viewModel.setSelectedGroup("group1")
        advanceUntilIdle()

        viewModel.onEvent(YourPositionUiEvent.ConfirmSettlement("s1"))
        advanceUntilIdle()

        assertEquals(1, actions.size)
        assertTrue(actions[0] is YourPositionUiAction.ShowError)
        actionsJob.cancel()
    }

    @Test
    fun `DisputeSettlement event opens dispute dialog`() = runTest(testDispatcher) {
        viewModel.onEvent(YourPositionUiEvent.DisputeSettlement("s1"))

        viewModel.onEvent(YourPositionUiEvent.DisputeReasonChanged("Invalid amount"))

        viewModel.onEvent(YourPositionUiEvent.DisputeCancelled)
    }

    @Test
    fun `DisputeSubmitted calls disputeSettlementUseCase and clears dialog state on success`() = runTest(
        testDispatcher
    ) {
        val actions = mutableListOf<YourPositionUiAction>()
        val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.actions.collect { actions.add(it) }
        }
        backgroundScope.launch { viewModel.uiState.collect {} }

        val group = Group(id = "group1", name = "Trip", currency = "EUR", members = listOf("user1"))
        coEvery { getGroupByIdUseCase("group1") } returns group
        every { getGroupContributionsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getCashWithdrawalsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupExpensesFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSubunitsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSettlementsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getMemberBalancesFlowUseCase.computeMemberBalances(any()) } returns emptyList()
        coEvery { disputeSettlementUseCase("group1", "s1", "Wrong amount") } returns Result.success(mockk())

        viewModel.setSelectedGroup("group1")
        advanceUntilIdle()

        viewModel.onEvent(YourPositionUiEvent.DisputeSettlement("s1"))
        viewModel.onEvent(YourPositionUiEvent.DisputeReasonChanged("Wrong amount"))
        viewModel.onEvent(YourPositionUiEvent.DisputeSubmitted)
        advanceUntilIdle()

        coVerify(exactly = 1) { disputeSettlementUseCase("group1", "s1", "Wrong amount") }
        assertEquals(1, actions.size)
        assertTrue(actions[0] is YourPositionUiAction.ShowSuccess)
        assertNull(viewModel.uiState.value.activeDisputeSettlementId)
        assertEquals("", viewModel.uiState.value.disputeReasonInput)
        actionsJob.cancel()
    }

    @Test
    fun `DisputeSubmitted with blank reason does nothing`() = runTest(testDispatcher) {
        val group = Group(id = "group1", name = "Trip", currency = "EUR", members = listOf("user1"))
        coEvery { getGroupByIdUseCase("group1") } returns group
        every { getGroupContributionsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getCashWithdrawalsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupExpensesFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSubunitsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSettlementsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getMemberBalancesFlowUseCase.computeMemberBalances(any()) } returns emptyList()

        viewModel.setSelectedGroup("group1")
        advanceUntilIdle()

        viewModel.onEvent(YourPositionUiEvent.DisputeSettlement("s1"))
        viewModel.onEvent(YourPositionUiEvent.DisputeReasonChanged("   "))
        viewModel.onEvent(YourPositionUiEvent.DisputeSubmitted)
        advanceUntilIdle()

        coVerify(exactly = 0) { disputeSettlementUseCase(any(), any(), any()) }
    }

    @Test
    fun `onEvent_NudgeDebtor_whenUseCaseSucceeds_emitsShowSuccessAction`() = runTest(testDispatcher) {
        val actions = mutableListOf<YourPositionUiAction>()
        val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.actions.collect { actions.add(it) }
        }
        backgroundScope.launch { viewModel.uiState.collect {} }

        val group = Group(id = "group1", name = "Trip", currency = "EUR", members = listOf("user1"))
        coEvery { getGroupByIdUseCase("group1") } returns group
        every { getGroupContributionsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getCashWithdrawalsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupExpensesFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSubunitsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSettlementsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getMemberBalancesFlowUseCase.computeMemberBalances(any()) } returns emptyList()
        coEvery { nudgeDebtorUseCase("group1", "s1") } returns Result.success(Unit)

        viewModel.setSelectedGroup("group1")
        advanceUntilIdle()

        viewModel.onEvent(YourPositionUiEvent.NudgeDebtor("s1"))
        advanceUntilIdle()

        coVerify(exactly = 1) { nudgeDebtorUseCase("group1", "s1") }
        assertEquals(1, actions.size)
        assertTrue(actions[0] is YourPositionUiAction.ShowSuccess)
        actionsJob.cancel()
    }

    @Test
    fun `onEvent_NudgeDebtor_whenUseCaseFails_emitsShowErrorAction`() = runTest(testDispatcher) {
        val actions = mutableListOf<YourPositionUiAction>()
        val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.actions.collect { actions.add(it) }
        }
        backgroundScope.launch { viewModel.uiState.collect {} }

        val group = Group(id = "group1", name = "Trip", currency = "EUR", members = listOf("user1"))
        coEvery { getGroupByIdUseCase("group1") } returns group
        every { getGroupContributionsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getCashWithdrawalsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupExpensesFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSubunitsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSettlementsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getMemberBalancesFlowUseCase.computeMemberBalances(any()) } returns emptyList()
        coEvery { nudgeDebtorUseCase("group1", "s1") } returns Result.failure(RuntimeException("Rate limit"))

        viewModel.setSelectedGroup("group1")
        advanceUntilIdle()

        viewModel.onEvent(YourPositionUiEvent.NudgeDebtor("s1"))
        advanceUntilIdle()

        coVerify(exactly = 1) { nudgeDebtorUseCase("group1", "s1") }
        assertEquals(1, actions.size)
        assertTrue(actions[0] is YourPositionUiAction.ShowError)
        actionsJob.cancel()
    }

    @Test
    fun `consensus actions are blocked when isOffline is true`() = runTest(testDispatcher) {
        val actions = mutableListOf<YourPositionUiAction>()
        val actionsJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.actions.collect { actions.add(it) }
        }
        backgroundScope.launch { viewModel.uiState.collect {} }

        val group = Group(id = "group1", name = "Trip", currency = "EUR", members = listOf("user1"))
        coEvery { getGroupByIdUseCase("group1") } returns group
        every { getGroupContributionsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getCashWithdrawalsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupExpensesFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSubunitsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getGroupSettlementsFlowUseCase("group1") } returns flowOf(emptyList())
        every { getMemberBalancesFlowUseCase.computeMemberBalances(any()) } returns emptyList()

        viewModel.setSelectedGroup("group1")
        advanceUntilIdle()

        isOnlineFlow.value = false
        advanceUntilIdle()

        viewModel.onEvent(YourPositionUiEvent.ConfirmSettlement("s1"))
        viewModel.onEvent(YourPositionUiEvent.DisputeSettlement("s1"))
        viewModel.onEvent(YourPositionUiEvent.NudgeDebtor("s1"))
        advanceUntilIdle()

        coVerify(exactly = 0) { confirmSettlementUseCase(any(), any()) }
        coVerify(exactly = 0) { disputeSettlementUseCase(any(), any(), any()) }
        coVerify(exactly = 0) { nudgeDebtorUseCase(any(), any()) }
        assertTrue(actions.all { it is YourPositionUiAction.ShowError })

        actionsJob.cancel()
    }
}
