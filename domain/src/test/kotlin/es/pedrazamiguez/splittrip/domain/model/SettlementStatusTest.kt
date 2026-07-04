package es.pedrazamiguez.splittrip.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SettlementStatusTest {

    @Test
    fun `SUGGESTED parses from string`() {
        assertEquals(SettlementStatus.SUGGESTED, SettlementStatus.fromString("SUGGESTED"))
    }

    @Test
    fun `CONFIRMED_BY_PAYER parses from string`() {
        assertEquals(SettlementStatus.CONFIRMED_BY_PAYER, SettlementStatus.fromString("CONFIRMED_BY_PAYER"))
    }

    @Test
    fun `DISPUTED parses from string`() {
        assertEquals(SettlementStatus.DISPUTED, SettlementStatus.fromString("DISPUTED"))
    }

    @Test
    fun `RESOLVED parses from string`() {
        assertEquals(SettlementStatus.RESOLVED, SettlementStatus.fromString("RESOLVED"))
    }

    @Test
    fun `unknown string defaults to SUGGESTED`() {
        assertEquals(SettlementStatus.SUGGESTED, SettlementStatus.fromString("UNKNOWN"))
    }

    @Test
    fun `null string defaults to SUGGESTED`() {
        assertEquals(SettlementStatus.SUGGESTED, SettlementStatus.fromString(null))
    }

    @Test
    fun `lowercase string matches case-insensitively`() {
        assertEquals(SettlementStatus.DISPUTED, SettlementStatus.fromString("disputed"))
    }
}
