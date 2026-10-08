package br.com.uri.meuprojeto.event

import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Test

class EventStatusTest {

    private val today = LocalDate.of(2026, 10, 8)

    @Test
    fun `future event is upcoming`() {
        assertEquals(
            EventStatus.UPCOMING,
            resolveEventStatus(event(date = "09/10/2026"), today)
        )
    }

    @Test
    fun `past event is ended`() {
        assertEquals(
            EventStatus.ENDED,
            resolveEventStatus(event(date = "07/10/2026"), today)
        )
    }

    @Test
    fun `event today is upcoming`() {
        assertEquals(
            EventStatus.UPCOMING,
            resolveEventStatus(event(date = "08/10/2026"), today)
        )
    }

    @Test
    fun `invalid date is unknown`() {
        assertEquals(
            EventStatus.UNKNOWN,
            resolveEventStatus(event(date = "data inválida"), today)
        )
    }

    @Test
    fun `empty date is unknown`() {
        assertEquals(
            EventStatus.UNKNOWN,
            resolveEventStatus(event(date = ""), today)
        )
    }

    @Test
    fun `future startsAt is upcoming`() {
        assertEquals(
            EventStatus.UPCOMING,
            resolveEventStatus(
                event(date = "", startsAt = timestampAt("2026-10-09T15:00:00Z")),
                today
            )
        )
    }

    @Test
    fun `past startsAt is ended`() {
        assertEquals(
            EventStatus.ENDED,
            resolveEventStatus(
                event(date = "", startsAt = timestampAt("2026-10-07T15:00:00Z")),
                today
            )
        )
    }

    @Test
    fun `startsAt corresponding to today is upcoming`() {
        assertEquals(
            EventStatus.UPCOMING,
            resolveEventStatus(
                event(date = "", startsAt = timestampAt("2026-10-08T15:00:00Z")),
                today
            )
        )
    }

    @Test
    fun `startsAt has priority over conflicting legacy date`() {
        assertEquals(
            EventStatus.UPCOMING,
            resolveEventStatus(
                event(
                    date = "07/10/2026",
                    startsAt = timestampAt("2026-10-09T15:00:00Z")
                ),
                today
            )
        )
    }

    @Test
    fun `falls back to legacy date when startsAt is null`() {
        assertEquals(
            EventStatus.ENDED,
            resolveEventStatus(event(date = "07/10/2026", startsAt = null), today)
        )
    }

    @Test
    fun `event on the first day of the next year is upcoming`() {
        assertEquals(
            EventStatus.UPCOMING,
            resolveEventStatus(
                event(date = "01/01/2027"),
                LocalDate.of(2026, 12, 31)
            )
        )
    }

    @Test
    fun `event on the last day of the previous year is ended`() {
        assertEquals(
            EventStatus.ENDED,
            resolveEventStatus(
                event(date = "31/12/2026"),
                LocalDate.of(2027, 1, 1)
            )
        )
    }

    private fun event(
        date: String,
        startsAt: Timestamp? = null
    ): Event = Event(
        id = "event-id",
        title = "Event",
        description = "Description",
        date = date,
        location = "Location",
        startsAt = startsAt
    )

    private fun timestampAt(value: String): Timestamp =
        Timestamp(Date.from(Instant.parse(value)))
}
