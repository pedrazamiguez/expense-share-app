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

    /**
     * Verifies that a subunit document exists on the Firestore server (not just local cache).
     * Forces a server round-trip — throws if the device is offline.
     *
     * Used by repositories to confirm that a locally-created subunit has been
     * successfully persisted to the server, enabling the PENDING_SYNC → SYNCED transition.
     *
     * @param groupId The ID of the group containing the subunit.
     * @param subunitId The ID of the subunit to verify.
     * @return true if the subunit exists on the server.
     * @throws Exception if the server is unreachable (e.g., airplane mode).
     */
    suspend fun verifySubunitOnServer(groupId: String, subunitId: String): Boolean
}
