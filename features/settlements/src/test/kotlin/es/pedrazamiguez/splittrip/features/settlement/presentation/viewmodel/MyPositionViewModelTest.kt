package es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.service.AppConfigService
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupSettlementsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.splittrip.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.splittrip.features.settlement.presentation.mapper.MyPositionUiMapper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
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
class MyPositionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getGroupByIdUseCase: GetGroupByIdUseCase
    private lateinit var getGroupContributionsFlowUseCase: GetGroupContributionsFlowUseCase
    private lateinit var getCashWithdrawalsFlowUseCase: GetCashWithdrawalsFlowUseCase
    private lateinit var getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase
    private lateinit var getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase
    private lateinit var getMemberBalancesFlowUseCase: GetMemberBalancesFlowUseCase
    private lateinit var getGroupSettlementsFlowUseCase: GetGroupSettlementsFlowUseCase
    private lateinit var authenticationService: AuthenticationService
    private lateinit var appConfigService: AppConfigService
    private lateinit var localeProvider: LocaleProvider
    private lateinit var myPositionUiMapper: MyPositionUiMapper
    private lateinit var useCases: MyPositionUseCases

    private lateinit var viewModel: MyPositionViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getGroupByIdUseCase = mockk()
        getGroupContributionsFlowUseCase = mockk()
        getCashWithdrawalsFlowUseCase = mockk()
        getGroupExpensesFlowUseCase = mockk()
        getGroupSubunitsFlowUseCase = mockk()
        getMemberBalancesFlowUseCase = mockk()
        getGroupSettlementsFlowUseCase = mockk()
        authenticationService = mockk()
        appConfigService = mockk()
        localeProvider = mockk()

        every { localeProvider.getCurrentLocale() } returns Locale.US
        every { appConfigService.defaultCurrencyCode } returns MutableStateFlow("EUR")
        every { appConfigService.balanceComputationDebounceMs } returns MutableStateFlow(0L)

        myPositionUiMapper = MyPositionUiMapper(localeProvider)

        useCases = MyPositionUseCases(
            getGroupByIdUseCase = getGroupByIdUseCase,
            getGroupContributionsFlowUseCase = getGroupContributionsFlowUseCase,
            getCashWithdrawalsFlowUseCase = getCashWithdrawalsFlowUseCase,
            getGroupExpensesFlowUseCase = getGroupExpensesFlowUseCase,
            getGroupSubunitsFlowUseCase = getGroupSubunitsFlowUseCase,
            getMemberBalancesFlowUseCase = getMemberBalancesFlowUseCase,
            getGroupSettlementsFlowUseCase = getGroupSettlementsFlowUseCase
        )

        viewModel = MyPositionViewModel(
            useCases = useCases,
            authenticationService = authenticationService,
            myPositionUiMapper = myPositionUiMapper,
            appConfigService = appConfigService,
            computationDispatcher = testDispatcher
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() {
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.personalPosition)
    }

    @Test
    fun `loads personal position for current user`() = runTest(testDispatcher) {
        val groupId = "group-1"
        val currentUserId = "user-1"

        coEvery { getGroupByIdUseCase(groupId) } returns Group(
            id = groupId,
            currency = "EUR",
            members = listOf("user-1", "user-2")
        )
        every { authenticationService.currentUserId() } returns currentUserId
        every { getGroupContributionsFlowUseCase(groupId) } returns flowOf(emptyList())
        every { getCashWithdrawalsFlowUseCase(groupId) } returns flowOf(emptyList())
        every { getGroupExpensesFlowUseCase(groupId) } returns flowOf(emptyList())
        every { getGroupSubunitsFlowUseCase(groupId) } returns flowOf(emptyList())
        every { getGroupSettlementsFlowUseCase(groupId) } returns flowOf(emptyList())

        coEvery { getMemberBalancesFlowUseCase.computeMemberBalances(any()) } returns listOf(
            MemberBalance(
                userId = "user-1",
                pocketBalance = 10000,
                cashInHand = 5000
            ),
            MemberBalance(
                userId = "user-2",
                pocketBalance = -15000,
                cashInHand = 0
            )
        )

        val collectorJob = launch { viewModel.uiState.collect {} }

        viewModel.setSelectedGroup(groupId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.personalPosition)
        assertEquals("EUR", state.personalPosition?.groupCurrencyCode)

        collectorJob.cancel()
    }

    @Test
    fun `personalPosition is null when current user has no balance entry`() = runTest(testDispatcher) {
        val groupId = "group-1"
        val currentUserId = "user-unknown"

        coEvery { getGroupByIdUseCase(groupId) } returns Group(
            id = groupId,
            currency = "EUR",
            members = listOf("user-1")
        )
        every { authenticationService.currentUserId() } returns currentUserId
        every { getGroupContributionsFlowUseCase(groupId) } returns flowOf(emptyList())
        every { getCashWithdrawalsFlowUseCase(groupId) } returns flowOf(emptyList())
        every { getGroupExpensesFlowUseCase(groupId) } returns flowOf(emptyList())
        every { getGroupSubunitsFlowUseCase(groupId) } returns flowOf(emptyList())
        every { getGroupSettlementsFlowUseCase(groupId) } returns flowOf(emptyList())

        coEvery { getMemberBalancesFlowUseCase.computeMemberBalances(any()) } returns listOf(
            MemberBalance(userId = "user-1", pocketBalance = 10000)
        )

        val collectorJob = launch { viewModel.uiState.collect {} }

        viewModel.setSelectedGroup(groupId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.personalPosition)

        collectorJob.cancel()
    }

    @Test
    fun `error in flow emits non-loading state`() = runTest(testDispatcher) {
        val groupId = "group-1"

        coEvery { getGroupByIdUseCase(groupId) } returns Group(id = groupId, currency = "EUR")
        every { authenticationService.currentUserId() } returns "user-1"
        every { getGroupContributionsFlowUseCase(groupId) } returns flow {
            throw IllegalStateException("Database error")
        }
        every { getCashWithdrawalsFlowUseCase(groupId) } returns flowOf(emptyList())
        every { getGroupExpensesFlowUseCase(groupId) } returns flowOf(emptyList())
        every { getGroupSubunitsFlowUseCase(groupId) } returns flowOf(emptyList())
        every { getGroupSettlementsFlowUseCase(groupId) } returns flowOf(emptyList())

        val collectorJob = launch { viewModel.uiState.collect {} }

        viewModel.setSelectedGroup(groupId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.personalPosition)

        collectorJob.cancel()
    }
}
