package es.pedrazamiguez.splittrip.data.firebase.firestore.datasource.impl

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import es.pedrazamiguez.splittrip.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.splittrip.data.firebase.firestore.document.SubunitDocument
import es.pedrazamiguez.splittrip.data.firebase.firestore.mapper.toDocument
import es.pedrazamiguez.splittrip.data.firebase.firestore.mapper.toDomain
import es.pedrazamiguez.splittrip.domain.datasource.cloud.CloudSubunitDataSource
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirestoreSubunitDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val authenticationService: AuthenticationService
) : CloudSubunitDataSource {

    override suspend fun addSubunit(groupId: String, subunit: Subunit) {
        val userId = authenticationService.requireUserId()
        val subunitId = subunit.id

        val groupDocRef = firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
        val subunitDocRef = groupDocRef
            .collection(SubunitDocument.COLLECTION_PATH)
            .document(subunitId)

        val subunitDocument = subunit.toDocument(
            subunitId,
            groupId,
            groupDocRef,
            userId
        )

        subunitDocRef
            .set(subunitDocument)
            .await()
    }

    override suspend fun updateSubunit(groupId: String, subunit: Subunit) {
        val userId = authenticationService.requireUserId()

        val groupDocRef = firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
        val subunitDocRef = groupDocRef
            .collection(SubunitDocument.COLLECTION_PATH)
            .document(subunit.id)

        val subunitDocument = subunit.toDocument(
            subunit.id,
            groupId,
            groupDocRef,
            userId
        )

        subunitDocRef
            .set(subunitDocument)
            .await()
    }

    override suspend fun deleteSubunit(groupId: String, subunitId: String) {
        firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
            .collection(SubunitDocument.COLLECTION_PATH)
            .document(subunitId)
            .delete()
            .await()
    }

    override suspend fun fetchSubunitsByGroupId(groupId: String): List<Subunit> {
        val snapshot = firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
            .collection(SubunitDocument.COLLECTION_PATH)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(SubunitDocument::class.java)?.toDomain()
        }.sortedBy { it.name }
    }

    override fun getSubunitsByGroupIdFlow(groupId: String): Flow<List<Subunit>> = callbackFlow {
        val subunitsCollection = createSubunitsCollection(groupId)

        val listener = createSubunitListener(subunitsCollection) { snapshot ->
            launch {
                val cachedSubunits = loadSubunitsFromCache(
                    subunitsCollection,
                    snapshot.documents
                )

                trySend(cachedSubunits)

                val cachedIds = cachedSubunits.map { it.id }.toSet()
                val missingIds = snapshot.documents
                    .map { it.id }
                    .filter { it !in cachedIds }

                if (missingIds.isNotEmpty()) {
                    val serverSubunits = loadSubunitsFromServer(
                        subunitsCollection,
                        missingIds
                    )
                    val allSubunits =
                        (cachedSubunits + serverSubunits).sortedBy { it.name }
                    trySend(allSubunits)
                }
            }
        }

        awaitClose { listener.remove() }
    }

    private fun createSubunitsCollection(groupId: String) = firestore
        .collection(GroupDocument.COLLECTION_PATH)
        .document(groupId)
        .collection(SubunitDocument.COLLECTION_PATH)

    private fun createSubunitListener(subunitsCollection: CollectionReference, onUpdate: (QuerySnapshot) -> Unit) =
        subunitsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to subunits")
                return@addSnapshotListener
            }
            snapshot?.let(onUpdate)
        }

    private suspend fun loadSubunitsFromCache(
        subunitsCollection: CollectionReference,
        documents: List<DocumentSnapshot>
    ): List<Subunit> = documents
        .mapNotNull { doc ->
            loadSingleSubunitFromCache(subunitsCollection, doc.id)
        }
        .sortedBy { it.name }

    @Suppress("kotlin:S6518")
    private suspend fun loadSingleSubunitFromCache(
        subunitsCollection: CollectionReference,
        subunitId: String
    ): Subunit? = try {
        val cachedDoc = subunitsCollection
            .document(subunitId)
            .get(Source.CACHE)
            .await()

        if (cachedDoc.exists()) {
            cachedDoc.toObject(SubunitDocument::class.java)?.toDomain()
        } else {
            null
        }
    } catch (e: Exception) {
        Timber.d(e, "Cache miss for subunit $subunitId, will load from server")
        null
    }

    private suspend fun loadSubunitsFromServer(
        subunitsCollection: CollectionReference,
        missingIds: List<String>
    ): List<Subunit> = try {
        missingIds
            .chunked(FIRESTORE_WHERE_IN_LIMIT)
            .flatMap { batch ->
                subunitsCollection
                    .whereIn(FieldPath.documentId(), batch)
                    .get(Source.SERVER)
                    .await()
                    .documents
                    .mapNotNull { it.toObject(SubunitDocument::class.java)?.toDomain() }
            }
    } catch (e: Exception) {
        Timber.w(e, "Failed to load subunits from server")
        emptyList()
    }

    private companion object {
        const val FIRESTORE_WHERE_IN_LIMIT = 30
    }
}
