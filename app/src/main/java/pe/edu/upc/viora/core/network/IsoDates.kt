package pe.edu.upc.viora.core.network

import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime

// Reading the ISO-8601 dates the backend sends. A missing or malformed value is null, never an exception:
// each mapper decides whether that drops the row or keeps it without the date.

/** "2026-05-14T15:40:00Z", or with an offset ("…-05:00"); null when absent or unreadable. */
fun String?.toInstantOrNull(): Instant? = this?.trim()?.takeIf { it.isNotEmpty() }?.let { raw ->
    runCatching { Instant.parse(raw) }
        .recoverCatching { OffsetDateTime.parse(raw).toInstant() }
        .getOrNull()
}

/** "2026-05-14"; null when absent or unreadable. */
fun String?.toLocalDateOrNull(): LocalDate? = this?.trim()?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
