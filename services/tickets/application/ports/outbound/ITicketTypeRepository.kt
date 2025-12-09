package tickets.application.ports.outbound

import java.util.UUID
import tickets.domain.TicketType
import tickets.domain.valueObjects.Quantity

interface ITicketTypeRepository {
    suspend fun add(ticketType: TicketType): UUID
    suspend fun getById(id: UUID): TicketType?
    suspend fun getByEventId(eventId: UUID): List<TicketType>
    suspend fun update(ticketType: TicketType): Boolean
    suspend fun delete(id: UUID): Boolean

    // Operações atômicas para controle de estoque (evita race conditions)
    suspend fun decrementAvailableQuantity(id: UUID, quantity: Quantity): Boolean
    suspend fun incrementAvailableQuantity(id: UUID, quantity: Quantity): Boolean
}
