package es.pedrazamiguez.expenseshareapp.domain.datasource.cloud

import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import kotlinx.coroutines.flow.Flow

interface CloudSubunitDataSource {

    suspend fun addSubunit(groupId: String, subunit: Subunit)

    suspend fun updateSubunit(groupId: String, subunit: Subunit)

    suspend fun deleteSubunit(groupId: String, subunitId: String)

    /**
     * Reactive stream of sub-units for real-time sync via Firestore snapshot listener.
     */
    fun getSubunitsByGroupIdFlow(groupId: String): Flow<List<Subunit>>
}

