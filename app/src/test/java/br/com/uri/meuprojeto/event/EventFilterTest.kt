package br.com.uri.meuprojeto.event

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class EventFilterTest {

    private val today = LocalDate.of(2026, 10, 13)

    @Test
    fun `empty query returns all events`() {
        assertIds(
            listOf(upcomingTechnology, endedCulture, unknown),
            filterEvents(
                events = allEvents,
                searchQuery = "",
                category = null,
                statusFilter = EventStatusFilter.ALL,
                today = today
            )
        )
    }

    @Test
    fun `search matches a title substring`() {
        assertIds(
            listOf(upcomingTechnology),
            filterEvents(allEvents, "emana", null, EventStatusFilter.ALL, today)
        )
    }

    @Test
    fun `search is case insensitive`() {
        assertIds(
            listOf(upcomingTechnology),
            filterEvents(allEvents, "SEMANA", null, EventStatusFilter.ALL, today)
        )
    }

    @Test
    fun `search without a match returns empty`() {
        assertEquals(
            emptyList<Event>(),
            filterEvents(allEvents, "inexistente", null, EventStatusFilter.ALL, today)
        )
    }

    @Test
    fun `search ignores external spaces`() {
        assertIds(
            listOf(upcomingTechnology),
            filterEvents(allEvents, "  semana  ", null, EventStatusFilter.ALL, today)
        )
    }

    @Test
    fun `missing category returns all events`() {
        assertIds(
            allEvents,
            filterEvents(allEvents, "", null, EventStatusFilter.ALL, today)
        )
    }

    @Test
    fun `category matches exactly`() {
        assertIds(
            listOf(upcomingTechnology),
            filterEvents(allEvents, "", "Tecnologia", EventStatusFilter.ALL, today)
        )
    }

    @Test
    fun `category matching is case insensitive and trims spaces`() {
        assertIds(
            listOf(upcomingTechnology),
            filterEvents(allEvents, "", "  TECNOLOGIA  ", EventStatusFilter.ALL, today)
        )
    }

    @Test
    fun `unknown category returns empty`() {
        assertEquals(
            emptyList<Event>(),
            filterEvents(allEvents, "", "Esportes", EventStatusFilter.ALL, today)
        )
    }

    @Test
    fun `event without category does not match a specific category`() {
        assertIds(
            listOf(upcomingTechnology),
            filterEvents(allEvents, "", "Tecnologia", EventStatusFilter.ALL, today)
        )
    }

    @Test
    fun `all status keeps upcoming ended and unknown`() {
        assertIds(
            allEvents,
            filterEvents(allEvents, "", null, EventStatusFilter.ALL, today)
        )
    }

    @Test
    fun `upcoming status returns only upcoming events`() {
        assertIds(
            listOf(upcomingTechnology),
            filterEvents(allEvents, "", null, EventStatusFilter.UPCOMING, today)
        )
    }

    @Test
    fun `ended status returns only ended events`() {
        assertIds(
            listOf(endedCulture),
            filterEvents(allEvents, "", null, EventStatusFilter.ENDED, today)
        )
    }

    @Test
    fun `unknown status is excluded from upcoming`() {
        assertIds(
            listOf(upcomingTechnology),
            filterEvents(allEvents, "", null, EventStatusFilter.UPCOMING, today)
        )
    }

    @Test
    fun `unknown status is excluded from ended`() {
        assertIds(
            listOf(endedCulture),
            filterEvents(allEvents, "", null, EventStatusFilter.ENDED, today)
        )
    }

    @Test
    fun `event today remains upcoming`() {
        val eventToday = event("today", "Evento de hoje", "13/10/2026", "Geral")

        assertIds(
            listOf(eventToday),
            filterEvents(
                events = listOf(eventToday),
                searchQuery = "",
                category = null,
                statusFilter = EventStatusFilter.UPCOMING,
                today = today
            )
        )
    }

    @Test
    fun `search and category are cumulative`() {
        assertIds(
            listOf(upcomingTechnology),
            filterEvents(allEvents, "semana", "tecnologia", EventStatusFilter.ALL, today)
        )
    }

    @Test
    fun `search and status are cumulative`() {
        assertIds(
            listOf(endedCulture),
            filterEvents(allEvents, "feira", null, EventStatusFilter.ENDED, today)
        )
    }

    @Test
    fun `category and status are cumulative`() {
        assertIds(
            listOf(endedCulture),
            filterEvents(allEvents, "", "cultura", EventStatusFilter.ENDED, today)
        )
    }

    @Test
    fun `search category and status are all cumulative`() {
        assertIds(
            listOf(endedCulture),
            filterEvents(allEvents, "FEIRA", " CULTURA ", EventStatusFilter.ENDED, today)
        )
    }

    @Test
    fun `filter preserves original order`() {
        val anotherCulture = event(
            "another-ended",
            "Outro evento cultural",
            "11/10/2026",
            "Cultura"
        )
        val events = listOf(endedCulture, upcomingTechnology, anotherCulture)

        assertIds(
            listOf(endedCulture, anotherCulture),
            filterEvents(events, "", "cultura", EventStatusFilter.ALL, today)
        )
    }

    @Test
    fun `available categories ignores empty values and deduplicates case insensitively`() {
        assertEquals(
            listOf("Tecnologia", "Cultura"),
            availableCategories(
                listOf(
                    upcomingTechnology,
                    event("second", "Second", "15/10/2026", " tecnologia "),
                    endedCulture,
                    unknown
                )
            )
        )
    }

    private val upcomingTechnology =
        event("upcoming", "Semana de Tecnologia", "14/10/2026", "Tecnologia")
    private val endedCulture =
        event("ended", "Feira Cultural", "12/10/2026", "Cultura")
    private val unknown =
        event("unknown", "Evento sem data", "", null)

    private val allEvents = listOf(upcomingTechnology, endedCulture, unknown)

    private fun event(
        id: String,
        title: String,
        date: String,
        category: String?
    ) = Event(
        id = id,
        title = title,
        description = "Description",
        date = date,
        location = "Location",
        category = category
    )

    private fun assertIds(expected: List<Event>, actual: List<Event>) {
        assertEquals(expected.map { it.id }, actual.map { it.id })
    }
}
