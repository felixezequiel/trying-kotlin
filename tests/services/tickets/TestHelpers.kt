package services.tickets

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import tickets.domain.TicketType
import tickets.domain.TicketTypeStatus
import tickets.domain.valueObjects.Price
import tickets.domain.valueObjects.Quantity
import tickets.domain.valueObjects.TicketName

object TestHelpers {
    fun createTestTicketType(
            id: UUID = UUID.randomUUID(),
            eventId: UUID = UUID.randomUUID(),
            name: String = "Test Ticket",
            description: String = "Test Description",
            price: BigDecimal = BigDecimal("100.00"),
            totalQuantity: Int = 100,
            availableQuantity: Int = 100,
            maxPerCustomer: Int = 4,
            status: TicketTypeStatus = TicketTypeStatus.ACTIVE,
            salesStartDate: Instant? = null,
            salesEndDate: Instant? = null
    ): TicketType {
        return TicketType(
                id = id,
                eventId = eventId,
                name = TicketName.of(name),
                description = description,
                price = Price.of(price),
                totalQuantity = Quantity.of(totalQuantity),
                availableQuantity = Quantity.of(availableQuantity),
                maxPerCustomer = Quantity.of(maxPerCustomer),
                status = status,
                salesStartDate = salesStartDate,
                salesEndDate = salesEndDate
        )
    }
}
