package es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions

import androidx.annotation.StringRes
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.features.expense.R

@StringRes
fun PaymentStatus.toStringRes(): Int = when (this) {
    PaymentStatus.RECEIVED -> R.string.payment_status_received
    PaymentStatus.PENDING -> R.string.payment_status_pending
    PaymentStatus.FINISHED -> R.string.payment_status_finished
    PaymentStatus.SCHEDULED -> R.string.payment_status_scheduled
    PaymentStatus.CANCELLED -> R.string.payment_status_cancelled
}
