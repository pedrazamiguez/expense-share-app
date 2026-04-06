package es.pedrazamiguez.splittrip.domain.model

import java.math.BigDecimal

data class Balance(val userId: String, val amount: BigDecimal, val currency: Currency)
