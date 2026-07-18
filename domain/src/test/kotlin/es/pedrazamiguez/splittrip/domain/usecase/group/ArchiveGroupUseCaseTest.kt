package es.pedrazamiguez.splittrip.domain.usecase.group

import es.pedrazamiguez.splittrip.domain.enums.GroupStatus
import es.pedrazamiguez.splittrip.domain.exception.CannotArchiveGroupException
import es.pedrazamiguez.splittrip.domain.exception.UnresolvedSettlementsException
import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.AreGroupSettlementsResolvedUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.impl.ArchiveGroupUseCaseImpl
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ArchiveGroupUseCaseTest {

    private lateinit var groupRepository: GroupRepository
    private lateinit var authenticationService: AuthenticationService
    private lateinit var getSettlementSuggestionsUseCase: GetSettlementSuggestionsUseCase
    private lateinit var areGroupSettlementsResolvedUseCase: AreGroupSettlementsResolvedUseCase
    private lateinit var useCase: ArchiveGroupUseCase

    private val sampleGroup = Group(
        id = "group-123",
        name = "Trip to Paris",
        currency = "EUR",
        members = listOf("user-1", "user-2"),
        status = GroupStatus.ACTIVE,
        createdBy = "user-1",
        createdAt = LocalDateTime.now(),
        lastUpdatedAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        groupRepository = mockk()
        authenticationService = mockk()
        getSettlementSuggestionsUseCase = mockk()
        areGroupSettlementsResolvedUseCase = mockk()

        useCase = ArchiveGroupUseCaseImpl(
            groupRepository = groupRepository,
            authenticationService = authenticationService,
            getSettlementSuggestionsUseCase = getSettlementSuggestionsUseCase,
            areGroupSettlementsResolvedUseCase = areGroupSettlementsResolvedUseCase
        )

        coEvery { getSettlementSuggestionsUseCase.persistForGroup(any()) } returns emptyList()
        coEvery { authenticationService.requireUserId() } returns "user-1"
    }

    @Nested
    inner class Invocation {

        @Test
        fun `archives active group and calls repository updateGroup`() = runTest {
            coEvery { groupRepository.getGroupByIdLocal("group-123") } returns sampleGroup
            coEvery { areGroupSettlementsResolvedUseCase("group-123") } returns emptyList()
            coEvery { groupRepository.updateGroup(any()) } just Runs

            val result = useCase("group-123")

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) {
                groupRepository.updateGroup(
                    match {
                        it.id == "group-123" && it.status == GroupStatus.ARCHIVED
                    }
                )
            }
        }

        @Test
        fun `returns failure result when group does not exist`() = runTest {
            coEvery { groupRepository.getGroupByIdLocal("invalid-id") } returns null

            val result = useCase("invalid-id")

            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is IllegalArgumentException)
            assertEquals("Group not found with id: invalid-id", exception?.message)
            coVerify(exactly = 0) { groupRepository.updateGroup(any()) }
        }

        @Test
        fun `throws when there are unresolved settlements`() = runTest {
            coEvery { groupRepository.getGroupByIdLocal("group-123") } returns sampleGroup
            val pendingSettlement = SettlementRecord(
                id = "settlement-1",
                groupId = "group-123",
                settlement = Settlement(
                    fromUserId = "user-1",
                    toUserId = "user-2",
                    amount = 1000L,
                    currency = "EUR",
                    sourcePocket = SettlementPocketType.CASH
                ),
                status = SettlementStatus.SUGGESTED,
                createdAt = LocalDateTime.now()
            )
            coEvery { areGroupSettlementsResolvedUseCase("group-123") } returns listOf(pendingSettlement)

            val result = useCase("group-123")

            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is UnresolvedSettlementsException)
            coVerify(exactly = 0) { groupRepository.updateGroup(any()) }
        }

        @Test
        fun `returns failure result when repository updateGroup throws exception`() = runTest {
            coEvery { groupRepository.getGroupByIdLocal("group-123") } returns sampleGroup
            coEvery { areGroupSettlementsResolvedUseCase("group-123") } returns emptyList()
            val repoException = RuntimeException("DB update error")
            coEvery { groupRepository.updateGroup(any()) } throws repoException

            val result = useCase("group-123")

            assertTrue(result.isFailure)
            assertEquals(repoException, result.exceptionOrNull())
        }

        @Test
        fun `invoke throws CannotArchiveGroupException if caller is not the creator`() = runTest {
            coEvery { groupRepository.getGroupByIdLocal("group-123") } returns sampleGroup
            coEvery { authenticationService.requireUserId() } returns "user-2"

            val result = useCase("group-123")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is CannotArchiveGroupException)
            coVerify(exactly = 0) { groupRepository.updateGroup(any()) }
        }

        @Test
        fun `invoke generates local archive event UUID and sets lastArchiveEventId on updated group`() = runTest {
            coEvery { groupRepository.getGroupByIdLocal("group-123") } returns sampleGroup
            coEvery { areGroupSettlementsResolvedUseCase("group-123") } returns emptyList()
            coEvery { groupRepository.updateGroup(any()) } just Runs

            val result = useCase("group-123")

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) {
                groupRepository.updateGroup(
                    match {
                        assertNotNull(it.lastArchiveEventId)
                        val isUuid = try {
                            java.util.UUID.fromString(it.lastArchiveEventId)
                            true
                        } catch (ignored: IllegalArgumentException) {
                            false
                        }
                        isUuid && it.status == GroupStatus.ARCHIVED
                    }
                )
            }
        }

        @Test
        fun `invoke uses getGroupByIdLocal (local-only read) instead of getGroupById`() = runTest {
            coEvery { groupRepository.getGroupByIdLocal("group-123") } returns sampleGroup
            coEvery { areGroupSettlementsResolvedUseCase("group-123") } returns emptyList()
            coEvery { groupRepository.updateGroup(any()) } just Runs

            useCase("group-123")

            coVerify(exactly = 1) { groupRepository.getGroupByIdLocal("group-123") }
            coVerify(exactly = 0) { groupRepository.getGroupById(any()) }
        }
    }
}
