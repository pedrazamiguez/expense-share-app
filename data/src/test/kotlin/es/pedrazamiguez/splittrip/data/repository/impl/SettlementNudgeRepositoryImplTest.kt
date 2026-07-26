package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.data.local.datastore.SettlementNudgePreferences
import es.pedrazamiguez.splittrip.domain.datasource.cloud.CloudSettlementDataSource
import es.pedrazamiguez.splittrip.domain.datasource.local.LocalSettlementDataSource
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SettlementNudgeRepositoryImplTest {

    private val settlementNudgePreferences: SettlementNudgePreferences = mockk()
    private val cloudSettlementDataSource: CloudSettlementDataSource = mockk()
    private val localSettlementDataSource: LocalSettlementDataSource = mockk()
    private val authenticationService: AuthenticationService = mockk()

    private lateinit var repository: SettlementNudgeRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = SettlementNudgeRepositoryImpl(
            settlementNudgePreferences = settlementNudgePreferences,
            cloudSettlementDataSource = cloudSettlementDataSource,
            localSettlementDataSource = localSettlementDataSource,
            authenticationService = authenticationService
        )
    }

    @Test
    fun `getNudgeTimestampsFlow delegates to preferences`() = runTest {
        val expected = mapOf("settlement-1" to 1000L)
        every { settlementNudgePreferences.nudgeTimestampsFlow } returns flowOf(expected)

        val result = repository.getNudgeTimestampsFlow().first()

        assertEquals(expected, result)
    }

    @Test
    fun `getLastNudgeTimestamp delegates to preferences`() = runTest {
        coEvery { settlementNudgePreferences.getLastNudgeTimestamp("s1") } returns 5000L

        val result = repository.getLastNudgeTimestamp("s1")

        assertEquals(5000L, result)
    }

    @Test
    fun `recordNudgeTimestamp delegates to preferences`() = runTest {
        coEvery { settlementNudgePreferences.recordNudgeTimestamp("s1", 5000L) } returns Unit

        repository.recordNudgeTimestamp("s1", 5000L)

        coVerify(exactly = 1) { settlementNudgePreferences.recordNudgeTimestamp("s1", 5000L) }
    }

    @Test
    fun `sendDebtorNudge when user not authenticated returns failure`() = runTest {
        every { authenticationService.currentUserId() } returns null

        val result = repository.sendDebtorNudge("group-1", "s1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `sendDebtorNudge when settlement not found returns failure`() = runTest {
        every { authenticationService.currentUserId() } returns "creditor-id"
        coEvery { localSettlementDataSource.getSettlementById("s1") } returns null

        val result = repository.sendDebtorNudge("group-1", "s1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `sendDebtorNudge when valid dispatches nudge to cloud data source`() = runTest {
        every { authenticationService.currentUserId() } returns "creditor-id"
        val record = SettlementRecord(
            id = "s1",
            groupId = "group-1",
            settlement = Settlement(
                fromUserId = "debtor-id",
                toUserId = "creditor-id",
                amount = 3700L,
                currency = "EUR",
                sourcePocket = SettlementPocketType.POCKET
            ),
            status = SettlementStatus.SUGGESTED,
            createdAt = LocalDateTime.now()
        )
        coEvery { localSettlementDataSource.getSettlementById("s1") } returns record
        coEvery {
            cloudSettlementDataSource.sendDebtorNudge(
                groupId = "group-1",
                settlementId = "s1",
                fromUserId = "creditor-id",
                toUserId = "debtor-id",
                amountCents = 3700L,
                currency = "EUR"
            )
        } returns Unit

        val result = repository.sendDebtorNudge("group-1", "s1")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            cloudSettlementDataSource.sendDebtorNudge(
                groupId = "group-1",
                settlementId = "s1",
                fromUserId = "creditor-id",
                toUserId = "debtor-id",
                amountCents = 3700L,
                currency = "EUR"
            )
        }
    }
}
