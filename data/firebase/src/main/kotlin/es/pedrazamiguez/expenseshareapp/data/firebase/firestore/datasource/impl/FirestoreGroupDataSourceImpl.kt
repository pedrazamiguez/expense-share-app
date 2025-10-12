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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
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

        batch.commit().addOnFailureListener { exception ->
            Timber.w(
                exception,
                "Batch commit failed"
            )
        }

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

                if (groupRefs.isEmpty()) {
                    trySend(emptyList()).isSuccess
                    return@addSnapshotListener
                }

                val groupIds = groupRefs.map { it.id }

                launch(ioDispatcher) {
                    try {
                        // First, try to get all groups from cache only
                        val cachedGroups = groupIds.mapNotNull { groupId ->
                            try {
                                val cachedDoc = firestore.collection(GroupDocument.COLLECTION_PATH).document(groupId)
                                    .get(com.google.firebase.firestore.Source.CACHE).await()

                                if (cachedDoc.exists()) {
                                    cachedDoc.toObject(GroupDocument::class.java)?.toDomain()
                                } else null
                            } catch (e: Exception) {
                                null // Cache miss, ignore for now
                            }
                        }.sortedByDescending { it.lastUpdatedAt }

                        // Send cached results immediately (even if partial)
                        if (cachedGroups.isNotEmpty()) {
                            trySend(cachedGroups).isSuccess
                        }

                        // Then fetch missing groups from server in background (no await to avoid timeout)
                        val missingGroupIds = groupIds.filter { groupId ->
                            cachedGroups.none { it.id == groupId }
                        }

                        if (missingGroupIds.isNotEmpty()) {
                            // Fire off server requests without waiting - they'll update cache for next time
                            missingGroupIds.forEach { groupId ->
                                launch {
                                    try {
                                        firestore.collection(GroupDocument.COLLECTION_PATH).document(groupId)
                                            .get() // This will update the cache for future calls
                                    } catch (e: Exception) {
                                        // Ignore server errors - we already have cache
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        close(e)
                    }
                }
            }
        }

        awaitClose { listener.remove() }
    }

}
