package tickets.infrastructure.persistence

import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import tickets.domain.TicketType
import tickets.domain.TicketTypeStatus
import tickets.domain.valueObjects.Quantity

class DatabaseContext {
    private val ticketTypes = ConcurrentHashMap<UUID, TicketType>()
    private val locks = ConcurrentHashMap<UUID, ReentrantLock>()

    private fun getLock(id: UUID): ReentrantLock {
        return locks.computeIfAbsent(id) { ReentrantLock() }
    }

    fun addTicketType(ticketType: TicketType): UUID {
        val now = Instant.now()
        ticketTypes[ticketType.id] = ticketType.copy(createdAt = now, updatedAt = now)
        return ticketType.id
    }

    fun findById(id: UUID): TicketType? {
        return ticketTypes[id]
    }

    fun findByEventId(eventId: UUID): List<TicketType> {
        return ticketTypes.values.filter { it.eventId == eventId }
    }

    fun updateTicketType(ticketType: TicketType): Boolean {
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

    fun deleteTicketType(id: UUID): Boolean {
        val lock = getLock(id)
        return lock.withLock { ticketTypes.remove(id) != null }
    }

    /**
     * Decrementa atomicamente a quantidade disponível. Retorna false se não houver estoque
     * suficiente. RN-T04: availableQuantity não pode ser negativo RN-T07: Status muda para SOLD_OUT
     * quando availableQuantity = 0
     */
    fun decrementAvailableQuantity(id: UUID, quantity: Int): Boolean {
        val lock = getLock(id)
        return lock.withLock {
            val ticketType = ticketTypes[id] ?: return@withLock false

            if (ticketType.availableQuantity.value < quantity) {
                return@withLock false
            }

            val newAvailableQuantity = Quantity.of(ticketType.availableQuantity.value - quantity)
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

    /**
     * Incrementa atomicamente a quantidade disponível. Usado quando uma reserva é cancelada.
     * RN-T07: Se estava SOLD_OUT e agora tem estoque, volta para ACTIVE
     */
    fun incrementAvailableQuantity(id: UUID, quantity: Int): Boolean {
        val lock = getLock(id)
        return lock.withLock {
            val ticketType = ticketTypes[id] ?: return@withLock false

            val newValue =
                    (ticketType.availableQuantity.value + quantity).coerceAtMost(
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
