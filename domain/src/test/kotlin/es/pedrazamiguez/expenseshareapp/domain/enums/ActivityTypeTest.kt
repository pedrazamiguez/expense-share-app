package es.pedrazamiguez.expenseshareapp.domain.enums

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ActivityTypeTest {

    @Nested
    inner class FromString {

        @ParameterizedTest
        @EnumSource(ActivityType::class)
        fun `resolves all enum entries by exact name`(type: ActivityType) {
            assertEquals(type, ActivityType.fromString(type.name))
        }

        @Test
        fun `resolves case-insensitive input`() {
            assertEquals(ActivityType.GROUP_CREATED, ActivityType.fromString("group_created"))
            assertEquals(ActivityType.EXPENSE_DELETED, ActivityType.fromString("expense_deleted"))
        }

        @Test
        fun `throws IllegalArgumentException for unknown type`() {
            assertThrows(IllegalArgumentException::class.java) {
                ActivityType.fromString("NONEXISTENT")
            }
        }
    }
}
