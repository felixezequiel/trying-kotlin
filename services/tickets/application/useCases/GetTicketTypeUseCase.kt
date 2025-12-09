package tickets.application.useCases

import java.util.UUID
import tickets.application.ports.outbound.ITicketTypeRepository
import tickets.domain.TicketType

class GetTicketTypeUseCase(private val ticketTypeRepository: ITicketTypeRepository) {

    suspend fun execute(ticketTypeId: UUID): TicketType? {
        return ticketTypeRepository.getById(ticketTypeId)
    }
}
