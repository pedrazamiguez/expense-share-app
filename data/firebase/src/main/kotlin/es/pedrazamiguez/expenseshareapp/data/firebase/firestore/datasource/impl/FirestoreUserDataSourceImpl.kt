package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.UserDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toLocalDateTimeUtc
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudUserDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.User
import kotlinx.coroutines.tasks.await
import timber.log.Timber
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

    override suspend fun getUsersByIds(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) return emptyList()

        return try {
            // Firestore whereIn has a max of 10 values per query,
            // so we split the IDs into chunks and merge the results.
            val maxInQuerySize = 10
            val distinctIds = userIds.distinct()

            val users = mutableListOf<User>()

            distinctIds.chunked(maxInQuerySize).forEach { chunk ->
                val snapshot = firestore
                    .collection(UserDocument.COLLECTION_PATH)
                    .whereIn("userId", chunk)
                    .get()
                    .await()

                snapshot.documents.mapNotNullTo(users) { doc ->
                    doc.toObject(UserDocument::class.java)?.let { userDoc ->
                        User(
                            userId = userDoc.userId,
                            email = userDoc.email,
                            displayName = userDoc.displayName,
                            profileImagePath = userDoc.profileImagePath,
                            createdAt = userDoc.createdAt.toLocalDateTimeUtc()
                        )
                    }
                }
            }

            users
        } catch (e: Exception) {
            Timber.e(e, "Error fetching users by IDs")
            emptyList()
        }
    }
}
