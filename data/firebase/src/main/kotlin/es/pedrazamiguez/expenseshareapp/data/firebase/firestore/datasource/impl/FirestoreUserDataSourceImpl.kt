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
        val userDocument = UserDocument(
            userId = user.userId,
            email = user.email,
            displayName = user.displayName,
            profileImagePath = user.profileImagePath,
            createdBy = user.userId,
            createdAt = now,
            lastUpdatedBy = user.userId,
            lastUpdatedAt = now
        )

        firestore.collection(UserDocument.COLLECTION_PATH)
            .document(user.userId)
            .set(userDocument, SetOptions.merge())
            .await()
    }
}

