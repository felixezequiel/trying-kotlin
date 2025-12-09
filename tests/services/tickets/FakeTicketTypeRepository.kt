package services.tickets

import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import tickets.application.ports.outbound.ITicketTypeRepository
import tickets.domain.TicketType
import tickets.domain.TicketTypeStatus
import tickets.domain.valueObjects.Quantity

/** Fake repository para testes unitários. */
class FakeTicketTypeRepository : ITicketTypeRepository {
    private val ticketTypes = ConcurrentHashMap<UUID, TicketType>()

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
        if (ticketTypes.containsKey(ticketType.id)) {
            ticketTypes[ticketType.id] = ticketType.copy(updatedAt = Instant.now())
            return true
        }
        return false
    }

    override suspend fun delete(id: UUID): Boolean {
        return ticketTypes.remove(id) != null
    }

    override suspend fun decrementAvailableQuantity(id: UUID, quantity: Quantity): Boolean {
        val ticketType = ticketTypes[id] ?: return false

        if (ticketType.availableQuantity.value < quantity.value) {
            return false
        }

        val newAvailableQuantity = Quantity.of(ticketType.availableQuantity.value - quantity.value)
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
        return true
    }

    override suspend fun incrementAvailableQuantity(id: UUID, quantity: Quantity): Boolean {
        val ticketType = ticketTypes[id] ?: return false

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
        return true
    }

    fun clear() {
        ticketTypes.clear()
    }

    fun count(): Int = ticketTypes.size
}
