package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.enums.Currency

data class Group(
    @PropertyName("groupId") val groupId: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("description") val description: String = "",
    @PropertyName("currency") val currency: Currency = Currency.EUR,
    @PropertyName("mainImagePath") val mainImagePath: String = "",
    @PropertyName("memberIds") val memberIds: List<String> = emptyList(),
    @PropertyName("subgroupIds") val subgroupIds: List<String>? = null,
    @PropertyName("createdBy") val createdBy: String = "",
    @PropertyName("createdAt") val createdAt: Timestamp = Timestamp.now(),
    @PropertyName("lastUpdatedBy") val lastUpdatedBy: String = "",
    @PropertyName("lastUpdatedAt") val lastUpdatedAt: Timestamp = Timestamp.now()
)
