package es.pedrazamiguez.expenseshareapp.features.main.navigation

import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("DeepLinkUtils")
class DeepLinkUtilsTest {

    @Nested
    @DisplayName("resolveTargetTab")
    inner class ResolveTargetTab {

        @Test
        fun `returns EXPENSES when expenseId is present`() {
            val result = DeepLinkUtils.resolveTargetTab(
                expenseId = "expense-123",
                contributionId = null,
                withdrawalId = null
            )
            assertEquals(Routes.EXPENSES, result)
        }

        @Test
        fun `returns BALANCES when contributionId is present`() {
            val result = DeepLinkUtils.resolveTargetTab(
                expenseId = null,
                contributionId = "contribution-456",
                withdrawalId = null
            )
            assertEquals(Routes.BALANCES, result)
        }

        @Test
        fun `returns BALANCES when withdrawalId is present`() {
            val result = DeepLinkUtils.resolveTargetTab(
                expenseId = null,
                contributionId = null,
                withdrawalId = "withdrawal-789"
            )
            assertEquals(Routes.BALANCES, result)
        }

        @Test
        fun `returns GROUPS when no specific ID is present`() {
            val result = DeepLinkUtils.resolveTargetTab(
                expenseId = null,
                contributionId = null,
                withdrawalId = null
            )
            assertEquals(Routes.GROUPS, result)
        }

        @Test
        fun `prioritizes EXPENSES when both expenseId and contributionId are present`() {
            val result = DeepLinkUtils.resolveTargetTab(
                expenseId = "expense-123",
                contributionId = "contribution-456",
                withdrawalId = null
            )
            assertEquals(Routes.EXPENSES, result)
        }

        @Test
        fun `prioritizes EXPENSES when both expenseId and withdrawalId are present`() {
            val result = DeepLinkUtils.resolveTargetTab(
                expenseId = "expense-123",
                contributionId = null,
                withdrawalId = "withdrawal-789"
            )
            assertEquals(Routes.EXPENSES, result)
        }

        @Test
        fun `returns BALANCES when both contributionId and withdrawalId are present`() {
            val result = DeepLinkUtils.resolveTargetTab(
                expenseId = null,
                contributionId = "contribution-456",
                withdrawalId = "withdrawal-789"
            )
            assertEquals(Routes.BALANCES, result)
        }
    }

    @Nested
    @DisplayName("constants")
    inner class Constants {

        @Test
        fun `DEEP_LINK_SCHEME is expenseshareapp`() {
            assertEquals("expenseshareapp", DeepLinkUtils.DEEP_LINK_SCHEME)
        }

        @Test
        fun `PATTERN_GROUP contains groupId placeholder`() {
            assertEquals(
                "expenseshareapp://groups/{groupId}",
                DeepLinkUtils.PATTERN_GROUP
            )
        }

        @Test
        fun `PATTERN_EXPENSES contains groupId placeholder`() {
            assertEquals(
                "expenseshareapp://groups/{groupId}/expenses",
                DeepLinkUtils.PATTERN_EXPENSES
            )
        }

        @Test
        fun `PATTERN_EXPENSE_DETAIL contains groupId and expenseId placeholders`() {
            assertEquals(
                "expenseshareapp://groups/{groupId}/expenses/{expenseId}",
                DeepLinkUtils.PATTERN_EXPENSE_DETAIL
            )
        }

        @Test
        fun `PATTERN_CONTRIBUTION contains groupId and contributionId placeholders`() {
            assertEquals(
                "expenseshareapp://groups/{groupId}/contributions/{contributionId}",
                DeepLinkUtils.PATTERN_CONTRIBUTION
            )
        }

        @Test
        fun `PATTERN_CASH_WITHDRAWAL contains groupId and withdrawalId placeholders`() {
            assertEquals(
                "expenseshareapp://groups/{groupId}/cash_withdrawals/{withdrawalId}",
                DeepLinkUtils.PATTERN_CASH_WITHDRAWAL
            )
        }
    }
}

