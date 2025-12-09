package tickets.application.useCases

import java.util.UUID
import tickets.application.ports.outbound.IUnitOfWork
import tickets.domain.TicketType
import tickets.domain.TicketTypeStatus

class ListTicketTypesByEventUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(eventId: UUID): List<TicketType> {
        return unitOfWork.ticketTypeRepository.getByEventId(eventId)
    }

    suspend fun executeActive(eventId: UUID): List<TicketType> {
        return unitOfWork.ticketTypeRepository.getByEventId(eventId).filter {
            it.status == TicketTypeStatus.ACTIVE
        }
    }
}
