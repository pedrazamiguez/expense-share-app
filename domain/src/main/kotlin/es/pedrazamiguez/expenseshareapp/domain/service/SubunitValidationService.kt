package es.pedrazamiguez.expenseshareapp.domain.service

import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Domain service responsible for validating subunit data.
 * Validation logic belongs in services, NOT in UseCases or ViewModels.
 *
 * Also handles auto-normalization: when [Subunit.memberShares] is empty
 * but [Subunit.memberIds] is populated, equal shares are generated
 * (e.g., 2 members → {userA: 0.5, userB: 0.5}).
 */
class SubunitValidationService {

    /**
     * Validates a subunit against group constraints and existing subunits.
     *
     * If [Subunit.memberShares] is empty but [Subunit.memberIds] is populated,
     * auto-normalizes equal shares before validation.
     *
     * @param subunit The subunit to validate.
     * @param groupMemberIds The list of member IDs belonging to the group.
     * @param existingSubunits Other subunits already in the group (for overlap check).
     * @param excludeSubunitId When updating, exclude this subunit from overlap check (self).
     * @return [ValidationResult.Valid] with the (possibly auto-normalized) subunit,
     *         or [ValidationResult.Invalid] with the specific error.
     */
    fun validate(
        subunit: Subunit,
        groupMemberIds: List<String>,
        existingSubunits: List<Subunit>,
        excludeSubunitId: String? = null
    ): ValidationResult {
        // Rule: Name must not be blank
        if (subunit.name.isBlank()) {
            return ValidationResult.Invalid(ValidationError.EMPTY_NAME)
        }

        // Rule: At least 1 member required
        if (subunit.memberIds.isEmpty()) {
            return ValidationResult.Invalid(ValidationError.NO_MEMBERS)
        }

        // Rule: All members must belong to the group
        val groupMemberSet = groupMemberIds.toSet()
        if (subunit.memberIds.any { it !in groupMemberSet }) {
            return ValidationResult.Invalid(ValidationError.MEMBER_NOT_IN_GROUP)
        }

        // Rule: No member can appear in another subunit of the same group
        val alreadyAssigned = existingSubunits
            .filter { it.id != excludeSubunitId }
            .flatMapTo(mutableSetOf()) { it.memberIds }
        if (subunit.memberIds.any { it in alreadyAssigned }) {
            return ValidationResult.Invalid(ValidationError.MEMBER_ALREADY_IN_SUBUNIT)
        }

        // Auto-normalize: generate equal shares when memberShares is empty
        val normalizedSubunit = if (subunit.memberShares.isEmpty()) {
            autoNormalize(subunit)
        } else {
            subunit
        }

        // Rule: Each member in memberIds must have an entry in memberShares
        val memberIdSet = normalizedSubunit.memberIds.toSet()
        val missingShares = normalizedSubunit.memberIds.filter { it !in normalizedSubunit.memberShares }
        if (missingShares.isNotEmpty()) {
            return ValidationResult.Invalid(ValidationError.MISSING_SHARE)
        }

        // Rule: memberShares must not contain entries for users outside memberIds
        if (normalizedSubunit.memberShares.keys.any { it !in memberIdSet }) {
            return ValidationResult.Invalid(ValidationError.EXTRA_SHARE)
        }

        // Rule: Share weights must sum to 1 (with tolerance)
        val totalShares = normalizedSubunit.memberShares.values
            .fold(BigDecimal.ZERO) { acc, share -> acc.add(share) }
        if (totalShares.subtract(BigDecimal.ONE).abs() > SHARE_SUM_TOLERANCE) {
            return ValidationResult.Invalid(ValidationError.SHARES_DO_NOT_SUM)
        }

        return ValidationResult.Valid(normalizedSubunit)
    }

    /**
     * Auto-generates equal shares for all members using [BigDecimal] arithmetic.
     * E.g., 2 members → {userA: 0.5, userB: 0.5}
     */
    private fun autoNormalize(subunit: Subunit): Subunit {
        val count = BigDecimal(subunit.memberIds.size)
        val equalShare = BigDecimal.ONE.divide(count, SHARE_SCALE, RoundingMode.DOWN)
        val shares = subunit.memberIds.associateWith { equalShare }
        return subunit.copy(memberShares = shares)
    }

    sealed interface ValidationResult {
        data class Valid(val subunit: Subunit) : ValidationResult
        data class Invalid(val error: ValidationError) : ValidationResult
    }

    enum class ValidationError {
        EMPTY_NAME,
        NO_MEMBERS,
        MEMBER_NOT_IN_GROUP,
        MEMBER_ALREADY_IN_SUBUNIT,
        SHARES_DO_NOT_SUM,
        MISSING_SHARE,
        EXTRA_SHARE
    }

    companion object {
        private const val SHARE_SCALE = 10
        private val SHARE_SUM_TOLERANCE = BigDecimal("0.001")
    }
}
