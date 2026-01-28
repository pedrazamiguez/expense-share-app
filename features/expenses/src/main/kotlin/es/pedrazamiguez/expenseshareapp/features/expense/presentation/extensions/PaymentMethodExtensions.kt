package es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions

import androidx.annotation.StringRes
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.features.expense.R

@StringRes
fun PaymentMethod.toStringRes(): Int = when (this) {
    PaymentMethod.CASH -> R.string.payment_method_cash
    PaymentMethod.BIZUM -> R.string.payment_method_bizum
    PaymentMethod.CREDIT_CARD -> R.string.payment_method_credit_card
    PaymentMethod.DEBIT_CARD -> R.string.payment_method_debit_card
    PaymentMethod.BANK_TRANSFER -> R.string.payment_method_bank_transfer
    PaymentMethod.PAYPAL -> R.string.payment_method_paypal
    PaymentMethod.VENMO -> R.string.payment_method_venmo
    PaymentMethod.OTHER -> R.string.payment_method_other
}
