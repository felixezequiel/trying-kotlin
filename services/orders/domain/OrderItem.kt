package orders.domain

import java.util.UUID
import orders.domain.valueObjects.Price
import orders.domain.valueObjects.Quantity

data class OrderItem(
        val id: UUID = UUID.randomUUID(),
        val ticketTypeId: UUID,
        val ticketTypeName: String,
        val quantity: Quantity,
        val unitPrice: Price,
        val subtotal: Price
) {
    companion object {
        fun create(
                ticketTypeId: UUID,
                ticketTypeName: String,
                quantity: Quantity,
                unitPrice: Price
        ): OrderItem {
            return OrderItem(
                    ticketTypeId = ticketTypeId,
                    ticketTypeName = ticketTypeName,
                    quantity = quantity,
                    unitPrice = unitPrice,
                    subtotal = unitPrice * quantity
            )
        }
    }
}
