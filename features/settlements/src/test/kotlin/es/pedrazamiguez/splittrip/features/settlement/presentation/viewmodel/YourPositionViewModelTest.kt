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
import es.pedrazamiguez.splittrip.features.settlement.presentation.mapper.YourPositionUiMapper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class YourPositionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getGroupByIdUseCase: GetGroupByIdUseCase = mockk()
    private val getGroupContributionsFlowUseCase: GetGroupContributionsFlowUseCase = mockk()
    private val getCashWithdrawalsFlowUseCase: GetCashWithdrawalsFlowUseCase = mockk()
    private val getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase = mockk()
    private val getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase = mockk()
    private val getMemberBalancesFlowUseCase: GetMemberBalancesFlowUseCase = mockk()
    private val getGroupSettlementsFlowUseCase: GetGroupSettlementsFlowUseCase = mockk()

    private val authenticationService: AuthenticationService = mockk()
    private val appConfigService: AppConfigService = mockk()
    private val localeProvider: LocaleProvider = mockk()

    private lateinit var useCases: YourPositionUseCases
    private lateinit var mapper: YourPositionUiMapper
    private lateinit var viewModel: YourPositionViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { localeProvider.getCurrentLocale() } returns Locale.US
        every { appConfigService.defaultCurrencyCode } returns MutableStateFlow("EUR")
        every { appConfigService.balanceComputationDebounceMs } returns MutableStateFlow(0L)
        every { authenticationService.currentUserId() } returns "user1"

        mapper = YourPositionUiMapper(localeProvider)

        useCases = YourPositionUseCases(
            getGroupByIdUseCase = getGroupByIdUseCase,
            getGroupContributionsFlowUseCase = getGroupContributionsFlowUseCase,
            getCashWithdrawalsFlowUseCase = getCashWithdrawalsFlowUseCase,
            getGroupExpensesFlowUseCase = getGroupExpensesFlowUseCase,
            getGroupSubunitsFlowUseCase = getGroupSubunitsFlowUseCase,
            getMemberBalancesFlowUseCase = getMemberBalancesFlowUseCase,
            getGroupSettlementsFlowUseCase = getGroupSettlementsFlowUseCase
        )

        viewModel = YourPositionViewModel(
            useCases = useCases,
            authenticationService = authenticationService,
            yourPositionUiMapper = mapper,
            appConfigService = appConfigService,
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
        assertNotNull(state.personalPosition)
        assertEquals("€500.00", state.personalPosition?.formattedNetPosition)
    }
}
