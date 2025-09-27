package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class User(
    @PropertyName("userId") val userId: String = "",
    @PropertyName("username") val username: String = "",
    @PropertyName("email") val email: String = "",
    @PropertyName("displayName") val displayName: String? = null,
    @PropertyName("profileImagePath") val profileImagePath: String? = null,
    @PropertyName("createdBy") val createdBy: String = "",
    @PropertyName("createdAt") val createdAt: Timestamp = Timestamp.now(),
    @PropertyName("lastUpdatedBy") val lastUpdatedBy: String = "",
    @PropertyName("lastUpdatedAt") val lastUpdatedAt: Timestamp = Timestamp.now()
)
