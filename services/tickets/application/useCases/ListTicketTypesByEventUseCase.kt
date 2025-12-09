package tickets.application.useCases

import java.util.UUID
import tickets.application.ports.outbound.ITicketTypeRepository
import tickets.domain.TicketType
import tickets.domain.TicketTypeStatus

class ListTicketTypesByEventUseCase(private val ticketTypeRepository: ITicketTypeRepository) {

    suspend fun execute(eventId: UUID): List<TicketType> {
        return ticketTypeRepository.getByEventId(eventId)
    }

    suspend fun executeActive(eventId: UUID): List<TicketType> {
        return ticketTypeRepository.getByEventId(eventId).filter {
            it.status == TicketTypeStatus.ACTIVE
        }
    }
}
