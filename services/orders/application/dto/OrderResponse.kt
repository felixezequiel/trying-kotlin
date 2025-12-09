package orders.application.dto

import kotlinx.serialization.Serializable
import orders.domain.Order
import orders.domain.PaymentStatus

@Serializable
data class OrderResponse(
        val id: String,
        val customerId: String,
        val reservationId: String,
        val eventId: String,
        val items: List<OrderItemResponse>,
        val totalAmount: String,
        val paymentStatus: PaymentStatus,
        val paymentMethod: String?,
        val createdAt: String,
        val paidAt: String?
) {
    companion object {
        fun fromDomain(order: Order): OrderResponse {
            return OrderResponse(
                    id = order.id.toString(),
                    customerId = order.customerId.toString(),
                    reservationId = order.reservationId.toString(),
                    eventId = order.eventId.toString(),
                    items = order.items.map { OrderItemResponse.fromDomain(it) },
                    totalAmount = order.totalAmount.value.toString(),
                    paymentStatus = order.paymentStatus,
                    paymentMethod = order.paymentMethod,
                    createdAt = order.createdAt.toString(),
                    paidAt = order.paidAt?.toString()
            )
        }
    }
}
