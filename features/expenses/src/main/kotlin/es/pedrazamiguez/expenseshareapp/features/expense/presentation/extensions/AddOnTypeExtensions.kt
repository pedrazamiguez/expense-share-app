package es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions

import androidx.annotation.StringRes
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.features.expense.R

@StringRes
fun AddOnType.toStringRes(): Int = when (this) {
    AddOnType.TIP -> R.string.add_on_type_tip
    AddOnType.FEE -> R.string.add_on_type_fee
    AddOnType.DISCOUNT -> R.string.add_on_type_discount
    AddOnType.SURCHARGE -> R.string.add_on_type_surcharge
}
