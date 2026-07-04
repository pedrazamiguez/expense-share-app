package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.usecase.UseCase

interface ConfirmSettlementUseCase : UseCase {
    suspend operator fun invoke(
        groupId: String,
        settlementId: String
    ): Result<SettlementRecord>
}
