package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.model

import com.google.firebase.firestore.PropertyName
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.enums.Currency

data class AddOn(
    @PropertyName("type") val type: AddOnType = AddOnType.TIP,
    @PropertyName("amount") val amount: Double = 0.0,
    @PropertyName("currency") val currency: Currency = Currency.EUR,
    @PropertyName("exchangeRate") val exchangeRate: Double = 1.0
)
