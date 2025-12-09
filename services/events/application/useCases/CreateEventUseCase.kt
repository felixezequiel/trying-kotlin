package events.application.useCases

import events.application.dto.CreateEventRequest
import events.application.ports.outbound.IEventRepository
import events.domain.Event
import events.domain.Venue
import java.time.Instant
import java.util.UUID

class CreateEventUseCase(private val eventRepository: IEventRepository) {

    suspend fun execute(partnerId: UUID, request: CreateEventRequest): UUID {
        val startDate = Instant.parse(request.startDate)
        val endDate = Instant.parse(request.endDate)

        // RN-E04: startDate deve ser futura
        if (startDate.isBefore(Instant.now())) {
            throw IllegalArgumentException("Data de início deve ser futura")
        }

        // RN-E05: endDate deve ser após startDate
        if (endDate.isBefore(startDate) || endDate == startDate) {
            throw IllegalArgumentException("Data de término deve ser após a data de início")
        }

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
                        name = request.name,
                        description = request.description,
                        venue = venue,
                        startDate = startDate,
                        endDate = endDate,
                        imageUrl = request.imageUrl
                )

        return eventRepository.add(event)
    }
}
