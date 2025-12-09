package events.application.useCases

import events.application.ports.outbound.IEventRepository
import events.domain.Event
import events.domain.EventStatus
import java.time.Instant
import java.util.UUID

class PublishEventUseCase(private val eventRepository: IEventRepository) {

    suspend fun execute(eventId: UUID, partnerId: UUID): Event {
        val event =
                eventRepository.getById(eventId)
                        ?: throw IllegalArgumentException("Evento não encontrado")

        // Verificar se o partner é dono do evento
        if (event.partnerId != partnerId) {
            throw IllegalStateException("Você não tem permissão para publicar este evento")
        }

        // Só pode publicar eventos em DRAFT
        if (event.status != EventStatus.DRAFT) {
            throw IllegalStateException("Apenas eventos em DRAFT podem ser publicados")
        }

        // RN-E03: Só pode publicar se tiver pelo menos 1 TicketType
        // TODO: Validar via chamada ao Tickets service (por enquanto, permitir publicação)

        val publishedEvent =
                event.copy(
                        status = EventStatus.PUBLISHED,
                        publishedAt = Instant.now(),
                        updatedAt = Instant.now()
                )

        eventRepository.update(publishedEvent)
        return publishedEvent
    }
}
