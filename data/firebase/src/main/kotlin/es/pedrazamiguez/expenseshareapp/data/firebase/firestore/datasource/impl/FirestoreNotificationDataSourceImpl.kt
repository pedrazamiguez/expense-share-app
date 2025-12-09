package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import android.os.Build
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.DeviceDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.UserDocument
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudNotificationDataSource
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.CloudMetadataService
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreNotificationDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val authenticationService: AuthenticationService,
    private val cloudMetadataService: CloudMetadataService
) : CloudNotificationDataSource {

    override suspend fun registerDeviceToken(token: String) {
        val userId = authenticationService.requireUserId()

        // Obtain a stable ID for this device
        val installationIdResult = cloudMetadataService.getAppInstallationId()
        val deviceId = installationIdResult.getOrDefault(UUID.randomUUID().toString())

        val deviceDoc = DeviceDocument(
            deviceId = deviceId,
            token = token,
            platform = "android",
            model = "${Build.MANUFACTURER} ${Build.MODEL}",
            androidVersion = Build.VERSION.RELEASE
        )

        // Use deviceId as document key to avoid duplicates when refreshing the token
        firestore
            .collection(UserDocument.COLLECTION_PATH)
            .document(userId)
            .collection(UserDocument.DEVICES_COLLECTION_PATH)
            .document(deviceId)
            .set(deviceDoc, SetOptions.merge())
            .await()
    }

    override suspend fun unregisterDeviceToken(token: String) {
        val userId = authenticationService.requireUserId()
        val devicesCollection = firestore
            .collection(UserDocument.COLLECTION_PATH)
            .document(userId)
            .collection(UserDocument.DEVICES_COLLECTION_PATH)

        // Find the document containing this token to delete it
        val snapshot = devicesCollection
            .whereEqualTo("token", token)
            .get()
            .await()

        for (document in snapshot.documents) {
            document.reference.delete().await()
        }
    }

}
