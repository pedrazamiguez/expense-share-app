package es.pedrazamiguez.splittrip.domain.enums

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("NotificationType.toCategory()")
class NotificationTypeCategoryTest {

    @Nested
    @DisplayName("expense types map to EXPENSES")
    inner class ExpenseTypes {

        @Test
        fun `EXPENSE_ADDED maps to EXPENSES`() {
            assertEquals(NotificationCategory.EXPENSES, NotificationType.EXPENSE_ADDED.toCategory())
        }

        @Test
        fun `EXPENSE_UPDATED maps to EXPENSES`() {
            assertEquals(NotificationCategory.EXPENSES, NotificationType.EXPENSE_UPDATED.toCategory())
        }

        @Test
        fun `EXPENSE_DELETED maps to EXPENSES`() {
            assertEquals(NotificationCategory.EXPENSES, NotificationType.EXPENSE_DELETED.toCategory())
        }
    }

    @Nested
    @DisplayName("membership types map to MEMBERSHIP")
    inner class MembershipTypes {

        @Test
        fun `MEMBER_ADDED maps to MEMBERSHIP`() {
            assertEquals(NotificationCategory.MEMBERSHIP, NotificationType.MEMBER_ADDED.toCategory())
        }

        @Test
        fun `MEMBER_REMOVED maps to MEMBERSHIP`() {
            assertEquals(NotificationCategory.MEMBERSHIP, NotificationType.MEMBER_REMOVED.toCategory())
        }

        @Test
        fun `GROUP_INVITE maps to MEMBERSHIP`() {
            assertEquals(NotificationCategory.MEMBERSHIP, NotificationType.GROUP_INVITE.toCategory())
        }

        @Test
        fun `GROUP_DELETED maps to MEMBERSHIP`() {
            assertEquals(NotificationCategory.MEMBERSHIP, NotificationType.GROUP_DELETED.toCategory())
        }
    }

    @Nested
    @DisplayName("financial types map to FINANCIAL")
    inner class FinancialTypes {

        @Test
        fun `CASH_WITHDRAWAL maps to FINANCIAL`() {
            assertEquals(NotificationCategory.FINANCIAL, NotificationType.CASH_WITHDRAWAL.toCategory())
        }

        @Test
        fun `CONTRIBUTION_ADDED maps to FINANCIAL`() {
            assertEquals(NotificationCategory.FINANCIAL, NotificationType.CONTRIBUTION_ADDED.toCategory())
        }

        @Test
        fun `SETTLEMENT_REQUEST maps to FINANCIAL`() {
            assertEquals(NotificationCategory.FINANCIAL, NotificationType.SETTLEMENT_REQUEST.toCategory())
        }
    }

    @Nested
    @DisplayName("DEFAULT type")
    inner class DefaultType {

        @Test
        fun `DEFAULT maps to null`() {
            assertNull(NotificationType.DEFAULT.toCategory())
        }
    }
}
