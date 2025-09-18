package es.pedrazamiguez.expenseshareapp.data.source.remote.model

import com.google.firebase.firestore.PropertyName
import es.pedrazamiguez.expenseshareapp.data.source.remote.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.data.source.remote.enums.Currency

data class AddOn(
    @PropertyName("type") val type: AddOnType = AddOnType.TIP,
    @PropertyName("amount") val amount: Double = 0.0,
    @PropertyName("currency") val currency: Currency = Currency.EUR,
    @PropertyName("exchangeRate") val exchangeRate: Double = 1.0
)
