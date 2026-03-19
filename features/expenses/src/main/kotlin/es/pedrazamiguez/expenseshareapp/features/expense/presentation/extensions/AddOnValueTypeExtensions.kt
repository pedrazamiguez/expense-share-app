package es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions

import androidx.annotation.StringRes
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnValueType
import es.pedrazamiguez.expenseshareapp.features.expense.R

@StringRes
fun AddOnValueType.toStringRes(): Int = when (this) {
    AddOnValueType.EXACT -> R.string.add_on_value_type_exact
    AddOnValueType.PERCENTAGE -> R.string.add_on_value_type_percentage
}
