package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset

// Firestore Timestamp -> LocalDateTime in UTC
fun Timestamp?.toLocalDateTimeUtc(): LocalDateTime? = this?.let {
    LocalDateTime.ofEpochSecond(
        it.seconds,
        it.nanoseconds,
        ZoneOffset.UTC
    )
}

// LocalDateTime -> Firestore Timestamp (UTC)
fun LocalDateTime?.toTimestampUtc(): Timestamp? = this?.let {
    val instant = it.toInstant(ZoneOffset.UTC)
    Timestamp(
        instant.epochSecond,
        instant.nano
    )
}
