package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.data.worker.GroupDeletionRetryScheduler
import es.pedrazamiguez.splittrip.domain.datasource.cloud.CloudGroupDataSource
import es.pedrazamiguez.splittrip.domain.datasource.local.LocalGroupDataSource
import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GroupRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var cloudGroupDataSource: CloudGroupDataSource
    private lateinit var localGroupDataSource: LocalGroupDataSource
    private lateinit var authenticationService: AuthenticationService
    private lateinit var groupDeletionRetryScheduler: GroupDeletionRetryScheduler
    private lateinit var repository: GroupRepositoryImpl

    private val testGroupId = "group-123"
    private val testGroup = Group(
        id = testGroupId,
        name = "Test Group",
        description = "A test group",
        currency = "EUR",
        extraCurrencies = emptyList(),
        members = listOf("user-1", "user-2", "user-3"),
        createdAt = LocalDateTime.of(2024, 1, 15, 12, 0),
        lastUpdatedAt = LocalDateTime.of(2024, 1, 15, 12, 0)
    )

    @BeforeEach
    fun setUp() {
        cloudGroupDataSource = mockk(relaxed = true)
        localGroupDataSource = mockk(relaxed = true)
        authenticationService = mockk(relaxed = true)
        groupDeletionRetryScheduler = mockk(relaxed = true)

        every { authenticationService.requireUserId() } returns "current-user-id"

        repository = GroupRepositoryImpl(
            cloudGroupDataSource = cloudGroupDataSource,
            localGroupDataSource = localGroupDataSource,
            authenticationService = authenticationService,
            groupDeletionRetryScheduler = groupDeletionRetryScheduler,
            ioDispatcher = testDispatcher
        )
    }

    @Nested
    inner class DeleteGroup {

        @Test
        fun `deletes group from local storage first`() = runTest(testDispatcher) {
            // Given
            coEvery { localGroupDataSource.deleteGroup(testGroupId) } just Runs

            // When
            repository.deleteGroup(testGroupId)

            // Then
            coVerify(exactly = 1) { localGroupDataSource.deleteGroup(testGroupId) }
        }

        @Test
        fun `requests group deletion from cloud in background`() = runTest(testDispatcher) {
            // Given
            coEvery { localGroupDataSource.deleteGroup(testGroupId) } just Runs
            coEvery { cloudGroupDataSource.requestGroupDeletion(testGroupId) } just Runs

            // When
            repository.deleteGroup(testGroupId)
            advanceUntilIdle()

            // Then - Cloud deletion request should be made in background
            coVerify(exactly = 1) { cloudGroupDataSource.requestGroupDeletion(testGroupId) }
        }

        @Test
        fun `local delete completes even if cloud request fails`() = runTest(testDispatcher) {
            // Given
            coEvery { localGroupDataSource.deleteGroup(testGroupId) } just Runs
            coEvery { cloudGroupDataSource.requestGroupDeletion(testGroupId) } throws
                RuntimeException("Network error")

            // When - Should not throw
            repository.deleteGroup(testGroupId)
            advanceUntilIdle()

            // Then - Local delete should have completed
            coVerify(exactly = 1) { localGroupDataSource.deleteGroup(testGroupId) }
        }

        @Test
        fun `schedules WorkManager retry when cloud request fails`() = runTest(testDispatcher) {
            // Given
            coEvery { localGroupDataSource.deleteGroup(testGroupId) } just Runs
            coEvery { cloudGroupDataSource.requestGroupDeletion(testGroupId) } throws
                RuntimeException("Network error")

            // When
            repository.deleteGroup(testGroupId)
            advanceUntilIdle()

            // Then - Retry scheduler should be invoked with the group ID
            verify(exactly = 1) { groupDeletionRetryScheduler.scheduleRetry(testGroupId) }
        }

        @Test
        fun `does not schedule retry when cloud request succeeds`() = runTest(testDispatcher) {
            // Given
            coEvery { localGroupDataSource.deleteGroup(testGroupId) } just Runs
            coEvery { cloudGroupDataSource.requestGroupDeletion(testGroupId) } just Runs

            // When
            repository.deleteGroup(testGroupId)
            advanceUntilIdle()

            // Then - Scheduler should NOT be called
            verify(exactly = 0) { groupDeletionRetryScheduler.scheduleRetry(any()) }
        }

        @Test
        fun `does not call old deleteGroup on cloud data source`() = runTest(testDispatcher) {
            // Given
            coEvery { localGroupDataSource.deleteGroup(testGroupId) } just Runs
            coEvery { cloudGroupDataSource.requestGroupDeletion(testGroupId) } just Runs

            // When
            repository.deleteGroup(testGroupId)
            advanceUntilIdle()

            // Then - Old deleteGroup should NOT be called; only requestGroupDeletion
            coVerify(exactly = 0) { cloudGroupDataSource.deleteGroup(any()) }
            coVerify(exactly = 1) { cloudGroupDataSource.requestGroupDeletion(testGroupId) }
        }
    }

    @Nested
    inner class GetAllGroupsFlow {

        @Test
        fun `returns flow from local data source`() = runTest(testDispatcher) {
            // Given
            val groups = listOf(testGroup)
            every { localGroupDataSource.getGroupsFlow() } returns flowOf(groups)
            every { cloudGroupDataSource.getAllGroupsFlow() } returns flowOf(emptyList())
            coEvery { localGroupDataSource.replaceAllGroups(any()) } just Runs

            // When
            val result = repository.getAllGroupsFlow().first()

            // Then
            assertEquals(groups, result)
        }

        @Test
        fun `returns groups with members from local data source`() = runTest(testDispatcher) {
            // Given
            val groupWithMembers = testGroup.copy(
                members = listOf("user-1", "user-2", "user-3")
            )
            every { localGroupDataSource.getGroupsFlow() } returns flowOf(listOf(groupWithMembers))
            every { cloudGroupDataSource.getAllGroupsFlow() } returns flowOf(emptyList())
            coEvery { localGroupDataSource.replaceAllGroups(any()) } just Runs

            // When
            var emittedGroups: List<Group>? = null
            repository.getAllGroupsFlow().collect { groups ->
                emittedGroups = groups
            }

            // Then
            assertEquals(3, emittedGroups?.first()?.members?.size)
            assertEquals(listOf("user-1", "user-2", "user-3"), emittedGroups?.first()?.members)
        }

        @Test
        fun `subscribes to real-time cloud changes and replaces local`() = runTest(testDispatcher) {
            // Given
            val cloudGroups = listOf(
                testGroup.copy(members = listOf("member-a", "member-b"))
            )
            every { localGroupDataSource.getGroupsFlow() } returns flowOf(emptyList())
            every { cloudGroupDataSource.getAllGroupsFlow() } returns flowOf(cloudGroups)
            coEvery { localGroupDataSource.replaceAllGroups(any()) } just Runs

            // When
            repository.getAllGroupsFlow().first()
            advanceUntilIdle()

            // Then - Verify groups replaced in local (not upserted) to handle deletions
            coVerify {
                localGroupDataSource.replaceAllGroups(
                    match { groups ->
                        groups.any { it.members == listOf("member-a", "member-b") }
                    }
                )
            }
        }

        @Test
        fun `cloud sync failure does not affect local data flow`() = runTest(testDispatcher) {
            // Given
            val localGroups = listOf(testGroup)
            every { localGroupDataSource.getGroupsFlow() } returns flowOf(localGroups)
            every { cloudGroupDataSource.getAllGroupsFlow() } returns flow {
                throw IOException("Network error")
            }

            // When
            val result = repository.getAllGroupsFlow().first()
            advanceUntilIdle()

            // Then - Should still return local data
            assertEquals(1, result.size)
            assertEquals(testGroup.id, result[0].id)
        }
    }

    @Nested
    inner class GetGroupById {

        @Test
        fun `returns group with members from local data source`() = runTest(testDispatcher) {
            // Given
            val groupWithMembers = testGroup.copy(
                members = listOf("user-1", "user-2")
            )
            coEvery { localGroupDataSource.getGroupById(testGroupId) } returns groupWithMembers

            // When
            val result = repository.getGroupById(testGroupId)

            // Then
            assertEquals(2, result?.members?.size)
            assertEquals(listOf("user-1", "user-2"), result?.members)
        }

        @Test
        fun `falls back to cloud and preserves members when not found locally`() = runTest(testDispatcher) {
            // Given
            val cloudGroup = testGroup.copy(
                members = listOf("cloud-user-1", "cloud-user-2", "cloud-user-3")
            )
            coEvery { localGroupDataSource.getGroupById(testGroupId) } returns null
            coEvery { cloudGroupDataSource.getGroupById(testGroupId) } returns cloudGroup
            coEvery { localGroupDataSource.saveGroup(any()) } just Runs

            // When
            val result = repository.getGroupById(testGroupId)

            // Then
            assertEquals(3, result?.members?.size)
            assertEquals(listOf("cloud-user-1", "cloud-user-2", "cloud-user-3"), result?.members)

            // Also verify it's cached to local
            coVerify {
                localGroupDataSource.saveGroup(
                    match { group ->
                        group.members == listOf("cloud-user-1", "cloud-user-2", "cloud-user-3")
                    }
                )
            }
        }

        @Test
        fun `handles group with no members`() = runTest(testDispatcher) {
            // Given
            val groupWithoutMembers = testGroup.copy(members = emptyList())
            coEvery { localGroupDataSource.getGroupById(testGroupId) } returns groupWithoutMembers

            // When
            val result = repository.getGroupById(testGroupId)

            // Then
            assertEquals(emptyList<String>(), result?.members)
        }
    }

    @Nested
    inner class CreateGroup {

        @Test
        fun `saves to local first then syncs to cloud`() = runTest(testDispatcher) {
            // Given
            val newGroup = testGroup.copy(id = "")
            coEvery { localGroupDataSource.saveGroup(any()) } just Runs
            coEvery { cloudGroupDataSource.createGroup(any()) } returns "new-id"

            // When
            repository.createGroup(newGroup)
            advanceUntilIdle()

            // Then - Local save should happen
            coVerify(exactly = 1) { localGroupDataSource.saveGroup(any()) }
        }
    }
}
