package tickets.domain

import java.time.Instant
import java.util.UUID
import tickets.domain.valueObjects.Price
import tickets.domain.valueObjects.Quantity
import tickets.domain.valueObjects.TicketName

data class TicketType(
        val id: UUID = UUID.randomUUID(),
        val eventId: UUID,
        val name: TicketName,
        val description: String,
        val price: Price,
        val totalQuantity: Quantity,
        val availableQuantity: Quantity,
        val maxPerCustomer: Quantity,
        val salesStartDate: Instant?,
        val salesEndDate: Instant?,
        val status: TicketTypeStatus = TicketTypeStatus.ACTIVE,
        val createdAt: Instant = Instant.now(),
        val updatedAt: Instant = Instant.now()
)
