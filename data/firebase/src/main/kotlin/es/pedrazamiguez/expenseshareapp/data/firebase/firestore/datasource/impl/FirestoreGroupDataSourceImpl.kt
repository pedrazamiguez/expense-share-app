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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreGroupDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val authenticationService: AuthenticationService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
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

        val batch = firestore.batch().apply {
            set(
                groupDocRef,
                groupDocument
            )
            set(
                memberDocRef,
                memberDocument
            )
        }

        batch.commit().await()

        return groupId
    }

    override suspend fun getGroupById(groupId: String): Group? {
        TODO("Not yet implemented")
    }

    override fun getAllGroupsFlow(): Flow<List<Group>> = callbackFlow {
        val userId = authenticationService.requireUserId()
        val listener = firestore.collectionGroup(GroupMemberDocument.SUBCOLLECTION_PATH).whereEqualTo(
            GroupMemberDocument.USER_ID_FIELD,
            userId
        ).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val groupRefs = snapshot.documents.mapNotNull {
                    it.getDocumentReference(GroupMemberDocument.FIELD_GROUP_REF) ?: it.reference.parent.parent
                }
                try {
                    launch(ioDispatcher) {
                        val groups = groupRefs.map { ref ->
                            async { ref.get().await()?.toObject(GroupDocument::class.java)?.toDomain() }
                        }.awaitAll().filterNotNull().sortedByDescending { it.lastUpdatedAt }
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
