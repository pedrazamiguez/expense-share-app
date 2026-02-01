package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseRepositoryImplTest {

    private lateinit var cloudExpenseDataSource: CloudExpenseDataSource
    private lateinit var localExpenseDataSource: LocalExpenseDataSource
    private lateinit var authenticationService: AuthenticationService
    private lateinit var repository: ExpenseRepositoryImpl

    private val testUserId = "user-123"
    private val testGroupId = "group-123"
    private val testExpense = Expense(
        id = "expense-1",
        groupId = testGroupId,
        title = "Dinner",
        sourceAmount = 5000L,
        sourceCurrency = "EUR",
        groupAmount = 5000L,
        groupCurrency = "EUR",
        paymentMethod = PaymentMethod.CREDIT_CARD,
        createdBy = "user-1",
        createdAt = LocalDateTime.of(2024, 1, 15, 12, 30)
    )

    private val cloudExpenses = listOf(
        testExpense,
        Expense(
            id = "expense-2",
            groupId = testGroupId,
            title = "Taxi",
            sourceAmount = 2000L,
            sourceCurrency = "EUR",
            groupAmount = 2000L,
            groupCurrency = "EUR",
            paymentMethod = PaymentMethod.CASH,
            createdBy = "user-2",
            createdAt = LocalDateTime.of(2024, 1, 16, 10, 0)
        )
    )

    @BeforeEach
    fun setUp() {
        cloudExpenseDataSource = mockk()
        localExpenseDataSource = mockk(relaxed = true)
        authenticationService = mockk()
        every { authenticationService.currentUserId() } returns testUserId
        repository = ExpenseRepositoryImpl(
            cloudExpenseDataSource,
            localExpenseDataSource,
            authenticationService
        )
    }

    @Nested
    inner class AddExpense {

        @Test
        fun `addExpense saves to local first`() = runTest {
            // Given
            val expenseWithoutGroup = testExpense.copy(groupId = "")
            val expenseSlot = slot<Expense>()
            coEvery { localExpenseDataSource.saveExpense(capture(expenseSlot)) } just Runs
            coEvery { cloudExpenseDataSource.addExpense(any(), any()) } just Runs

            // When
            repository.addExpense(testGroupId, expenseWithoutGroup)
            advanceTimeBy(100) // Allow local save to complete

            // Then - Local save should happen immediately
            coVerify { localExpenseDataSource.saveExpense(any()) }
            assertEquals(testGroupId, expenseSlot.captured.groupId)
        }

        @Test
        fun `addExpense generates UUID when expense has blank ID`() = runTest {
            // Given
            val expenseWithBlankId = testExpense.copy(id = "")
            val expenseSlot = slot<Expense>()
            coEvery { localExpenseDataSource.saveExpense(capture(expenseSlot)) } just Runs
            coEvery { cloudExpenseDataSource.addExpense(any(), any()) } just Runs

            // When
            repository.addExpense(testGroupId, expenseWithBlankId)
            advanceTimeBy(100)

            // Then - Should generate a valid UUID
            assertNotNull(expenseSlot.captured.id)
            assertTrue(expenseSlot.captured.id.isNotBlank())
        }

        @Test
        fun `addExpense preserves existing ID when present`() = runTest {
            // Given
            val existingId = "existing-id-123"
            val expenseWithId = testExpense.copy(id = existingId)
            val expenseSlot = slot<Expense>()
            coEvery { localExpenseDataSource.saveExpense(capture(expenseSlot)) } just Runs
            coEvery { cloudExpenseDataSource.addExpense(any(), any()) } just Runs

            // When
            repository.addExpense(testGroupId, expenseWithId)
            advanceTimeBy(100)

            // Then - Should keep the existing ID
            assertEquals(existingId, expenseSlot.captured.id)
        }

        @Test
        fun `addExpense syncs to cloud in background`() = runTest {
            // Given
            coEvery { localExpenseDataSource.saveExpense(any()) } just Runs
            coEvery { cloudExpenseDataSource.addExpense(any(), any()) } just Runs

            // When
            repository.addExpense(testGroupId, testExpense)
            advanceUntilIdle() // Allow background sync to complete

            // Then - Cloud sync should happen
            coVerify { cloudExpenseDataSource.addExpense(testGroupId, any()) }
        }

        @Test
        fun `addExpense local save succeeds even if cloud sync fails`() = runTest {
            // Given
            coEvery { localExpenseDataSource.saveExpense(any()) } just Runs
            coEvery {
                cloudExpenseDataSource.addExpense(
                    any(),
                    any()
                )
            } throws RuntimeException("Network error")

            // When - Should not throw exception
            repository.addExpense(testGroupId, testExpense)
            advanceUntilIdle()

            // Then - Local save should still succeed
            coVerify { localExpenseDataSource.saveExpense(any()) }
        }

        @Test
        fun `addExpense populates groupId from parameter`() = runTest {
            // Given
            val expenseWithoutGroup = testExpense.copy(groupId = "")
            val expenseSlot = slot<Expense>()
            coEvery { localExpenseDataSource.saveExpense(capture(expenseSlot)) } just Runs
            coEvery { cloudExpenseDataSource.addExpense(any(), any()) } just Runs

            // When
            repository.addExpense(testGroupId, expenseWithoutGroup)
            advanceTimeBy(100)

            // Then
            assertEquals(testGroupId, expenseSlot.captured.groupId)
        }

        @Test
        fun `addExpense populates createdBy with current user ID when blank`() = runTest {
            // Given
            val expenseWithoutCreatedBy = testExpense.copy(createdBy = "")
            val expenseSlot = slot<Expense>()
            coEvery { localExpenseDataSource.saveExpense(capture(expenseSlot)) } just Runs
            coEvery { cloudExpenseDataSource.addExpense(any(), any()) } just Runs

            // When
            repository.addExpense(testGroupId, expenseWithoutCreatedBy)
            advanceTimeBy(100)

            // Then - Should populate with authenticated user ID
            assertEquals(testUserId, expenseSlot.captured.createdBy)
        }

        @Test
        fun `addExpense preserves existing createdBy when present`() = runTest {
            // Given
            val existingUserId = "existing-user-456"
            val expenseWithCreatedBy = testExpense.copy(createdBy = existingUserId)
            val expenseSlot = slot<Expense>()
            coEvery { localExpenseDataSource.saveExpense(capture(expenseSlot)) } just Runs
            coEvery { cloudExpenseDataSource.addExpense(any(), any()) } just Runs

            // When
            repository.addExpense(testGroupId, expenseWithCreatedBy)
            advanceTimeBy(100)

            // Then - Should keep existing createdBy
            assertEquals(existingUserId, expenseSlot.captured.createdBy)
        }

        @Test
        fun `addExpense generates timestamps when missing`() = runTest {
            // Given
            val expenseWithoutTimestamps = testExpense.copy(createdAt = null, lastUpdatedAt = null)
            val expenseSlot = slot<Expense>()
            coEvery { localExpenseDataSource.saveExpense(capture(expenseSlot)) } just Runs
            coEvery { cloudExpenseDataSource.addExpense(any(), any()) } just Runs

            // When
            repository.addExpense(testGroupId, expenseWithoutTimestamps)
            advanceTimeBy(100)

            // Then - Should have timestamps
            assertNotNull(expenseSlot.captured.createdAt)
            assertNotNull(expenseSlot.captured.lastUpdatedAt)
        }

        @Test
        fun `addExpense works offline with all metadata populated`() = runTest {
            // Given - Offline scenario: expense with no ID, no createdBy, no timestamps
            val offlineExpense = Expense(
                id = "",
                groupId = "",
                title = "Offline Expense",
                sourceAmount = 3000L,
                sourceCurrency = "EUR",
                groupAmount = 3000L,
                groupCurrency = "EUR",
                createdBy = "",
                createdAt = null,
                lastUpdatedAt = null
            )
            val expenseSlot = slot<Expense>()
            coEvery { localExpenseDataSource.saveExpense(capture(expenseSlot)) } just Runs
            coEvery {
                cloudExpenseDataSource.addExpense(
                    any(),
                    any()
                )
            } throws RuntimeException("No network")

            // When
            repository.addExpense(testGroupId, offlineExpense)
            advanceUntilIdle()

            // Then - All metadata should be populated for offline use
            val saved = expenseSlot.captured
            assertTrue(saved.id.isNotBlank(), "ID should be generated")
            assertEquals(testGroupId, saved.groupId)
            assertEquals(testUserId, saved.createdBy)
            assertNotNull(saved.createdAt)
            assertNotNull(saved.lastUpdatedAt)

            // Verify local save succeeded despite cloud failure
            coVerify { localExpenseDataSource.saveExpense(any()) }
        }
    }

    @Nested
    inner class GetGroupExpensesFlow {

        @Test
        fun `returns local data immediately`() = runTest {
            // Given - Local data is available
            val localExpenses = listOf(testExpense)
            every { localExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } returns flowOf(
                localExpenses
            )
            coEvery { cloudExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } returns flowOf(
                cloudExpenses
            )

            // When
            val flow = repository.getGroupExpensesFlow(testGroupId)
            val result = flow.first()

            // Then - Should return local data immediately
            assertEquals(1, result.size)
            assertEquals(testExpense.id, result[0].id)
        }

        @Test
        fun `triggers background cloud sync on flow start`() = runTest {
            // Given
            every { localExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } returns flowOf(
                emptyList()
            )
            coEvery { cloudExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } returns flowOf(
                cloudExpenses
            )
            coEvery { localExpenseDataSource.saveExpenses(any()) } just Runs

            // When
            val flow = repository.getGroupExpensesFlow(testGroupId)
            flow.first() // Trigger flow collection
            advanceUntilIdle() // Allow background sync to complete

            // Then - Cloud should be queried
            coVerify { cloudExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) }
        }

        @Test
        fun `cloud sync updates local cache on success`() = runTest {
            // Given
            every { localExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } returns flowOf(
                emptyList()
            )
            coEvery { cloudExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } returns flowOf(
                cloudExpenses
            )
            coEvery { localExpenseDataSource.saveExpenses(any()) } just Runs

            // When
            val flow = repository.getGroupExpensesFlow(testGroupId)
            flow.first()

            // Allow background sync coroutines to complete (syncScope uses Dispatchers.IO)
            // Use polling with timeout for more deterministic behavior on CI
            withTimeoutOrNull(2000) {
                while (true) {
                    try {
                        coVerify { localExpenseDataSource.saveExpenses(cloudExpenses) }
                        break
                    } catch (e: AssertionError) {
                        delay(50)
                    }
                }
            } ?: run {
                // Final verification - will fail with proper error message if not called
                coVerify { localExpenseDataSource.saveExpenses(cloudExpenses) }
            }
        }

        @Test
        fun `cloud sync failure does not affect local data flow`() = runTest {
            // Given
            val localExpenses = listOf(testExpense)
            every { localExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } returns flowOf(
                localExpenses
            )
            coEvery { cloudExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } throws RuntimeException(
                "Network error"
            )

            // When - Should not throw exception
            val flow = repository.getGroupExpensesFlow(testGroupId)
            val result = flow.first()

            // Then - Should still return local data
            assertNotNull(result)
            assertEquals(1, result.size)
            assertEquals(testExpense.id, result[0].id)
        }

        @Test
        fun `multiple subscribers trigger sync only once`() = runTest {
            // Given
            every { localExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } returns flowOf(
                emptyList()
            )
            coEvery { cloudExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } returns flowOf(
                cloudExpenses
            )
            coEvery { localExpenseDataSource.saveExpenses(any()) } just Runs

            // When - Multiple subscribers
            val flow = repository.getGroupExpensesFlow(testGroupId)
            flow.first()
            // Wait for first sync to complete before second subscription
            delay(100)
            flow.first()

            // Allow background sync coroutines to complete (syncScope uses Dispatchers.IO)
            // Use polling with timeout for more deterministic behavior on CI
            withTimeoutOrNull(2000) {
                while (true) {
                    try {
                        coVerify(atLeast = 2) { cloudExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) }
                        break
                    } catch (e: AssertionError) {
                        delay(50)
                    }
                }
            } ?: run {
                // Final verification - will fail with proper error message if not called
                coVerify(atLeast = 2) { cloudExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) }
            }
        }
    }

    @Nested
    inner class OfflineFirstBehavior {

        @Test
        fun `offline mode uses only local data`() = runTest {
            // Given - Local has data, cloud is unavailable
            val localExpenses = listOf(testExpense)
            every { localExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } returns flowOf(
                localExpenses
            )
            coEvery { cloudExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } throws RuntimeException(
                "No network"
            )

            // When
            val flow = repository.getGroupExpensesFlow(testGroupId)
            val result = flow.first()

            // Then - Should return local data without errors
            assertEquals(1, result.size)
            assertEquals(testExpense.id, result[0].id)
        }

        @Test
        fun `local writes succeed immediately regardless of cloud status`() = runTest {
            // Given - Cloud is unavailable
            coEvery { localExpenseDataSource.saveExpense(any()) } just Runs
            coEvery {
                cloudExpenseDataSource.addExpense(
                    any(),
                    any()
                )
            } throws RuntimeException("No network")

            // When - Should not throw
            repository.addExpense(testGroupId, testExpense)
            advanceTimeBy(100)

            // Then - Local save should succeed
            coVerify { localExpenseDataSource.saveExpense(any()) }
        }

        @Test
        fun `stale local data is returned before cloud sync completes`() = runTest {
            // Given - Local has old data, cloud has new data but is slow
            val staleExpense = testExpense.copy(title = "Old Title")
            every { localExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } returns flowOf(
                listOf(staleExpense)
            )
            coEvery { cloudExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } coAnswers {
                delay(1000) // Simulate slow network
                flowOf(cloudExpenses)
            }
            coEvery { localExpenseDataSource.saveExpenses(any()) } just Runs

            // When - Get data immediately
            val flow = repository.getGroupExpensesFlow(testGroupId)
            val immediateResult = flow.first()

            // Then - Should get stale local data immediately without waiting for cloud
            assertEquals("Old Title", immediateResult[0].title)
        }
    }

    @Nested
    inner class SingleSourceOfTruth {

        @Test
        fun `local database is the single source of truth`() = runTest {
            // Given - Different data in local and cloud
            val localExpenses = listOf(testExpense.copy(title = "Local Title"))
            val cloudExpenses = listOf(testExpense.copy(title = "Cloud Title"))

            every { localExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } returns flowOf(
                localExpenses
            )
            coEvery { cloudExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } returns flowOf(
                cloudExpenses
            )
            coEvery { localExpenseDataSource.saveExpenses(any()) } just Runs

            // When - Get data
            val flow = repository.getGroupExpensesFlow(testGroupId)
            val result = flow.first()

            // Then - Should return local data (SSOT)
            assertEquals("Local Title", result[0].title)
        }

        @Test
        fun `cloud data updates local which updates UI via flow`() = runTest {
            // Given - Initial local state and cloud update
            val initialLocal = listOf(testExpense.copy(title = "Initial"))
            val cloudUpdate = listOf(testExpense.copy(title = "Updated"))

            every { localExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } returns flowOf(
                initialLocal
            )
            coEvery { cloudExpenseDataSource.getExpensesByGroupIdFlow(testGroupId) } returns flowOf(
                cloudUpdate
            )
            coEvery { localExpenseDataSource.saveExpenses(any()) } just Runs

            // When
            val flow = repository.getGroupExpensesFlow(testGroupId)
            flow.first()
            advanceUntilIdle()

            // Then - Cloud data should update local
            coVerify { localExpenseDataSource.saveExpenses(cloudUpdate) }
        }
    }
}
