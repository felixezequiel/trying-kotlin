package events.domain

import events.domain.valueObjects.DateRange
import events.domain.valueObjects.EventDescription
import events.domain.valueObjects.EventName
import java.time.Instant
import java.util.UUID

data class Event(
        val id: UUID = UUID.randomUUID(),
        val partnerId: UUID,
        val name: EventName,
        val description: EventDescription,
        val venue: Venue,
        val dateRange: DateRange,
        val status: EventStatus = EventStatus.DRAFT,
        val imageUrl: String? = null,
        val createdAt: Instant = Instant.now(),
        val publishedAt: Instant? = null,
        val updatedAt: Instant = Instant.now()
) {
        val startDate: Instant
                get() = dateRange.start
        val endDate: Instant
                get() = dateRange.end
}
