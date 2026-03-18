package es.pedrazamiguez.expenseshareapp.data.worker

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * [GroupDeletionRetryScheduler] backed by WorkManager.
 *
 * Enqueues a unique, network-constrained [GroupDeletionWorker] per group ID with
 * exponential backoff (starting at 30 seconds). The work survives app kills and
 * device reboots, retrying automatically once connectivity is restored.
 */
class GroupDeletionRetrySchedulerImpl(
    private val workManager: WorkManager
) : GroupDeletionRetryScheduler {

    override fun scheduleRetry(groupId: String) {
        val request = OneTimeWorkRequestBuilder<GroupDeletionWorker>()
            .setInputData(workDataOf(GroupDeletionWorker.KEY_GROUP_ID to groupId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            "$WORK_NAME_PREFIX$groupId",
            ExistingWorkPolicy.KEEP,
            request
        )

        Timber.d("Scheduled WorkManager retry for group deletion: $groupId")
    }

    companion object {
        private const val WORK_NAME_PREFIX = "group-deletion-"
    }
}

