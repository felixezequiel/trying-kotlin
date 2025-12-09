package reservations.domain

import java.util.UUID
import reservations.domain.valueObjects.Price
import reservations.domain.valueObjects.Quantity

data class ReservationItem(
        val id: UUID = UUID.randomUUID(),
        val ticketTypeId: UUID,
        val ticketTypeName: String, // Desnormalizado para histórico
        val quantity: Quantity,
        val unitPrice: Price, // Preço no momento da reserva
        val subtotal: Price // quantity * unitPrice
) {
    companion object {
        fun create(
                ticketTypeId: UUID,
                ticketTypeName: String,
                quantity: Quantity,
                unitPrice: Price
        ): ReservationItem {
            return ReservationItem(
                    ticketTypeId = ticketTypeId,
                    ticketTypeName = ticketTypeName,
                    quantity = quantity,
                    unitPrice = unitPrice,
                    subtotal = unitPrice * quantity
            )
        }
    }
}
