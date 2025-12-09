package events.application.useCases

import events.application.ports.outbound.IEventRepository
import events.domain.Event
import events.domain.EventStatus
import java.util.UUID

class ListEventsUseCase(private val eventRepository: IEventRepository) {

    suspend fun executePublic(): List<Event> {
        return eventRepository.getPublishedEvents()
    }

    suspend fun executeByPartner(partnerId: UUID): List<Event> {
        return eventRepository.getByPartnerId(partnerId)
    }

    suspend fun executeByStatus(status: EventStatus): List<Event> {
        return eventRepository.getByStatus(status)
    }

    suspend fun executeAll(): List<Event> {
        return eventRepository.getAll()
    }
}
