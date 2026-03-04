package es.pedrazamiguez.expenseshareapp.features.balance.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.feature.AddCashWithdrawalFeature
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.feature.BalancesFeature

fun NavGraphBuilder.balancesGraph() {
    composable(route = Routes.BALANCES) {
        BalancesFeature()
    }

    composable(route = Routes.ADD_CASH_WITHDRAWAL) {
        val navController = LocalTabNavController.current
        AddCashWithdrawalFeature(
            onWithdrawalSuccess = {
                navController.popBackStack()
            }
        )
    }
}
