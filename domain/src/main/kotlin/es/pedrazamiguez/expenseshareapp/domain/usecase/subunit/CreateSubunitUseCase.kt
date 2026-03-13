package es.pedrazamiguez.expenseshareapp.domain.usecase.subunit

import es.pedrazamiguez.expenseshareapp.domain.exception.ValidationException
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.SubunitRepository
import es.pedrazamiguez.expenseshareapp.domain.service.GroupMembershipService
import es.pedrazamiguez.expenseshareapp.domain.service.SubunitValidationService
import kotlinx.coroutines.flow.first

/**
 * Use case for creating a new sub-unit within a group.
 *
 * Flow:
 * 1. Verify caller membership in the group.
 * 2. Fetch existing sub-units for overlap validation.
 * 3. Fetch group member list for membership validation.
 * 4. Validate via [SubunitValidationService].
 * 5. Persist via [SubunitRepository].
 */
class CreateSubunitUseCase(
    private val subunitRepository: SubunitRepository,
    private val groupRepository: GroupRepository,
    private val groupMembershipService: GroupMembershipService,
    private val subunitValidationService: SubunitValidationService
) {

    /**
     * Creates a sub-unit in the specified group.
     *
     * @param groupId The group to create the sub-unit in.
     * @param subunit The sub-unit data to create.
     * @return [Result.success] with the generated sub-unit ID, or [Result.failure] on error.
     */
    suspend operator fun invoke(groupId: String, subunit: Subunit): Result<String> = runCatching {
        groupMembershipService.requireMembership(groupId)

        val existingSubunits = subunitRepository.getGroupSubunitsFlow(groupId).first()
        val group = requireNotNull(groupRepository.getGroupById(groupId)) {
            "Group $groupId not found after membership check"
        }
        val groupMemberIds = group.members

        val validationResult = subunitValidationService.validate(
            subunit = subunit,
            groupMemberIds = groupMemberIds,
            existingSubunits = existingSubunits
        )

        when (validationResult) {
            is SubunitValidationService.ValidationResult.Invalid -> {
                throw ValidationException("Validation failed: ${validationResult.error.name}")
            }

            is SubunitValidationService.ValidationResult.Valid -> {
                subunitRepository.createSubunit(groupId, validationResult.subunit)
            }
        }
    }
}
