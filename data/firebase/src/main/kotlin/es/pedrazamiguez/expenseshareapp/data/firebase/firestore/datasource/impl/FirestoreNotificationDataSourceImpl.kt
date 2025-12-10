package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.DeviceDocument
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudNotificationDataSource
import es.pedrazamiguez.expenseshareapp.domain.provider.AppMetadataProvider
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.CloudMetadataService
import kotlinx.coroutines.tasks.await
import java.util.UUID

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
            isEmulator = appMetadataProvider.isEmulator
        )

        firestore.collection(DeviceDocument.collectionPath(userId))
            .document(deviceId)
            .set(deviceDoc, SetOptions.merge())
            .await()
    }

    override suspend fun unregisterDeviceToken(token: String) {
        val userId = authenticationService.requireUserId()
        val devicesCollection = firestore.collection(DeviceDocument.collectionPath(userId))

        // Find the document containing this token to delete it
        val snapshot = devicesCollection.whereEqualTo(
            DeviceDocument.TOKEN_FIELD, token
        )
            .get()
            .await()

        for (document in snapshot.documents) {
            document.reference.delete()
                .await()
        }
    }

}
