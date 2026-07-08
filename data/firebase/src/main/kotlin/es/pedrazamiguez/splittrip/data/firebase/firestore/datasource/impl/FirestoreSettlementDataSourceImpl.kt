package es.pedrazamiguez.splittrip.data.firebase.firestore.datasource.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import es.pedrazamiguez.splittrip.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.splittrip.data.firebase.firestore.mapper.toDocument
import es.pedrazamiguez.splittrip.data.firebase.firestore.mapper.toSettlementRecord
import es.pedrazamiguez.splittrip.domain.datasource.cloud.CloudSettlementDataSource
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirestoreSettlementDataSourceImpl(
    private val firestore: FirebaseFirestore
) : CloudSettlementDataSource {

    override fun getSettlementsByGroupIdFlow(groupId: String): Flow<List<SettlementRecord>> =
        callbackFlow {
            val collectionRef = firestore
                .collection(GroupDocument.COLLECTION_PATH)
                .document(groupId)
                .collection(SETTLEMENTS_COLLECTION)

            val listener = collectionRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to settlements for group $groupId")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val records = snapshot.documents.mapNotNull { it.toSettlementRecord() }
                    trySend(records)
                }
            }

            awaitClose { listener.remove() }
        }

    override suspend fun upsertSettlement(groupId: String, record: SettlementRecord) {
        val docRef = firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
            .collection(SETTLEMENTS_COLLECTION)
            .document(record.id)

        docRef.set(record.toDocument(), SetOptions.merge()).await()
    }

    override suspend fun deleteSettlement(groupId: String, id: String) {
        firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
            .collection(SETTLEMENTS_COLLECTION)
            .document(id)
            .delete()
            .await()
    }

    override suspend fun verifySettlementOnServer(groupId: String, id: String): Boolean {
        val doc = firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
            .collection(SETTLEMENTS_COLLECTION)
            .document(id)
            .get(Source.SERVER)
            .await()
        return doc.exists()
    }

    private companion object {
        const val SETTLEMENTS_COLLECTION = "settlements"
    }
}
