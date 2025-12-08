package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.UserDocument
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudNotificationDataSource
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.tasks.await

class FirestoreNotificationDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val authenticationService: AuthenticationService
) : CloudNotificationDataSource {

    override suspend fun registerDeviceToken(token: String) {
        val userId = authenticationService.requireUserId()

        val deviceData = mapOf(
            "token" to token,
            "platform" to "android",
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore
            .collection(UserDocument.COLLECTION_PATH)
            .document(userId)
            .collection(UserDocument.DEVICES_COLLECTION_PATH)
            .document(token)
            .set(
                deviceData,
                SetOptions.merge()
            )
            .await()
    }

    override suspend fun unregisterDeviceToken(token: String) {
        val userId = authenticationService.requireUserId()

        firestore
            .collection(UserDocument.COLLECTION_PATH)
            .document(userId)
            .collection(UserDocument.DEVICES_COLLECTION_PATH)
            .document(token)
            .delete()
            .await()
    }

}
