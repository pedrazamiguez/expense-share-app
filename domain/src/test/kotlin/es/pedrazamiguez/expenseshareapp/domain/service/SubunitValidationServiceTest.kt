package es.pedrazamiguez.expenseshareapp.domain.service

import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("SubunitValidationService")
class SubunitValidationServiceTest {

    private val service = SubunitValidationService()

    private val groupMemberIds = listOf("user-1", "user-2", "user-3", "user-4")

    // ── EMPTY_NAME ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("EMPTY_NAME validation")
    inner class EmptyName {

        @Test
        fun `returns EMPTY_NAME when name is blank`() {
            val subunit = Subunit(
                name = "",
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf("user-1" to 0.5, "user-2" to 0.5)
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Invalid)
            assertEquals(
                SubunitValidationService.ValidationError.EMPTY_NAME,
                (result as SubunitValidationService.ValidationResult.Invalid).error
            )
        }

        @Test
        fun `returns EMPTY_NAME when name is whitespace only`() {
            val subunit = Subunit(
                name = "   ",
                memberIds = listOf("user-1"),
                memberShares = mapOf("user-1" to 1.0)
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Invalid)
            assertEquals(
                SubunitValidationService.ValidationError.EMPTY_NAME,
                (result as SubunitValidationService.ValidationResult.Invalid).error
            )
        }
    }

    // ── NO_MEMBERS ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("NO_MEMBERS validation")
    inner class NoMembers {

        @Test
        fun `returns NO_MEMBERS when memberIds is empty`() {
            val subunit = Subunit(
                name = "Couple",
                memberIds = emptyList(),
                memberShares = emptyMap()
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Invalid)
            assertEquals(
                SubunitValidationService.ValidationError.NO_MEMBERS,
                (result as SubunitValidationService.ValidationResult.Invalid).error
            )
        }
    }

    // ── MEMBER_NOT_IN_GROUP ───────────────────────────────────────────────────

    @Nested
    @DisplayName("MEMBER_NOT_IN_GROUP validation")
    inner class MemberNotInGroup {

        @Test
        fun `returns MEMBER_NOT_IN_GROUP when member is not in the group`() {
            val subunit = Subunit(
                name = "Couple",
                memberIds = listOf("user-1", "outsider"),
                memberShares = mapOf("user-1" to 0.5, "outsider" to 0.5)
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Invalid)
            assertEquals(
                SubunitValidationService.ValidationError.MEMBER_NOT_IN_GROUP,
                (result as SubunitValidationService.ValidationResult.Invalid).error
            )
        }

        @Test
        fun `returns MEMBER_NOT_IN_GROUP when all members are outsiders`() {
            val subunit = Subunit(
                name = "Strangers",
                memberIds = listOf("outsider-1", "outsider-2"),
                memberShares = mapOf("outsider-1" to 0.5, "outsider-2" to 0.5)
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Invalid)
            assertEquals(
                SubunitValidationService.ValidationError.MEMBER_NOT_IN_GROUP,
                (result as SubunitValidationService.ValidationResult.Invalid).error
            )
        }
    }

    // ── MEMBER_ALREADY_IN_SUBUNIT ─────────────────────────────────────────────

