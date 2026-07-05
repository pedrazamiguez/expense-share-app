package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetGroupSettlementsFlowUseCaseImplTest {

    private val settlementRepository = mockk<SettlementRepository>()
    private lateinit var useCase: GetGroupSettlementsFlowUseCaseImpl

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
        useCase = GetGroupSettlementsFlowUseCaseImpl(settlementRepository)
    }

    @Test
    fun `delegates to repository and returns flow of settlements`() = runTest {
        val records = listOf(
            makeRecord("s1", SettlementStatus.SUGGESTED),
            makeRecord("s2", SettlementStatus.RESOLVED)
        )
        every { settlementRepository.getGroupSettlementsFlow(groupId) } returns flowOf(records)

        val result = useCase(groupId).first()

        assertEquals(records, result)
    }

    @Test
    fun `returns empty flow when group has no settlements`() = runTest {
        every { settlementRepository.getGroupSettlementsFlow(groupId) } returns flowOf(emptyList())

        val result = useCase(groupId).first()

        assertEquals(0, result.size)
    }
}
