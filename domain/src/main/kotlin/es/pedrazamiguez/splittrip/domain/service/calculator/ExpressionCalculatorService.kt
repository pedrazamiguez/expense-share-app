package es.pedrazamiguez.splittrip.domain.service.calculator

import java.math.BigDecimal

interface ExpressionCalculatorService {
    fun evaluate(expression: String): ExpressionResult
}

sealed class ExpressionResult {
    data class Success(val value: BigDecimal) : ExpressionResult()
    sealed class Failure : ExpressionResult() {
        data object Malformed : Failure()
        data object DivisionByZero : Failure()
        data object Empty : Failure()
    }
}
