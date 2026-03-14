package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import kotlinx.coroutines.flow.Flow

class GetCashWithdrawalsFlowUseCase(private val cashWithdrawalRepository: CashWithdrawalRepository) {
    operator fun invoke(groupId: String): Flow<List<CashWithdrawal>> =
        cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId)
}
