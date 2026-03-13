package es.pedrazamiguez.expenseshareapp.domain.repository

import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import kotlinx.coroutines.flow.Flow

interface SubunitRepository {

    suspend fun createSubunit(groupId: String, subunit: Subunit): String

    suspend fun updateSubunit(groupId: String, subunit: Subunit)

    suspend fun deleteSubunit(groupId: String, subunitId: String)

    fun getGroupSubunitsFlow(groupId: String): Flow<List<Subunit>>

    suspend fun getSubunitById(subunitId: String): Subunit?
}

