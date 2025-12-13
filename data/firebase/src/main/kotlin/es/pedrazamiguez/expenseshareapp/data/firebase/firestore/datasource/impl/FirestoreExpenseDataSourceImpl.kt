package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.ExpenseDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toDomain
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID

class FirestoreExpenseDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val authenticationService: AuthenticationService
) : CloudExpenseDataSource {

    override suspend fun addExpense(
        groupId: String, expense: Expense
    ) {
        val userId = authenticationService.requireUserId()
        val expenseId = UUID
            .randomUUID()
            .toString()

        val groupDocRef = firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
        val expenseDocRef = groupDocRef
            .collection(ExpenseDocument.COLLECTION_PATH)
            .document(expenseId)

        val expenseDocument = expense.toDocument(
            expenseId,
            groupId,
            groupDocRef,
            userId
        )

        expenseDocRef
            .set(expenseDocument)
            .addOnFailureListener { exception ->
                Timber.w(
                    exception,
                    "Add expense failed"
                )
            }

    }

    override fun getExpensesByGroupIdFlow(groupId: String): Flow<List<Expense>> = callbackFlow {
        val expensesCollection = createExpensesCollection(groupId)

        val listener = createExpenseListener(expensesCollection) { snapshot ->
            launch {
                val cachedExpenses = loadExpensesFromCache(
                    expensesCollection,
                    snapshot.documents
                )

                trySend(cachedExpenses)

                val cachedExpenseIds = cachedExpenses
                    .map { it.id }
                    .toSet()
                val missingExpenseIds = snapshot.documents
                    .map { it.id }
                    .filter { it !in cachedExpenseIds }

                if (missingExpenseIds.isNotEmpty()) {
                    val serverExpenses = loadExpensesFromServer(
                        expensesCollection,
                        missingExpenseIds
                    )
                    val allExpenses = (cachedExpenses + serverExpenses).sortedByDescending {
                        it.createdAt ?: it.lastUpdatedAt
                    }
                    trySend(allExpenses)
                }
            }
        }

        awaitClose { listener.remove() }
    }

    private fun createExpensesCollection(groupId: String) = firestore
        .collection(GroupDocument.COLLECTION_PATH)
        .document(groupId)
        .collection(ExpenseDocument.COLLECTION_PATH)

    private fun createExpenseListener(
        expensesCollection: CollectionReference, onUpdate: (QuerySnapshot) -> Unit
    ) = expensesCollection.addSnapshotListener { snapshot, error ->
        if (error != null) {
            Timber.e(
                error,
                "Error listening to expenses"
            )
            return@addSnapshotListener
        }

        snapshot?.let(onUpdate)
    }

    private suspend fun loadExpensesFromCache(
        expensesCollection: CollectionReference, documents: List<DocumentSnapshot>
    ): List<Expense> = documents
        .mapNotNull { doc ->
            loadSingleExpenseFromCache(
                expensesCollection,
                doc.id
            )
        }
        .sortedByDescending { it.createdAt ?: it.lastUpdatedAt }

    private suspend fun loadSingleExpenseFromCache(
        expensesCollection: CollectionReference, expenseId: String
    ): Expense? = try {
        @Suppress("kotlin:S6518")
        val cachedDoc = expensesCollection
            .document(expenseId)
            .get(Source.CACHE)
            .await()

        if (cachedDoc.exists()) {
            cachedDoc
                .toObject(ExpenseDocument::class.java)
                ?.toDomain()
        } else null
    } catch (_: Exception) {
        Timber.d("Cache miss for expense $expenseId")
        null
    }

    private suspend fun loadExpensesFromServer(
        expensesCollection: CollectionReference, expenseIds: List<String>
    ): List<Expense> {
        return try {
            expensesCollection
                .whereIn(
                    FieldPath.documentId(),
                    expenseIds
                )
                .get()
                .await().documents.mapNotNull {
                    it
                        .toObject(ExpenseDocument::class.java)
                        ?.toDomain()
                }
        } catch (e: Exception) {
            Timber.e(
                e,
                "Failed to load expenses from server"
            )
            emptyList()
        }
    }

}
