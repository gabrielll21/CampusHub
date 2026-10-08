package br.com.uri.meuprojeto.event

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EventDateUtilsTest {

    @Test
    fun `parses a valid legacy date`() {
        assertEquals(LocalDate.of(2026, 10, 20), resolveEventDate(event(date = "20/10/2026")))
    }

    @Test
    fun `parses the first day of the year`() {
        assertEquals(LocalDate.of(2027, 1, 1), resolveEventDate(event(date = "01/01/2027")))
    }

    @Test
    fun `parses the last day of the year`() {
        assertEquals(LocalDate.of(2026, 12, 31), resolveEventDate(event(date = "31/12/2026")))
    }

    @Test
    fun `returns null for an empty date`() {
        assertNull(resolveEventDate(event(date = "")))
    }

    @Test
    fun `returns null for invalid text`() {
        assertNull(resolveEventDate(event(date = "data inválida")))
    }

    @Test
    fun `returns null for an impossible day`() {
        assertNull(resolveEventDate(event(date = "32/10/2026")))
    }

    @Test
    fun `returns null for an impossible month`() {
        assertNull(resolveEventDate(event(date = "20/13/2026")))
    }

    @Test
    fun `accepts a valid leap day`() {
        assertEquals(LocalDate.of(2024, 2, 29), resolveEventDate(event(date = "29/02/2024")))
    }

    @Test
    fun `rejects a leap day in a non leap year`() {
        assertNull(resolveEventDate(event(date = "29/02/2023")))
    }

    @Test
    fun `converts timestamp using Sao Paulo timezone`() {
        val timestamp = timestampAt("2026-10-20T02:30:00Z")

        assertEquals(
            LocalDate.of(2026, 10, 19),
            resolveEventDate(event(date = "20/10/2026", startsAt = timestamp))
        )
    }

    @Test
    fun `startsAt takes priority over legacy date`() {
        val timestamp = timestampAt("2026-11-05T15:00:00Z")

        assertEquals(
            LocalDate.of(2026, 11, 5),
            resolveEventDate(event(date = "20/10/2026", startsAt = timestamp))
        )
    }

    @Test
    fun `falls back to legacy date when startsAt is null`() {
        assertEquals(
            LocalDate.of(2026, 10, 20),
            resolveEventDate(event(date = "20/10/2026", startsAt = null))
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

    private fun timestampAt(value: String): Timestamp {
        val instant = java.time.Instant.parse(value)
        return Timestamp(Date.from(instant))
    }
}
