package es.pedrazamiguez.splittrip.features.contribution.navigation

import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.splittrip.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.features.contribution.presentation.feature.AddContributionFeature

fun NavGraphBuilder.contributionsGraph() {
    sharedComposable(route = Routes.ADD_CONTRIBUTION) {
        val navController = LocalTabNavController.current
        AddContributionFeature(
            onContributionSuccess = {
                navController.popBackStack()
            }
        )
    }
}
