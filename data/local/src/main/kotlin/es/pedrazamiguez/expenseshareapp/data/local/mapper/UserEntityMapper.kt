package es.pedrazamiguez.expenseshareapp.data.local.mapper

import es.pedrazamiguez.expenseshareapp.data.local.entity.UserEntity
import es.pedrazamiguez.expenseshareapp.domain.model.User

fun UserEntity.toDomain(): User = User(
    userId = userId,
    email = email,
    displayName = displayName,
    profileImagePath = profileImagePath
)

fun User.toEntity(): UserEntity = UserEntity(
    userId = userId,
    email = email,
    displayName = displayName,
    profileImagePath = profileImagePath,
    lastUpdatedAtMillis = System.currentTimeMillis()
)

fun List<UserEntity>.toDomain(): List<User> = map { it.toDomain() }
fun List<User>.toEntities(): List<UserEntity> = map { it.toEntity() }

