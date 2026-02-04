package es.pedrazamiguez.expenseshareapp.domain.usecase.group

import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DeleteGroupUseCaseTest {

    private lateinit var groupRepository: GroupRepository
    private lateinit var useCase: DeleteGroupUseCase

    @BeforeEach
    fun setUp() {
        groupRepository = mockk()
        useCase = DeleteGroupUseCase(groupRepository)
    }

    @Nested
    inner class Invocation {

        @Test
        fun `delegates to repository deleteGroup`() = runTest {
            // Given
            val groupId = "group-123"
            coEvery { groupRepository.deleteGroup(groupId) } just Runs

            // When
            useCase(groupId)

            // Then
            coVerify(exactly = 1) { groupRepository.deleteGroup(groupId) }
        }

        @Test
        fun `passes correct groupId to repository`() = runTest {
            // Given
            val groupId = "specific-group-id-456"
            coEvery { groupRepository.deleteGroup(any()) } just Runs

            // When
            useCase(groupId)

            // Then
            coVerify { groupRepository.deleteGroup(groupId) }
        }

        @Test
        fun `propagates exception from repository`() = runTest {
            // Given
            val groupId = "group-123"
            val exception = RuntimeException("Delete failed")
            coEvery { groupRepository.deleteGroup(groupId) } throws exception

            // When/Then
            try {
                useCase(groupId)
                assert(false) { "Expected exception to be thrown" }
            } catch (e: RuntimeException) {
                assert(e.message == "Delete failed")
            }
        }
    }
}
