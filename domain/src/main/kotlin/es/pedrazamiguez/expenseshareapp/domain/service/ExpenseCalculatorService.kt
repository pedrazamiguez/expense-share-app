package es.pedrazamiguez.expenseshareapp.domain.service

import java.math.BigDecimal
import java.math.RoundingMode

class ExpenseCalculatorService {

    fun calculateGroupAmount(sourceAmount: BigDecimal, rate: BigDecimal): BigDecimal {
        if (rate.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        // Source * Rate = Target (e.g. 1000 THB * 0.027 = 27 EUR)
        return sourceAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP)
    }

    fun calculateImpliedRate(sourceAmount: BigDecimal, groupAmount: BigDecimal): BigDecimal {
        if (sourceAmount.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        // Target / Source = Rate (e.g. 27.35 EUR / 1000 THB = 0.02735)
        return groupAmount.divide(sourceAmount, 6, RoundingMode.HALF_UP)
    }
}
