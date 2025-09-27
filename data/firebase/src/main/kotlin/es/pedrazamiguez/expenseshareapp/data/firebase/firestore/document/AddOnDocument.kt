package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document

data class AddOnDocument(
    val type: String = "TIP",
    val amountCents: Long = 0L,
    val currency: String = "EUR",
    val exchangeRate: Double? = null
)
