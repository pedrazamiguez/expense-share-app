package es.pedrazamiguez.splittrip.data.local.mapper

import es.pedrazamiguez.splittrip.data.local.entity.GroupEntity
import es.pedrazamiguez.splittrip.domain.model.Group
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GroupEntityMapperTest {

    private val testTimestamp = LocalDateTime.of(2026, 3, 15, 10, 30, 0)
    private val testTimestampMillis = testTimestamp
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()

    private val fullEntity = GroupEntity(
        id = "grp-1",
        name = "Trip to Paris",
        description = "Summer 2026",
        currencyCode = "EUR",
        extraCurrencies = listOf("USD", "GBP"),
        memberIds = listOf("charlie", "alice", "bob"),
        mainImagePath = "images/paris.jpg",
        createdAtMillis = testTimestampMillis,
        lastUpdatedAtMillis = testTimestampMillis
    )

    @Nested
    inner class ToDomain {

        @Test
        fun `maps all core fields correctly`() {
            val group = fullEntity.toDomain()

            assertEquals("grp-1", group.id)
            assertEquals("Trip to Paris", group.name)
            assertEquals("Summer 2026", group.description)
            assertEquals("EUR", group.currency)
            assertEquals(listOf("USD", "GBP"), group.extraCurrencies)
            assertEquals("images/paris.jpg", group.mainImagePath)
        }

        @Test
        fun `null description maps to empty string`() {
            val entity = fullEntity.copy(description = null)
            val group = entity.toDomain()
            assertEquals("", group.description)
        }

        @Test
        fun `members are sorted alphabetically`() {
            val group = fullEntity.toDomain()
            assertEquals(listOf("alice", "bob", "charlie"), group.members)
        }

        @Test
        fun `maps timestamps correctly`() {
            val group = fullEntity.toDomain()
            assertEquals(testTimestamp, group.createdAt)
            assertEquals(testTimestamp, group.lastUpdatedAt)
        }

        @Test
        fun `null timestamps map to null`() {
            val entity = fullEntity.copy(createdAtMillis = null, lastUpdatedAtMillis = null)
            val group = entity.toDomain()
            assertNull(group.createdAt)
            assertNull(group.lastUpdatedAt)
        }
    }

    @Nested
    inner class ToEntity {

        private val fullGroup = Group(
            id = "grp-1",
            name = "Trip to Paris",
            description = "Summer 2026",
            currency = "EUR",
            extraCurrencies = listOf("USD"),
            members = listOf("alice", "bob"),
            mainImagePath = null,
            createdAt = testTimestamp,
            lastUpdatedAt = testTimestamp
        )

        @Test
        fun `maps all core fields correctly`() {
            val entity = fullGroup.toEntity()

            assertEquals("grp-1", entity.id)
            assertEquals("Trip to Paris", entity.name)
            assertEquals("Summer 2026", entity.description)
            assertEquals("EUR", entity.currencyCode)
        }

        @Test
        fun `blank description maps to null`() {
            val group = fullGroup.copy(description = "")
            val entity = group.toEntity()
            assertNull(entity.description)
        }

        @Test
        fun `whitespace description maps to null`() {
            val group = fullGroup.copy(description = "   ")
            val entity = group.toEntity()
            assertNull(entity.description)
        }

        @Test
        fun `maps timestamps to millis`() {
            val entity = fullGroup.toEntity()
            assertEquals(testTimestampMillis, entity.createdAtMillis)
            assertEquals(testTimestampMillis, entity.lastUpdatedAtMillis)
        }

        @Test
        fun `null timestamps map to null millis`() {
            val group = fullGroup.copy(createdAt = null, lastUpdatedAt = null)
            val entity = group.toEntity()
            assertNull(entity.createdAtMillis)
            assertNull(entity.lastUpdatedAtMillis)
        }
    }

    @Nested
    inner class ListExtensions {

        @Test
        fun `toDomain maps list of entities`() {
            val entities = listOf(fullEntity, fullEntity.copy(id = "grp-2"))
            val groups = entities.toDomain()
            assertEquals(2, groups.size)
            assertEquals("grp-2", groups[1].id)
        }

        @Test
        fun `toEntity maps list of domain objects`() {
            val group = fullEntity.toDomain()
            val entities = listOf(group, group.copy(id = "grp-2")).toEntity()
            assertEquals(2, entities.size)
            assertEquals("grp-2", entities[1].id)
        }
    }
}
