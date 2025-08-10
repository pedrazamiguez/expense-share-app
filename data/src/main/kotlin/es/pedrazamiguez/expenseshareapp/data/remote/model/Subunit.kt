package es.pedrazamiguez.expenseshareapp.data.remote.model

import com.google.firebase.firestore.PropertyName

data class Subunit(
    @PropertyName("subunitId") val subunitId: String = "",
    @PropertyName("groupId") val groupId: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("memberShares") val memberShares: Map<String, Double> = emptyMap()
)
