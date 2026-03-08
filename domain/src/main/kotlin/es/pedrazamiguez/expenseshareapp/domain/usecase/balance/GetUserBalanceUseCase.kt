package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository
import java.math.BigDecimal

class GetUserBalanceUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<BigDecimal> = runCatching {
        userRepository.getUserBalance(userId)
    }
}
