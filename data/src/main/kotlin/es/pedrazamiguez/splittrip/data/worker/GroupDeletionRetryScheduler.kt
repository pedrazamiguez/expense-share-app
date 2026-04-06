package es.pedrazamiguez.splittrip.data.worker

/**
 * Abstraction for scheduling a retry of a failed group deletion cloud request.
 *
 * The implementation uses WorkManager to guarantee delivery even if the app is
 * killed or the device reboots, waiting for network connectivity before retrying.
 */
interface GroupDeletionRetryScheduler {

    /**
     * Enqueues a persistent, network-constrained work request to retry
     * [CloudGroupDataSource.requestGroupDeletion] for the given [groupId].
     *
     * The request is unique per group: if a retry is already pending for the
     * same group, the existing work is kept (not replaced).
     */
    fun scheduleRetry(groupId: String)
}
