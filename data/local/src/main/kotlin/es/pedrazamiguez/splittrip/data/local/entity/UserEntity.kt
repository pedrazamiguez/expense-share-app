package es.pedrazamiguez.splittrip.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val email: String,
    val displayName: String?,
    val profileImagePath: String?,
    val createdAtMillis: Long?,
    val lastUpdatedAtMillis: Long?,
    val bio: String? = null,
    val syncStatus: String = "SYNCED",
    val isPending: Boolean = false,
    val timezone: String? = null,
    val preferredReminderTime: String? = null,
    @ColumnInfo(defaultValue = "FREE")
    val tier: String = "FREE"
)
