package tickets.application.useCases

import java.time.Instant
import java.util.UUID
import tickets.application.dto.CreateTicketTypeRequest
import tickets.application.ports.outbound.IUnitOfWork
import tickets.domain.TicketType
import tickets.domain.valueObjects.Price
import tickets.domain.valueObjects.Quantity
import tickets.domain.valueObjects.TicketName

class CreateTicketTypeUseCase(private val unitOfWork: IUnitOfWork) {

    suspend fun execute(request: CreateTicketTypeRequest): UUID {
        val eventId =
                try {
                    UUID.fromString(request.eventId)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("Event ID inválido")
                }

        // Validação encapsulada nos Value Objects
        val name = TicketName.of(request.name)
        val price = Price.fromString(request.price)
        val totalQuantity = Quantity.positive(request.totalQuantity)
        val maxPerCustomer = Quantity.atLeast(request.maxPerCustomer, 1)

        val salesStartDate = request.salesStartDate?.let { Instant.parse(it) }
        val salesEndDate = request.salesEndDate?.let { Instant.parse(it) }

        // Validar que salesEndDate é após salesStartDate se ambos estiverem definidos
        if (salesStartDate != null && salesEndDate != null && salesEndDate.isBefore(salesStartDate)
        ) {
            throw IllegalArgumentException("Data de fim das vendas deve ser após a data de início")
        }

        val ticketType =
                TicketType(
                        eventId = eventId,
                        name = name,
                        description = request.description,
                        price = price,
                        totalQuantity = totalQuantity,
                        availableQuantity = totalQuantity, // Inicia com total disponível
                        maxPerCustomer = maxPerCustomer,
                        salesStartDate = salesStartDate,
                        salesEndDate = salesEndDate
                )

        return unitOfWork.ticketTypeRepository.add(ticketType)
    }
}
