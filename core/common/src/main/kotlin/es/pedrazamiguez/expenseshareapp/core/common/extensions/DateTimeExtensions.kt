package es.pedrazamiguez.expenseshareapp.core.common.extensions

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Converts epoch milliseconds to [LocalDateTime] in UTC.
 *
 * All entity mappers (Room ↔ Domain) must use UTC for consistent
 * `Long` ↔ `LocalDateTime` conversions, matching the cloud layer's
 * `FirebaseTimestampMapper`.
 */
fun Long.toLocalDateTimeUtc(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC)

/**
 * Converts [LocalDateTime] to epoch milliseconds assuming UTC.
 *
 * Counterpart to [toLocalDateTimeUtc] — the same UTC zone must be used
 * in both directions to guarantee round-trip fidelity.
 */
fun LocalDateTime.toEpochMillisUtc(): Long =
    this.toInstant(ZoneOffset.UTC).toEpochMilli()
