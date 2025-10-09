package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import com.google.firebase.firestore.FirebaseFirestore
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.ExpenseDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper.toDocument
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreExpenseDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val authenticationService: AuthenticationService,
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

        expenseDocRef.set(expenseDocument).await()

    }

}
