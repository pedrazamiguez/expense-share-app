package es.pedrazamiguez.splittrip.domain.usecase.settlement

interface NudgeDebtorUseCase {
    suspend operator fun invoke(groupId: String, settlementId: String): Result<Unit>
}
