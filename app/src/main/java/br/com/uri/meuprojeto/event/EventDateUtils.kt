package br.com.uri.meuprojeto.event

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

internal val EVENT_TIME_ZONE_ID: ZoneId = ZoneId.of("America/Sao_Paulo")

private val LEGACY_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter
        .ofPattern("dd/MM/uuuu", Locale.ROOT)
        .withResolverStyle(ResolverStyle.STRICT)

fun resolveEventDate(event: Event): LocalDate? {
    event.startsAt?.let { startsAt ->
        return startsAt
            .toDate()
            .toInstant()
            .atZone(EVENT_TIME_ZONE_ID)
            .toLocalDate()
    }

    if (event.date.isBlank()) {
        return null
    }

    return try {
        LocalDate.parse(event.date, LEGACY_DATE_FORMATTER)
    } catch (_: DateTimeParseException) {
        null
    }
}

fun resolveEventStatus(
    event: Event,
    today: LocalDate = LocalDate.now(EVENT_TIME_ZONE_ID)
): EventStatus = when (val eventDate = resolveEventDate(event)) {
    null -> EventStatus.UNKNOWN
    else -> if (eventDate < today) EventStatus.ENDED else EventStatus.UPCOMING
}
