package es.pedrazamiguez.splittrip.domain.usecase.group

import es.pedrazamiguez.splittrip.domain.enums.GroupStatus
import es.pedrazamiguez.splittrip.domain.exception.CannotLeaveGroupException
import es.pedrazamiguez.splittrip.domain.exception.GroupArchivedException
import es.pedrazamiguez.splittrip.domain.exception.UnresolvedSettlementsException
import es.pedrazamiguez.splittrip.domain.model.CurrencyAmount
import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.repository.ExpenseRepository
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.repository.SubunitRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.AreMemberSettlementsResolvedUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.ResolveCashOnLeaveUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.impl.LeaveGroupUseCaseImpl
import es.pedrazamiguez.splittrip.domain.usecase.subunit.ReassignSubunitSharesUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LeaveGroupUseCaseTest {

    private lateinit var groupRepository: GroupRepository
    private lateinit var authenticationService: AuthenticationService
    private lateinit var getSettlementSuggestionsUseCase: GetSettlementSuggestionsUseCase
    private lateinit var areMemberSettlementsResolvedUseCase: AreMemberSettlementsResolvedUseCase
    private lateinit var reassignSubunitSharesUseCase: ReassignSubunitSharesUseCase
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var contributionRepository: ContributionRepository
    private lateinit var cashWithdrawalRepository: CashWithdrawalRepository
    private lateinit var subunitRepository: SubunitRepository
    private lateinit var getMemberBalancesFlowUseCase: GetMemberBalancesFlowUseCase
    private lateinit var resolveCashOnLeaveUseCase: ResolveCashOnLeaveUseCase
    private lateinit var settlementRepository: SettlementRepository
    private lateinit var useCase: LeaveGroupUseCase

    private val groupId = "group-123"
    private val currentUserId = "user-1"
    private val anotherUserId = "user-2"

    private val sampleGroup = Group(
        id = groupId,
        name = "Trip to Paris",
        currency = "EUR",
        members = listOf(currentUserId, anotherUserId),
        status = GroupStatus.ACTIVE,
        createdBy = anotherUserId,
        createdAt = LocalDateTime.now(),
        lastUpdatedAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        groupRepository = mockk()
        authenticationService = mockk()
        getSettlementSuggestionsUseCase = mockk()
        areMemberSettlementsResolvedUseCase = mockk()
        reassignSubunitSharesUseCase = mockk()
        expenseRepository = mockk()
        contributionRepository = mockk()
        cashWithdrawalRepository = mockk()
        subunitRepository = mockk()
        getMemberBalancesFlowUseCase = mockk()
        resolveCashOnLeaveUseCase = mockk()
        settlementRepository = mockk()

        useCase = LeaveGroupUseCaseImpl(
            groupRepository = groupRepository,
            authenticationService = authenticationService,
            getSettlementSuggestionsUseCase = getSettlementSuggestionsUseCase,
            areMemberSettlementsResolvedUseCase = areMemberSettlementsResolvedUseCase,
            reassignSubunitSharesUseCase = reassignSubunitSharesUseCase,
            expenseRepository = expenseRepository,
            contributionRepository = contributionRepository,
            cashWithdrawalRepository = cashWithdrawalRepository,
            subunitRepository = subunitRepository,
            getMemberBalancesFlowUseCase = getMemberBalancesFlowUseCase,
            resolveCashOnLeaveUseCase = resolveCashOnLeaveUseCase,
            settlementRepository = settlementRepository
        )

        every { authenticationService.requireUserId() } returns currentUserId
        coEvery { getSettlementSuggestionsUseCase.persistForGroup(groupId) } returns emptyList()
        coEvery { reassignSubunitSharesUseCase(groupId, currentUserId) } returns Result.success(Unit)
        coEvery { groupRepository.leaveGroup(any()) } just Runs

        // Set default behaviors for new dependencies to keep existing tests passing
        coEvery { expenseRepository.getGroupExpensesFlow(any()) } returns kotlinx.coroutines.flow.flowOf(emptyList())
        coEvery { contributionRepository.getGroupContributionsFlow(any()) } returns
            kotlinx.coroutines.flow.flowOf(emptyList())
        coEvery { cashWithdrawalRepository.getGroupWithdrawalsFlow(any()) } returns
            kotlinx.coroutines.flow.flowOf(emptyList())
        coEvery { subunitRepository.getGroupSubunits(any()) } returns emptyList()
        coEvery { settlementRepository.getGroupSettlements(any()) } returns emptyList()
        coEvery {
            getMemberBalancesFlowUseCase.computeMemberBalances(
                contributions = any(),
                withdrawals = any(),
                expenses = any(),
                subunits = any(),
                groupMemberIds = any(),
                groupCurrency = any(),
                settlements = any(),
                attributionStrategy = any()
            )
        } returns listOf(
            MemberBalance(userId = currentUserId, pocketBalance = 0L, cashInHand = 0L)
        )
        coEvery { resolveCashOnLeaveUseCase(any(), any(), any(), any()) } returns Result.success(Unit)
    }

    @Nested
    inner class Invocation {

        @Test
        fun `leaves group successfully when all settlements are resolved`() = runTest {
            coEvery { groupRepository.getGroupById(groupId) } returns sampleGroup
            coEvery { areMemberSettlementsResolvedUseCase(groupId, currentUserId) } returns emptyList()

            val result = useCase(groupId)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { groupRepository.leaveGroup(groupId) }
        }

        @Test
        fun `throws when group is not found`() = runTest {
            coEvery { groupRepository.getGroupById(groupId) } returns null

            val result = useCase(groupId)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        }

        @Test
        fun `throws when group is archived`() = runTest {
            val archivedGroup = sampleGroup.copy(status = GroupStatus.ARCHIVED)
            coEvery { groupRepository.getGroupById(groupId) } returns archivedGroup

            val result = useCase(groupId)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is GroupArchivedException)
            coVerify(exactly = 0) { groupRepository.leaveGroup(any()) }
        }

        @Test
        fun `throws when user is not a member`() = runTest {
            val groupWithoutUser = sampleGroup.copy(members = listOf(anotherUserId))
            every { authenticationService.requireUserId() } returns currentUserId
            coEvery { groupRepository.getGroupById(groupId) } returns groupWithoutUser

            val result = useCase(groupId)

            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is CannotLeaveGroupException)
            assertEquals(CannotLeaveGroupException.Reason.NOT_A_MEMBER, (exception as CannotLeaveGroupException).reason)
            coVerify(exactly = 0) { groupRepository.leaveGroup(any()) }
        }

        @Test
        fun `throws when user is the creator`() = runTest {
            val groupAsCreator = sampleGroup.copy(createdBy = currentUserId)
            coEvery { groupRepository.getGroupById(groupId) } returns groupAsCreator

            val result = useCase(groupId)

            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is CannotLeaveGroupException)
            assertEquals(CannotLeaveGroupException.Reason.IS_CREATOR, (exception as CannotLeaveGroupException).reason)
            coVerify(exactly = 0) { groupRepository.leaveGroup(any()) }
        }

        @Test
        fun `throws when user has unresolved settlements`() = runTest {
            coEvery { groupRepository.getGroupById(groupId) } returns sampleGroup
            val pendingSettlement = SettlementRecord(
                id = "settlement-1",
                groupId = groupId,
                settlement = Settlement(
                    fromUserId = currentUserId,
                    toUserId = anotherUserId,
                    amount = 1000L,
                    currency = "EUR",
                    sourcePocket = SettlementPocketType.CASH
                ),
                status = SettlementStatus.SUGGESTED,
                createdAt = LocalDateTime.now()
            )
            coEvery {
                areMemberSettlementsResolvedUseCase(groupId, currentUserId)
            } returns listOf(pendingSettlement)

            val result = useCase(groupId)

            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is UnresolvedSettlementsException)
            assertEquals(1, (exception as UnresolvedSettlementsException).pendingSettlements.size)
            coVerify(exactly = 0) { groupRepository.leaveGroup(any()) }
        }

        @Test
        fun `calls persistForGroup before checking resolution`() = runTest {
            coEvery { groupRepository.getGroupById(groupId) } returns sampleGroup
            coEvery { areMemberSettlementsResolvedUseCase(groupId, currentUserId) } returns emptyList()

            useCase(groupId)

            coVerifyOrder {
                getSettlementSuggestionsUseCase.persistForGroup(groupId)
                reassignSubunitSharesUseCase(groupId, currentUserId)
                groupRepository.leaveGroup(groupId)
            }
        }

        @Test
        fun `propagates failure from reassignSubunitSharesUseCase`() = runTest {
            coEvery { groupRepository.getGroupById(groupId) } returns sampleGroup
            coEvery { areMemberSettlementsResolvedUseCase(groupId, currentUserId) } returns emptyList()
            coEvery { reassignSubunitSharesUseCase(groupId, currentUserId) } returns
                Result.failure(RuntimeException("share error"))

            val result = useCase(groupId)

            assertTrue(result.isFailure)
            coVerify(exactly = 0) { groupRepository.leaveGroup(any()) }
        }

        @Test
        fun `leaves group successfully when cashInHand is zero — resolveCashOnLeaveUseCase is NOT called`() = runTest {
            coEvery { groupRepository.getGroupById(groupId) } returns sampleGroup
            coEvery { areMemberSettlementsResolvedUseCase(groupId, currentUserId) } returns emptyList()
            coEvery {
                getMemberBalancesFlowUseCase.computeMemberBalances(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns listOf(
                MemberBalance(userId = currentUserId, pocketBalance = 0L, cashInHand = 0L)
            )

            val result = useCase(groupId)

            assertTrue(result.isSuccess)
            coVerify(exactly = 0) { resolveCashOnLeaveUseCase(any(), any(), any(), any()) }
            coVerify(exactly = 1) { groupRepository.leaveGroup(groupId) }
        }

        @Test
        fun `leaves group when cashInHand is non-zero and totalBalance is zero - resolve is called`() = runTest {
            coEvery { groupRepository.getGroupById(groupId) } returns sampleGroup
            coEvery { areMemberSettlementsResolvedUseCase(groupId, currentUserId) } returns emptyList()
            val userBalance = MemberBalance(
                userId = currentUserId,
                pocketBalance = -1000L,
                cashInHand = 1000L,
                cashInHandByCurrency = listOf(
                    CurrencyAmount(currency = "EUR", amountCents = 1000L, equivalentCents = 1000L)
                )
            )
            coEvery {
                getMemberBalancesFlowUseCase.computeMemberBalances(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns listOf(userBalance)

            val result = useCase(groupId)

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) {
                resolveCashOnLeaveUseCase(groupId, currentUserId, userBalance, sampleGroup.currency)
            }
            coVerify(exactly = 1) { groupRepository.leaveGroup(groupId) }
        }

        @Test
        fun `fails with NON_ZERO_POCKET_BALANCE when totalBalance is non-zero - no resolve`() = runTest {
            coEvery { groupRepository.getGroupById(groupId) } returns sampleGroup
            coEvery { areMemberSettlementsResolvedUseCase(groupId, currentUserId) } returns emptyList()
            coEvery {
                getMemberBalancesFlowUseCase.computeMemberBalances(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns listOf(
                MemberBalance(userId = currentUserId, pocketBalance = 1000L, cashInHand = 0L)
            )

            val result = useCase(groupId)

            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is CannotLeaveGroupException)
            assertEquals(
                CannotLeaveGroupException.Reason.NON_ZERO_POCKET_BALANCE,
                (exception as CannotLeaveGroupException).reason
            )
            coVerify(exactly = 0) { resolveCashOnLeaveUseCase(any(), any(), any(), any()) }
            coVerify(exactly = 0) { groupRepository.leaveGroup(any()) }
        }

        @Test
        fun `propagates failure from resolveCashOnLeaveUseCase as Result failure`() = runTest {
            coEvery { groupRepository.getGroupById(groupId) } returns sampleGroup
            coEvery { areMemberSettlementsResolvedUseCase(groupId, currentUserId) } returns emptyList()
            val userBalance = MemberBalance(
                userId = currentUserId,
                pocketBalance = -1000L,
                cashInHand = 1000L,
                cashInHandByCurrency = listOf(
                    CurrencyAmount(currency = "EUR", amountCents = 1000L, equivalentCents = 1000L)
                )
            )
            coEvery {
                getMemberBalancesFlowUseCase.computeMemberBalances(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns listOf(userBalance)
            coEvery {
                resolveCashOnLeaveUseCase(groupId, currentUserId, userBalance, sampleGroup.currency)
            } returns Result.failure(RuntimeException("resolve error"))

            val result = useCase(groupId)

            assertTrue(result.isFailure)
            coVerify(exactly = 0) { groupRepository.leaveGroup(any()) }
        }
    }
}
