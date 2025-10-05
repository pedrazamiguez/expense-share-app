package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import com.google.firebase.firestore.FirebaseFirestore
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupMemberDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toAdminMemberDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toDomain
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreGroupDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val authenticationService: AuthenticationService
) : CloudGroupDataSource {

    override suspend fun createGroup(group: Group): String {

        val userId = authenticationService.requireUserId()
        val groupId = UUID.randomUUID().toString()

        val groupsCollection = firestore.collection(GroupDocument.COLLECTION_PATH)
        val groupDocRef = groupsCollection.document(groupId)
        val memberDocRef = firestore.collection(GroupMemberDocument.collectionPath(groupId)).document(userId)

        val groupDocument = group.toDocument(
            groupId,
            userId
        )
        val memberDocument = toAdminMemberDocument(
            groupDocRef,
            userId
        )

        val batch = firestore.batch()
        batch.set(
            groupDocRef,
            groupDocument
        )
        batch.set(
            memberDocRef,
            memberDocument
        )

        batch.commit().await()

        return groupId
    }

    override suspend fun getGroupById(groupId: String): Group? {
        TODO("Not yet implemented")
    }

    override suspend fun getAllGroups(): List<Group> = coroutineScope {

        val userId = authenticationService.requireUserId()

        // 1. Query all memberships for the user
        val memberDocs = firestore.collectionGroup(GroupMemberDocument.SUBCOLLECTION_PATH).whereEqualTo(
            "userId",
            userId
        ).get().await().documents

        // 2. Extract group document references
        val groupRefs = memberDocs.mapNotNull { it.getDocumentReference("groupRef") ?: it.reference.parent.parent }

        // 3. Fetch all groups in parallel
        val groups = groupRefs.map { ref ->
            async {
                ref.get().await()?.toObject(GroupDocument::class.java)?.toDomain()
            }
        }.awaitAll().filterNotNull()

        return@coroutineScope groups
    }

    override fun getAllGroupsFlow(): Flow<List<Group>> = callbackFlow {
        val userId = authenticationService.requireUserId()
        val listener = firestore.collectionGroup(GroupMemberDocument.SUBCOLLECTION_PATH).whereEqualTo(
            "userId",
            userId
        ).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val groupRefs = snapshot.documents.mapNotNull {
                    it.getDocumentReference("groupRef") ?: it.reference.parent.parent
                }
                try {
                    // Launch a coroutine inside the callback
                    launch(Dispatchers.IO) {
                        val groups = groupRefs.map { ref ->
                            async { ref.get().await()?.toObject(GroupDocument::class.java)?.toDomain() }
                        }.awaitAll().filterNotNull()
                        trySend(groups).isSuccess
                    }
                } catch (e: Exception) {
                    close(e)
                }
            }
        }

        awaitClose { listener.remove() }
    }

}
