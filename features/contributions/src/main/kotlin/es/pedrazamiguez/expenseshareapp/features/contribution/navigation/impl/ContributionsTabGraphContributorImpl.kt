package es.pedrazamiguez.expenseshareapp.features.contribution.navigation.impl

import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.TabGraphContributor
import es.pedrazamiguez.expenseshareapp.features.contribution.navigation.contributionsGraph

/**
 * Contributes the Add Contribution route into the host tab's [NavHost].
 *
 * Registered via Koin as a [TabGraphContributor] so the balances tab can
 * discover and include this graph at runtime without a compile-time dependency
 * on the `:features:contributions` module.
 */
class ContributionsTabGraphContributorImpl : TabGraphContributor {
    override fun contributeGraph(builder: NavGraphBuilder) {
        builder.contributionsGraph()
    }
}
