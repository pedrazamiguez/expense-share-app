package es.pedrazamiguez.splittrip.domain.datasource.cloud

import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import kotlinx.coroutines.flow.Flow

interface CloudSettlementDataSource {
    fun getSettlementsByGroupIdFlow(groupId: String): Flow<List<SettlementRecord>>
    suspend fun upsertSettlement(groupId: String, record: SettlementRecord)
    suspend fun verifySettlementOnServer(groupId: String, id: String): Boolean
}
