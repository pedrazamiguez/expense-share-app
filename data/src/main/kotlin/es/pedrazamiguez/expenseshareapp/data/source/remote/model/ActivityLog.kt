package es.pedrazamiguez.expenseshareapp.data.source.remote.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import es.pedrazamiguez.expenseshareapp.data.source.remote.enums.ActivityType

data class ActivityLog(
    @PropertyName("activityId") val activityId: String = "",
    @PropertyName("type") val type: ActivityType = ActivityType.UNKNOWN,
    @PropertyName("byUserId") val byUserId: String = "",
    @PropertyName("onGroupId") val onGroupId: String = "",
    @PropertyName("targetExpenseId") val targetExpenseId: String = "",
    @PropertyName("loggedAt") val loggedAt: Timestamp = Timestamp.now()
)
