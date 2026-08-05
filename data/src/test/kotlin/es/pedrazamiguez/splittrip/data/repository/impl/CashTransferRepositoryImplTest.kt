package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.core.performance.PerformanceMonitor
import es.pedrazamiguez.splittrip.data.local.dao.CashTransferDao
import es.pedrazamiguez.splittrip.data.local.entity.CashTransferEntity
import es.pedrazamiguez.splittrip.domain.datasource.cloud.CloudCashTransferDataSource
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.CashTransfer
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
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
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CashTransferRepositoryImplTest {

    private lateinit var cashTransferDao: CashTransferDao
    private lateinit var cloudDataSource: CloudCashTransferDataSource
    private lateinit var performanceMonitor: PerformanceMonitor
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var repository: CashTransferRepositoryImpl

    private val testGroupId = "group-123"
    private val testTransfer = CashTransfer(
        id = "transfer-123",
        groupId = testGroupId,
        fromUserId = "user-1",
        toUserId = "user-2",
        amountCents = 1000L,
        currency = "USD",
        equivalentBaseAmountCents = 1000L,
        createdAt = 123456L
    )

    @BeforeEach
    fun setup() {
        cashTransferDao = mockk()
        cloudDataSource = mockk()
        performanceMonitor = mockk(relaxed = true) {
            io.mockk.coEvery { traceAsync<Any?>(any(), any()) } coAnswers { secondArg<suspend () -> Any?>().invoke() }
            io.mockk.every { trace<Any?>(any(), any()) } answers { secondArg<() -> Any?>().invoke() }
        }
        testDispatcher = StandardTestDispatcher()

        repository = CashTransferRepositoryImpl(
            cashTransferDao = cashTransferDao,
            cloudCashTransferDataSource = cloudDataSource,
            performanceMonitor = performanceMonitor,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `addTransfer saves to local DB and launches sync`() = runTest(testDispatcher) {
        val capturedEntity = slot<CashTransferEntity>()
        coEvery { cashTransferDao.insert(capture(capturedEntity)) } just Runs
        coEvery { cloudDataSource.upsertCashTransfer(any()) } returns Result.success(Unit)
        coEvery { cashTransferDao.getCashTransferById(any()) } returns null
        coEvery { cashTransferDao.updateSyncStatus(any(), any()) } just Runs

        val result = repository.addTransfer(testTransfer)

        assertTrue(result.isSuccess)
        assertEquals("transfer-123", capturedEntity.captured.id)
        assertEquals(SyncStatus.PENDING_SYNC, capturedEntity.captured.syncStatus)

        advanceUntilIdle()

        coVerify { cloudDataSource.upsertCashTransfer(any()) }
        coVerify { cashTransferDao.updateSyncStatus(testTransfer.id, SyncStatus.SYNCED.name) }
    }

    @Test
    fun `observeGroupCashTransfers flows local DB and subscribes to cloud changes`() = runTest(testDispatcher) {
        val localEntities = listOf(
            CashTransferEntity(
                id = "transfer-1",
                groupId = testGroupId,
                fromUserId = "user-1",
                toUserId = "user-2",
                amountCents = 500L,
                currency = "USD",
                equivalentBaseAmountCents = 500L,
                createdAt = 123L,
                syncStatus = SyncStatus.SYNCED
            )
        )

        every { cashTransferDao.observeByGroupId(testGroupId) } returns flowOf(localEntities)
        every { cloudDataSource.getGroupCashTransfersFlow(testGroupId) } returns flowOf(emptyList())
        coEvery { cashTransferDao.getPendingSyncCashTransferIds(testGroupId) } returns emptyList()
        coEvery { cashTransferDao.replaceCashTransfersForGroup(any(), any()) } just Runs

        val result = repository.observeGroupCashTransfers(testGroupId).first()

        assertEquals(1, result.size)
        assertEquals("transfer-1", result[0].id)

        advanceUntilIdle()
        coVerify { cloudDataSource.getGroupCashTransfersFlow(testGroupId) }
    }
}
