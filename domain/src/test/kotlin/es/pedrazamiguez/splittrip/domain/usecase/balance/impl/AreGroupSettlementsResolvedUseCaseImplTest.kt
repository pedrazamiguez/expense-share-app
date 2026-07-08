package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import io.mockk.coEvery
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AreGroupSettlementsResolvedUseCaseImplTest {

    private val settlementRepository = mockk<SettlementRepository>()
    private lateinit var useCase: AreGroupSettlementsResolvedUseCaseImpl

    private val groupId = "group-123"

    private fun makeRecord(id: String, status: SettlementStatus) = SettlementRecord(
        id = id,
        groupId = groupId,
        settlement = Settlement(
            fromUserId = "user-1",
            toUserId = "user-2",
            amount = 1000L,
            currency = "EUR",
            sourcePocket = SettlementPocketType.CASH
        ),
        status = status,
        createdAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        useCase = AreGroupSettlementsResolvedUseCaseImpl(settlementRepository)
    }

    @Test
    fun `returns empty when all settlements are RESOLVED`() = runTest {
        coEvery { settlementRepository.getGroupSettlements(groupId) } returns listOf(
            makeRecord("s1", SettlementStatus.RESOLVED),
            makeRecord("s2", SettlementStatus.RESOLVED)
        )

        val result = useCase(groupId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns all non-RESOLVED settlements`() = runTest {
        coEvery { settlementRepository.getGroupSettlements(groupId) } returns listOf(
            makeRecord("s1", SettlementStatus.RESOLVED),
            makeRecord("s2", SettlementStatus.SUGGESTED),
            makeRecord("s3", SettlementStatus.CONFIRMED_BY_PAYER),
            makeRecord("s4", SettlementStatus.DISPUTED)
        )

        val result = useCase(groupId)

        assertEquals(3, result.size)
        assertEquals(listOf("s2", "s3", "s4"), result.map { it.id })
    }

    @Test
    fun `empty group returns empty`() = runTest {
        coEvery { settlementRepository.getGroupSettlements(groupId) } returns emptyList()

        val result = useCase(groupId)

        assertTrue(result.isEmpty())
    }
}
