package es.pedrazamiguez.splittrip.features.main.navigation

import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes

/**
 * Pure utility functions for deep link argument resolution.
 *
 * Determines which tab to auto-switch to based on the deep link arguments
 * extracted from the URI by Navigation Compose.
 */
object DeepLinkUtils {

    /**
     * Deep link URI scheme used by the app.
     */
    const val DEEP_LINK_SCHEME = "splittrip"

    /**
     * URI patterns for deep link matching in Navigation Compose.
     */
    const val PATTERN_GROUP = "$DEEP_LINK_SCHEME://groups/{groupId}"
    const val PATTERN_EXPENSES = "$DEEP_LINK_SCHEME://groups/{groupId}/expenses"
    const val PATTERN_EXPENSE_DETAIL = "$DEEP_LINK_SCHEME://groups/{groupId}/expenses/{expenseId}"
    const val PATTERN_CONTRIBUTION = "$DEEP_LINK_SCHEME://groups/{groupId}/contributions/{contributionId}"
    const val PATTERN_CASH_WITHDRAWAL = "$DEEP_LINK_SCHEME://groups/{groupId}/cash_withdrawals/{withdrawalId}"

    /**
     * Navigation argument keys extracted from deep link URIs.
     */
    const val ARG_GROUP_ID = "groupId"
    const val ARG_EXPENSE_ID = "expenseId"
    const val ARG_CONTRIBUTION_ID = "contributionId"
    const val ARG_WITHDRAWAL_ID = "withdrawalId"

    /**
     * Resolves which tab to auto-switch to based on which deep link arguments are present.
     *
     * | Deep link pattern                                      | Target tab       |
     * |--------------------------------------------------------|------------------|
     * | `groups/{groupId}`                                     | `Routes.GROUPS`  |
     * | `groups/{groupId}/expenses`                            | `Routes.EXPENSES`|
     * | `groups/{groupId}/expenses/{expenseId}`                | `Routes.EXPENSES`|
     * | `groups/{groupId}/contributions/{contributionId}`      | `Routes.BALANCES`|
     * | `groups/{groupId}/cash_withdrawals/{withdrawalId}`     | `Routes.BALANCES`|
     *
     * @param expenseId The expense ID from the deep link, or `null`.
     * @param isExpensesListPath `true` when the deep link matched the `/expenses` path
     *   without an expense ID (i.e., the expenses-list deep link). This cannot be
     *   distinguished from the group-only deep link via arguments alone because both
     *   produce the same argument state; the caller detects it from the URI path.
     * @param contributionId The contribution ID from the deep link, or `null`.
     * @param withdrawalId The cash withdrawal ID from the deep link, or `null`.
     * @return The route of the tab to switch to, defaulting to [Routes.GROUPS].
     */
    fun resolveTargetTab(
        expenseId: String?,
        isExpensesListPath: Boolean,
        contributionId: String?,
        withdrawalId: String?
    ): String = when {
        expenseId != null -> Routes.EXPENSES
        isExpensesListPath -> Routes.EXPENSES
        contributionId != null -> Routes.BALANCES
        withdrawalId != null -> Routes.BALANCES
        else -> Routes.GROUPS
    }
}
