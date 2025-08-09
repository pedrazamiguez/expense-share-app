package es.pedrazamiguez.expenseshareapp.data.remote.model

import com.google.firebase.firestore.PropertyName

data class ExpenseSplit(
    @PropertyName("userId") val userId: String = "",
    @PropertyName("amount") val amount: Double = 0.0,
    @PropertyName("percentage") val percentage: Double? = null,
    @PropertyName("isExcluded") val isExcluded: Boolean = false,
    @PropertyName("isCoveredBy") val isCoveredBy: String? = null
)
