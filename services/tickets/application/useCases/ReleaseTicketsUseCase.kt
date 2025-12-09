package tickets.application.useCases

import java.util.UUID
import tickets.application.dto.ReleaseTicketsRequest
import tickets.application.ports.outbound.IUnitOfWork
import tickets.domain.TicketType
import tickets.domain.valueObjects.Quantity

class ReleaseTicketsUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(request: ReleaseTicketsRequest): TicketType {
        val ticketTypeId =
                try {
                    UUID.fromString(request.ticketTypeId)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("Ticket Type ID inválido")
                }

        // Validação encapsulada no Value Object
        val quantity = Quantity.positive(request.quantity)

        unitOfWork.ticketTypeRepository.getById(ticketTypeId)
                ?: throw IllegalArgumentException("Tipo de ingresso não encontrado")

        // Operação atômica de incremento
        unitOfWork.ticketTypeRepository.incrementAvailableQuantity(ticketTypeId, quantity)
        // Retornar estado atualizado
        return unitOfWork.ticketTypeRepository.getById(ticketTypeId)!!
    }
}
