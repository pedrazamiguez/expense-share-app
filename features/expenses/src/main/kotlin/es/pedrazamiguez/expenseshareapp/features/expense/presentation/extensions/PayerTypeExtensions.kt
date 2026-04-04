package es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions

import androidx.annotation.StringRes
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.features.expense.R

@StringRes
fun PayerType.toFundingSourceStringRes(): Int = when (this) {
    PayerType.GROUP -> R.string.funding_source_group_pocket
    PayerType.USER -> R.string.funding_source_my_money
    PayerType.SUBUNIT -> error("PayerType.SUBUNIT is not user-selectable")
}
