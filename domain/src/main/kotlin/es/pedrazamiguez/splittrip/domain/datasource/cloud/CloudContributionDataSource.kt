package es.pedrazamiguez.splittrip.domain.datasource.cloud

import es.pedrazamiguez.splittrip.domain.model.Contribution
import kotlinx.coroutines.flow.Flow

interface CloudContributionDataSource {

    suspend fun addContribution(groupId: String, contribution: Contribution)

    suspend fun deleteContribution(groupId: String, contributionId: String)

    /**
     * One-shot fetch of contributions for sync purposes.
     */
    suspend fun fetchContributionsByGroupId(groupId: String): List<Contribution>

    /**
     * Reactive stream of contributions for real-time UI observers.
     * Emits local cache first, then server data as it arrives.
     */
    fun getContributionsByGroupIdFlow(groupId: String): Flow<List<Contribution>>

    /**
     * Verifies that a contribution document exists on the Firestore server (not just local cache).
     * Forces a server round-trip — throws if the device is offline.
     *
     * Used by repositories to confirm that a locally-created contribution has been
     * successfully persisted to the server, enabling the PENDING_SYNC → SYNCED transition.
     *
     * @param groupId The ID of the group containing the contribution.
     * @param contributionId The ID of the contribution to verify.
     * @return true if the contribution exists on the server.
     * @throws Exception if the server is unreachable (e.g., airplane mode).
     */
    suspend fun verifyContributionOnServer(groupId: String, contributionId: String): Boolean
}
