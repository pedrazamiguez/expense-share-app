package es.pedrazamiguez.expenseshareapp.features.withdrawal.navigation

import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.feature.AddCashWithdrawalFeature

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
