package es.pedrazamiguez.expenseshareapp.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudGroupDataSource
import org.koin.core.context.GlobalContext
import timber.log.Timber

/**
 * WorkManager [CoroutineWorker] that retries a failed group deletion cloud request.
 *
 * This worker is enqueued by [GroupDeletionRetrySchedulerImpl] when the initial
 * `requestGroupDeletion()` call fails (e.g. device is offline). It is configured
 * with a network-connectivity constraint and exponential backoff, so it will
 * keep retrying until the Firestore update succeeds.
 *
 * The operation is idempotent: setting `deletionRequested = true` on a group
 * document that already has it set is a safe no-op.
 */
class GroupDeletionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val groupId = inputData.getString(KEY_GROUP_ID)
        if (groupId.isNullOrBlank()) {
            Timber.e("GroupDeletionWorker: missing group ID — failing permanently")
            return Result.failure()
        }

        return try {
            val cloudDataSource: CloudGroupDataSource = GlobalContext.get().get()
            cloudDataSource.requestGroupDeletion(groupId)
            Timber.d("GroupDeletionWorker: successfully requested deletion for group $groupId")
            Result.success()
        } catch (e: Exception) {
            Timber.w(
                e,
                "GroupDeletionWorker: attempt $runAttemptCount failed for group $groupId — retrying"
            )
            Result.retry()
        }
    }

    companion object {
        const val KEY_GROUP_ID = "group_id"
    }
}
