package es.pedrazamiguez.expenseshareapp.domain.repository

import java.math.BigDecimal

interface UserRepository {
    suspend fun getUserBalance(userId: String): BigDecimal
}
