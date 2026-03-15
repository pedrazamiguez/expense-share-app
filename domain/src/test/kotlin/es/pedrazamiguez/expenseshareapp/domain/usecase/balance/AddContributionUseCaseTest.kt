package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.exception.NotGroupMemberException
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
import es.pedrazamiguez.expenseshareapp.domain.service.GroupMembershipService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AddContributionUseCaseTest {

    private lateinit var contributionRepository: ContributionRepository
    private lateinit var groupMembershipService: GroupMembershipService
    private lateinit var useCase: AddContributionUseCase

    private val groupId = "group-123"
    private val contribution = Contribution(
        id = "contribution-1",
        groupId = groupId,
        amount = 5000L,
        currency = "EUR"
    )

    @BeforeEach
    fun setUp() {
        contributionRepository = mockk(relaxed = true)
        groupMembershipService = mockk()
        coEvery { groupMembershipService.requireMembership(any()) } just Runs
        useCase = AddContributionUseCase(contributionRepository, groupMembershipService)
    }

    // ── Delegation ────────────────────────────────────────────────────────────

    @Nested
    inner class Delegation {

        @Test
        fun `delegates to contribution repository`() = runTest {
            // When
            useCase(groupId, contribution)

            // Then
            coVerify(exactly = 1) { contributionRepository.addContribution(groupId, contribution) }
        }

        @Test
        fun `passes correct groupId and contribution`() = runTest {
            // When
            useCase(groupId, contribution)

            // Then
            coVerify {
                contributionRepository.addContribution(
                    match { it == groupId },
                    match { it == contribution }
                )
            }
        }
    }

    // ── Membership validation ─────────────────────────────────────────────────

    @Nested
    inner class MembershipValidation {

        @Test
        fun `throws NotGroupMemberException when user is not a member`() = runTest {
            // Given
            coEvery {
                groupMembershipService.requireMembership(groupId)
            } throws NotGroupMemberException(groupId = groupId, userId = "user-123")

            // When / Then
            try {
                useCase(groupId, contribution)
                fail("Expected NotGroupMemberException to be thrown")
            } catch (e: NotGroupMemberException) {
                assertTrue(e.groupId == groupId)
            }
        }

        @Test
        fun `does not save contribution when membership check fails`() = runTest {
            // Given
            coEvery {
                groupMembershipService.requireMembership(groupId)
            } throws NotGroupMemberException(groupId = groupId, userId = "user-123")

            // When
            runCatching { useCase(groupId, contribution) }

            // Then
            coVerify(exactly = 0) { contributionRepository.addContribution(any(), any()) }
        }

        @Test
        fun `calls requireMembership before saving`() = runTest {
            // When
            useCase(groupId, contribution)

            // Then
            coVerify(exactly = 1) { groupMembershipService.requireMembership(groupId) }
        }
    }
}
