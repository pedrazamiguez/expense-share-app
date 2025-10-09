package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository

class GetUserBalanceUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<Double> = runCatching {
        userRepository.getUserBalance(userId)
    }
}
