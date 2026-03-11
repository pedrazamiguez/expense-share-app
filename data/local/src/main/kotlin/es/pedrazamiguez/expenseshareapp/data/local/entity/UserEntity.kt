package es.pedrazamiguez.expenseshareapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val email: String,
    val displayName: String?,
    val profileImagePath: String?,
    val lastUpdatedAtMillis: Long?
)

