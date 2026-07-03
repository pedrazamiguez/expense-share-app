package es.pedrazamiguez.splittrip.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "settlement_records",
    indices = [
        Index("groupId"),
        Index("fromUserId"),
        Index("toUserId")
    ]
)
data class SettlementRecordEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val fromUserId: String,
    val toUserId: String,
    val amountCents: Long,
    val currency: String,
    val sourcePocket: String,
    val status: String,
    val syncStatus: String,
    val createdAt: Long,
    val confirmedByPayerAt: Long?,
    val confirmedByPayeeAt: Long?,
    val resolvedAt: Long?,
    val disputedBy: String? = null,
    val disputeReason: String? = null
)
