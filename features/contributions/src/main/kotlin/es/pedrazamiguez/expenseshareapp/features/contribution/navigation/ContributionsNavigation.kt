package es.pedrazamiguez.expenseshareapp.features.contribution.navigation

import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.features.contribution.presentation.feature.AddContributionFeature

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
