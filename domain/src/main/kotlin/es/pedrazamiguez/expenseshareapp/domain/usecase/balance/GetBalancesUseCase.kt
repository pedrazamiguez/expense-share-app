package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.model.Balance

class GetBalancesUseCase {
    suspend operator fun invoke(): Result<List<Balance>> {
        return try {
            // Placeholder for actual implementation
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
