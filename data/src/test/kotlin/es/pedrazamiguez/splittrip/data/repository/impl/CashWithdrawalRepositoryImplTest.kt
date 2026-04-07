package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.domain.datasource.cloud.CloudCashWithdrawalDataSource
import es.pedrazamiguez.splittrip.domain.datasource.local.LocalCashWithdrawalDataSource
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CashWithdrawalRepositoryImplTest {

    private lateinit var cloudDataSource: CloudCashWithdrawalDataSource
    private lateinit var localDataSource: LocalCashWithdrawalDataSource
    private lateinit var authenticationService: AuthenticationService
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var repository: CashWithdrawalRepositoryImpl

    private val testUserId = "user-123"
    private val testGroupId = "group-123"
    private val testWithdrawal = CashWithdrawal(
        id = "w-1",
        groupId = testGroupId,
        withdrawnBy = "user-1",
        amountWithdrawn = 1000000L,
        remainingAmount = 1000000L,
        currency = "THB",
        deductedBaseAmount = 27000L,
        exchangeRate = java.math.BigDecimal("37.037"),
        createdAt = LocalDateTime.of(2026, 1, 15, 12, 0)
    )

    @BeforeEach
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        cloudDataSource = mockk(relaxed = true)
        localDataSource = mockk(relaxed = true)
        authenticationService = mockk()
        every { authenticationService.currentUserId() } returns testUserId

        repository = CashWithdrawalRepositoryImpl(
            cloudCashWithdrawalDataSource = cloudDataSource,
            localCashWithdrawalDataSource = localDataSource,
            authenticationService = authenticationService,
            ioDispatcher = testDispatcher
        )
    }

    @Nested
    inner class AddWithdrawal {

        @Test
        fun `saves to local first before cloud sync`() = runTest(testDispatcher) {
            // Given
            coEvery { localDataSource.saveWithdrawal(any()) } just Runs
            coEvery { cloudDataSource.addWithdrawal(any(), any()) } just Runs

            // When
            repository.addWithdrawal(testGroupId, testWithdrawal)
            advanceUntilIdle()

            // Then
            coVerifyOrder {
                localDataSource.saveWithdrawal(any())
                cloudDataSource.addWithdrawal(testGroupId, any())
            }
        }

        @Test
        fun `generates UUID when id is blank`() = runTest(testDispatcher) {
            // Given
            val withdrawalNoId = testWithdrawal.copy(id = "")
            val slot = slot<CashWithdrawal>()
            coEvery { localDataSource.saveWithdrawal(capture(slot)) } just Runs

            // When
            repository.addWithdrawal(testGroupId, withdrawalNoId)

            // Then
            assertTrue(slot.captured.id.isNotBlank())
        }

        @Test
        fun `sets createdBy to current authenticated user`() = runTest(testDispatcher) {
            // Given — withdrawal has a different withdrawnBy (impersonation scenario)
            val withdrawal = testWithdrawal.copy(withdrawnBy = "target-user")
            val slot = slot<CashWithdrawal>()
            coEvery { localDataSource.saveWithdrawal(capture(slot)) } just Runs

            // When
            repository.addWithdrawal(testGroupId, withdrawal)

            // Then — createdBy is always the authenticated user (actor), not the target
            assertEquals(testUserId, slot.captured.createdBy)
            assertEquals("target-user", slot.captured.withdrawnBy)
        }

        @Test
        fun `sets remainingAmount to amountWithdrawn when not set`() = runTest(testDispatcher) {
            // Given
            val withdrawal = testWithdrawal.copy(remainingAmount = 0)
            val slot = slot<CashWithdrawal>()
            coEvery { localDataSource.saveWithdrawal(capture(slot)) } just Runs

            // When
            repository.addWithdrawal(testGroupId, withdrawal)

            // Then
            assertEquals(testWithdrawal.amountWithdrawn, slot.captured.remainingAmount)
        }

        @Test
        fun `syncs to cloud in background`() = runTest(testDispatcher) {
            // Given
            coEvery { localDataSource.saveWithdrawal(any()) } just Runs
            coEvery { cloudDataSource.addWithdrawal(any(), any()) } just Runs

            // When
            repository.addWithdrawal(testGroupId, testWithdrawal)
            advanceUntilIdle()

            // Then
            coVerify { cloudDataSource.addWithdrawal(testGroupId, any()) }
        }

        @Test
        fun `cloud failure does not affect local save`() = runTest(testDispatcher) {
            // Given
            coEvery { localDataSource.saveWithdrawal(any()) } just Runs
            coEvery { cloudDataSource.addWithdrawal(any(), any()) } throws RuntimeException("No network")

            // When - Should not throw
            repository.addWithdrawal(testGroupId, testWithdrawal)
            advanceUntilIdle()

            // Then - Local save should succeed
            coVerify { localDataSource.saveWithdrawal(any()) }
        }

        @Test
        fun `saves with PENDING_SYNC status`() = runTest(testDispatcher) {
            // Given
            coEvery { localDataSource.saveWithdrawal(any()) } just Runs

            // When
            repository.addWithdrawal(testGroupId, testWithdrawal)

            // Then
            coVerify {
                localDataSource.saveWithdrawal(
                    match { it.syncStatus == SyncStatus.PENDING_SYNC }
                )
            }
        }

        @Test
        fun `updates to SYNCED after successful cloud sync`() = runTest(testDispatcher) {
            // Given
            coEvery { localDataSource.saveWithdrawal(any()) } just Runs
            coEvery { cloudDataSource.addWithdrawal(any(), any()) } just Runs
            coEvery { localDataSource.updateSyncStatus(any(), any()) } just Runs

            // When
            repository.addWithdrawal(testGroupId, testWithdrawal)
            advanceUntilIdle()

            // Then
            coVerify { localDataSource.updateSyncStatus(any(), SyncStatus.SYNCED) }
        }

        @Test
        fun `updates to SYNC_FAILED after cloud sync failure`() = runTest(testDispatcher) {
            // Given
            coEvery { localDataSource.saveWithdrawal(any()) } just Runs
            coEvery {
                cloudDataSource.addWithdrawal(any(), any())
            } throws RuntimeException("No network")
            coEvery { localDataSource.updateSyncStatus(any(), any()) } just Runs

            // When
            repository.addWithdrawal(testGroupId, testWithdrawal)
            advanceUntilIdle()

            // Then
            coVerify { localDataSource.updateSyncStatus(any(), SyncStatus.SYNC_FAILED) }
        }
    }

    @Nested
    inner class GetGroupWithdrawalsFlow {

        @Test
        fun `returns local data flow`() = runTest(testDispatcher) {
            // Given
            val localWithdrawals = listOf(testWithdrawal)
            every { localDataSource.getWithdrawalsByGroupIdFlow(testGroupId) } returns flowOf(localWithdrawals)
            every { cloudDataSource.getWithdrawalsByGroupIdFlow(testGroupId) } returns flowOf(localWithdrawals)
            coEvery { localDataSource.replaceWithdrawalsForGroup(any(), any()) } just Runs

            // When
            val flow = repository.getGroupWithdrawalsFlow(testGroupId)
            val result = flow.first()
            advanceUntilIdle()

            // Then
            assertEquals(1, result.size)
            assertEquals(testWithdrawal.id, result[0].id)
        }
    }

    @Nested
    inner class RefundTranche {

        @Test
        fun `refunds amount back to withdrawal`() = runTest(testDispatcher) {
            // Given
            val withdrawal = testWithdrawal.copy(remainingAmount = 500000L)
            coEvery { localDataSource.getWithdrawalById("w-1") } returns withdrawal
            coEvery { localDataSource.updateRemainingAmount(any(), any()) } just Runs

            // When
            repository.refundTranche("w-1", 200000L)
            advanceUntilIdle()

            // Then - Should update with original remaining + refunded amount
            coVerify { localDataSource.updateRemainingAmount("w-1", 700000L) }
        }
    }

    @Nested
    inner class DeleteWithdrawal {

        @Test
        fun `deletes from local first`() = runTest(testDispatcher) {
            // Given
            coEvery { localDataSource.deleteWithdrawal("w-1") } just Runs
            coEvery { cloudDataSource.deleteWithdrawal(any(), any()) } just Runs

            // When
            repository.deleteWithdrawal(testGroupId, "w-1")
            advanceUntilIdle()

            // Then
            coVerifyOrder {
                localDataSource.deleteWithdrawal("w-1")
                cloudDataSource.deleteWithdrawal(testGroupId, "w-1")
            }
        }

        @Test
        fun `queues cloud deletion even when withdrawal is PENDING_SYNC`() = runTest(testDispatcher) {
            // Given — withdrawal was created offline, never synced. Firestore SDK has the
            // create write cached; queuing a deletion ensures it executes after the create.
            val withdrawalId = "pending-w"
            coEvery { localDataSource.deleteWithdrawal(withdrawalId) } just Runs
            coEvery { cloudDataSource.deleteWithdrawal(any(), any()) } just Runs

            // When
            repository.deleteWithdrawal(testGroupId, withdrawalId)
            advanceUntilIdle()

            // Then — cloud deletion should be queued (Firestore SDK handles write ordering)
            coVerify(exactly = 1) {
                cloudDataSource.deleteWithdrawal(testGroupId, withdrawalId)
            }
            // Local delete should still happen
            coVerify(exactly = 1) { localDataSource.deleteWithdrawal(withdrawalId) }
        }

        @Test
        fun `syncs to cloud when withdrawal is SYNCED`() = runTest(testDispatcher) {
            // Given
            val withdrawalId = "synced-w"
            val syncedWithdrawal = testWithdrawal.copy(
                id = withdrawalId,
                syncStatus = SyncStatus.SYNCED
            )
            coEvery { localDataSource.getWithdrawalById(withdrawalId) } returns syncedWithdrawal
            coEvery { localDataSource.deleteWithdrawal(withdrawalId) } just Runs
            coEvery { cloudDataSource.deleteWithdrawal(any(), any()) } just Runs

            // When
            repository.deleteWithdrawal(testGroupId, withdrawalId)
            advanceUntilIdle()

            // Then — cloud deletion should happen
            coVerify(exactly = 1) {
                cloudDataSource.deleteWithdrawal(testGroupId, withdrawalId)
            }
        }

        @Test
        fun `syncs to cloud when withdrawal not found locally`() = runTest(testDispatcher) {
            // Given — withdrawal not found (null syncStatus != PENDING_SYNC)
            val withdrawalId = "unknown-w"
            coEvery { localDataSource.getWithdrawalById(withdrawalId) } returns null
            coEvery { localDataSource.deleteWithdrawal(withdrawalId) } just Runs
            coEvery { cloudDataSource.deleteWithdrawal(any(), any()) } just Runs

            // When
            repository.deleteWithdrawal(testGroupId, withdrawalId)
            advanceUntilIdle()

            // Then — cloud deletion should still happen
            coVerify(exactly = 1) {
                cloudDataSource.deleteWithdrawal(testGroupId, withdrawalId)
            }
        }

        @Test
        fun `cloud failure does not affect local delete`() = runTest(testDispatcher) {
            // Given
            coEvery { localDataSource.deleteWithdrawal("w-1") } just Runs
            coEvery {
                cloudDataSource.deleteWithdrawal(any(), any())
            } throws RuntimeException("Network error")

            // When
            repository.deleteWithdrawal(testGroupId, "w-1")
            advanceUntilIdle()

            // Then - Local delete should still have happened
            coVerify(exactly = 1) { localDataSource.deleteWithdrawal("w-1") }
        }
    }
}
