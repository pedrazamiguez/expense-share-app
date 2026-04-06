package es.pedrazamiguez.splittrip.domain.repository

import es.pedrazamiguez.splittrip.domain.model.Subunit
import kotlinx.coroutines.flow.Flow

interface SubunitRepository {

    suspend fun createSubunit(groupId: String, subunit: Subunit): String

    suspend fun updateSubunit(groupId: String, subunit: Subunit)

    suspend fun deleteSubunit(groupId: String, subunitId: String)

    fun getGroupSubunitsFlow(groupId: String): Flow<List<Subunit>>

    /**
     * One-shot read of all subunits for a group from local storage.
     * Unlike [getGroupSubunitsFlow], this does NOT trigger cloud subscription
     * side effects, making it safe for validation reads in use cases.
     */
    suspend fun getGroupSubunits(groupId: String): List<Subunit>

    suspend fun getSubunitById(subunitId: String): Subunit?
}
