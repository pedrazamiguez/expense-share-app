package es.pedrazamiguez.splittrip.domain.usecase.group

import es.pedrazamiguez.splittrip.domain.enums.GroupStatus
import es.pedrazamiguez.splittrip.domain.exception.CannotLeaveGroupException
import es.pedrazamiguez.splittrip.domain.exception.GroupArchivedException
import es.pedrazamiguez.splittrip.domain.exception.UnresolvedSettlementsException
import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.AreMemberSettlementsResolvedUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
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

        useCase = LeaveGroupUseCaseImpl(
            groupRepository = groupRepository,
            authenticationService = authenticationService,
            getSettlementSuggestionsUseCase = getSettlementSuggestionsUseCase,
            areMemberSettlementsResolvedUseCase = areMemberSettlementsResolvedUseCase,
            reassignSubunitSharesUseCase = reassignSubunitSharesUseCase
        )

        every { authenticationService.requireUserId() } returns currentUserId
        coEvery { getSettlementSuggestionsUseCase.persistForGroup(groupId) } returns emptyList()
        coEvery { reassignSubunitSharesUseCase(groupId, currentUserId) } returns Result.success(Unit)
        coEvery { groupRepository.leaveGroup(any()) } just Runs
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
            assertEquals("Cannot leave group: not_a_member", exception?.message)
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
            assertEquals("Cannot leave group: is_creator", exception?.message)
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
    }
}
