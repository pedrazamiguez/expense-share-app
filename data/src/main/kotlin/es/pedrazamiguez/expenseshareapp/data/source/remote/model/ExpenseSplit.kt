package es.pedrazamiguez.expenseshareapp.data.source.remote.model

import com.google.firebase.firestore.PropertyName

data class ExpenseSplit(
    @PropertyName("userId") val userId: String = "",
    @PropertyName("subunitId") val subunitId: String? = null,
    @PropertyName("amount") val amount: Double = 0.0,
    @PropertyName("percentage") val percentage: Double = 0.0,
    @PropertyName("isExcluded") val isExcluded: Boolean = false,
    @PropertyName("isCoveredBy") val isCoveredBy: String? = null
)
