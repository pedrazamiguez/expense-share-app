package es.pedrazamiguez.splittrip.domain.usecase.balance.impl.strategy

import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CashSettlementPaymentStrategyTest {

    private lateinit var strategy: CashSettlementPaymentStrategy

    @BeforeEach
    fun setUp() {
        strategy = CashSettlementPaymentStrategy()
    }

    @Test
    fun `appliesTo returns true for CASH pocket`() {
        assertTrue(strategy.appliesTo(SettlementPocketType.CASH))
    }

    @Test
    fun `appliesTo returns false for POCKET pocket`() {
        assertFalse(strategy.appliesTo(SettlementPocketType.POCKET))
    }

    @Test
    fun `processPayment throws UnsupportedOperationException`() {
        val record = SettlementRecord(
            id = "test-id",
            groupId = "group-id",
            settlement = Settlement(
                fromUserId = "user1",
                toUserId = "user2",
                amount = 1000L,
                currency = "EUR",
                sourcePocket = SettlementPocketType.CASH
            ),
            status = SettlementStatus.CONFIRMED_BY_PAYER,
            createdAt = LocalDateTime.now()
        )

        val group = Group(
            id = "group-id",
            name = "Test Group",
            description = "",
            currency = "EUR",
            members = listOf("user1", "user2"),
            createdAt = LocalDateTime.now(),
            lastUpdatedAt = LocalDateTime.now()
        )

        assertThrows(UnsupportedOperationException::class.java) {
            kotlinx.coroutines.runBlocking {
                strategy.processPayment(
                    record = record,
                    updated = record.copy(status = SettlementStatus.RESOLVED),
                    group = group,
                    groupId = group.id,
                    currentUserId = "user1"
                )
            }
        }
    }
}
