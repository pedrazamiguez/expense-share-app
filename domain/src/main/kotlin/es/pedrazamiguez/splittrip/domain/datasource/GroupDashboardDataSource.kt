package es.pedrazamiguez.splittrip.domain.datasource

import es.pedrazamiguez.splittrip.domain.model.GroupDashboardReadModel
import kotlinx.coroutines.flow.Flow

interface GroupDashboardDataSource {
    fun getDashboardSnapshotFlow(groupId: String): Flow<GroupDashboardReadModel>
}
