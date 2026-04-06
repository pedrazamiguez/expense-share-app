package es.pedrazamiguez.splittrip.features.expense.presentation.extensions

import es.pedrazamiguez.splittrip.domain.enums.SplitType
import es.pedrazamiguez.splittrip.features.expense.R

fun SplitType.toStringRes(): Int = when (this) {
    SplitType.EQUAL -> R.string.split_type_equal
    SplitType.EXACT -> R.string.split_type_exact
    SplitType.PERCENT -> R.string.split_type_percent
}
