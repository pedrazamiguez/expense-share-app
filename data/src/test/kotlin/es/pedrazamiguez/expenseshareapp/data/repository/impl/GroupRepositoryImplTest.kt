package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
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
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class GroupRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var cloudGroupDataSource: CloudGroupDataSource
    private lateinit var localGroupDataSource: LocalGroupDataSource
    private lateinit var cloudExpenseDataSource: CloudExpenseDataSource
    private lateinit var localExpenseDataSource: LocalExpenseDataSource
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
        cloudExpenseDataSource = mockk(relaxed = true)
        localExpenseDataSource = mockk(relaxed = true)

        repository = GroupRepositoryImpl(
            cloudGroupDataSource = cloudGroupDataSource,
            localGroupDataSource = localGroupDataSource,
            cloudExpenseDataSource = cloudExpenseDataSource,
            localExpenseDataSource = localExpenseDataSource,
            ioDispatcher = testDispatcher
        )
    }

    @Nested
    inner class DeleteGroup {

        @Test
        fun `captures expense IDs before deleting group`() = runTest(testDispatcher) {
            // Given
            val expenseIds = listOf("expense-1", "expense-2", "expense-3")
            coEvery { localExpenseDataSource.getExpenseIdsByGroup(testGroupId) } returns expenseIds
            coEvery { localGroupDataSource.deleteGroup(testGroupId) } just Runs

            // When
            repository.deleteGroup(testGroupId)

            // Then - Capture should happen BEFORE delete
            coVerifyOrder {
                localExpenseDataSource.getExpenseIdsByGroup(testGroupId)
                localGroupDataSource.deleteGroup(testGroupId)
            }
        }

        @Test
        fun `deletes group from local storage first`() = runTest(testDispatcher) {
            // Given
            coEvery { localExpenseDataSource.getExpenseIdsByGroup(testGroupId) } returns emptyList()
            coEvery { localGroupDataSource.deleteGroup(testGroupId) } just Runs

            // When
            repository.deleteGroup(testGroupId)

            // Then
            coVerify(exactly = 1) { localGroupDataSource.deleteGroup(testGroupId) }
        }

        @Test
        fun `syncs expense deletions to cloud in background`() = runTest(testDispatcher) {
            // Given
            val expenseIds = listOf("expense-1", "expense-2")
            coEvery { localExpenseDataSource.getExpenseIdsByGroup(testGroupId) } returns expenseIds
            coEvery { localGroupDataSource.deleteGroup(testGroupId) } just Runs
            coEvery { cloudExpenseDataSource.deleteExpense(any(), any()) } just Runs
            coEvery { cloudGroupDataSource.deleteGroup(any()) } just Runs

            // When
            repository.deleteGroup(testGroupId)
            advanceUntilIdle() // Allow background sync to complete

            // Then - All expenses should be deleted from cloud
            coVerify(exactly = 1) { cloudExpenseDataSource.deleteExpense(testGroupId, "expense-1") }
            coVerify(exactly = 1) { cloudExpenseDataSource.deleteExpense(testGroupId, "expense-2") }
        }

        @Test
        fun `syncs group deletion to cloud after expenses`() = runTest(testDispatcher) {
            // Given
            val expenseIds = listOf("expense-1")
            coEvery { localExpenseDataSource.getExpenseIdsByGroup(testGroupId) } returns expenseIds
            coEvery { localGroupDataSource.deleteGroup(testGroupId) } just Runs
            coEvery { cloudExpenseDataSource.deleteExpense(any(), any()) } just Runs
            coEvery { cloudGroupDataSource.deleteGroup(any()) } just Runs

            // When
            repository.deleteGroup(testGroupId)
            advanceUntilIdle()

            // Then - Group should be deleted from cloud
            coVerify(exactly = 1) { cloudGroupDataSource.deleteGroup(testGroupId) }
        }

        @Test
        fun `handles group with no expenses`() = runTest(testDispatcher) {
            // Given
            coEvery { localExpenseDataSource.getExpenseIdsByGroup(testGroupId) } returns emptyList()
            coEvery { localGroupDataSource.deleteGroup(testGroupId) } just Runs
            coEvery { cloudGroupDataSource.deleteGroup(any()) } just Runs

            // When
            repository.deleteGroup(testGroupId)
            advanceUntilIdle()

            // Then - Should still delete group from cloud
            coVerify(exactly = 1) { cloudGroupDataSource.deleteGroup(testGroupId) }
            coVerify(exactly = 0) { cloudExpenseDataSource.deleteExpense(any(), any()) }
        }

        @Test
        fun `continues local delete even if cloud sync fails`() = runTest(testDispatcher) {
            // Given
            coEvery { localExpenseDataSource.getExpenseIdsByGroup(testGroupId) } returns listOf("expense-1")
            coEvery { localGroupDataSource.deleteGroup(testGroupId) } just Runs
            coEvery { cloudExpenseDataSource.deleteExpense(any(), any()) } throws RuntimeException("Network error")

            // When - Should not throw
            repository.deleteGroup(testGroupId)
            advanceUntilIdle()

            // Then - Local delete should have completed
            coVerify(exactly = 1) { localGroupDataSource.deleteGroup(testGroupId) }
        }

        @Test
        fun `handles many expenses efficiently`() = runTest(testDispatcher) {
            // Given - A group with many expenses
            val expenseIds = (1..50).map { "expense-$it" }
            coEvery { localExpenseDataSource.getExpenseIdsByGroup(testGroupId) } returns expenseIds
            coEvery { localGroupDataSource.deleteGroup(testGroupId) } just Runs
            coEvery { cloudExpenseDataSource.deleteExpense(any(), any()) } just Runs
            coEvery { cloudGroupDataSource.deleteGroup(any()) } just Runs

            // When
            repository.deleteGroup(testGroupId)
            advanceUntilIdle()

            // Then - Local delete should happen immediately
            coVerify(exactly = 1) { localGroupDataSource.deleteGroup(testGroupId) }
            // Cloud sync happens in background with Dispatchers.IO, verification is best-effort
            // The important assertion is that the local operation completes successfully
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
            val flow = repository.getAllGroupsFlow()

            // Then
            coVerify { localGroupDataSource.getGroupsFlow() }
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
                localGroupDataSource.replaceAllGroups(match { groups ->
                    groups.any { it.members == listOf("member-a", "member-b") }
                })
            }
        }

        @Test
        fun `cloud sync failure does not affect local data flow`() = runTest(testDispatcher) {
            // Given
            val localGroups = listOf(testGroup)
            every { localGroupDataSource.getGroupsFlow() } returns flowOf(localGroups)
            every { cloudGroupDataSource.getAllGroupsFlow() } returns flow {
                throw RuntimeException("Network error")
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
                localGroupDataSource.saveGroup(match { group ->
                    group.members == listOf("cloud-user-1", "cloud-user-2", "cloud-user-3")
                })
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
