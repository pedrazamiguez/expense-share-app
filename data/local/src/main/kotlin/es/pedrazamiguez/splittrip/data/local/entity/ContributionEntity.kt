package es.pedrazamiguez.splittrip.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contributions",
    indices = [
        Index(value = ["groupId"]),
        Index(value = ["groupId", "linkedExpenseId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ContributionEntity(
    @PrimaryKey
    val id: String,
    val groupId: String,
    val userId: String,
    val createdBy: String = "",
    val contributionScope: String = "USER",
    val subunitId: String? = null,
    val linkedExpenseId: String? = null,
    val amount: Long,
    val currency: String,
    val createdAtMillis: Long?,
    val lastUpdatedAtMillis: Long?
)
