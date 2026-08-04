package es.pedrazamiguez.splittrip.domain.repository

import es.pedrazamiguez.splittrip.domain.model.CashTransfer
import kotlinx.coroutines.flow.Flow

interface CashTransferRepository {
    fun observeGroupCashTransfers(groupId: String): Flow<List<CashTransfer>>
    suspend fun addTransfer(transfer: CashTransfer): Result<Unit>
}
