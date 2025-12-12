package tickets.application.useCases

import java.time.Instant
import java.util.UUID
import tickets.application.ports.outbound.IUnitOfWork
import tickets.domain.TicketType
import tickets.domain.TicketTypeStatus

class ActivateTicketTypeUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(ticketTypeId: UUID): TicketType {
        val existingTicketType =
                unitOfWork.ticketTypeRepository.getById(ticketTypeId)
                        ?: throw IllegalArgumentException("Tipo de ingresso não encontrado")

        if (existingTicketType.status == TicketTypeStatus.ACTIVE) {
            throw IllegalStateException("Tipo de ingresso já está ativo")
        }

        val activatedTicketType =
                existingTicketType.copy(status = TicketTypeStatus.ACTIVE, updatedAt = Instant.now())

        val success = unitOfWork.ticketTypeRepository.update(activatedTicketType)
        if (!success) {
            throw IllegalStateException("Falha ao ativar tipo de ingresso")
        }

        return activatedTicketType
    }
}
