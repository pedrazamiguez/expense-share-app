package es.pedrazamiguez.splittrip.features.withdrawal.navigation

import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.splittrip.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.features.withdrawal.presentation.feature.AddCashWithdrawalFeature

fun NavGraphBuilder.withdrawalsGraph() {
    sharedComposable(route = Routes.ADD_CASH_WITHDRAWAL) {
        val navController = LocalTabNavController.current
        AddCashWithdrawalFeature(
            onWithdrawalSuccess = {
                navController.popBackStack()
            }
        )
    }
}
