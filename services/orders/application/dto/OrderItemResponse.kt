package orders.application.dto

import kotlinx.serialization.Serializable
import orders.domain.OrderItem

@Serializable
data class OrderItemResponse(
        val id: String,
        val ticketTypeId: String,
        val ticketTypeName: String,
        val quantity: Int,
        val unitPrice: String,
        val subtotal: String
) {
    companion object {
        fun fromDomain(item: OrderItem): OrderItemResponse {
            return OrderItemResponse(
                    id = item.id.toString(),
                    ticketTypeId = item.ticketTypeId.toString(),
                    ticketTypeName = item.ticketTypeName,
                    quantity = item.quantity.value,
                    unitPrice = item.unitPrice.value.toString(),
                    subtotal = item.subtotal.value.toString()
            )
        }
    }
}
