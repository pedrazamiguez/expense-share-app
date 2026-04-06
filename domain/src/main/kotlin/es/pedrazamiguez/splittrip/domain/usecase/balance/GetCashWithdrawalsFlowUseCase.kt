package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository
import kotlinx.coroutines.flow.Flow

class GetCashWithdrawalsFlowUseCase(private val cashWithdrawalRepository: CashWithdrawalRepository) {
    operator fun invoke(groupId: String): Flow<List<CashWithdrawal>> =
        cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId)
}
