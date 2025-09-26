package es.pedrazamiguez.expenseshareapp.domain.usecase

import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository

class GetUserBalanceUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<Double> {
        return try {
            val balance = userRepository.getUserBalance(userId)
            Result.success(balance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
