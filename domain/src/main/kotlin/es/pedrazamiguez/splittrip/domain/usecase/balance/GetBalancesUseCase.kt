package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.model.Balance

class GetBalancesUseCase {
    suspend operator fun invoke(): Result<List<Balance>> = try {
        // Placeholder for actual implementation
        Result.success(emptyList())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
