package es.pedrazamiguez.splittrip.domain.usecase.balance.impl.strategy

import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CashSettlementPaymentStrategyTest {

    private val strategy = CashSettlementPaymentStrategy()

    @Test
    fun `appliesTo returns true for CASH pocket`() {
        assertTrue(strategy.appliesTo(SettlementPocketType.CASH))
    }

    @Test
    fun `appliesTo returns false for POCKET pocket`() {
        assertFalse(strategy.appliesTo(SettlementPocketType.POCKET))
    }

    @Test
    fun `processPayment performs no operations`() = runTest {
        // Just verify it doesn't throw or do anything unexpected
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
            status = SettlementStatus.SUGGESTED,
            createdAt = LocalDateTime.now()
        )

        val group = Group(
            id = "group-id",
            name = "Test Group",
            description = "",
            currency = "EUR",
            members = emptyList(),
            createdAt = LocalDateTime.now(),
            lastUpdatedAt = LocalDateTime.now()
        )

        // Does nothing and should complete successfully
        strategy.processPayment(
            record = record,
            updated = record.copy(status = SettlementStatus.RESOLVED),
            group = group,
            groupId = group.id,
            currentUserId = "user1"
        )
    }
}
