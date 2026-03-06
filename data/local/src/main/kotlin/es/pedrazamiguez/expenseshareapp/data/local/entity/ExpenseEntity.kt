package es.pedrazamiguez.expenseshareapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["groupId"])],
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val groupId: String,
    val title: String,
    val sourceAmount: Long,
    val sourceCurrency: String,
    val sourceTipAmount: Long,
    val sourceFeeAmount: Long,
    val groupAmount: Long,
    val groupCurrency: String,
    val exchangeRate: Double,
    val category: String? = null,
    val vendor: String? = null,
    val paymentMethod: String,
    val paymentStatus: String? = null,
    val dueDateMillis: Long? = null,
    val receiptLocalUri: String? = null,
    val createdBy: String,
    val payerType: String,
    val createdAtMillis: Long?,
    val lastUpdatedAtMillis: Long?,
    val cashTranchesJson: String? = null
)
