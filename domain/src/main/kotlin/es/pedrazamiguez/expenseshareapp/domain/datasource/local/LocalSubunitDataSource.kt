package es.pedrazamiguez.expenseshareapp.domain.datasource.local

import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import kotlinx.coroutines.flow.Flow

interface LocalSubunitDataSource {

    fun getSubunitsByGroupIdFlow(groupId: String): Flow<List<Subunit>>

    /**
     * One-shot read of all subunits for a group.
     * Does not trigger any sync or subscription side effects.
     */
    suspend fun getSubunitsByGroupId(groupId: String): List<Subunit>

    suspend fun saveSubunit(subunit: Subunit)

    suspend fun deleteSubunit(subunitId: String)

    suspend fun deleteSubunitsByGroupId(groupId: String)

    /**
     * Atomically replaces all subunits for a group with the provided list.
     * Used during real-time sync to reconcile local state with the cloud snapshot.
     */
    suspend fun replaceSubunitsForGroup(groupId: String, subunits: List<Subunit>)

    suspend fun getSubunitIdsByGroup(groupId: String): List<String>

    suspend fun getSubunitById(subunitId: String): Subunit?

    suspend fun clearAllSubunits()
}
