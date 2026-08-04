package es.pedrazamiguez.splittrip.domain.usecase.balance.impl.strategy

import es.pedrazamiguez.splittrip.domain.model.CashTransfer
import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.CashTransferRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CashSettlementPaymentStrategyTest {

    private val cashTransferRepository = mockk<CashTransferRepository>()
    private lateinit var strategy: CashSettlementPaymentStrategy

    @BeforeEach
    fun setUp() {
        coEvery { cashTransferRepository.addTransfer(any()) } returns Result.success(Unit)
        strategy = CashSettlementPaymentStrategy(cashTransferRepository)
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
    fun `processPayment persists a CashTransfer for the resolved settlement`() = runTest {
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

        strategy.processPayment(
            record = record,
            updated = record.copy(status = SettlementStatus.RESOLVED),
            group = group,
            groupId = group.id,
            currentUserId = "user1"
        )

        coVerify(exactly = 1) {
            cashTransferRepository.addTransfer(
                match { transfer: CashTransfer ->
                    transfer.groupId == "group-id" &&
                        transfer.fromUserId == "user1" &&
                        transfer.toUserId == "user2" &&
                        transfer.amountCents == 1000L &&
                        transfer.currency == "EUR" &&
                        transfer.id.isNotBlank() &&
                        transfer.createdAt > 0L
                }
            )
        }
    }
}
