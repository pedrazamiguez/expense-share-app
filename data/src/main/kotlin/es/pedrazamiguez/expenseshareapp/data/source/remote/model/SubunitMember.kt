package es.pedrazamiguez.expenseshareapp.data.source.remote.model

import com.google.firebase.firestore.PropertyName

data class SubunitMember(
    @PropertyName("subunitId") val subunitId: String = "",
    @PropertyName("userId") val userId: String = ""
)
