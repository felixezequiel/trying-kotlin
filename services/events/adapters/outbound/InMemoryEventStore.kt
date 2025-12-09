package events.adapters.outbound

import events.application.ports.outbound.IEventRepository
import events.application.ports.outbound.ITransactionManager
import events.domain.Event
import events.domain.EventStatus
import java.time.Instant
import java.util.UUID

/**
 * Store em memória que gerencia eventos e transações. Esta classe encapsula tanto o repositório
 * quanto o gerenciador de transações para persistência em memória.
 */
class InMemoryEventStore {
    private val events = mutableListOf<Event>()

    val repository: IEventRepository = InMemoryEventRepository()
    val transactionManager: ITransactionManager = InMemoryTransactionManagerImpl()

    private inner class InMemoryEventRepository : IEventRepository {
        override suspend fun add(event: Event): UUID {
            val now = Instant.now()
            events.add(event.copy(createdAt = now, updatedAt = now))
            return event.id
        }

        override suspend fun getById(id: UUID): Event? {
            return events.find { it.id == id }
        }

        override suspend fun getByPartnerId(partnerId: UUID): List<Event> {
            return events.filter { it.partnerId == partnerId }
        }

        override suspend fun getByStatus(status: EventStatus): List<Event> {
            return events.filter { it.status == status }
        }

        override suspend fun getPublishedEvents(): List<Event> {
            return events.filter { it.status == EventStatus.PUBLISHED }
        }

        override suspend fun getAll(): List<Event> {
            return events.toList()
        }

        override suspend fun update(event: Event): Boolean {
            val index = events.indexOfFirst { it.id == event.id }
            if (index == -1) return false
            events[index] = event.copy(updatedAt = Instant.now())
            return true
        }

        override suspend fun delete(id: UUID): Boolean {
            return events.removeIf { it.id == id }
        }
    }

    private inner class InMemoryTransactionManagerImpl : ITransactionManager {
        override suspend fun <T> execute(block: suspend () -> T): T {
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
    }
}
