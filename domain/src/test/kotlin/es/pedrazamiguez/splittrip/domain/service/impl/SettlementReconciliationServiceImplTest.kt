package es.pedrazamiguez.splittrip.domain.service.impl

import es.pedrazamiguez.splittrip.domain.model.CurrencyAmount
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("SettlementReconciliationServiceImpl")
class SettlementReconciliationServiceImplTest {

    private lateinit var service: SettlementReconciliationServiceImpl

    @BeforeEach
    fun setUp() {
        service = SettlementReconciliationServiceImpl()
    }

    @Nested
    @DisplayName("applyResolvedSettlements")
    inner class ApplyResolvedSettlements {

        @Test
        fun `returns original balances when no resolved settlements exist`() {
            val balances = listOf(
                MemberBalance(userId = "user-1", pocketBalance = 1000L),
                MemberBalance(userId = "user-2", pocketBalance = -1000L)
            )
            val settlements = listOf(
                SettlementRecord(
                    id = "settlement-1",
                    groupId = "group-1",
                    settlement = Settlement("user-2", "user-1", 1000L, "EUR", SettlementPocketType.POCKET),
                    status = SettlementStatus.SUGGESTED,
                    createdAt = LocalDateTime.now()
                )
            )

            val result = service.applyResolvedSettlements(balances, settlements, "EUR")

            assertEquals(balances, result)
        }

        @Test
        fun `applies pocket settlement correctly`() {
            val balances = listOf(
                MemberBalance(
                    userId = "user-1",
                    pocketBalance = 1000L,
                    withdrawn = 0L,
                    contributed = 1000L
                ),
                MemberBalance(
                    userId = "user-2",
                    pocketBalance = -1000L,
                    withdrawn = 1000L,
                    contributed = 0L
                )
            )
            val settlements = listOf(
                SettlementRecord(
                    id = "settlement-1",
                    groupId = "group-1",
                    settlement = Settlement("user-2", "user-1", 1000L, "EUR", SettlementPocketType.POCKET),
                    status = SettlementStatus.RESOLVED,
                    createdAt = LocalDateTime.now()
                )
            )

            val result = service.applyResolvedSettlements(balances, settlements, "EUR")
            val balanceMap = result.associateBy { it.userId }

            val user1 = balanceMap["user-1"]!!
            val user2 = balanceMap["user-2"]!!

            // user-2 pays user-1.
            // user-2 pocketBalance becomes 0. user-2 contributed increases.
            assertEquals(0L, user2.pocketBalance)
            assertEquals(1000L, user2.contributed)

            // user-1 pocketBalance becomes 0. user-1 withdrawn increases.
            assertEquals(0L, user1.pocketBalance)
            assertEquals(1000L, user1.withdrawn)
        }

        @Test
        fun `applies cash settlement correctly without altering cashInHand`() {
            val balances = listOf(
                MemberBalance(
                    userId = "user-1", // Withdrew 10000 cash, 4000 left
                    withdrawn = 10000L,
                    contributed = 6000L,
                    cashInHand = 4000L,
                    cashInHandByCurrency = listOf(CurrencyAmount("EUR", 4000L, 4000L)),
                    withdrawnByCurrency = listOf(CurrencyAmount("EUR", 10000L, 10000L))
                ),
                MemberBalance(
                    userId = "user-2", // Spent 6000 cash (owes user-1 6000)
                    withdrawn = 0L,
                    contributed = 0L,
                    cashInHand = 0L,
                    cashInHandByCurrency = emptyList(),
                    withdrawnByCurrency = emptyList()
                )
            )
            val settlements = listOf(
                SettlementRecord(
                    id = "settlement-2",
                    groupId = "group-1",
                    settlement = Settlement("user-2", "user-1", 6000L, "EUR", SettlementPocketType.CASH),
                    status = SettlementStatus.RESOLVED,
                    createdAt = LocalDateTime.now()
                )
            )

            val result = service.applyResolvedSettlements(balances, settlements, "EUR")
            val balanceMap = result.associateBy { it.userId }

            val user1 = balanceMap["user-1"]!!
            val user2 = balanceMap["user-2"]!!

            // user-2 pays 6000 cash to user-1.
            assertEquals(0L, user2.contributed) // Service no longer touches contributed
            assertEquals(-6000L, user2.withdrawn) // Decreases by 6000
            assertEquals(6000L, user2.pocketBalance) // Increases by 6000
            assertEquals(-6000L, user2.cashInHand) // Decreases by 6000
            val user2Eur = user2.withdrawnByCurrency.find { it.currency == "EUR" }
            assertEquals(-6000L, user2Eur?.amountCents) // Decreases by 6000

            // user-1 receives 6000 cash from user-2.
            assertEquals(6000L, user1.contributed) // Service no longer touches contributed
            assertEquals(16000L, user1.withdrawn) // Service INCREASES withdrawn to reflect cash received
            assertEquals(-6000L, user1.pocketBalance) // Service DECREASES pocketBalance because debt is paid off
            assertEquals(10000L, user1.cashInHand) // Increased (4000 + 6000)
            val user1Eur = user1.withdrawnByCurrency.find { it.currency == "EUR" }
            assertEquals(16000L, user1Eur?.amountCents)
        }
    }
}
