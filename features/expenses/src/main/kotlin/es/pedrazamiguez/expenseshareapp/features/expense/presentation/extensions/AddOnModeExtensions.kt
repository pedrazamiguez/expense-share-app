package es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions

import androidx.annotation.StringRes
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.features.expense.R

@StringRes
fun AddOnMode.toStringRes(): Int = when (this) {
    AddOnMode.ON_TOP -> R.string.add_on_mode_on_top
    AddOnMode.INCLUDED -> R.string.add_on_mode_included
}
