package es.pedrazamiguez.splittrip.data.local.mapper

import es.pedrazamiguez.splittrip.core.common.extensions.toEpochMillisUtc
import es.pedrazamiguez.splittrip.core.common.extensions.toLocalDateTimeUtc
import es.pedrazamiguez.splittrip.data.local.entity.UserEntity
import es.pedrazamiguez.splittrip.domain.model.User

fun UserEntity.toDomain(): User = User(
    userId = userId,
    email = email,
    displayName = displayName,
    profileImagePath = profileImagePath,
    createdAt = createdAtMillis?.toLocalDateTimeUtc()
)

fun User.toEntity(): UserEntity = UserEntity(
    userId = userId,
    email = email,
    displayName = displayName,
    profileImagePath = profileImagePath,
    createdAtMillis = createdAt?.toEpochMillisUtc(),
    lastUpdatedAtMillis = System.currentTimeMillis()
)

fun List<UserEntity>.toDomain(): List<User> = map { it.toDomain() }
fun List<User>.toEntities(): List<UserEntity> = map { it.toEntity() }
