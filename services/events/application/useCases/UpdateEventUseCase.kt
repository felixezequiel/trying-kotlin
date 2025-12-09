package events.application.useCases

import events.application.dto.UpdateEventRequest
import events.application.ports.outbound.IEventRepository
import events.domain.Event
import events.domain.EventStatus
import events.domain.Venue
import java.time.Instant
import java.util.UUID

class UpdateEventUseCase(private val eventRepository: IEventRepository) {

    suspend fun execute(eventId: UUID, partnerId: UUID, request: UpdateEventRequest): Event {
        val event =
                eventRepository.getById(eventId)
                        ?: throw IllegalArgumentException("Evento não encontrado")

        // Verificar se o partner é dono do evento
        if (event.partnerId != partnerId) {
            throw IllegalStateException("Você não tem permissão para editar este evento")
        }

        // RN-E06: Não pode editar evento CANCELLED ou FINISHED
        if (event.status == EventStatus.CANCELLED || event.status == EventStatus.FINISHED) {
            throw IllegalStateException("Não é possível editar evento ${event.status}")
        }

        val newStartDate = request.startDate?.let { Instant.parse(it) } ?: event.startDate
        val newEndDate = request.endDate?.let { Instant.parse(it) } ?: event.endDate

        // RN-E04: startDate deve ser futura (apenas se alterada)
        if (request.startDate != null && newStartDate.isBefore(Instant.now())) {
            throw IllegalArgumentException("Data de início deve ser futura")
        }

        // RN-E05: endDate deve ser após startDate
        if (newEndDate.isBefore(newStartDate) || newEndDate == newStartDate) {
            throw IllegalArgumentException("Data de término deve ser após a data de início")
        }

        val newVenue =
                request.venue?.let {
                    Venue(
                            name = it.name,
                            address = it.address,
                            city = it.city,
                            state = it.state,
                            zipCode = it.zipCode,
                            capacity = it.capacity
                    )
                }
                        ?: event.venue

        val updatedEvent =
                event.copy(
                        name = request.name ?: event.name,
                        description = request.description ?: event.description,
                        venue = newVenue,
                        startDate = newStartDate,
                        endDate = newEndDate,
                        imageUrl = request.imageUrl ?: event.imageUrl,
                        updatedAt = Instant.now()
                )

        eventRepository.update(updatedEvent)
        return updatedEvent
    }
}
