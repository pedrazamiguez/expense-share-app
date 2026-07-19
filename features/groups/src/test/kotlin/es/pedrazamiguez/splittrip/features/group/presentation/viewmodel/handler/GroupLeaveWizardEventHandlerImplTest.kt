package es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.handler

import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.domain.exception.CannotLeaveGroupException
import es.pedrazamiguez.splittrip.domain.exception.UnresolvedSettlementsException
import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.AreMemberSettlementsResolvedUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.ConfirmSettlementUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupSettlementsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
import es.pedrazamiguez.splittrip.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.LeaveGroupUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.ObserveGroupUseCase
import es.pedrazamiguez.splittrip.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.splittrip.features.group.presentation.mapper.LeaveWizardUiMapper
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveBalanceSummaryUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveCashResolutionUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveSubunitImpactUiModel
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveWizardStep
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GroupLeaveWizardEventHandlerImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var authenticationService: AuthenticationService
    private lateinit var observeGroupUseCase: ObserveGroupUseCase
    private lateinit var getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase
    private lateinit var getGroupContributionsFlowUseCase: GetGroupContributionsFlowUseCase
    private lateinit var getCashWithdrawalsFlowUseCase: GetCashWithdrawalsFlowUseCase
    private lateinit var getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase
    private lateinit var getMemberBalancesFlowUseCase: GetMemberBalancesFlowUseCase
    private lateinit var getSettlementSuggestionsUseCase: GetSettlementSuggestionsUseCase
    private lateinit var areMemberSettlementsResolvedUseCase: AreMemberSettlementsResolvedUseCase
    private lateinit var getMemberProfilesUseCase: GetMemberProfilesUseCase
    private lateinit var confirmSettlementUseCase: ConfirmSettlementUseCase
    private lateinit var leaveGroupUseCase: LeaveGroupUseCase
    private lateinit var getGroupSettlementsFlowUseCase: GetGroupSettlementsFlowUseCase
    private lateinit var leaveWizardUiMapper: LeaveWizardUiMapper

    private lateinit var handler: GroupLeaveWizardEventHandlerImpl
    private val onSuccessActions = mutableListOf<UiText>()
    private val onErrorActions = mutableListOf<UiText>()
    private val onSuccess: suspend (UiText) -> Unit = { onSuccessActions.add(it) }
    private val onError: suspend (UiText) -> Unit = { onErrorActions.add(it) }

    private val testGroupId = "group-123"
    private val testGroup = Group(
        id = testGroupId,
        name = "Summer Trip",
        currency = "EUR",
        members = listOf("user-1", "user-2"),
        createdBy = "user-1"
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authenticationService = mockk(relaxed = true)
        observeGroupUseCase = mockk(relaxed = true)
        getGroupExpensesFlowUseCase = mockk(relaxed = true)
        getGroupContributionsFlowUseCase = mockk(relaxed = true)
        getCashWithdrawalsFlowUseCase = mockk(relaxed = true)
        getGroupSubunitsFlowUseCase = mockk(relaxed = true)
        getMemberBalancesFlowUseCase = mockk(relaxed = true)
        getSettlementSuggestionsUseCase = mockk(relaxed = true)
        areMemberSettlementsResolvedUseCase = mockk(relaxed = true)
        getMemberProfilesUseCase = mockk(relaxed = true)
        confirmSettlementUseCase = mockk(relaxed = true)
        leaveGroupUseCase = mockk(relaxed = true)
        getGroupSettlementsFlowUseCase = mockk(relaxed = true)
        leaveWizardUiMapper = mockk(relaxed = true)

        every { authenticationService.requireUserId() } returns "user-1"
        every { observeGroupUseCase(any()) } returns flowOf(testGroup)
        every { getGroupExpensesFlowUseCase(any()) } returns flowOf(emptyList())
        every { getGroupContributionsFlowUseCase(any()) } returns flowOf(emptyList())
        every { getCashWithdrawalsFlowUseCase(any()) } returns flowOf(emptyList())
        every { getGroupSubunitsFlowUseCase(any()) } returns flowOf(emptyList())
        every { getGroupSettlementsFlowUseCase(any()) } returns flowOf(emptyList())
        coEvery { getMemberProfilesUseCase(any()) } returns emptyMap()
        coEvery { areMemberSettlementsResolvedUseCase(any(), any()) } returns emptyList()

        handler = GroupLeaveWizardEventHandlerImpl(
            authenticationService = authenticationService,
            observeGroupUseCase = observeGroupUseCase,
            getGroupExpensesFlowUseCase = getGroupExpensesFlowUseCase,
            getGroupContributionsFlowUseCase = getGroupContributionsFlowUseCase,
            getCashWithdrawalsFlowUseCase = getCashWithdrawalsFlowUseCase,
            getGroupSubunitsFlowUseCase = getGroupSubunitsFlowUseCase,
            getMemberBalancesFlowUseCase = getMemberBalancesFlowUseCase,
            getSettlementSuggestionsUseCase = getSettlementSuggestionsUseCase,
            areMemberSettlementsResolvedUseCase = areMemberSettlementsResolvedUseCase,
            getMemberProfilesUseCase = getMemberProfilesUseCase,
            confirmSettlementUseCase = confirmSettlementUseCase,
            leaveGroupUseCase = leaveGroupUseCase,
            getGroupSettlementsFlowUseCase = getGroupSettlementsFlowUseCase,
            leaveWizardUiMapper = leaveWizardUiMapper
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `handleLeaveClicked populates wizardState and opens sheet`() = runTest(testDispatcher) {
        handler.bind(this, onSuccess, onError)
        val memberBalance = MemberBalance(userId = "user-1", pocketBalance = 2500L, cashInHand = 1000L)
        val subunit = Subunit(
            id = "sub-1",
            groupId = testGroupId,
            name = "Test",
            memberShares = mapOf("user-1" to BigDecimal.ONE)
        )
        every { getGroupSubunitsFlowUseCase(testGroupId) } returns flowOf(listOf(subunit))
        every {
            getMemberBalancesFlowUseCase.computeMemberBalances(any(), any(), any(), any(), any(), any(), any(), any())
        } returns listOf(memberBalance)
        every {
            leaveWizardUiMapper.toBalanceSummaryUiModel(
                memberBalance = any(),
                memberBalances = any(),
                currentUserId = any(),
                memberProfiles = any(),
                currency = any()
            )
        } returns LeaveBalanceSummaryUiModel("€25.00", "€10.00", "€35.00")
        every { leaveWizardUiMapper.toCashResolutionUiModel(any(), any()) } returns
            LeaveCashResolutionUiModel(requiresDeposit = true, formattedAmount = "€10.00")
        every { leaveWizardUiMapper.toSubunitImpactUiModel(any()) } returns
            LeaveSubunitImpactUiModel(hasSubunitImpact = true)

        handler.handleLeaveClicked(testGroupId)
        advanceUntilIdle()

        val wizardState = handler.wizardState.value
        assertTrue(wizardState.showSheet)
        assertEquals(LeaveWizardStep.BALANCE_SUMMARY, wizardState.currentStep)
        assertTrue(wizardState.activeSteps.contains(LeaveWizardStep.BALANCE_SUMMARY))
        assertTrue(wizardState.activeSteps.contains(LeaveWizardStep.CASH_RESOLUTION))
        assertTrue(wizardState.activeSteps.contains(LeaveWizardStep.CONFIRMATION))
    }

    @Test
    fun `handleLeaveClicked with zero balance opens wizard at CONFIRMATION step`() = runTest(testDispatcher) {
        handler.bind(this, onSuccess, onError)
        val memberBalance = MemberBalance(userId = "user-1", pocketBalance = 0L, cashInHand = 0L)
        every {
            getMemberBalancesFlowUseCase.computeMemberBalances(any(), any(), any(), any(), any(), any(), any(), any())
        } returns listOf(memberBalance)

        handler.handleLeaveClicked(testGroupId)
        advanceUntilIdle()

        val wizardState = handler.wizardState.value
        assertTrue(wizardState.showSheet)
        assertEquals(LeaveWizardStep.CONFIRMATION, wizardState.currentStep)
        assertEquals(listOf(LeaveWizardStep.CONFIRMATION), wizardState.activeSteps)
    }

    @Test
    fun `handleWizardNext advances currentStep or calls handleLeave`() = runTest(testDispatcher) {
        handler.bind(this, onSuccess, onError)
        val memberBalance = MemberBalance(userId = "user-1", pocketBalance = 2500L, cashInHand = 0L)
        every {
            getMemberBalancesFlowUseCase.computeMemberBalances(any(), any(), any(), any(), any(), any(), any(), any())
        } returns listOf(memberBalance)

        handler.handleLeaveClicked(testGroupId)
        advanceUntilIdle()

        assertEquals(LeaveWizardStep.BALANCE_SUMMARY, handler.wizardState.value.currentStep)

        handler.handleWizardNext(testGroupId)
        advanceUntilIdle()

        assertEquals(LeaveWizardStep.CONFIRMATION, handler.wizardState.value.currentStep)

        coEvery { leaveGroupUseCase(testGroupId) } returns Result.success(Unit)
        handler.handleWizardNext(testGroupId)
        advanceUntilIdle()

        coVerify(exactly = 1) { leaveGroupUseCase(testGroupId) }
    }

    @Test
    fun `handleWizardBack moves currentStep backward or closes sheet`() = runTest(testDispatcher) {
        handler.bind(this, onSuccess, onError)
        val memberBalance = MemberBalance(userId = "user-1", pocketBalance = 2500L, cashInHand = 0L)
        every {
            getMemberBalancesFlowUseCase.computeMemberBalances(any(), any(), any(), any(), any(), any(), any(), any())
        } returns listOf(memberBalance)

        handler.handleLeaveClicked(testGroupId)
        advanceUntilIdle()

        handler.handleWizardNext(testGroupId)
        advanceUntilIdle()
        assertEquals(LeaveWizardStep.CONFIRMATION, handler.wizardState.value.currentStep)

        handler.handleWizardBack()
        advanceUntilIdle()
        assertEquals(LeaveWizardStep.BALANCE_SUMMARY, handler.wizardState.value.currentStep)

        handler.handleWizardBack()
        advanceUntilIdle()
        assertFalse(handler.wizardState.value.showSheet)
    }

    @Test
    fun `handleConfirmSettlement invokes confirmSettlementUseCase`() = runTest(testDispatcher) {
        handler.bind(this, onSuccess, onError)
        coEvery { confirmSettlementUseCase(testGroupId, "s-1") } returns Result.success(mockk())

        handler.handleConfirmSettlement(testGroupId, "s-1")
        advanceUntilIdle()

        coVerify(exactly = 1) { confirmSettlementUseCase(testGroupId, "s-1") }
    }

    @Test
    fun `handleLeave failure with CannotLeaveGroupException emits error`() = runTest(testDispatcher) {
        handler.bind(this, onSuccess, onError)
        val ex = CannotLeaveGroupException(CannotLeaveGroupException.Reason.NON_ZERO_POCKET_BALANCE)
        coEvery { leaveGroupUseCase(testGroupId) } returns Result.failure(ex)

        handler.handleLeave(testGroupId)
        advanceUntilIdle()

        assertTrue(onErrorActions.isNotEmpty())
    }

    @Test
    fun `handleLeave failure with UnresolvedSettlementsException navigates to SETTLEMENTS step`() =
        runTest(testDispatcher) {
            handler.bind(this, onSuccess, onError)
            val ex = UnresolvedSettlementsException(testGroupId, emptyList())
            coEvery { leaveGroupUseCase(testGroupId) } returns Result.failure(ex)

            handler.handleLeave(testGroupId)
            advanceUntilIdle()

            val state = handler.wizardState.value
            assertTrue(state.activeSteps.contains(LeaveWizardStep.SETTLEMENTS))
            assertEquals(LeaveWizardStep.SETTLEMENTS, state.currentStep)
        }

    @Test
    fun `handleJumpToStep transitions step correctly when step is in activeSteps`() = runTest(testDispatcher) {
        handler.bind(this, onSuccess, onError)
        val memberBalance = MemberBalance(userId = "user-1", pocketBalance = 2500L, cashInHand = 0L)
        every {
            getMemberBalancesFlowUseCase.computeMemberBalances(any(), any(), any(), any(), any(), any(), any(), any())
        } returns listOf(memberBalance)

        handler.handleLeaveClicked(testGroupId)
        advanceUntilIdle()

        // Steps will be BALANCE_SUMMARY and CONFIRMATION
        assertEquals(LeaveWizardStep.BALANCE_SUMMARY, handler.wizardState.value.currentStep)

        handler.handleJumpToStep(LeaveWizardStep.CONFIRMATION)
        assertEquals(LeaveWizardStep.CONFIRMATION, handler.wizardState.value.currentStep)

        // Try to jump to step not in activeSteps (e.g. SETTLEMENTS)
        handler.handleJumpToStep(LeaveWizardStep.SETTLEMENTS)
        // Should remain at CONFIRMATION
        assertEquals(LeaveWizardStep.CONFIRMATION, handler.wizardState.value.currentStep)
    }
}
