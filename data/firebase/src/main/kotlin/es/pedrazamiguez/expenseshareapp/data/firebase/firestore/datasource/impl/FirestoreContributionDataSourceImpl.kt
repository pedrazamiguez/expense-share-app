package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.ContributionDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toDomain
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudContributionDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirestoreContributionDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val authenticationService: AuthenticationService
) : CloudContributionDataSource {

    override suspend fun addContribution(groupId: String, contribution: Contribution) {
        val userId = authenticationService.requireUserId()
        val contributionId = contribution.id

        val groupDocRef = firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
        val contributionDocRef = groupDocRef
            .collection(ContributionDocument.COLLECTION_PATH)
            .document(contributionId)

        val contributionDocument = contribution.toDocument(
            contributionId,
            groupId,
            groupDocRef,
            userId
        )

        contributionDocRef
            .set(contributionDocument)
            .addOnFailureListener { exception ->
                Timber.w(exception, "Add contribution failed")
            }
    }

    override suspend fun deleteContribution(groupId: String, contributionId: String) {
        firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
            .collection(ContributionDocument.COLLECTION_PATH)
            .document(contributionId)
            .delete()
            .await()
    }

    override suspend fun fetchContributionsByGroupId(groupId: String): List<Contribution> {
        val snapshot = firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
            .collection(ContributionDocument.COLLECTION_PATH)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(ContributionDocument::class.java)?.toDomain()
        }.sortedByDescending { it.createdAt ?: it.lastUpdatedAt }
    }

    override fun getContributionsByGroupIdFlow(groupId: String): Flow<List<Contribution>> =
        callbackFlow {
            val contributionsCollection = createContributionsCollection(groupId)

            val listener = createContributionListener(contributionsCollection) { snapshot ->
                launch {
                    val cachedContributions = loadContributionsFromCache(
                        contributionsCollection,
                        snapshot.documents
                    )

                    trySend(cachedContributions)

                    val cachedIds = cachedContributions.map { it.id }.toSet()
                    val missingIds = snapshot.documents
                        .map { it.id }
                        .filter { it !in cachedIds }

                    if (missingIds.isNotEmpty()) {
                        val serverContributions = loadContributionsFromServer(
                            contributionsCollection,
                            missingIds
                        )
                        val allContributions =
                            (cachedContributions + serverContributions).sortedByDescending {
                                it.createdAt ?: it.lastUpdatedAt
                            }
                        trySend(allContributions)
                    }
                }
            }

            awaitClose { listener.remove() }
        }

    private fun createContributionsCollection(groupId: String) = firestore
        .collection(GroupDocument.COLLECTION_PATH)
        .document(groupId)
        .collection(ContributionDocument.COLLECTION_PATH)

    private fun createContributionListener(
        contributionsCollection: CollectionReference,
        onUpdate: (QuerySnapshot) -> Unit
    ) = contributionsCollection.addSnapshotListener { snapshot, error ->
        if (error != null) {
            Timber.e(error, "Error listening to contributions")
            return@addSnapshotListener
        }
        snapshot?.let(onUpdate)
    }

    private suspend fun loadContributionsFromCache(
        contributionsCollection: CollectionReference,
        documents: List<DocumentSnapshot>
    ): List<Contribution> = documents
        .mapNotNull { doc ->
            loadSingleContributionFromCache(contributionsCollection, doc.id)
        }
        .sortedByDescending { it.createdAt ?: it.lastUpdatedAt }

    @Suppress("kotlin:S6518")
    private suspend fun loadSingleContributionFromCache(
        contributionsCollection: CollectionReference,
        contributionId: String
    ): Contribution? = try {
        val cachedDoc = contributionsCollection
            .document(contributionId)
            .get(Source.CACHE)
            .await()

        if (cachedDoc.exists()) {
            cachedDoc.toObject(ContributionDocument::class.java)?.toDomain()
        } else {
            null
        }
    } catch (e: Exception) {
        Timber.d("Cache miss for contribution $contributionId, will load from server")
        null
    }

    private suspend fun loadContributionsFromServer(
        contributionsCollection: CollectionReference,
        missingIds: List<String>
    ): List<Contribution> = try {
        contributionsCollection
            .whereIn("contributionId", missingIds)
            .get(Source.SERVER)
            .await()
            .documents
            .mapNotNull { it.toObject(ContributionDocument::class.java)?.toDomain() }
    } catch (e: Exception) {
        Timber.w(e, "Failed to load contributions from server")
        emptyList()
    }
}

