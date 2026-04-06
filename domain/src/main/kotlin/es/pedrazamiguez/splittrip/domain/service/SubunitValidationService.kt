package es.pedrazamiguez.splittrip.domain.service

import es.pedrazamiguez.splittrip.domain.model.Subunit
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
        val structureError = validateStructure(subunit, groupMemberIds, existingSubunits, excludeSubunitId)
        if (structureError != null) return structureError

        // Auto-normalize: generate equal shares when memberShares is empty
        val normalizedSubunit = if (subunit.memberShares.isEmpty()) {
            autoNormalize(subunit)
        } else {
            subunit
        }

        return validateShares(normalizedSubunit)
    }

    private fun validateStructure(
        subunit: Subunit,
        groupMemberIds: List<String>,
        existingSubunits: List<Subunit>,
        excludeSubunitId: String?
    ): ValidationResult.Invalid? {
        val groupMemberSet = groupMemberIds.toSet()
        val alreadyAssigned = existingSubunits
            .filter { it.id != excludeSubunitId }
            .flatMapTo(mutableSetOf()) { it.memberIds }

        return when {
            subunit.name.isBlank() -> ValidationResult.Invalid(ValidationError.EMPTY_NAME)
            subunit.memberIds.isEmpty() -> ValidationResult.Invalid(ValidationError.NO_MEMBERS)
            subunit.memberIds.any { it !in groupMemberSet } ->
                ValidationResult.Invalid(ValidationError.MEMBER_NOT_IN_GROUP)
            subunit.memberIds.any { it in alreadyAssigned } ->
                ValidationResult.Invalid(ValidationError.MEMBER_ALREADY_IN_SUBUNIT)
            else -> null
        }
    }

    private fun validateShares(subunit: Subunit): ValidationResult {
        val memberIdSet = subunit.memberIds.toSet()
        val totalShares = subunit.memberShares.values
            .fold(BigDecimal.ZERO) { acc, share -> acc.add(share) }

        return when {
            subunit.memberIds.any { it !in subunit.memberShares } ->
                ValidationResult.Invalid(ValidationError.MISSING_SHARE)
            subunit.memberShares.keys.any { it !in memberIdSet } ->
                ValidationResult.Invalid(ValidationError.EXTRA_SHARE)
            totalShares.subtract(BigDecimal.ONE).abs() > SHARE_SUM_TOLERANCE ->
                ValidationResult.Invalid(ValidationError.SHARES_DO_NOT_SUM)
            else -> ValidationResult.Valid(subunit)
        }
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
