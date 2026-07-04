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

class AreMemberSettlementsResolvedUseCaseImplTest {

    private val settlementRepository = mockk<SettlementRepository>()
    private lateinit var useCase: AreMemberSettlementsResolvedUseCaseImpl

    private val groupId = "group-123"
    private val userId = "user-1"
    private val anotherUserId = "user-2"

    private fun makeRecord(id: String, status: SettlementStatus) = SettlementRecord(
        id = id,
        groupId = groupId,
        settlement = Settlement(
            fromUserId = userId,
            toUserId = anotherUserId,
            amount = 1000L,
            currency = "EUR",
            sourcePocket = SettlementPocketType.CASH
        ),
        status = status,
        createdAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        useCase = AreMemberSettlementsResolvedUseCaseImpl(settlementRepository)
    }

    @Test
    fun `returns empty when all settlements are RESOLVED`() = runTest {
        val records = listOf(
            makeRecord("s1", SettlementStatus.RESOLVED),
            makeRecord("s2", SettlementStatus.RESOLVED)
        )
        coEvery { settlementRepository.getMemberSettlements(groupId, userId) } returns records

        val result = useCase(groupId, userId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns SUGGESTED records as unresolved`() = runTest {
        val resolved = makeRecord("s1", SettlementStatus.RESOLVED)
        val suggested = makeRecord("s2", SettlementStatus.SUGGESTED)
        coEvery { settlementRepository.getMemberSettlements(groupId, userId) } returns
            listOf(resolved, suggested)

        val result = useCase(groupId, userId)

        assertEquals(1, result.size)
        assertEquals("s2", result.first().id)
    }

    @Test
    fun `returns CONFIRMED_BY_PAYER records as unresolved`() = runTest {
        val record = makeRecord("s1", SettlementStatus.CONFIRMED_BY_PAYER)
        coEvery { settlementRepository.getMemberSettlements(groupId, userId) } returns listOf(record)

        val result = useCase(groupId, userId)

        assertEquals(1, result.size)
    }

    @Test
    fun `only returns records involving the specified userId`() = runTest {
        coEvery { settlementRepository.getMemberSettlements(groupId, "other-user") } returns emptyList()

        val result = useCase(groupId, "other-user")

        assertTrue(result.isEmpty())
    }
}
