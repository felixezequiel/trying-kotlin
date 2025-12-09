package tickets.application.useCases

import java.util.UUID
import tickets.application.dto.ReserveTicketsRequest
import tickets.application.dto.ReserveTicketsResponse
import tickets.application.ports.outbound.IUnitOfWork
import tickets.domain.TicketTypeStatus
import tickets.domain.valueObjects.Quantity

class ReserveTicketsUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(request: ReserveTicketsRequest): ReserveTicketsResponse {
        val ticketTypeId =
                try {
                    UUID.fromString(request.ticketTypeId)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("Ticket Type ID inválido")
                }

        // Validação encapsulada no Value Object
        val quantity = Quantity.positive(request.quantity)

        val ticketType =
                unitOfWork.ticketTypeRepository.getById(ticketTypeId)
                        ?: throw IllegalArgumentException("Tipo de ingresso não encontrado")

        // RN-T08: Não pode reservar se status != ACTIVE
        if (ticketType.status != TicketTypeStatus.ACTIVE) {
            throw IllegalStateException("Tipo de ingresso não está disponível para reserva")
        }

        // Validar maxPerCustomer
        if (quantity > ticketType.maxPerCustomer) {
            throw IllegalArgumentException(
                    "Quantidade excede o máximo permitido por cliente (${ticketType.maxPerCustomer.value})"
            )
        }

        // RN-T06: Reserva falha se availableQuantity < quantidade solicitada
        if (ticketType.availableQuantity < quantity) {
            throw IllegalStateException(
                    "Quantidade insuficiente disponível. Disponível: ${ticketType.availableQuantity.value}"
            )
        }

        // Operação atômica de decremento
        val success =
                unitOfWork.ticketTypeRepository.decrementAvailableQuantity(ticketTypeId, quantity)
        if (!success) {
            throw IllegalStateException("Falha ao reservar ingressos. Tente novamente.")
        }

        // Buscar estado atualizado
        val updatedTicketType = unitOfWork.ticketTypeRepository.getById(ticketTypeId)!!

        return ReserveTicketsResponse(
                success = true,
                ticketTypeId = ticketTypeId.toString(),
                reservedQuantity = quantity.value,
                unitPrice = ticketType.price.value.toString(),
                remainingQuantity = updatedTicketType.availableQuantity.value
        )
    }
}
