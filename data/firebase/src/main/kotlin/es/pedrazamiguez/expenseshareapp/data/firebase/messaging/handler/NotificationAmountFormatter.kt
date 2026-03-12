package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatCurrencyAmount

/**
 * Extracts `amountCents` and `currencyCode` from the FCM data map and formats
 * them using the app's current locale via [LocaleProvider] and [formatCurrencyAmount].
 *
 * Falls back to an empty string if the required fields are missing.
 */
fun formatNotificationAmount(data: Map<String, String>, localeProvider: LocaleProvider): String {
    val amountCents = data["amountCents"]?.toLongOrNull() ?: return ""
    val currencyCode = data["currencyCode"] ?: return ""
    return formatCurrencyAmount(
        amount = amountCents,
        currencyCode = currencyCode,
        locale = localeProvider.getCurrentLocale()
    )
}

