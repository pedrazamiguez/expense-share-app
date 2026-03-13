package es.pedrazamiguez.expenseshareapp.domain.usecase.subunit

import es.pedrazamiguez.expenseshareapp.domain.exception.ValidationException
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.SubunitRepository
import es.pedrazamiguez.expenseshareapp.domain.service.GroupMembershipService
import es.pedrazamiguez.expenseshareapp.domain.service.SubunitValidationService

/**
 * Use case for updating an existing sub-unit within a group.
 *
 * Same validation as [CreateSubunitUseCase], but passes the sub-unit's own ID
 * as [excludeSubunitId] to skip self-overlap during the member-uniqueness check.
 */
class UpdateSubunitUseCase(
    private val subunitRepository: SubunitRepository,
    private val groupRepository: GroupRepository,
    private val groupMembershipService: GroupMembershipService,
    private val subunitValidationService: SubunitValidationService
) {

    /**
     * Updates a sub-unit in the specified group.
     *
     * @param groupId The group the sub-unit belongs to.
     * @param subunit The sub-unit data with updated fields.
     * @return [Result.success] on success, or [Result.failure] on validation/permission error.
     */
    suspend operator fun invoke(groupId: String, subunit: Subunit): Result<Unit> = runCatching {
        require(subunit.id.isNotBlank()) { "Subunit ID must not be blank for update" }

        groupMembershipService.requireMembership(groupId)

        val existingSubunits = subunitRepository.getGroupSubunits(groupId)
        val group = requireNotNull(groupRepository.getGroupById(groupId)) {
            "Group $groupId not found after membership check"
        }
        val groupMemberIds = group.members

        val validationResult = subunitValidationService.validate(
            subunit = subunit,
            groupMemberIds = groupMemberIds,
            existingSubunits = existingSubunits,
            excludeSubunitId = subunit.id
        )

        when (validationResult) {
            is SubunitValidationService.ValidationResult.Invalid -> {
                throw ValidationException("Validation failed: ${validationResult.error.name}")
            }

            is SubunitValidationService.ValidationResult.Valid -> {
                subunitRepository.updateSubunit(groupId, validationResult.subunit)
            }
        }
    }
}
