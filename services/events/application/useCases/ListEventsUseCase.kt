package events.application.useCases

import events.application.ports.outbound.IUnitOfWork
import events.domain.Event
import events.domain.EventStatus
import java.util.UUID

class ListEventsUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun executePublic(): List<Event> {
        return unitOfWork.eventRepository.getPublishedEvents()
    }

    suspend fun executeByPartner(partnerId: UUID): List<Event> {
        return unitOfWork.eventRepository.getByPartnerId(partnerId)
    }

    suspend fun executeByStatus(status: EventStatus): List<Event> {
        return unitOfWork.eventRepository.getByStatus(status)
    }

    suspend fun executeAll(): List<Event> {
        return unitOfWork.eventRepository.getAll()
    }
}