    @Nested
    @DisplayName("MEMBER_ALREADY_IN_SUBUNIT validation")
    inner class MemberAlreadyInSubunit {

        @Test
        fun `returns MEMBER_ALREADY_IN_SUBUNIT when member is in another sub-unit`() {
            val existingSubunit = Subunit(
                id = "existing-1",
                name = "Existing Couple",
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf("user-1" to 0.5, "user-2" to 0.5)
            )
            val newSubunit = Subunit(
                name = "New Pair",
                memberIds = listOf("user-2", "user-3"),
                memberShares = mapOf("user-2" to 0.5, "user-3" to 0.5)
            )

            val result = service.validate(newSubunit, groupMemberIds, listOf(existingSubunit))

            assertTrue(result is SubunitValidationService.ValidationResult.Invalid)
            assertEquals(
                SubunitValidationService.ValidationError.MEMBER_ALREADY_IN_SUBUNIT,
                (result as SubunitValidationService.ValidationResult.Invalid).error
            )
        }

        @Test
        fun `skips self when excludeSubunitId is provided during update`() {
            val existingSubunit = Subunit(
                id = "subunit-1",
                name = "Couple",
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf("user-1" to 0.5, "user-2" to 0.5)
            )

            val result = service.validate(
                subunit = existingSubunit,
                groupMemberIds = groupMemberIds,
                existingSubunits = listOf(existingSubunit),
                excludeSubunitId = "subunit-1"
            )

            assertTrue(result is SubunitValidationService.ValidationResult.Valid)
        }
    }

    // ── SHARES_DO_NOT_SUM ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("SHARES_DO_NOT_SUM validation")
    inner class SharesDoNotSum {

        @Test
        fun `returns SHARES_DO_NOT_SUM when shares sum to more than 1`() {
            val subunit = Subunit(
                name = "Bad Shares",
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf("user-1" to 0.6, "user-2" to 0.6)
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Invalid)
            assertEquals(
                SubunitValidationService.ValidationError.SHARES_DO_NOT_SUM,
                (result as SubunitValidationService.ValidationResult.Invalid).error
            )
        }

        @Test
        fun `returns SHARES_DO_NOT_SUM when shares sum to less than 1`() {
            val subunit = Subunit(
                name = "Low Shares",
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf("user-1" to 0.2, "user-2" to 0.2)
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Invalid)
            assertEquals(
                SubunitValidationService.ValidationError.SHARES_DO_NOT_SUM,
                (result as SubunitValidationService.ValidationResult.Invalid).error
            )
        }

        @Test
        fun `passes when shares sum is within tolerance of 1`() {
            // 1/3 + 1/3 + 1/3 has floating-point imprecision but should pass tolerance
            val subunit = Subunit(
                name = "Three Way",
                memberIds = listOf("user-1", "user-2", "user-3"),
                memberShares = mapOf(
                    "user-1" to 1.0 / 3.0,
                    "user-2" to 1.0 / 3.0,
                    "user-3" to 1.0 / 3.0
                )
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Valid)
        }
    }

    // ── MISSING_SHARE ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("MISSING_SHARE validation")
    inner class MissingShare {

        @Test
        fun `returns MISSING_SHARE when a member has no share entry`() {
            val subunit = Subunit(
                name = "Partial Shares",
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf("user-1" to 1.0)
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Invalid)
            assertEquals(
                SubunitValidationService.ValidationError.MISSING_SHARE,
                (result as SubunitValidationService.ValidationResult.Invalid).error
            )
        }
    }

    // ── EXTRA_SHARE ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("EXTRA_SHARE validation")
    inner class ExtraShare {

        @Test
        fun `returns EXTRA_SHARE when memberShares contains user not in memberIds`() {
            val subunit = Subunit(
                name = "Orphan Share",
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf(
                    "user-1" to 0.3,
                    "user-2" to 0.3,
                    "user-3" to 0.4
                )
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Invalid)
            assertEquals(
                SubunitValidationService.ValidationError.EXTRA_SHARE,
                (result as SubunitValidationService.ValidationResult.Invalid).error
            )
        }

        @Test
        fun `returns EXTRA_SHARE when shares have completely different keys than memberIds`() {
            val subunit = Subunit(
                name = "Wrong Shares",
                memberIds = listOf("user-1"),
                memberShares = mapOf(
                    "user-1" to 0.5,
                    "user-2" to 0.5
                )
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Invalid)
            assertEquals(
                SubunitValidationService.ValidationError.EXTRA_SHARE,
                (result as SubunitValidationService.ValidationResult.Invalid).error
            )
        }
    }

