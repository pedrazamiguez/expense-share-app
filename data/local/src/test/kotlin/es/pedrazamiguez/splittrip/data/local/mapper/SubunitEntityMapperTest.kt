package es.pedrazamiguez.splittrip.data.local.mapper

import es.pedrazamiguez.splittrip.data.local.entity.SubunitEntity
import es.pedrazamiguez.splittrip.domain.model.Subunit
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SubunitEntityMapperTest {

    private val testSubunitId = "subunit-123"
    private val testGroupId = "group-456"
    private val testCreatedBy = "user-789"
    private val testName = "Antonio & Me"
    private val testMemberIds = listOf("user-789", "user-012")
    private val testMemberShares = mapOf("user-789" to BigDecimal("0.5"), "user-012" to BigDecimal("0.5"))
    private val testTimestamp = LocalDateTime.of(2026, 3, 13, 12, 30, 0)
    private val testTimestampMillis = testTimestamp
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()

    @Nested
    inner class ToEntity {

        private val fullSubunit = Subunit(
            id = testSubunitId,
            groupId = testGroupId,
            name = testName,
            memberIds = testMemberIds,
            memberShares = testMemberShares,
            createdBy = testCreatedBy,
            createdAt = testTimestamp,
            lastUpdatedAt = testTimestamp
        )

        @Test
        fun `maps all core fields correctly`() {
            val entity = fullSubunit.toEntity()

            assertEquals(testSubunitId, entity.id)
            assertEquals(testGroupId, entity.groupId)
            assertEquals(testName, entity.name)
            assertEquals(testMemberIds, entity.memberIds)
            assertEquals(testMemberShares, entity.memberShares)
            assertEquals(testCreatedBy, entity.createdBy)
        }

        @Test
        fun `maps timestamps correctly`() {
            val entity = fullSubunit.toEntity()

            assertEquals(testTimestampMillis, entity.createdAtMillis)
            assertEquals(testTimestampMillis, entity.lastUpdatedAtMillis)
        }

        @Test
        fun `falls back to current time when createdAt is null`() {
            val beforeMillis = System.currentTimeMillis()
            val subunitNullTimestamps = fullSubunit.copy(
                createdAt = null,
                lastUpdatedAt = null
            )

            val entity = subunitNullTimestamps.toEntity()

            val afterMillis = System.currentTimeMillis()
            assertNotNull(entity.createdAtMillis)
            assertTrue(entity.createdAtMillis!! in beforeMillis..afterMillis)
            assertEquals(entity.createdAtMillis, entity.lastUpdatedAtMillis)
        }

        @Test
        fun `uses createdAt as lastUpdatedAt fallback when lastUpdatedAt is null`() {
            val subunit = fullSubunit.copy(lastUpdatedAt = null)

            val entity = subunit.toEntity()

            assertEquals(testTimestampMillis, entity.createdAtMillis)
            assertEquals(testTimestampMillis, entity.lastUpdatedAtMillis)
        }

        @Test
        fun `handles empty memberIds and memberShares`() {
            val subunit = fullSubunit.copy(
                memberIds = emptyList(),
                memberShares = emptyMap()
            )

            val entity = subunit.toEntity()

            assertTrue(entity.memberIds.isEmpty())
            assertTrue(entity.memberShares.isEmpty())
        }
    }

    @Nested
    inner class ToDomain {

        private val fullEntity = SubunitEntity(
            id = testSubunitId,
            groupId = testGroupId,
            name = testName,
            memberIds = testMemberIds,
            memberShares = testMemberShares,
            createdBy = testCreatedBy,
            createdAtMillis = testTimestampMillis,
            lastUpdatedAtMillis = testTimestampMillis
        )

        @Test
        fun `maps all core fields correctly`() {
            val subunit = fullEntity.toDomain()

            assertEquals(testSubunitId, subunit.id)
            assertEquals(testGroupId, subunit.groupId)
            assertEquals(testName, subunit.name)
            assertEquals(testMemberIds.sorted(), subunit.memberIds)
            assertEquals(testMemberShares, subunit.memberShares)
            assertEquals(testCreatedBy, subunit.createdBy)
        }

        @Test
        fun `maps timestamps correctly`() {
            val subunit = fullEntity.toDomain()

            assertEquals(testTimestamp, subunit.createdAt)
            assertEquals(testTimestamp, subunit.lastUpdatedAt)
        }

        @Test
        fun `null timestamps map to null domain fields`() {
            val entityNullTimestamps = fullEntity.copy(
                createdAtMillis = null,
                lastUpdatedAtMillis = null
            )

            val subunit = entityNullTimestamps.toDomain()

            assertNull(subunit.createdAt)
            assertNull(subunit.lastUpdatedAt)
        }

        @Test
        fun `handles empty memberIds and memberShares`() {
            val entity = fullEntity.copy(
                memberIds = emptyList(),
                memberShares = emptyMap()
            )

            val subunit = entity.toDomain()

            assertTrue(subunit.memberIds.isEmpty())
            assertTrue(subunit.memberShares.isEmpty())
        }
    }

    @Nested
    inner class RoundTrip {

        @Test
        fun `domain to entity to domain preserves all fields`() {
            val original = Subunit(
                id = testSubunitId,
                groupId = testGroupId,
                name = testName,
                memberIds = testMemberIds,
                memberShares = testMemberShares,
                createdBy = testCreatedBy,
                createdAt = testTimestamp,
                lastUpdatedAt = testTimestamp
            )

            val roundTripped = original.toEntity().toDomain()

            assertEquals(original.id, roundTripped.id)
            assertEquals(original.groupId, roundTripped.groupId)
            assertEquals(original.name, roundTripped.name)
            assertEquals(original.memberIds.sorted(), roundTripped.memberIds)
            assertEquals(original.memberShares, roundTripped.memberShares)
            assertEquals(original.createdBy, roundTripped.createdBy)
            assertEquals(original.createdAt, roundTripped.createdAt)
            assertEquals(original.lastUpdatedAt, roundTripped.lastUpdatedAt)
        }

        @Test
        fun `list mapping preserves all elements`() {
            val subunits = listOf(
                Subunit(
                    id = "sub-1",
                    groupId = testGroupId,
                    name = "Couple A",
                    memberIds = listOf("u1", "u2"),
                    memberShares = mapOf("u1" to BigDecimal("0.6"), "u2" to BigDecimal("0.4")),
                    createdBy = "u1",
                    createdAt = testTimestamp,
                    lastUpdatedAt = testTimestamp
                ),
                Subunit(
                    id = "sub-2",
                    groupId = testGroupId,
                    name = "Family B",
                    memberIds = listOf("u3", "u4", "u5"),
                    memberShares = mapOf(
                        "u3" to BigDecimal("0.4"),
                        "u4" to BigDecimal("0.3"),
                        "u5" to BigDecimal("0.3")
                    ),
                    createdBy = "u3",
                    createdAt = testTimestamp,
                    lastUpdatedAt = testTimestamp
                )
            )

            val entities = subunits.map { it.toEntity() }
            val roundTripped = entities.toDomain()

            assertEquals(2, roundTripped.size)
            assertEquals("sub-1", roundTripped[0].id)
            assertEquals("Couple A", roundTripped[0].name)
            assertEquals("sub-2", roundTripped[1].id)
            assertEquals("Family B", roundTripped[1].name)
        }
    }
}
