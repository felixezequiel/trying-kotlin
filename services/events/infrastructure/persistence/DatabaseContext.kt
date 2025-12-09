package events.infrastructure.persistence

import events.domain.Event
import events.domain.EventStatus
import java.time.Instant
import java.util.UUID

class DatabaseContext {
    private val events = mutableListOf<Event>()

    suspend fun <T> executeTransaction(block: suspend () -> T): T {
        println("Iniciando transação...")
        val snapshot = events.toList()
        try {
            val result = block()
            println("Transação concluída com sucesso (commit).")
            return result
        } catch (e: Exception) {
            println("Transação falhou (rollback): ${e.message}")
            events.clear()
            events.addAll(snapshot)
            throw e
        }
    }

    fun addEvent(event: Event): UUID {
        val now = Instant.now()
        events.add(event.copy(createdAt = now, updatedAt = now))
        return event.id
    }

    fun findById(id: UUID): Event? {
        return events.find { it.id == id }
    }

    fun findByPartnerId(partnerId: UUID): List<Event> {
        return events.filter { it.partnerId == partnerId }
    }

    fun findByStatus(status: EventStatus): List<Event> {
        return events.filter { it.status == status }
    }

    fun findPublishedEvents(): List<Event> {
        return events.filter { it.status == EventStatus.PUBLISHED }
    }

    fun getAllEvents(): List<Event> {
        return events.toList()
    }

    fun updateEvent(event: Event): Boolean {
        val index = events.indexOfFirst { it.id == event.id }
        if (index == -1) return false
        events[index] = event.copy(updatedAt = Instant.now())
        return true
    }

    fun deleteEvent(id: UUID): Boolean {
        return events.removeIf { it.id == id }
    }
}
