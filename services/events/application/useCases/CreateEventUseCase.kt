package events.application.useCases

import events.application.dto.CreateEventRequest
import events.application.ports.outbound.IEventRepository
import events.domain.Event
import events.domain.Venue
import events.domain.valueObjects.DateRange
import events.domain.valueObjects.EventDescription
import events.domain.valueObjects.EventName
import java.util.UUID

class CreateEventUseCase(private val eventRepository: IEventRepository) {

    suspend fun execute(partnerId: UUID, request: CreateEventRequest): UUID {
        // Validação encapsulada nos Value Objects
        val name = EventName.of(request.name)
        val description = EventDescription.of(request.description)
        val dateRange = DateRange.fromStrings(request.startDate, request.endDate)

        val venue =
                Venue(
                        name = request.venue.name,
                        address = request.venue.address,
                        city = request.venue.city,
                        state = request.venue.state,
                        zipCode = request.venue.zipCode,
                        capacity = request.venue.capacity
                )

        // RN-E02: Evento inicia em DRAFT
        val event =
                Event(
                        partnerId = partnerId,
                        name = name,
                        description = description,
                        venue = venue,
                        dateRange = dateRange,
                        imageUrl = request.imageUrl
                )

        return eventRepository.add(event)
    }
}
