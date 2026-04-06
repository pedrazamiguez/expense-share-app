package es.pedrazamiguez.splittrip.domain.usecase.subunit

import es.pedrazamiguez.splittrip.domain.exception.ValidationException
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.repository.SubunitRepository
import es.pedrazamiguez.splittrip.domain.service.GroupMembershipService
import es.pedrazamiguez.splittrip.domain.service.SubunitValidationService

/**
 * Use case for updating an existing subunit within a group.
 *
 * Same validation as [CreateSubunitUseCase], but passes the subunit's own ID
 * as [excludeSubunitId] to skip self-overlap during the member-uniqueness check.
 */
class UpdateSubunitUseCase(
    private val subunitRepository: SubunitRepository,
    private val groupRepository: GroupRepository,
    private val groupMembershipService: GroupMembershipService,
    private val subunitValidationService: SubunitValidationService
) {

    /**
     * Updates a subunit in the specified group.
     *
     * @param groupId The group the subunit belongs to.
     * @param subunit The subunit data with updated fields.
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
