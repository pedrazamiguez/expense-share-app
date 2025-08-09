package es.pedrazamiguez.expenseshareapp.data.remote.model

import com.google.firebase.firestore.PropertyName

data class Subunit(
    @PropertyName("name") val name: String = ""
)
