package es.pedrazamiguez.splittrip.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contributions",
    indices = [
        Index(value = ["groupId"]),
        Index(value = ["groupId", "linkedExpenseId"]),
        Index(value = ["groupId", "syncStatus"])
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
    @ColumnInfo(defaultValue = "null")
    val linkedSettlementId: String? = null,
    val amount: Long,
    val currency: String,
    @ColumnInfo(defaultValue = "null")
    val equivalentBaseAmount: Long? = null,
    @ColumnInfo(defaultValue = "null")
    val exchangeRate: String? = null,
    @ColumnInfo(defaultValue = "null")
    val contributionDateMillis: Long? = null,
    val createdAtMillis: Long?,
    val lastUpdatedAtMillis: Long?,
    val syncStatus: String = "SYNCED"
)
