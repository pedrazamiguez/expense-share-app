package es.pedrazamiguez.expenseshareapp.data.remote.model

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class Group(
    @PropertyName("groupId") val groupId: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("description") val description: String = "",
    @PropertyName("currencyCode") val currencyCode: String = "",
    @PropertyName("mainImagePath") val mainImagePath: String = "",
    @PropertyName("memberIds") val memberIds: List<String> = emptyList(),
    @PropertyName("subgroupIds") val subgroupIds: List<String>? = null,
    @PropertyName("createdBy") val createdBy: String = "",
    @PropertyName("createdAt") val createdAt: Date = Date(),
    @PropertyName("lastUpdatedBy") val lastUpdatedBy: String = "",
    @PropertyName("lastUpdatedAt") val lastUpdatedAt: Date = Date()
)
