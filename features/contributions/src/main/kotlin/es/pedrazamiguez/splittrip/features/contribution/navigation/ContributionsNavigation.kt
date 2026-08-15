package es.pedrazamiguez.splittrip.features.contribution.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import es.pedrazamiguez.splittrip.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes.CONTRIBUTION_WIZARD_ARG_CONTRIBUTION_ID
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes.CONTRIBUTION_WIZARD_ARG_GROUP_ID
import es.pedrazamiguez.splittrip.features.contribution.presentation.feature.AddContributionFeature

fun NavGraphBuilder.contributionsGraph() {
    sharedComposable(
        route = Routes.CONTRIBUTION_WIZARD,
        arguments = listOf(
            navArgument(CONTRIBUTION_WIZARD_ARG_GROUP_ID) { type = NavType.StringType },
            navArgument(CONTRIBUTION_WIZARD_ARG_CONTRIBUTION_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue =
                    null
            }
        )
    ) { backStackEntry ->
        val navController = LocalTabNavController.current
        val groupId = backStackEntry.arguments?.getString(CONTRIBUTION_WIZARD_ARG_GROUP_ID)
        val contributionId = backStackEntry.arguments?.getString(CONTRIBUTION_WIZARD_ARG_CONTRIBUTION_ID)

        AddContributionFeature(
            groupId = groupId ?: "",
            contributionId = contributionId,
            onContributionSuccess = {
                navController.popBackStack()
            }
        )
    }
}
