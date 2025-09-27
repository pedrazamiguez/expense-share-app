package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document

data class SubunitDocument(
    val subunitId: String = "",
    val groupId: String = "",
    val groupRefPath: String? = null,
    val name: String = "",
    val memberShares: Map<String, Double> = emptyMap()
)
