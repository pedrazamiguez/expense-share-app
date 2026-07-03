package es.pedrazamiguez.splittrip.domain.usecase.subunit

import es.pedrazamiguez.splittrip.domain.usecase.UseCase

interface ReassignSubunitSharesUseCase : UseCase {
    suspend operator fun invoke(groupId: String, leavingUserId: String): Result<Unit>
}
