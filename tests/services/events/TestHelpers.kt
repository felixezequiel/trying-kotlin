package services.events

import events.domain.Event
import events.domain.EventStatus
import events.domain.Venue
import events.domain.valueObjects.DateRange
import events.domain.valueObjects.EventDescription
import events.domain.valueObjects.EventName
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

object TestHelpers {
    fun createTestEvent(
            id: UUID = UUID.randomUUID(),
            partnerId: UUID = UUID.randomUUID(),
            name: String = "Test Event",
            description: String = "Test Description",
            venueName: String = "Test Venue",
            startDate: Instant = Instant.now().plus(7, ChronoUnit.DAYS),
            endDate: Instant = Instant.now().plus(8, ChronoUnit.DAYS),
            status: EventStatus = EventStatus.DRAFT,
            imageUrl: String? = null
    ): Event {
        return Event(
                id = id,
                partnerId = partnerId,
                name = EventName.of(name),
                description = EventDescription.of(description),
                venue =
                        Venue(
                                name = venueName,
                                address = "Test Address",
                                city = "Test City",
                                state = "TS",
                                zipCode = "00000-000",
                                capacity = null
                        ),
                dateRange = DateRange.of(startDate, endDate),
                status = status,
                imageUrl = imageUrl
        )
    }
}
