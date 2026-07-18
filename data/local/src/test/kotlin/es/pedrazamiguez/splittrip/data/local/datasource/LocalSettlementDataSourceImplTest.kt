package es.pedrazamiguez.splittrip.data.local.datasource

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import es.pedrazamiguez.splittrip.data.local.dao.SettlementRecordDao
import es.pedrazamiguez.splittrip.data.local.database.AppDatabase
import es.pedrazamiguez.splittrip.data.local.datasource.impl.LocalSettlementDataSourceImpl
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class LocalSettlementDataSourceImplTest {

    private lateinit var db: AppDatabase
    private lateinit var settlementRecordDao: SettlementRecordDao
    private lateinit var localDataSource: LocalSettlementDataSourceImpl

    private val testGroupId = "group-123"

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries()
            .build()
        settlementRecordDao = db.settlementRecordDao()
        localDataSource = LocalSettlementDataSourceImpl(settlementRecordDao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun replaceSettlementsForGroup_preservesPendingSyncItems_andDeletesStaleSyncedItems() = runTest {
        // Given
        val pendingSettlement = SettlementRecord(
            id = "pending-1",
            groupId = testGroupId,
            settlement = Settlement(
                fromUserId = "user-1",
                toUserId = "user-2",
                amount = 1000L,
                currency = "EUR",
                sourcePocket = SettlementPocketType.CASH
            ),
            status = SettlementStatus.SUGGESTED,
            createdAt = LocalDateTime.now()
        )
        // Save as PENDING_SYNC
        localDataSource.saveSettlement(pendingSettlement, SyncStatus.PENDING_SYNC)

        val syncedStaleSettlement = SettlementRecord(
            id = "synced-stale",
            groupId = testGroupId,
            settlement = Settlement(
                fromUserId = "user-1",
                toUserId = "user-3",
                amount = 2000L,
                currency = "EUR",
                sourcePocket = SettlementPocketType.CASH
            ),
            status = SettlementStatus.SUGGESTED,
            createdAt = LocalDateTime.now()
        )
        // Save as SYNCED
        localDataSource.saveSettlement(syncedStaleSettlement, SyncStatus.SYNCED)

        val newRemoteSettlement = SettlementRecord(
            id = "synced-remote-new",
            groupId = testGroupId,
            settlement = Settlement(
                fromUserId = "user-2",
                toUserId = "user-3",
                amount = 1500L,
                currency = "EUR",
                sourcePocket = SettlementPocketType.CASH
            ),
            status = SettlementStatus.SUGGESTED,
            createdAt = LocalDateTime.now()
        )

        // When
        localDataSource.replaceSettlementsForGroup(
            groupId = testGroupId,
            records = listOf(newRemoteSettlement)
        )

        // Then
        // 1. Unsynced item (pending-1) should be preserved
        val pendingResult = localDataSource.getSettlementById("pending-1")
        assertNotNull(pendingResult)
        assertEquals(SyncStatus.PENDING_SYNC, localDataSource.getSyncStatus("pending-1"))

        // 2. Synced stale item (synced-stale) should be deleted because it is not in the remote list
        val staleResult = localDataSource.getSettlementById("synced-stale")
        assertNull(staleResult)

        // 3. New remote item (synced-remote-new) should be added (defaults to SYNCED when upserted during reconciliation)
        val newResult = localDataSource.getSettlementById("synced-remote-new")
        assertNotNull(newResult)
        assertEquals(1500L, newResult?.settlement?.amount)
    }
}
