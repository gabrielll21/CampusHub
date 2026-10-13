package br.com.uri.meuprojeto.event

import java.time.LocalDate
import java.util.Locale

enum class EventStatusFilter {
    ALL,
    UPCOMING,
    ENDED
}

fun filterEvents(
    events: List<Event>,
    searchQuery: String,
    category: String?,
    statusFilter: EventStatusFilter,
    today: LocalDate = LocalDate.now(EVENT_TIME_ZONE_ID)
): List<Event> {
    val normalizedQuery = searchQuery.trim().lowercase(Locale.ROOT)
    val normalizedCategory = category
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.lowercase(Locale.ROOT)

    return events.filter { event ->
        val matchesTitle = normalizedQuery.isEmpty() ||
            event.title.lowercase(Locale.ROOT).contains(normalizedQuery)

        val eventCategory = event.category
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.lowercase(Locale.ROOT)
        val matchesCategory = normalizedCategory == null ||
            eventCategory == normalizedCategory

        val matchesStatus = when (statusFilter) {
            EventStatusFilter.ALL -> true
            EventStatusFilter.UPCOMING ->
                resolveEventStatus(event, today) == EventStatus.UPCOMING
            EventStatusFilter.ENDED ->
                resolveEventStatus(event, today) == EventStatus.ENDED
        }

        matchesTitle && matchesCategory && matchesStatus
    }
}

fun availableCategories(events: List<Event>): List<String> {
    val seenCategories = mutableSetOf<String>()

    return events.mapNotNull { event ->
        event.category
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }.filter { category ->
        seenCategories.add(category.lowercase(Locale.ROOT))
    }
}