    // ── Valid sub-unit ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Valid sub-unit")
    inner class ValidSubunit {

        @Test
        fun `valid sub-unit with explicit shares passes`() {
            val subunit = Subunit(
                name = "Couple",
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf("user-1" to 0.5, "user-2" to 0.5)
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Valid)
            val validResult = result as SubunitValidationService.ValidationResult.Valid
            assertEquals(subunit, validResult.subunit)
        }

        @Test
        fun `valid sub-unit with single member passes`() {
            val subunit = Subunit(
                name = "Solo",
                memberIds = listOf("user-1"),
                memberShares = mapOf("user-1" to 1.0)
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Valid)
        }

        @Test
        fun `valid sub-unit with unequal shares summing to 1 passes`() {
            val subunit = Subunit(
                name = "Family",
                memberIds = listOf("user-1", "user-2", "user-3"),
                memberShares = mapOf(
                    "user-1" to 0.4,
                    "user-2" to 0.3,
                    "user-3" to 0.3
                )
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Valid)
        }

        @Test
        fun `valid sub-unit alongside non-overlapping existing sub-units`() {
            val existingSubunit = Subunit(
                id = "existing-1",
                name = "Existing Couple",
                memberIds = listOf("user-1", "user-2"),
                memberShares = mapOf("user-1" to 0.5, "user-2" to 0.5)
            )
            val newSubunit = Subunit(
                name = "Other Pair",
                memberIds = listOf("user-3", "user-4"),
                memberShares = mapOf("user-3" to 0.5, "user-4" to 0.5)
            )

            val result = service.validate(newSubunit, groupMemberIds, listOf(existingSubunit))

            assertTrue(result is SubunitValidationService.ValidationResult.Valid)
        }
    }

    // ── Auto-normalization ────────────────────────────────────────────────────

    @Nested
    @DisplayName("Auto-normalization of equal shares")
    inner class AutoNormalization {

        @Test
        fun `auto-generates equal shares when memberShares is empty`() {
            val subunit = Subunit(
                name = "Couple",
                memberIds = listOf("user-1", "user-2"),
                memberShares = emptyMap()
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Valid)
            val validResult = result as SubunitValidationService.ValidationResult.Valid
            assertEquals(0.5, validResult.subunit.memberShares["user-1"])
            assertEquals(0.5, validResult.subunit.memberShares["user-2"])
        }

        @Test
        fun `auto-generates equal shares for three members`() {
            val subunit = Subunit(
                name = "Trio",
                memberIds = listOf("user-1", "user-2", "user-3"),
                memberShares = emptyMap()
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Valid)
            val validResult = result as SubunitValidationService.ValidationResult.Valid
            val expectedShare = 1.0 / 3.0
            assertEquals(expectedShare, validResult.subunit.memberShares["user-1"])
            assertEquals(expectedShare, validResult.subunit.memberShares["user-2"])
            assertEquals(expectedShare, validResult.subunit.memberShares["user-3"])
        }

        @Test
        fun `auto-generates full share for single member`() {
            val subunit = Subunit(
                name = "Solo",
                memberIds = listOf("user-1"),
                memberShares = emptyMap()
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Valid)
            val validResult = result as SubunitValidationService.ValidationResult.Valid
            assertEquals(1.0, validResult.subunit.memberShares["user-1"])
        }

        @Test
        fun `does not auto-normalize when memberShares is provided`() {
            val shares = mapOf("user-1" to 0.7, "user-2" to 0.3)
            val subunit = Subunit(
                name = "Couple",
                memberIds = listOf("user-1", "user-2"),
                memberShares = shares
            )

            val result = service.validate(subunit, groupMemberIds, emptyList())

            assertTrue(result is SubunitValidationService.ValidationResult.Valid)
            val validResult = result as SubunitValidationService.ValidationResult.Valid
            assertEquals(0.7, validResult.subunit.memberShares["user-1"])
            assertEquals(0.3, validResult.subunit.memberShares["user-2"])
        }
    }
}

