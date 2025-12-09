package tickets.application.useCases

import java.time.Instant
import java.util.UUID
import tickets.application.ports.outbound.ITicketTypeRepository
import tickets.domain.TicketType
import tickets.domain.TicketTypeStatus

class DeactivateTicketTypeUseCase(private val ticketTypeRepository: ITicketTypeRepository) {

    suspend fun execute(ticketTypeId: UUID): TicketType {
        val existingTicketType =
                ticketTypeRepository.getById(ticketTypeId)
                        ?: throw IllegalArgumentException("Tipo de ingresso não encontrado")

        if (existingTicketType.status == TicketTypeStatus.INACTIVE) {
            throw IllegalStateException("Tipo de ingresso já está inativo")
        }

        val deactivatedTicketType =
                existingTicketType.copy(
                        status = TicketTypeStatus.INACTIVE,
                        updatedAt = Instant.now()
                )

        val success = ticketTypeRepository.update(deactivatedTicketType)
        if (!success) {
            throw IllegalStateException("Falha ao desativar tipo de ingresso")
        }

        return deactivatedTicketType
    }
}
