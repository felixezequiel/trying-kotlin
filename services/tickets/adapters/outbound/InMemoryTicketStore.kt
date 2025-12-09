package tickets.adapters.outbound

import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import tickets.application.ports.outbound.ITicketStore
import tickets.application.ports.outbound.ITicketTypeRepository
import tickets.application.ports.outbound.ITransactionManager
import tickets.domain.TicketType
import tickets.domain.TicketTypeStatus
import tickets.domain.valueObjects.Quantity

/**
 * Store em memória que gerencia ticket types e transações. Implementa ITicketStore para garantir
 * que qualquer implementação (Postgres, etc.) tenha o mesmo contrato.
 */
class InMemoryTicketStore : ITicketStore {
    private val ticketTypes = ConcurrentHashMap<UUID, TicketType>()
    private val locks = ConcurrentHashMap<UUID, ReentrantLock>()

    override val repository: ITicketTypeRepository = InMemoryTicketTypeRepository()
    override val transactionManager: ITransactionManager = InMemoryTransactionManagerImpl()

    private fun getLock(id: UUID): ReentrantLock {
        return locks.computeIfAbsent(id) { ReentrantLock() }
    }

    private inner class InMemoryTicketTypeRepository : ITicketTypeRepository {
        override suspend fun add(ticketType: TicketType): UUID {
            val now = Instant.now()
            ticketTypes[ticketType.id] = ticketType.copy(createdAt = now, updatedAt = now)
            return ticketType.id
        }

        override suspend fun getById(id: UUID): TicketType? {
            return ticketTypes[id]
        }

        override suspend fun getByEventId(eventId: UUID): List<TicketType> {
            return ticketTypes.values.filter { it.eventId == eventId }
        }

        override suspend fun update(ticketType: TicketType): Boolean {
            val lock = getLock(ticketType.id)
            return lock.withLock {
                if (ticketTypes.containsKey(ticketType.id)) {
                    ticketTypes[ticketType.id] = ticketType.copy(updatedAt = Instant.now())
                    true
                } else {
                    false
                }
            }
        }

        override suspend fun delete(id: UUID): Boolean {
            val lock = getLock(id)
            return lock.withLock { ticketTypes.remove(id) != null }
        }

        override suspend fun decrementAvailableQuantity(id: UUID, quantity: Quantity): Boolean {
            val lock = getLock(id)
            return lock.withLock {
                val ticketType = ticketTypes[id] ?: return@withLock false

                if (ticketType.availableQuantity.value < quantity.value) {
                    return@withLock false
                }

                val newAvailableQuantity =
                        Quantity.of(ticketType.availableQuantity.value - quantity.value)
                val newStatus =
                        if (newAvailableQuantity.isZero()) {
                            TicketTypeStatus.SOLD_OUT
                        } else {
                            ticketType.status
                        }

                ticketTypes[id] =
                        ticketType.copy(
                                availableQuantity = newAvailableQuantity,
                                status = newStatus,
                                updatedAt = Instant.now()
                        )
                true
            }
        }

        override suspend fun incrementAvailableQuantity(id: UUID, quantity: Quantity): Boolean {
            val lock = getLock(id)
            return lock.withLock {
                val ticketType = ticketTypes[id] ?: return@withLock false

                val newValue =
                        (ticketType.availableQuantity.value + quantity.value).coerceAtMost(
                                ticketType.totalQuantity.value
                        )
                val newAvailableQuantity = Quantity.of(newValue)

                val newStatus =
                        if (ticketType.status == TicketTypeStatus.SOLD_OUT && newValue > 0) {
                            TicketTypeStatus.ACTIVE
                        } else {
                            ticketType.status
                        }

                ticketTypes[id] =
                        ticketType.copy(
                                availableQuantity = newAvailableQuantity,
                                status = newStatus,
                                updatedAt = Instant.now()
                        )
                true
            }
        }
    }

    private inner class InMemoryTransactionManagerImpl : ITransactionManager {
        override suspend fun <T> execute(block: suspend () -> T): T {
            println("Iniciando transação...")
            // Para ConcurrentHashMap, não há snapshot simples - usamos uma abordagem simplificada
            // Em produção, usar banco de dados real com transações ACID
            try {
                val result = block()
                println("Transação concluída com sucesso (commit).")
                return result
            } catch (e: Exception) {
                println("Transação falhou (rollback): ${e.message}")
                throw e
            }
        }
    }
}
