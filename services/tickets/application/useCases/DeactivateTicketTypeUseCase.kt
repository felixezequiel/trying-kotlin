package tickets.application.useCases

import java.time.Instant
import java.util.UUID
import tickets.application.ports.outbound.IUnitOfWork
import tickets.domain.TicketType
import tickets.domain.TicketTypeStatus

class DeactivateTicketTypeUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(ticketTypeId: UUID): TicketType {
        val existingTicketType =
                unitOfWork.ticketTypeRepository.getById(ticketTypeId)
                        ?: throw IllegalArgumentException("Tipo de ingresso não encontrado")

        if (existingTicketType.status == TicketTypeStatus.INACTIVE) {
            throw IllegalStateException("Tipo de ingresso já está inativo")
        }

        val deactivatedTicketType =
                existingTicketType.copy(
                        status = TicketTypeStatus.INACTIVE,
                        updatedAt = Instant.now()
                )

        val success = unitOfWork.ticketTypeRepository.update(deactivatedTicketType)
        if (!success) {
            throw IllegalStateException("Falha ao desativar tipo de ingresso")
        }

        return deactivatedTicketType
    }
}
