package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.model.BalancesDashboardDomainModel
import es.pedrazamiguez.splittrip.domain.usecase.UseCase
import kotlinx.coroutines.flow.Flow

interface GetBalancesDashboardFlowUseCase : UseCase {
    operator fun invoke(
        groupId: String,
        currency: String,
        groupMemberIds: List<String>
    ): Flow<BalancesDashboardDomainModel>
}
