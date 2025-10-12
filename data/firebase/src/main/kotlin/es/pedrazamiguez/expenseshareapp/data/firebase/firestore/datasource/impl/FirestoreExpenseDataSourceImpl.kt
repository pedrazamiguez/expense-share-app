package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.ExpenseDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toDomain
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
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

class FirestoreExpenseDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val authenticationService: AuthenticationService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CloudExpenseDataSource {

    override suspend fun addExpense(
        groupId: String,
        expense: Expense
    ) {

        val userId = authenticationService.requireUserId()
        val expenseId = UUID.randomUUID().toString()

        val groupsCollection = firestore.collection(GroupDocument.COLLECTION_PATH)
        val groupDocRef = groupsCollection.document(groupId)

        val expensesCollection = groupDocRef.collection(ExpenseDocument.COLLECTION_PATH)
        val expenseDocRef = expensesCollection.document(expenseId)

        val expenseDocument = expense.toDocument(
            expenseId,
            groupId,
            groupDocRef,
            userId
        )

        expenseDocRef.set(expenseDocument).addOnFailureListener { exception ->
            Timber.w(
                exception,
                "Add expense failed"
            )
        }
    }

    override fun getExpensesByGroupIdFlow(groupId: String): Flow<List<Expense>> = callbackFlow {
        val groupsCollection = firestore.collection(GroupDocument.COLLECTION_PATH)
        val groupDocRef = groupsCollection.document(groupId)
        val expensesCollection = groupDocRef.collection(ExpenseDocument.COLLECTION_PATH)

        val listener = expensesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                launch(ioDispatcher) {
                    try {
                        // First, try to get all expenses from cache only
                        val cachedExpenses = snapshot.documents.mapNotNull { doc ->
                            try {
                                @Suppress("kotlin:S6518") // Sonar warning doesn't apply to Firestore API
                                val cachedDoc = expensesCollection.document(doc.id).get(Source.CACHE).await()

                                if (cachedDoc.exists()) {
                                    cachedDoc.toObject(ExpenseDocument::class.java)?.toDomain()
                                } else null
                            } catch (e: Exception) {
                                null // Cache miss, ignore for now
                            }
                        }.sortedByDescending { it.id } // Sort by expense ID or you can add a timestamp field

                        // Send cached results immediately
                        trySend(cachedExpenses).isSuccess

                        // Then fetch missing expenses from server in background
                        val cachedExpenseIds = cachedExpenses.map { it.id }.toSet()
                        val missingExpenseIds = snapshot.documents.map { it.id }.filter { it !in cachedExpenseIds }

                        if (missingExpenseIds.isNotEmpty()) {
                            // Fire off server requests without waiting - they'll update cache for next time
                            missingExpenseIds.forEach { expenseId ->
                                launch {
                                    try {
                                        expensesCollection.document(expenseId).get()
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
