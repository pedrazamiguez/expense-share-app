package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetContributionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("GetContributionUseCaseImpl")
class GetContributionUseCaseImplTest {

    private lateinit var contributionRepository: ContributionRepository
    private lateinit var useCase: GetContributionUseCase

    private val testContributionId = "contrib-123"
    private val testContribution = Contribution(
        id = testContributionId,
        groupId = "group-123",
        userId = "user-1",
        createdBy = "user-1",
        amount = 5000L,
        currency = "EUR",
        createdAt = LocalDateTime.now(),
        lastUpdatedAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        contributionRepository = mockk()
        useCase = GetContributionUseCaseImpl(contributionRepository)
    }

    @Test
    fun `returns contribution from repository when found`() = runTest {
        coEvery { contributionRepository.getContribution(testContributionId) } returns testContribution

        val result = useCase(testContributionId)

        assertEquals(testContribution, result)
        coVerify(exactly = 1) { contributionRepository.getContribution(testContributionId) }
    }

    @Test
    fun `returns null when contribution is not found`() = runTest {
        coEvery { contributionRepository.getContribution(testContributionId) } returns null

        val result = useCase(testContributionId)

        assertNull(result)
        coVerify(exactly = 1) { contributionRepository.getContribution(testContributionId) }
    }

    @Test
    fun `propagates exception from repository`() = runTest {
        val exception = RuntimeException("Repository failed")
        coEvery { contributionRepository.getContribution(testContributionId) } throws exception

        val thrownException = assertThrows<RuntimeException> {
            useCase(testContributionId)
        }
        assertEquals("Repository failed", thrownException.message)
    }
}
