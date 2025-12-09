package events.application.ports.outbound

import events.domain.Event
import events.domain.EventStatus
import java.util.UUID

interface IEventRepository {
    suspend fun add(event: Event): UUID
    suspend fun getById(id: UUID): Event?
    suspend fun getByPartnerId(partnerId: UUID): List<Event>
    suspend fun getByStatus(status: EventStatus): List<Event>
    suspend fun getPublishedEvents(): List<Event>
    suspend fun getAll(): List<Event>
    suspend fun update(event: Event): Boolean
    suspend fun delete(id: UUID): Boolean
}
