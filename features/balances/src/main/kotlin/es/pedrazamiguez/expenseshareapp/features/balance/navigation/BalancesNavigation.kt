package es.pedrazamiguez.expenseshareapp.features.balance.navigation

import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.sharedComposable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.feature.AddCashWithdrawalFeature
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.feature.AddContributionFeature
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.feature.BalancesFeature

fun NavGraphBuilder.balancesGraph() {
    sharedComposable(route = Routes.BALANCES) {
        BalancesFeature()
    }

    sharedComposable(route = Routes.ADD_CONTRIBUTION) {
        val navController = LocalTabNavController.current
        AddContributionFeature(
            onContributionSuccess = {
                navController.popBackStack()
            }
        )
    }

    sharedComposable(route = Routes.ADD_CASH_WITHDRAWAL) {
        val navController = LocalTabNavController.current
        AddCashWithdrawalFeature(
            onWithdrawalSuccess = {
                navController.popBackStack()
            }
        )
    }
}
