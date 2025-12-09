package events.adapters.outbound

import events.application.ports.outbound.IEventRepository
import events.domain.Event
import events.domain.EventStatus
import events.infrastructure.persistence.DatabaseContext
import java.util.UUID

class EventRepositoryAdapter(private val dbContext: DatabaseContext) : IEventRepository {

    override suspend fun add(event: Event): UUID {
        return dbContext.addEvent(event)
    }

    override suspend fun getById(id: UUID): Event? {
        return dbContext.findById(id)
    }

    override suspend fun getByPartnerId(partnerId: UUID): List<Event> {
        return dbContext.findByPartnerId(partnerId)
    }

    override suspend fun getByStatus(status: EventStatus): List<Event> {
        return dbContext.findByStatus(status)
    }

    override suspend fun getPublishedEvents(): List<Event> {
        return dbContext.findPublishedEvents()
    }

    override suspend fun getAll(): List<Event> {
        return dbContext.getAllEvents()
    }

    override suspend fun update(event: Event): Boolean {
        return dbContext.updateEvent(event)
    }

    override suspend fun delete(id: UUID): Boolean {
        return dbContext.deleteEvent(id)
    }
}
