package es.pedrazamiguez.splittrip.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "membership_removal_events",
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
data class MembershipRemovalEventEntity(
    @PrimaryKey
    val id: String,
    val groupId: String,
    val userId: String,
    val createdAtMillis: Long,
    val processed: Boolean = false,
    val syncStatus: String = "SYNCED"
)
