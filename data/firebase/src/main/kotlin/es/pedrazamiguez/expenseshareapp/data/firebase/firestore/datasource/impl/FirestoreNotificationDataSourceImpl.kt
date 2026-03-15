package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import es.pedrazamiguez.expenseshareapp.core.common.provider.AppMetadataProvider
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.DeviceDocument
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudNotificationDataSource
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.CloudMetadataService
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirestoreNotificationDataSourceImpl(
    private val appMetadataProvider: AppMetadataProvider,
    private val firestore: FirebaseFirestore,
    private val authenticationService: AuthenticationService,
    private val cloudMetadataService: CloudMetadataService
) : CloudNotificationDataSource {

    override suspend fun registerDeviceToken(token: String) {
        val userId = authenticationService.requireUserId()
        val installationIdResult = cloudMetadataService.getAppInstallationId()
        val deviceId = installationIdResult.getOrDefault(
            UUID.randomUUID()
                .toString()
        )

        val deviceDoc = DeviceDocument(
            deviceId = deviceId,
            token = token,
            model = appMetadataProvider.deviceModel,
            androidVersion = appMetadataProvider.androidVersion,
            appVersionName = appMetadataProvider.appVersionName,
            appVersionCode = appMetadataProvider.appVersionCode,
            isEmulator = appMetadataProvider.isEmulator,
            lastUpdatedAt = Timestamp.now()
        )

        firestore.collection(DeviceDocument.collectionPath(userId))
            .document(deviceId)
            .set(deviceDoc, SetOptions.merge())
            .await()
    }

    override suspend fun unregisterCurrentDevice() {
        val userId = authenticationService.requireUserId()
        val installationIdResult = cloudMetadataService.getAppInstallationId()
        val deviceId = installationIdResult.getOrNull()

        if (deviceId == null) {
            Timber.w("Cannot unregister device: Installation ID unavailable")
            return
        }

        firestore.collection(DeviceDocument.collectionPath(userId))
            .document(deviceId)
            .delete()
            .await()

        Timber.d("Unregistered device %s for user", deviceId)
    }

    override suspend fun removeStaleDevices() {
        val userId = authenticationService.requireUserId()
        val devicesCollection = firestore.collection(DeviceDocument.collectionPath(userId))

        // Phase 0: Delete orphaned device documents from previous installations
        // After a reinstall, the Firebase Installation ID changes. The old device
        // document (with a stale token) remains and causes failed FCM sends on
        // every notification until the 90-day staleness threshold kicks in.
        try {
            val currentDeviceId = cloudMetadataService.getAppInstallationId().getOrNull()
            if (currentDeviceId != null) {
                val allDevices = devicesCollection.get().await()
                val orphanDocs = allDevices.documents.filter { it.id != currentDeviceId }
                if (orphanDocs.isNotEmpty()) {
                    val deleteTasks = orphanDocs.map { it.reference.delete() }
                    Tasks.whenAll(deleteTasks).await()
                    Timber.d(
                        "Deleted %d orphaned device documents (not matching current FID %s)",
                        orphanDocs.size,
                        currentDeviceId
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Failed to clean up orphaned device documents")
        }

        // Phase 1: Delete documents older than the stale threshold
        try {
            val thresholdMillis = TimeUnit.DAYS.toMillis(DeviceDocument.STALE_THRESHOLD_DAYS)
            val staleThreshold = Timestamp(Date(System.currentTimeMillis() - thresholdMillis))

            val staleDocs = devicesCollection
                .whereLessThan(DeviceDocument.LAST_UPDATED_AT_FIELD, staleThreshold)
                .get()
                .await()

            if (staleDocs.documents.isNotEmpty()) {
                val deleteTasks = staleDocs.documents.map { it.reference.delete() }
                Tasks.whenAll(deleteTasks).await()
                Timber.d(
                    "Deleted %d stale device documents (> %d days old)",
                    staleDocs.size(),
                    DeviceDocument.STALE_THRESHOLD_DAYS
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Failed to delete stale device documents")
        }

        // Phase 2: Enforce max devices cap (keep only the N most recent)
        try {
            val allDevices = devicesCollection
                .orderBy(DeviceDocument.LAST_UPDATED_AT_FIELD, Query.Direction.DESCENDING)
                .get()
                .await()

            if (allDevices.size() > DeviceDocument.MAX_DEVICES_PER_USER) {
                val excessDocs = allDevices.documents.drop(DeviceDocument.MAX_DEVICES_PER_USER)
                val deleteTasks = excessDocs.map { it.reference.delete() }
                Tasks.whenAll(deleteTasks).await()
                Timber.d(
                    "Deleted %d excess device documents (cap: %d)",
                    excessDocs.size,
                    DeviceDocument.MAX_DEVICES_PER_USER
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Failed to enforce max devices cap")
        }
    }
}
