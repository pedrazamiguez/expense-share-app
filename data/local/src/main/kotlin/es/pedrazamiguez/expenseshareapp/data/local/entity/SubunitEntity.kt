package es.pedrazamiguez.expenseshareapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "subunits",
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
data class SubunitEntity(
    @PrimaryKey
    val id: String,
    val groupId: String,
    val name: String,
    val memberIds: List<String>,
    val memberShares: Map<String, BigDecimal>,
    val createdBy: String,
    val createdAtMillis: Long?,
    val lastUpdatedAtMillis: Long?
)
