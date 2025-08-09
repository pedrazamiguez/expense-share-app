package es.pedrazamiguez.expenseshareapp.data.remote.model

import com.google.firebase.firestore.PropertyName

data class SubunitMember(
    @PropertyName("userId") val userId: String = ""
)
