package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toDocument
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreGroupDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CloudGroupDataSource {

    override suspend fun createGroup(group: Group): String {
        val groupsCollection = firestore.collection(GroupDocument.COLLECTION_NAME)

        val groupId = UUID.randomUUID().toString()
        val docRef = groupsCollection.document(groupId)

        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

        val groupDocument = group.toDocument(groupId, userId)

        docRef.set(groupDocument).await()
        return groupId
    }

    override suspend fun getGroupById(groupId: String): Group? {
        TODO("Not yet implemented")
    }

    override suspend fun getAllGroups(): List<Group> {
        TODO("Not yet implemented")
    }

}
