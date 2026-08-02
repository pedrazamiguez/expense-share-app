package es.pedrazamiguez.splittrip.domain.service.impl

import es.pedrazamiguez.splittrip.domain.service.RemainderDistributionService
import es.pedrazamiguez.splittrip.domain.service.cashdebt.CashDebtNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CashDebtScalingServiceImplTest {

    private lateinit var remainderDistributionService: RemainderDistributionService
    private lateinit var service: CashDebtScalingServiceImpl

    @BeforeEach
    fun setup() {
        remainderDistributionService = RemainderDistributionServiceImpl()
        service = CashDebtScalingServiceImpl(remainderDistributionService)
    }

    @Test
    fun `group cash pool overspending is distributed proportionally among members with remaining cash shares`() {
        val nodes = listOf(
            CashDebtNode("Antonio", -133333L, 0L),
            CashDebtNode("Andres", 166667L, 166667L),
            CashDebtNode("Pepe", 166666L, 166666L)
        )

        val result = service.scaleBalances(nodes)

        assertEquals(-133333L, result.find { it.userId == "Antonio" }?.balance)
        assertEquals(66667L, result.find { it.userId == "Andres" }?.balance)
        assertEquals(66666L, result.find { it.userId == "Pepe" }?.balance)
    }

    @Test
    fun `creditor spending within allowance does not shift debt assigned to them`() {
        val nodes = listOf(
            CashDebtNode("Antonio", -133333L, 0L),
            CashDebtNode("Andres", 146667L, 166667L),
            CashDebtNode("Pepe", 166666L, 166666L)
        )

        val result = service.scaleBalances(nodes)

        assertEquals(66667L, result.find { it.userId == "Andres" }?.balance)
        assertEquals(66666L, result.find { it.userId == "Pepe" }?.balance)
    }

    @Test
    fun `creditor spending beyond allowance spills over debt to other creditors`() {
        val nodes = listOf(
            CashDebtNode("Antonio", -133333L, 0L),
            CashDebtNode("Andres", 46667L, 166667L),
            CashDebtNode("Pepe", 166666L, 166666L)
        )

        val result = service.scaleBalances(nodes)

        assertEquals(46667L, result.find { it.userId == "Andres" }?.balance)
        assertEquals(86666L, result.find { it.userId == "Pepe" }?.balance)
    }

    @Test
    fun `active weights sum is zero uses equal distribution fallback`() {
        val nodes = listOf(
            CashDebtNode("Debtor", -100L, 0L),
            CashDebtNode("Creditor1", 50L, 0L),
            CashDebtNode("Creditor2", 100L, 0L)
        )

        val result = service.scaleBalances(nodes)

        assertEquals(50L, result.find { it.userId == "Creditor1" }?.balance)
        assertEquals(50L, result.find { it.userId == "Creditor2" }?.balance)
    }
}
