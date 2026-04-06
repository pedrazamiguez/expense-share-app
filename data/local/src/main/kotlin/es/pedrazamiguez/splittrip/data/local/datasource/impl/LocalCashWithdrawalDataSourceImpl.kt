package es.pedrazamiguez.splittrip.data.local.datasource.impl

import es.pedrazamiguez.splittrip.data.local.dao.CashWithdrawalDao
import es.pedrazamiguez.splittrip.data.local.mapper.toDomain
import es.pedrazamiguez.splittrip.data.local.mapper.toEntity
import es.pedrazamiguez.splittrip.domain.datasource.local.LocalCashWithdrawalDataSource
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalCashWithdrawalDataSourceImpl(private val cashWithdrawalDao: CashWithdrawalDao) :
    LocalCashWithdrawalDataSource {

    override fun getWithdrawalsByGroupIdFlow(groupId: String): Flow<List<CashWithdrawal>> =
        cashWithdrawalDao.getWithdrawalsByGroupIdFlow(groupId).map { entities ->
            entities.toDomain()
        }

    override suspend fun getAvailableWithdrawals(groupId: String, currency: String): List<CashWithdrawal> =
        cashWithdrawalDao.getAvailableByGroupAndCurrency(groupId, currency).toDomain()

    override suspend fun getWithdrawalById(withdrawalId: String): CashWithdrawal? =
        cashWithdrawalDao.getWithdrawalById(withdrawalId)?.toDomain()

    override suspend fun saveWithdrawal(withdrawal: CashWithdrawal) {
        cashWithdrawalDao.insertWithdrawal(withdrawal.toEntity())
    }

    override suspend fun updateRemainingAmount(withdrawalId: String, newRemaining: Long) {
        cashWithdrawalDao.updateRemainingAmount(withdrawalId, newRemaining)
    }

    override suspend fun updateRemainingAmounts(updates: List<Pair<String, Long>>) {
        cashWithdrawalDao.updateRemainingAmounts(updates)
    }

    override suspend fun deleteWithdrawal(withdrawalId: String) {
        cashWithdrawalDao.deleteWithdrawal(withdrawalId)
    }

    override suspend fun deleteWithdrawalsByGroupId(groupId: String) {
        cashWithdrawalDao.deleteWithdrawalsByGroupId(groupId)
    }

    override suspend fun replaceWithdrawalsForGroup(groupId: String, withdrawals: List<CashWithdrawal>) {
        cashWithdrawalDao.replaceWithdrawalsForGroup(
            groupId,
            withdrawals.map { it.toEntity() }
        )
    }

    override suspend fun getWithdrawalIdsByGroup(groupId: String): List<String> =
        cashWithdrawalDao.getWithdrawalIdsByGroupId(groupId)

    override suspend fun updateSyncStatus(withdrawalId: String, syncStatus: SyncStatus) {
        cashWithdrawalDao.updateSyncStatus(withdrawalId, syncStatus.name)
    }

    override suspend fun clearAllWithdrawals() {
        cashWithdrawalDao.clearAllWithdrawals()
    }
}
