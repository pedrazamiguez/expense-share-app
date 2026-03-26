package es.pedrazamiguez.expenseshareapp.core.common.extensions

import java.text.Collator
import java.util.Locale

/**
 * Creates a locale-aware [Comparator] that sorts by the extracted name using
 * [Collator] rules (accent/case-insensitive), falling back to raw [String.compareTo]
 * when collation ranks are equal for deterministic ordering.
 *
 * @param locale       The locale for collation rules.
 * @param nameSelector A function that extracts the name to sort by from each element.
 * @return A [Comparator] that uses [Collator.SECONDARY] strength for locale-aware ordering.
 */
fun <T> localeAwareComparator(locale: Locale, nameSelector: (T) -> String): Comparator<T> {
    val collator = Collator.getInstance(locale).apply {
        strength = Collator.SECONDARY
    }
    return Comparator { a, b ->
        val nameA = nameSelector(a)
        val nameB = nameSelector(b)
        val collated = collator.compare(nameA, nameB)
        if (collated != 0) collated else nameA.compareTo(nameB)
    }
}
