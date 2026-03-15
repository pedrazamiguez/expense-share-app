package es.pedrazamiguez.expenseshareapp.features.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.features.main.presentation.screen.MainScreen

fun NavGraphBuilder.mainGraph(
    navigationProviders: List<NavigationProvider>,
    screenUiProviders: List<ScreenUiProvider>
) {
    composable(
        route = Routes.MAIN,
        deepLinks = listOf(
            navDeepLink { uriPattern = DeepLinkUtils.PATTERN_GROUP },
            navDeepLink { uriPattern = DeepLinkUtils.PATTERN_EXPENSES },
            navDeepLink { uriPattern = DeepLinkUtils.PATTERN_EXPENSE_DETAIL },
            navDeepLink { uriPattern = DeepLinkUtils.PATTERN_CONTRIBUTION },
            navDeepLink { uriPattern = DeepLinkUtils.PATTERN_CASH_WITHDRAWAL }
        ),
        arguments = listOf(
            navArgument(DeepLinkUtils.ARG_GROUP_ID) {
                type = NavType.StringType; defaultValue = ""
            },
            navArgument(DeepLinkUtils.ARG_EXPENSE_ID) {
                type = NavType.StringType; defaultValue = ""
            },
            navArgument(DeepLinkUtils.ARG_CONTRIBUTION_ID) {
                type = NavType.StringType; defaultValue = ""
            },
            navArgument(DeepLinkUtils.ARG_WITHDRAWAL_ID) {
                type = NavType.StringType; defaultValue = ""
            }
        )
    ) { backStackEntry ->
        val deepLinkGroupId = backStackEntry.arguments
            ?.getString(DeepLinkUtils.ARG_GROUP_ID)?.ifBlank { null }
        val deepLinkExpenseId = backStackEntry.arguments
            ?.getString(DeepLinkUtils.ARG_EXPENSE_ID)?.ifBlank { null }
        val deepLinkContributionId = backStackEntry.arguments
            ?.getString(DeepLinkUtils.ARG_CONTRIBUTION_ID)?.ifBlank { null }
        val deepLinkWithdrawalId = backStackEntry.arguments
            ?.getString(DeepLinkUtils.ARG_WITHDRAWAL_ID)?.ifBlank { null }

        // Resolve target tab only when a deep link group is present
        val deepLinkTargetTab = if (deepLinkGroupId != null) {
            DeepLinkUtils.resolveTargetTab(
                expenseId = deepLinkExpenseId,
                contributionId = deepLinkContributionId,
                withdrawalId = deepLinkWithdrawalId
            )
        } else {
            null
        }

        MainScreen(
            navigationProviders = navigationProviders,
            screenUiProviders = screenUiProviders,
            deepLinkGroupId = deepLinkGroupId,
            deepLinkTargetTab = deepLinkTargetTab
        )
    }
}
