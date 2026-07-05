package es.pedrazamiguez.splittrip.domain.repository

import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import kotlinx.coroutines.flow.Flow

interface SettlementRepository {
    fun getGroupSettlementsFlow(groupId: String): Flow<List<SettlementRecord>>
    suspend fun getGroupSettlements(groupId: String): List<SettlementRecord>
    suspend fun getMemberSettlements(groupId: String, userId: String): List<SettlementRecord>
    suspend fun getSettlementById(id: String): SettlementRecord?
    suspend fun addSettlement(record: SettlementRecord)
    suspend fun updateSettlement(record: SettlementRecord)
    suspend fun deleteSettlement(record: SettlementRecord)
}
