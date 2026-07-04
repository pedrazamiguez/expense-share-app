package es.pedrazamiguez.splittrip.features.group.navigation.impl

import androidx.navigation.NavGraphBuilder
import es.pedrazamiguez.splittrip.core.designsystem.navigation.TabGraphContributor
import es.pedrazamiguez.splittrip.features.group.navigation.settlementOverviewGraph

class GroupSettlementTabGraphContributorImpl : TabGraphContributor {
    override fun contributeGraph(builder: NavGraphBuilder) {
        builder.settlementOverviewGraph()
    }
}
