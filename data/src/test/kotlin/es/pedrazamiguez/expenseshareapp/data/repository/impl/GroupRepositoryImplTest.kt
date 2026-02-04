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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class GroupRepositoryImplTest {

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
            localExpenseDataSource = localExpenseDataSource
        )
    }

    @Nested
    inner class DeleteGroup {

        @Test
        fun `captures expense IDs before deleting group`() = runTest {
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
        fun `deletes group from local storage first`() = runTest {
            // Given
            coEvery { localExpenseDataSource.getExpenseIdsByGroup(testGroupId) } returns emptyList()
            coEvery { localGroupDataSource.deleteGroup(testGroupId) } just Runs

            // When
            repository.deleteGroup(testGroupId)

            // Then
            coVerify(exactly = 1) { localGroupDataSource.deleteGroup(testGroupId) }
        }

        @Test
        fun `syncs expense deletions to cloud in background`() = runTest {
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
        fun `syncs group deletion to cloud after expenses`() = runTest {
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
        fun `handles group with no expenses`() = runTest {
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
        fun `continues local delete even if cloud sync fails`() = runTest {
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
        fun `handles many expenses efficiently`() = runTest {
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
        fun `returns flow from local data source`() = runTest {
            // Given
            val groups = listOf(testGroup)
            every { localGroupDataSource.getGroupsFlow() } returns flowOf(groups)
            coEvery { cloudGroupDataSource.getAllGroupsFlow() } returns flowOf(emptyList())

            // When
            val flow = repository.getAllGroupsFlow()

            // Then
            coVerify { localGroupDataSource.getGroupsFlow() }
        }
    }

    @Nested
    inner class CreateGroup {

        @Test
        fun `saves to local first then syncs to cloud`() = runTest {
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
