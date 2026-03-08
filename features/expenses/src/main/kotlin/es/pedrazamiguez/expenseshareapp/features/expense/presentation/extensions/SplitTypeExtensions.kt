package es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions

import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.features.expense.R

fun SplitType.toStringRes(): Int = when (this) {
    SplitType.EQUAL -> R.string.split_type_equal
    SplitType.EXACT -> R.string.split_type_exact
    SplitType.PERCENT -> R.string.split_type_percent
}

