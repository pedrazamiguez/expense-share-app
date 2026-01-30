package es.pedrazamiguez.expenseshareapp.data.local.mapper

import es.pedrazamiguez.expenseshareapp.data.local.entity.GroupEntity
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Maps GroupEntity (Room) to Group (Domain).
 */
fun GroupEntity.toDomain(): Group = Group(
    id = id,
    name = name,
    description = description ?: "",
    currency = currencyCode,
    extraCurrencies = extraCurrencies,
    members = memberIds,
    mainImagePath = mainImagePath,
    createdAt = createdAtMillis?.toLocalDateTime(),
    lastUpdatedAt = lastUpdatedAtMillis?.toLocalDateTime()
)

/**
 * Maps Group (Domain) to GroupEntity (Room).
 */
fun Group.toEntity(): GroupEntity = GroupEntity(
    id = id,
    name = name,
    description = description.ifBlank { null },
    currencyCode = currency,
    extraCurrencies = extraCurrencies,
    memberIds = members,
    mainImagePath = mainImagePath,
    createdAtMillis = createdAt?.toEpochMillis(),
    lastUpdatedAtMillis = lastUpdatedAt?.toEpochMillis()
)

/**
 * Maps a list of GroupEntity to a list of Group.
 */
fun List<GroupEntity>.toDomain(): List<Group> = map { it.toDomain() }

/**
 * Maps a list of Group to a list of GroupEntity.
 */
fun List<Group>.toEntity(): List<GroupEntity> = map { it.toEntity() }

// Extension functions for date conversion
private fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

private fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
