package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.UserDocument
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudUserDataSource
import kotlinx.coroutines.tasks.await

class FirestoreUserDataSourceImpl(
    private val firestore: FirebaseFirestore
) : CloudUserDataSource {

    override suspend fun saveGoogleUser(
        userId: String,
        email: String,
        displayName: String?,
        profilePictureUrl: String?
    ) {
        val userMap = mapOf(
            "userId" to userId,
            "email" to email,
            "displayName" to displayName,
            "profileImagePath" to profilePictureUrl,
            "createdBy" to userId
        )

        firestore.collection(UserDocument.COLLECTION_PATH)
            .document(userId)
            .set(userMap, SetOptions.merge())
            .await()
    }
}

