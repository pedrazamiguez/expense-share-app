package es.pedrazamiguez.splittrip.domain.datasource.cloud

import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import kotlinx.coroutines.flow.Flow

interface CloudCashWithdrawalDataSource {

    suspend fun addWithdrawal(groupId: String, withdrawal: CashWithdrawal)

    suspend fun updateWithdrawal(groupId: String, withdrawal: CashWithdrawal)

    suspend fun deleteWithdrawal(groupId: String, withdrawalId: String)

    /**
     * One-shot fetch of withdrawals for sync purposes.
     */
    suspend fun fetchWithdrawalsByGroupId(groupId: String): List<CashWithdrawal>

    /**
     * Reactive stream of withdrawals for real-time UI observers.
     * Emits local cache first, then server data as it arrives.
     */
    fun getWithdrawalsByGroupIdFlow(groupId: String): Flow<List<CashWithdrawal>>
}
