package es.pedrazamiguez.expenseshareapp.data.remote.model

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class ActivityLog(
    @PropertyName("type") val type: String = "",
    @PropertyName("byUserId") val byUserId: String = "",
    @PropertyName("targetExpenseId") val targetExpenseId: String = "",
    @PropertyName("timestamp") val timestamp: Date = Date()
)
