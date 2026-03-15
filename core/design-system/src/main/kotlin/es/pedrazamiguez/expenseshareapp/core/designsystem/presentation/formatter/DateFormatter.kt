package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun LocalDateTime.formatShortDate(locale: Locale = Locale.getDefault()): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM", locale)
    return format(formatter)
}

fun LocalDateTime.formatMediumDate(locale: Locale = Locale.getDefault()): String {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
    return format(formatter)
}
