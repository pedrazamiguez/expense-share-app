package es.pedrazamiguez.expenseshareapp.core.common.extensions

import java.text.Collator
import java.util.Locale

/**
 * Creates a locale-aware [Comparator] that sorts by the extracted name using
 * [Collator] rules (accent/case-insensitive), falling back to natural string
 * order when collation ranks are equal.
 *
 * @param locale       The locale for collation rules.
 * @param nameSelector A function that extracts the name to sort by from each element.
 * @return A [Comparator] that uses [Collator.SECONDARY] strength for locale-aware ordering.
 */
fun <T> localeAwareComparator(locale: Locale, nameSelector: (T) -> String): Comparator<T> {
    val collator = Collator.getInstance(locale).apply {
        strength = Collator.SECONDARY
    }
    return compareBy(collator, nameSelector)
}
