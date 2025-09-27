package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.model

import com.google.firebase.firestore.PropertyName

data class SubunitMemberDocument(
    @PropertyName("subunitId") val subunitId: String = "",
    @PropertyName("userId") val userId: String = ""
)
