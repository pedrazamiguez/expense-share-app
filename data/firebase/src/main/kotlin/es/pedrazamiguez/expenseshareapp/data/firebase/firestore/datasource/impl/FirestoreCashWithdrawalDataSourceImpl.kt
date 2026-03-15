package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.CashWithdrawalDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toDomain
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudCashWithdrawalDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirestoreCashWithdrawalDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val authenticationService: AuthenticationService
) : CloudCashWithdrawalDataSource {

    override suspend fun addWithdrawal(groupId: String, withdrawal: CashWithdrawal) {
        val userId = authenticationService.requireUserId()
        val withdrawalId = withdrawal.id

        val groupDocRef = firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
        val withdrawalDocRef = groupDocRef
            .collection(CashWithdrawalDocument.COLLECTION_PATH)
            .document(withdrawalId)

        val withdrawalDocument = withdrawal.toDocument(
            withdrawalId,
            groupId,
            groupDocRef,
            userId
        )

        withdrawalDocRef
            .set(withdrawalDocument)
            .await()
    }

    override suspend fun updateWithdrawal(groupId: String, withdrawal: CashWithdrawal) {
        val userId = authenticationService.requireUserId()

        val groupDocRef = firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
        val withdrawalDocRef = groupDocRef
            .collection(CashWithdrawalDocument.COLLECTION_PATH)
            .document(withdrawal.id)

        val withdrawalDocument = withdrawal.toDocument(
            withdrawal.id,
            groupId,
            groupDocRef,
            userId
        )

        withdrawalDocRef
            .set(withdrawalDocument)
            .await()
    }

    override suspend fun deleteWithdrawal(groupId: String, withdrawalId: String) {
        firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
            .collection(CashWithdrawalDocument.COLLECTION_PATH)
            .document(withdrawalId)
            .delete()
            .await()
    }

    override suspend fun fetchWithdrawalsByGroupId(groupId: String): List<CashWithdrawal> {
        val snapshot = firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
            .collection(CashWithdrawalDocument.COLLECTION_PATH)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(CashWithdrawalDocument::class.java)?.toDomain()
        }.sortedByDescending { it.createdAt ?: it.lastUpdatedAt }
    }

    override fun getWithdrawalsByGroupIdFlow(groupId: String): Flow<List<CashWithdrawal>> = callbackFlow {
        val withdrawalsCollection = createWithdrawalsCollection(groupId)

        val listener = createWithdrawalListener(withdrawalsCollection) { snapshot ->
            launch {
                val cachedWithdrawals = loadWithdrawalsFromCache(
                    withdrawalsCollection,
                    snapshot.documents
                )

                trySend(cachedWithdrawals)

                val cachedIds = cachedWithdrawals.map { it.id }.toSet()
                val missingIds = snapshot.documents
                    .map { it.id }
                    .filter { it !in cachedIds }

                if (missingIds.isNotEmpty()) {
                    val serverWithdrawals = loadWithdrawalsFromServer(
                        withdrawalsCollection,
                        missingIds
                    )
                    val allWithdrawals =
                        (cachedWithdrawals + serverWithdrawals).sortedByDescending {
                            it.createdAt ?: it.lastUpdatedAt
                        }
                    trySend(allWithdrawals)
                }
            }
        }

        awaitClose { listener.remove() }
    }

    private fun createWithdrawalsCollection(groupId: String) = firestore
        .collection(GroupDocument.COLLECTION_PATH)
        .document(groupId)
        .collection(CashWithdrawalDocument.COLLECTION_PATH)

    private fun createWithdrawalListener(
        withdrawalsCollection: CollectionReference,
        onUpdate: (QuerySnapshot) -> Unit
    ) = withdrawalsCollection.addSnapshotListener { snapshot, error ->
        if (error != null) {
            Timber.e(error, "Error listening to cash withdrawals")
            return@addSnapshotListener
        }
        snapshot?.let(onUpdate)
    }

    private suspend fun loadWithdrawalsFromCache(
        withdrawalsCollection: CollectionReference,
        documents: List<DocumentSnapshot>
    ): List<CashWithdrawal> = documents
        .mapNotNull { doc ->
            loadSingleWithdrawalFromCache(withdrawalsCollection, doc.id)
        }
        .sortedByDescending { it.createdAt ?: it.lastUpdatedAt }

    @Suppress("kotlin:S6518")
    private suspend fun loadSingleWithdrawalFromCache(
        withdrawalsCollection: CollectionReference,
        withdrawalId: String
    ): CashWithdrawal? = try {
        val cachedDoc = withdrawalsCollection
            .document(withdrawalId)
            .get(Source.CACHE)
            .await()

        if (cachedDoc.exists()) {
            cachedDoc.toObject(CashWithdrawalDocument::class.java)?.toDomain()
        } else {
            null
        }
    } catch (e: Exception) {
        Timber.d("Cache miss for cash withdrawal $withdrawalId, will load from server")
        null
    }

    private suspend fun loadWithdrawalsFromServer(
        withdrawalsCollection: CollectionReference,
        missingIds: List<String>
    ): List<CashWithdrawal> = try {
        missingIds
            .chunked(FIRESTORE_WHERE_IN_LIMIT)
            .flatMap { batch ->
                withdrawalsCollection
                    .whereIn(FieldPath.documentId(), batch)
                    .get(Source.SERVER)
                    .await()
                    .documents
                    .mapNotNull { it.toObject(CashWithdrawalDocument::class.java)?.toDomain() }
            }
    } catch (e: Exception) {
        Timber.w(e, "Failed to load cash withdrawals from server")
        emptyList()
    }

    private companion object {
        const val FIRESTORE_WHERE_IN_LIMIT = 30
    }
}
