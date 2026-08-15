package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.exception.NotGroupMemberException
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.service.GroupMembershipService
import es.pedrazamiguez.splittrip.domain.usecase.balance.UpdateContributionUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.just
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("UpdateContributionUseCaseImpl")
class UpdateContributionUseCaseImplTest {

    private lateinit var contributionRepository: ContributionRepository
    private lateinit var groupMembershipService: GroupMembershipService
    private lateinit var useCase: UpdateContributionUseCase

    private val testGroupId = "group-123"
    private val testContribution = Contribution(
        id = "contrib-1",
        groupId = testGroupId,
        userId = "user-1",
        createdBy = "user-1",
        amount = 1000L,
        currency = "EUR",
        createdAt = LocalDateTime.now(),
        lastUpdatedAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        contributionRepository = mockk()
        groupMembershipService = mockk()
        coEvery { groupMembershipService.requireMembership(any()) } just Runs
        coEvery { contributionRepository.updateContribution(any(), any()) } just Runs

        useCase = UpdateContributionUseCaseImpl(
            contributionRepository = contributionRepository,
            groupMembershipService = groupMembershipService
        )
    }

    @Nested
    inner class MembershipValidation {

        @Test
        fun `throws NotGroupMemberException when user is not a member`() = runTest {
            coEvery {
                groupMembershipService.requireMembership(testGroupId)
            } throws NotGroupMemberException(groupId = testGroupId, userId = "user-1")

            val exception = assertThrows<NotGroupMemberException> {
                useCase(testGroupId, testContribution)
            }
            assertEquals(testGroupId, exception.groupId)
        }

        @Test
        fun `does not update contribution when membership check fails`() = runTest {
            coEvery {
                groupMembershipService.requireMembership(testGroupId)
            } throws NotGroupMemberException(groupId = testGroupId, userId = "user-1")

            runCatching { useCase(testGroupId, testContribution) }

            coVerify(exactly = 0) { contributionRepository.updateContribution(any(), any()) }
        }

        @Test
        fun `calls requireMembership before updating`() = runTest {
            useCase(testGroupId, testContribution)

            coVerifyOrder {
                groupMembershipService.requireMembership(testGroupId)
                contributionRepository.updateContribution(testGroupId, testContribution)
            }
        }
    }

    @Nested
    inner class Invocation {

        @Test
        fun `delegates to repository updateContribution`() = runTest {
            useCase(testGroupId, testContribution)

            coVerify(exactly = 1) { contributionRepository.updateContribution(testGroupId, testContribution) }
        }

        @Test
        fun `propagates exception from repository`() = runTest {
            val exception = RuntimeException("Update failed")
            coEvery { contributionRepository.updateContribution(testGroupId, testContribution) } throws exception

            val thrownException = assertThrows<RuntimeException> {
                useCase(testGroupId, testContribution)
            }
            assertEquals("Update failed", thrownException.message)
        }
    }
}
