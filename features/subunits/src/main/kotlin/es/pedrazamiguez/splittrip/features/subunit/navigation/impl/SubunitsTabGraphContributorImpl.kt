package es.pedrazamiguez.splittrip.features.subunit.navigation.impl

import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.splittrip.core.designsystem.navigation.TabGraphContributor
import es.pedrazamiguez.splittrip.features.subunit.navigation.subunitsGraph

/**
 * Contributes the Subunit Management and Create/Edit Subunit routes
 * into the host tab's [NavHost].
 *
 * Registered via Koin as a [TabGraphContributor] so the groups tab can
 * discover and include this graph at runtime without a compile-time dependency
 * on the `:features:subunits` module.
 */
class SubunitsTabGraphContributorImpl : TabGraphContributor {
    override fun contributeGraph(builder: NavGraphBuilder) {
        builder.subunitsGraph()
    }
}
