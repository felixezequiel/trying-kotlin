package tickets.application.useCases

import java.util.UUID
import tickets.application.ports.outbound.IUnitOfWork
import tickets.domain.TicketType

class GetTicketTypeUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(ticketTypeId: UUID): TicketType? {
        return unitOfWork.ticketTypeRepository.getById(ticketTypeId)
    }
}
