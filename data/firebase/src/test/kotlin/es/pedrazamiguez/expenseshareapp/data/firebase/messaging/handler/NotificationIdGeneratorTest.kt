package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("stableNotificationId")
class NotificationIdGeneratorTest {

    @Test
    fun `same inputs produce same ID`() {
        val id1 = stableNotificationId("EXPENSE_ADDED", "group1", "expense1")
        val id2 = stableNotificationId("EXPENSE_ADDED", "group1", "expense1")
        assertEquals(id1, id2)
    }

    @Test
    fun `different types produce different IDs`() {
        val id1 = stableNotificationId("EXPENSE_ADDED", "group1", "expense1")
        val id2 = stableNotificationId("EXPENSE_DELETED", "group1", "expense1")
        assertNotEquals(id1, id2)
    }

    @Test
    fun `different groups produce different IDs`() {
        val id1 = stableNotificationId("EXPENSE_ADDED", "group1", "expense1")
        val id2 = stableNotificationId("EXPENSE_ADDED", "group2", "expense1")
        assertNotEquals(id1, id2)
    }

    @Test
    fun `different entities produce different IDs`() {
        val id1 = stableNotificationId("EXPENSE_ADDED", "group1", "expense1")
        val id2 = stableNotificationId("EXPENSE_ADDED", "group1", "expense2")
        assertNotEquals(id1, id2)
    }

    @Test
    fun `null inputs produce consistent IDs`() {
        val id1 = stableNotificationId(null, null, null)
        val id2 = stableNotificationId(null, null, null)
        assertEquals(id1, id2)
    }

    @Test
    fun `partial null inputs produce consistent IDs`() {
        val id1 = stableNotificationId("DEFAULT", null, null)
        val id2 = stableNotificationId("DEFAULT", null, null)
        assertEquals(id1, id2)
    }
}

