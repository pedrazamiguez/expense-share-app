package es.pedrazamiguez.expenseshareapp.features.withdrawal.navigation.impl

import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.TabGraphContributor
import es.pedrazamiguez.expenseshareapp.features.withdrawal.navigation.withdrawalsGraph

/**
 * Contributes the Add Cash Withdrawal route into the host tab's [NavHost].
 *
 * Registered via Koin as a [TabGraphContributor] so the balances tab can
 * discover and include this graph at runtime without a compile-time dependency
 * on the `:features:withdrawals` module.
 */
class WithdrawalsTabGraphContributorImpl : TabGraphContributor {
    override fun contributeGraph(builder: NavGraphBuilder) {
        builder.withdrawalsGraph()
    }
}
