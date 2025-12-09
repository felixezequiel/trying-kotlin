package tickets.application.useCases

import java.util.UUID
import tickets.application.dto.ReleaseTicketsRequest
import tickets.application.ports.outbound.ITicketTypeRepository
import tickets.domain.TicketType
import tickets.domain.valueObjects.Quantity

class ReleaseTicketsUseCase(private val ticketTypeRepository: ITicketTypeRepository) {

    suspend fun execute(request: ReleaseTicketsRequest): TicketType {
        val ticketTypeId =
                try {
                    UUID.fromString(request.ticketTypeId)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("Ticket Type ID inválido")
                }

        // Validação encapsulada no Value Object
        val quantity = Quantity.positive(request.quantity)

        ticketTypeRepository.getById(ticketTypeId)
                ?: throw IllegalArgumentException("Tipo de ingresso não encontrado")

        // Operação atômica de incremento
        ticketTypeRepository.incrementAvailableQuantity(ticketTypeId, quantity)
        // Retornar estado atualizado
        return ticketTypeRepository.getById(ticketTypeId)!!
    }
}
