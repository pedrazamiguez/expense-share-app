package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import kotlinx.coroutines.tasks.await

class FirestoreGroupDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CloudGroupDataSource {

    override suspend fun createGroup(group: Group): String {
        val groupsCollection = firestore.collection("groups")
        val docRef = groupsCollection.document()

        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

        val groupDoc = GroupDocument(
            groupId = docRef.id,
            name = group.name,
            description = group.description,
            currency = group.currency,
            createdBy = userId
        )

        docRef.set(groupDoc).await()
        return docRef.id
    }

    override suspend fun getGroupById(groupId: String): Group? {
        TODO("Not yet implemented")
    }

    override suspend fun getAllGroups(): List<Group> {
        TODO("Not yet implemented")
    }
}