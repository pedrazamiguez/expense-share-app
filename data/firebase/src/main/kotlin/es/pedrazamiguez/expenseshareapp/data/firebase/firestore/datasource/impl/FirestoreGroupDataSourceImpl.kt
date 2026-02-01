package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupMemberDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toAdminMemberDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toDomain
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirestoreGroupDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val authenticationService: AuthenticationService
) : CloudGroupDataSource {

    override suspend fun createGroup(group: Group): String {
        val userId = authenticationService.requireUserId()
        val groupId = group.id

        val groupsCollection = firestore.collection(GroupDocument.COLLECTION_PATH)
        val groupDocRef = groupsCollection.document(groupId)
        val memberDocRef = firestore
            .collection(GroupMemberDocument.collectionPath(groupId))
            .document(userId)

        val groupDocument = group.toDocument(
            groupId,
            userId
        )
        val memberDocument = toAdminMemberDocument(
            groupDocRef,
            userId
        )

        val batch = firestore
            .batch()
            .apply {
                set(
                    groupDocRef,
                    groupDocument
                )
                set(
                    memberDocRef,
                    memberDocument
                )
            }

        batch
            .commit()
            .addOnFailureListener { exception ->
                Timber.w(
                    exception,
                    "Create group failed"
                )
            }

        return groupId
    }

    override suspend fun getGroupById(groupId: String): Group? {
        // Try cache first
        val cachedGroup = loadSingleGroupFromCache(groupId)
        if (cachedGroup != null) {
            return cachedGroup
        }

        // If not in cache, fetch from server
        return try {
            val groupDoc = firestore
                .collection(GroupDocument.COLLECTION_PATH)
                .document(groupId)
                .get()
                .await()

            if (groupDoc.exists()) {
                groupDoc
                    .toObject(GroupDocument::class.java)
                    ?.toDomain()
            } else {
                Timber.w("Group not found: $groupId")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching group $groupId from server")
            null
        }
    }

    override fun getAllGroupsFlow(): Flow<List<Group>> = callbackFlow {
        val userId = authenticationService.requireUserId()

        val listener = createGroupMemberListener(userId) { groupRefs ->
            if (groupRefs.isEmpty()) {
                trySend(emptyList())
                return@createGroupMemberListener
            }

            launch {
                val groupIds = groupRefs.map { it.id }
                val cachedGroups = loadGroupsFromCache(groupIds)

                trySend(cachedGroups)

                val missingGroupIds = groupIds.filter { groupId ->
                    cachedGroups.none { it.id == groupId }
                }

                if (missingGroupIds.isNotEmpty()) {
                    val serverGroups = loadGroupsFromServer(missingGroupIds)
                    val allGroups =
                        (cachedGroups + serverGroups).sortedByDescending { it.lastUpdatedAt }
                    trySend(allGroups)
                }
            }
        }

        awaitClose { listener.remove() }
    }

    private fun createGroupMemberListener(
        userId: String, onUpdate: (List<DocumentReference>) -> Unit
    ) = firestore
        .collectionGroup(GroupMemberDocument.SUBCOLLECTION_PATH)
        .whereEqualTo(
            GroupMemberDocument.USER_ID_FIELD,
            userId
        )
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(
                    error,
                    "Error listening to group members"
                )
                return@addSnapshotListener
            }

            snapshot?.let { snap ->
                val groupRefs = extractGroupReferences(snap.documents)
                onUpdate(groupRefs)
            }
        }

    private fun extractGroupReferences(documents: List<DocumentSnapshot>) =
        documents.mapNotNull { doc ->
            doc.getDocumentReference(GroupMemberDocument.FIELD_GROUP_REF)
                ?: doc.reference.parent.parent
        }

    private suspend fun loadGroupsFromCache(groupIds: List<String>): List<Group> = groupIds
        .mapNotNull { groupId ->
            loadSingleGroupFromCache(groupId)
        }
        .sortedByDescending { it.lastUpdatedAt }

    private suspend fun loadSingleGroupFromCache(groupId: String): Group? = try {
        @Suppress("kotlin:S6518")
        val cachedDoc = firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
            .get(Source.CACHE)
            .await()

        if (cachedDoc.exists()) {
            cachedDoc
                .toObject(GroupDocument::class.java)
                ?.toDomain()
        } else null
    } catch (_: Exception) {
        Timber.d("Cache miss for group $groupId")
        null
    }

    private suspend fun loadGroupsFromServer(groupIds: List<String>): List<Group> {
        val groups = mutableListOf<Group>()

        groupIds.forEach { groupId ->
            try {
                val groupDoc = firestore
                    .collection(GroupDocument.COLLECTION_PATH)
                    .document(groupId)
                    .get()
                    .await()

                if (groupDoc.exists()) {
                    groupDoc
                        .toObject(GroupDocument::class.java)
                        ?.toDomain()
                        ?.let { group ->
                            groups.add(group)
                        }
                }
            } catch (e: Exception) {
                Timber.e(
                    e,
                    "Error fetching group $groupId from server"
                )
            }
        }

        return groups
    }
}
