package events.application.useCases

import events.application.ports.outbound.IEventRepository
import events.domain.Event
import events.domain.EventStatus
import java.util.UUID

class GetEventUseCase(private val eventRepository: IEventRepository) {

    suspend fun execute(eventId: UUID): Event? {
        return eventRepository.getById(eventId)
    }

    suspend fun executePublic(eventId: UUID): Event? {
        val event = eventRepository.getById(eventId) ?: return null

        // Eventos DRAFT não são visíveis publicamente
        if (event.status == EventStatus.DRAFT) {
            return null
        }

        return event
    }

    suspend fun executeForPartner(eventId: UUID, partnerId: UUID): Event? {
        val event = eventRepository.getById(eventId) ?: return null

        // Partner só pode ver seus próprios eventos
        if (event.partnerId != partnerId) {
            return null
        }

        return event
    }
}
