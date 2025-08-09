package es.pedrazamiguez.expenseshareapp.data.remote.model

import com.google.firebase.firestore.PropertyName

data class GroupMember(
    @PropertyName("role") val role: String = "admin",
    @PropertyName("alias") val alias: String? = null
)
