package tickets.adapters.outbound

import java.util.UUID
import tickets.application.ports.outbound.ITicketTypeRepository
import tickets.domain.TicketType
import tickets.domain.valueObjects.Quantity
import tickets.infrastructure.persistence.DatabaseContext

class TicketTypeRepositoryAdapter(private val dbContext: DatabaseContext) : ITicketTypeRepository {

    override suspend fun add(ticketType: TicketType): UUID {
        return dbContext.addTicketType(ticketType)
    }

    override suspend fun getById(id: UUID): TicketType? {
        return dbContext.findById(id)
    }

    override suspend fun getByEventId(eventId: UUID): List<TicketType> {
        return dbContext.findByEventId(eventId)
    }

    override suspend fun update(ticketType: TicketType): Boolean {
        return dbContext.updateTicketType(ticketType)
    }

    override suspend fun delete(id: UUID): Boolean {
        return dbContext.deleteTicketType(id)
    }

    override suspend fun decrementAvailableQuantity(id: UUID, quantity: Quantity): Boolean {
        // Infrastructure faz a conversão para tipo primitivo
        return dbContext.decrementAvailableQuantity(id, quantity.value)
    }

    override suspend fun incrementAvailableQuantity(id: UUID, quantity: Quantity): Boolean {
        // Infrastructure faz a conversão para tipo primitivo
        return dbContext.incrementAvailableQuantity(id, quantity.value)
    }
}
