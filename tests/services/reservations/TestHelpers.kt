package services.reservations

import java.math.BigDecimal
import java.util.UUID
import reservations.application.ports.outbound.TicketTypeInfo
import reservations.domain.Reservation
import reservations.domain.ReservationItem
import reservations.domain.ReservationStatus
import reservations.domain.valueObjects.Price
import reservations.domain.valueObjects.Quantity

object TestHelpers {
    fun createTestReservationItem(
            id: UUID = UUID.randomUUID(),
            ticketTypeId: UUID = UUID.randomUUID(),
            ticketTypeName: String = "Test Ticket",
            quantity: Int = 2,
            unitPrice: BigDecimal = BigDecimal("100.00")
    ): ReservationItem {
        return ReservationItem.create(
                ticketTypeId = ticketTypeId,
                ticketTypeName = ticketTypeName,
                quantity = Quantity.positive(quantity),
                unitPrice = Price.of(unitPrice)
        )
    }

    fun createTestReservation(
            id: UUID = UUID.randomUUID(),
            customerId: UUID = UUID.randomUUID(),
            eventId: UUID = UUID.randomUUID(),
            items: List<ReservationItem> = listOf(createTestReservationItem()),
            status: ReservationStatus = ReservationStatus.ACTIVE
    ): Reservation {
        return Reservation.create(customerId = customerId, eventId = eventId, items = items)
    }

    fun createTestTicketTypeInfo(
            id: UUID = UUID.randomUUID(),
            eventId: UUID = UUID.randomUUID(),
            name: String = "Test Ticket",
            price: BigDecimal = BigDecimal("100.00"),
            availableQuantity: Int = 100,
            maxPerCustomer: Int = 4
    ): TicketTypeInfo {
        return TicketTypeInfo(
                id = id,
                eventId = eventId,
                name = name,
                price = Price.of(price),
                availableQuantity = Quantity.of(availableQuantity),
                maxPerCustomer = Quantity.of(maxPerCustomer)
        )
    }
}
