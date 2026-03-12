package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler

import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatCurrencyAmount
import java.util.Locale

/**
 * Extracts `amountCents` and `currencyCode` from the FCM data map and formats
 * them using the device's current locale via [formatCurrencyAmount].
 *
 * Falls back to an empty string if the required fields are missing.
 */
fun formatNotificationAmount(data: Map<String, String>): String {
    val amountCents = data["amountCents"]?.toLongOrNull() ?: return ""
    val currencyCode = data["currencyCode"] ?: return ""
    return formatCurrencyAmount(
        amount = amountCents,
        currencyCode = currencyCode,
        locale = Locale.getDefault()
    )
}

