package es.pedrazamiguez.splittrip.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus

@Entity(tableName = "cash_transfers")
data class CashTransferEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val fromUserId: String,
    val toUserId: String,
    val amountCents: Long,
    val currency: String,
    val equivalentBaseAmountCents: Long,
    val createdAt: Long,
    val syncStatus: SyncStatus
)
