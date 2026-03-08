package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.UserDocument
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudUserDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.User
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirestoreUserDataSourceImpl(
    private val firestore: FirebaseFirestore
) : CloudUserDataSource {

    override suspend fun saveUser(user: User) {
        val now = Timestamp(Date())
        val docRef = firestore.collection(UserDocument.COLLECTION_PATH)
            .document(user.userId)

        // Check if user document already exists to avoid overwriting createdAt
        val existingDoc = docRef.get().await()

        val data = mutableMapOf<String, Any>(
            "userId" to user.userId,
            "email" to user.email,
            "lastUpdatedBy" to user.userId,
            "lastUpdatedAt" to now
        )

        user.displayName?.let { data["displayName"] = it }
        user.profileImagePath?.let { data["profileImagePath"] = it }

        // Only set creation metadata for new users
        if (!existingDoc.exists()) {
            data["createdBy"] = user.userId
            data["createdAt"] = now
        }

        docRef.set(data, SetOptions.merge()).await()
    }
}

