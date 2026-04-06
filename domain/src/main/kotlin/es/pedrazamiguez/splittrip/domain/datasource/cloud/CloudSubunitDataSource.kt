package es.pedrazamiguez.splittrip.domain.datasource.cloud

import es.pedrazamiguez.splittrip.domain.model.Subunit
import kotlinx.coroutines.flow.Flow

interface CloudSubunitDataSource {

    suspend fun addSubunit(groupId: String, subunit: Subunit)

    suspend fun updateSubunit(groupId: String, subunit: Subunit)

    suspend fun deleteSubunit(groupId: String, subunitId: String)

    /**
     * One-shot fetch of subunits for sync purposes.
     */
    suspend fun fetchSubunitsByGroupId(groupId: String): List<Subunit>

    /**
     * Reactive stream of subunits for real-time sync via Firestore snapshot listener.
     */
    fun getSubunitsByGroupIdFlow(groupId: String): Flow<List<Subunit>>
}
