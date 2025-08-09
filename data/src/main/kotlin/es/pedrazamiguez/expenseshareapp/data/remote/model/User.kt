package es.pedrazamiguez.expenseshareapp.data.remote.model

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class User(
    @PropertyName("name") val name: String = "",
    @PropertyName("email") val email: String = "",
    @PropertyName("createdAt") val createdAt: Date = Date()
)
