package tickets.application.useCases

import java.time.Instant
import java.util.UUID
import tickets.application.dto.UpdateTicketTypeRequest
import tickets.application.ports.outbound.ITicketTypeRepository
import tickets.domain.TicketType
import tickets.domain.TicketTypeStatus
import tickets.domain.valueObjects.Price
import tickets.domain.valueObjects.Quantity
import tickets.domain.valueObjects.TicketName

class UpdateTicketTypeUseCase(private val ticketTypeRepository: ITicketTypeRepository) {

    suspend fun execute(ticketTypeId: UUID, request: UpdateTicketTypeRequest): TicketType {
        val existingTicketType =
                ticketTypeRepository.getById(ticketTypeId)
                        ?: throw IllegalArgumentException("Tipo de ingresso não encontrado")

        // Validação encapsulada nos Value Objects
        val newName = request.name?.let { TicketName.of(it) }
        val newPrice = request.price?.let { Price.fromString(it) }
        val newTotalQuantity = request.totalQuantity?.let { Quantity.positive(it) }
        val newMaxPerCustomer = request.maxPerCustomer?.let { Quantity.atLeast(it, 1) }

        val newStatus =
                request.status?.let {
                    try {
                        TicketTypeStatus.valueOf(it.uppercase())
                    } catch (e: IllegalArgumentException) {
                        throw IllegalArgumentException("Status inválido: $it")
                    }
                }

        val salesStartDate = request.salesStartDate?.let { Instant.parse(it) }
        val salesEndDate = request.salesEndDate?.let { Instant.parse(it) }

        // Calcular nova quantidade disponível se totalQuantity mudar
        val finalTotalQuantity = newTotalQuantity ?: existingTicketType.totalQuantity
        val soldQuantity =
                existingTicketType.totalQuantity.value - existingTicketType.availableQuantity.value
        val newAvailableQuantity =
                Quantity.of((finalTotalQuantity.value - soldQuantity).coerceAtLeast(0))

        val updatedTicketType =
                existingTicketType.copy(
                        name = newName ?: existingTicketType.name,
                        description = request.description ?: existingTicketType.description,
                        price = newPrice ?: existingTicketType.price,
                        totalQuantity = finalTotalQuantity,
                        availableQuantity = newAvailableQuantity,
                        maxPerCustomer = newMaxPerCustomer ?: existingTicketType.maxPerCustomer,
                        salesStartDate = salesStartDate ?: existingTicketType.salesStartDate,
                        salesEndDate = salesEndDate ?: existingTicketType.salesEndDate,
                        status = newStatus ?: existingTicketType.status,
                        updatedAt = Instant.now()
                )

        val success = ticketTypeRepository.update(updatedTicketType)
        if (!success) {
            throw IllegalStateException("Falha ao atualizar tipo de ingresso")
        }

        return updatedTicketType
    }
}
