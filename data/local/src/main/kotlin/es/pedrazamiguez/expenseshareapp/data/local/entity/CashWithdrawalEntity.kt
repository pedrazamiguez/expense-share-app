package es.pedrazamiguez.expenseshareapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cash_withdrawals",
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
data class CashWithdrawalEntity(
    @PrimaryKey
    val id: String,
    val groupId: String,
    val withdrawnBy: String,
    val amountWithdrawn: Long,
    val remainingAmount: Long,
    val currency: String,
    val deductedBaseAmount: Long,
    val exchangeRate: Double,
    val createdAtMillis: Long?,
    val lastUpdatedAtMillis: Long?
)

