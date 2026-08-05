package es.pedrazamiguez.splittrip.domain.datasource.cloud

import es.pedrazamiguez.splittrip.domain.model.CashTransfer
import kotlinx.coroutines.flow.Flow

interface CloudCashTransferDataSource {
    suspend fun upsertCashTransfer(transfer: CashTransfer): Result<Unit>
    suspend fun deleteCashTransfer(groupId: String, transferId: String): Result<Unit>
    fun getGroupCashTransfersFlow(groupId: String): Flow<List<CashTransfer>>
    suspend fun verifyCashTransferOnServer(groupId: String, transferId: String): Boolean
}
