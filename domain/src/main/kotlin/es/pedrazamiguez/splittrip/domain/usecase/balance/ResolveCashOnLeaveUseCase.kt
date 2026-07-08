package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.usecase.UseCase

interface ResolveCashOnLeaveUseCase : UseCase {
    suspend operator fun invoke(
        groupId: String,
        userId: String,
        memberBalance: MemberBalance,
        groupCurrency: String
    ): Result<Unit>
}
